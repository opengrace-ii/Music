package com.example.data.downloader

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class ResolvedStream(
    val streamUrl: String,
    val headers: Map<String, String>,
    val cookies: String? = null
)

object YouTubeStreamResolver {
    private const val TAG = "YouTubeStreamResolver"

    const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private val YT_PATTERNS = listOf(
        Pattern.compile("(?:youtu\\.be\\/|v\\/|u\\/\\w\\/|embed\\/|shorts\\/)([^#\\&\\?]{11})"),
        Pattern.compile("[\\?&]v=([^#\\&\\?]{11})"),
        Pattern.compile("^([^#\\&\\?]{11})$")
    )

    /**
     * Extracts an 11-character YouTube video ID cleanly from any URL variant:
     * - https://youtu.be/0T8NnR21s0Q?si=z9pRnlG2tkjJ87Ci
     * - https://www.youtube.com/watch?v=Xc6JIstj8GY&list=RDXc6JIstj8GY&index=1
     * - https://m.youtube.com/watch?v=0T8NnR21s0Q
     * - https://www.youtube.com/shorts/0T8NnR21s0Q
     */
    fun extractYouTubeId(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        val trimmed = rawUrl.trim()

        try {
            val normalized = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else trimmed

            val uri = Uri.parse(normalized)
            val host = uri.host?.lowercase() ?: ""

            // youtu.be/VIDEO_ID
            if (host.contains("youtu.be")) {
                val path = uri.path?.removePrefix("/") ?: ""
                val id = path.substringBefore('/').substringBefore('?')
                if (id.length == 11) return id
            }

            // youtube.com variants
            if (host.contains("youtube.com")) {
                val path = uri.path ?: ""
                val vParam = uri.getQueryParameter("v")
                if (!vParam.isNullOrBlank() && vParam.length == 11) {
                    return vParam
                }
                if (path.contains("/shorts/")) {
                    val id = path.substringAfter("/shorts/").substringBefore('/').substringBefore('?')
                    if (id.length == 11) return id
                }
                if (path.contains("/embed/")) {
                    val id = path.substringAfter("/embed/").substringBefore('/').substringBefore('?')
                    if (id.length == 11) return id
                }
            }
        } catch (_: Exception) {}

        // Fallback pattern matching
        for (pattern in YT_PATTERNS) {
            val matcher = pattern.matcher(trimmed)
            if (matcher.find()) {
                val candidate = matcher.group(1)
                if (candidate != null && candidate.length == 11) {
                    return candidate
                }
            }
        }

        return null
    }

    /**
     * Returns standard canonical watch URL for YouTube.
     */
    fun canonicalWatchUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

    /**
     * Strips chunked range query parameters so Google Video can stream the full file from 0 to EOF.
     */
    fun prepareFullStreamUrl(streamUrl: String): String {
        return streamUrl
            .replace("&range=\\d+-\\d+".toRegex(), "")
            .replace("\\?range=\\d+-\\d+&".toRegex(), "?")
            .replace("\\?range=\\d+-\\d+".toRegex(), "")
    }

    /**
     * Attempts to resolve the direct Google Video audio stream by launching a headless background
     * WebView and intercepting the audio playback request.
     */
    suspend fun resolveStreamInBackground(
        context: Context,
        videoId: String,
        timeoutMs: Long = 5000
    ): ResolvedStream? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            var finished = false
            var webView: WebView? = null
            val handler = Handler(Looper.getMainLooper())

            fun complete(result: ResolvedStream?) {
                if (!finished) {
                    finished = true
                    try {
                        webView?.stopLoading()
                        webView?.destroy()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error destroying resolver webview: ${e.message}")
                    }
                    webView = null
                    cont.resume(result)
                }
            }

            val timeoutRunnable = Runnable {
                Log.d(TAG, "Background resolver timed out for $videoId")
                complete(null)
            }
            handler.postDelayed(timeoutRunnable, timeoutMs)

            cont.invokeOnCancellation {
                handler.removeCallbacks(timeoutRunnable)
                try {
                    webView?.stopLoading()
                    webView?.destroy()
                } catch (_: Exception) {}
                webView = null
            }

            try {
                val wv = WebView(context)
                webView = wv
                wv.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    cacheMode = WebSettings.LOAD_DEFAULT
                    userAgentString = DEFAULT_USER_AGENT
                }

                wv.webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val reqUrl = request?.url?.toString() ?: ""
                        if (reqUrl.contains("googlevideo.com/videoplayback") &&
                            (reqUrl.contains("mime=audio") || reqUrl.contains("audio/"))
                        ) {
                            val headers = request?.requestHeaders ?: emptyMap()
                            val cookies = try {
                                CookieManager.getInstance().getCookie(reqUrl)
                            } catch (_: Exception) { null }

                            handler.removeCallbacks(timeoutRunnable)
                            view?.post {
                                complete(ResolvedStream(
                                    streamUrl = prepareFullStreamUrl(reqUrl),
                                    headers = headers,
                                    cookies = cookies
                                ))
                            }
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            """
                            (function() {
                                var v = document.querySelector('video');
                                if (v) {
                                    v.muted = true;
                                    v.play();
                                }
                            })();
                            """.trimIndent(),
                            null
                        )
                    }
                }

                wv.loadUrl("https://www.youtube.com/embed/$videoId?autoplay=1&enablejsapi=1")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize headless WebView resolver", e)
                handler.removeCallbacks(timeoutRunnable)
                complete(null)
            }
        }
    }
}
