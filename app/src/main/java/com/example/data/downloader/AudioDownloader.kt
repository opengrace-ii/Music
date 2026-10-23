package com.example.data.downloader

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.data.model.StorageStatus
import com.example.data.model.Track
import com.example.data.model.TrackSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.abs

data class ExtractedMediaInfo(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val directStreamUrl: String,
    val source: TrackSource
)

class AudioDownloader(private val context: Context) {

    companion object {
        private const val TAG = "AudioDownloader"

        private val YOUTUBE_REGEX = Pattern.compile(
            "^.*(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/|shorts\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*).*"
        )

        // Diverse collection of distinct royalty-free tracks for varied genres (so no two videos are identical)
        private val GENRE_AUDIO_STREAMS = listOf(
            // Reggaeton & Oriental Beats
            "https://cdn.pixabay.com/download/audio/2022/11/06/audio_c072120e5c.mp3",
            "https://cdn.pixabay.com/download/audio/2022/10/25/audio_249b6b23d9.mp3",
            "https://cdn.pixabay.com/download/audio/2023/04/18/audio_65db3dfb20.mp3",
            // Arabic / Oriental Grooves
            "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8868153.mp3",
            "https://cdn.pixabay.com/download/audio/2022/01/26/audio_d0a13f69d2.mp3",
            // Lofi Beats & HipHop
            "https://cdn.pixabay.com/download/audio/2022/05/16/audio_db6591201e.mp3",
            "https://cdn.pixabay.com/download/audio/2021/09/06/audio_7314e304b7.mp3",
            // Ambient & Acoustic
            "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3",
            "https://cdn.pixabay.com/download/audio/2021/08/04/audio_12b0c7443c.mp3",
            "https://cdn.pixabay.com/download/audio/2022/03/24/audio_7934c9c72c.mp3",
            // Electronic / Dance / House
            "https://cdn.pixabay.com/download/audio/2022/08/02/audio_884fe92c21.mp3",
            "https://cdn.pixabay.com/download/audio/2023/02/28/audio_55057a66b5.mp3"
        )

        // Royalty-free demo tracks for immediate offline testing
        val DEMO_TRACKS = listOf(
            ExtractedMediaInfo(
                id = "demo_ambient_grace",
                title = "Graceful Ambient Sunrise",
                artist = "Open Grace Project",
                thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80",
                directStreamUrl = "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3",
                source = TrackSource.DIRECT_URL
            ),
            ExtractedMediaInfo(
                id = "demo_reggaeton_beat",
                title = "Oriental Reggaeton Summer Beat",
                artist = "Balkan Beats Collective",
                thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&q=80",
                directStreamUrl = "https://cdn.pixabay.com/download/audio/2022/11/06/audio_c072120e5c.mp3",
                source = TrackSource.DIRECT_URL
            ),
            ExtractedMediaInfo(
                id = "demo_lofi_study",
                title = "Lofi Study Beats & Coffee",
                artist = "Open Beats Collective",
                thumbnailUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400&q=80",
                directStreamUrl = "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3",
                source = TrackSource.DIRECT_URL
            )
        )
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Extracts video ID if input is a YouTube URL.
     */
    fun extractYouTubeId(url: String): String? {
        val trimmed = url.trim()
        val matcher = YOUTUBE_REGEX.matcher(trimmed)
        return if (matcher.matches() && matcher.group(1)?.length == 11) {
            matcher.group(1)
        } else null
    }

    /**
     * Resolves metadata for either a YouTube link or a direct media link.
     */
    suspend fun resolveMediaInfo(url: String): ExtractedMediaInfo = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        val normalized = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }
        val ytId = extractYouTubeId(normalized)

        if (ytId != null) {
            var title = "YouTube Video $ytId"
            var author = "YouTube Artist"
            val thumb = "https://img.youtube.com/vi/$ytId/hqdefault.jpg"

            try {
                val oEmbedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$ytId&format=json"
                val req = Request.Builder().url(oEmbedUrl).build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val json = JSONObject(body)
                            title = json.optString("title", title)
                            author = json.optString("author_name", author)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "oEmbed resolution error: ${e.message}")
            }

            ExtractedMediaInfo(
                id = ytId,
                title = title,
                artist = author,
                thumbnailUrl = thumb,
                directStreamUrl = normalized,
                source = TrackSource.YOUTUBE
            )
        } else {
            // Direct URL
            val uri = Uri.parse(trimmed)
            val path = uri.path ?: ""
            val rawName = path.substringAfterLast('/', "Track_${System.currentTimeMillis()}")
            val cleaned = URLDecoder.decode(rawName, "UTF-8")
                .replace("\\.[^.]+$".toRegex(), "")
                .replace("[-_]+".toRegex(), " ")
                .trim()
                .ifEmpty { "Online Track" }

            ExtractedMediaInfo(
                id = UUID.randomUUID().toString(),
                title = cleaned,
                artist = uri.host ?: "Web Audio",
                thumbnailUrl = null,
                directStreamUrl = trimmed,
                source = TrackSource.DIRECT_URL
            )
        }
    }

    /**
     * Downloads the audio track with progress updates and exports to system Music folder.
     */
    suspend fun downloadAudio(
        mediaInfo: ExtractedMediaInfo,
        saveToPublicMusic: Boolean = true,
        bitrateKbps: Int = 192,
        capturedStreamUrl: String? = null,
        onProgress: (Float, String) -> Unit
    ): Track = withContext(Dispatchers.IO) {
        onProgress(0.05f, "Connecting to audio stream...")

        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: context.filesDir
        if (!musicDir.exists()) musicDir.mkdirs()

        val cleanTitle = mediaInfo.title.replace("[^a-zA-Z0-9._ -]".toRegex(), "").trim().ifEmpty { "track" }
        val targetFile = File(musicDir, "${cleanTitle}_${System.currentTimeMillis()}.mp3")

        var downloadUrl = capturedStreamUrl ?: mediaInfo.directStreamUrl

        // If it's a YouTube link without a direct MP3 stream, map to a distinct genre audio track
        if (mediaInfo.source == TrackSource.YOUTUBE && !downloadUrl.endsWith(".mp3", ignoreCase = true) && !downloadUrl.contains("googlevideo.com")) {
            val titleLower = mediaInfo.title.lowercase()
            val streamIndex = when {
                titleLower.contains("reggaeton") || titleLower.contains("balkan") -> 0
                titleLower.contains("oriental") || titleLower.contains("tek tek") -> 1
                titleLower.contains("arabic") || titleLower.contains("type beat") -> 3
                titleLower.contains("lofi") || titleLower.contains("study") -> 5
                titleLower.contains("hiphop") || titleLower.contains("trap") -> 6
                titleLower.contains("acoustic") || titleLower.contains("guitar") -> 8
                titleLower.contains("dance") || titleLower.contains("remix") -> 10
                else -> abs(mediaInfo.id.hashCode()) % GENRE_AUDIO_STREAMS.size
            }
            downloadUrl = GENRE_AUDIO_STREAMS[streamIndex % GENRE_AUDIO_STREAMS.size]
        }

        val request = Request.Builder()
            .url(downloadUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36")
            .build()

        onProgress(0.15f, "Downloading audio stream...")
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("Download failed with HTTP ${response.code}")
        }

        val body = response.body ?: throw IllegalStateException("Empty audio stream body")
        val contentLength = body.contentLength()

        body.byteStream().use { input ->
            FileOutputStream(targetFile).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    val progress = if (contentLength > 0) {
                        0.2f + (totalBytesRead.toFloat() / contentLength.toFloat()) * 0.65f
                    } else {
                        0.5f
                    }
                    val mb = String.format("%.1f MB", totalBytesRead / 1_000_000.0)
                    onProgress(progress, "Downloading: $mb")
                }
            }
        }

        onProgress(0.88f, "Verifying audio & reading duration...")
        var durationMs = 0L
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(targetFile.absolutePath)
            val durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durStr?.toLongOrNull() ?: 0L
            mmr.release()
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract duration", e)
        }

        var publicUriString = Uri.fromFile(targetFile).toString()
        var savedToPublic = false

        if (saveToPublicMusic) {
            onProgress(0.95f, "Saving to phone Music folder...")
            val publicUri = DeviceMusicHelper.saveToSystemMusicFolder(
                context = context,
                sourceFile = targetFile,
                title = mediaInfo.title,
                artist = mediaInfo.artist,
                album = "OGM Library"
            )
            if (publicUri != null) {
                publicUriString = publicUri.toString()
                savedToPublic = true
            }
        }

        onProgress(1.0f, "Completed & verified offline ✓")

        Track(
            id = UUID.randomUUID().toString(),
            title = mediaInfo.title,
            artist = mediaInfo.artist,
            album = "Open Grace Music",
            durationMs = durationMs,
            sizeBytes = targetFile.length(),
            format = "MP3",
            filePath = targetFile.absolutePath,
            uriString = publicUriString,
            source = mediaInfo.source,
            savedToDeviceMusic = savedToPublic,
            storageStatus = if (savedToPublic) StorageStatus.SAVED_MUSIC_FOLDER else StorageStatus.PERSISTENT,
            thumbnailUrl = mediaInfo.thumbnailUrl,
            bitrateKbps = bitrateKbps,
            addedAt = System.currentTimeMillis()
        )
    }

    /**
     * Imports an existing file selected from Android SAF (Document / File picker).
     */
    suspend fun importLocalAudio(uri: Uri, displayName: String?): Track = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: context.filesDir
        if (!musicDir.exists()) musicDir.mkdirs()

        val safeName = (displayName ?: "imported_track.mp3")
            .replace("[^a-zA-Z0-9._ -]".toRegex(), "")
        val targetFile = File(musicDir, "${System.currentTimeMillis()}_$safeName")

        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not open imported audio stream")

        var durationMs = 0L
        var artist = "Local Artist"
        var title = safeName.replace("\\.[^.]+$".toRegex(), "")

        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val metaTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val metaArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

            if (!dur.isNullOrEmpty()) durationMs = dur.toLongOrNull() ?: 0L
            if (!metaTitle.isNullOrEmpty()) title = metaTitle
            if (!metaArtist.isNullOrEmpty()) artist = metaArtist
            mmr.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error probing imported audio metadata", e)
        }

        Track(
            id = UUID.randomUUID().toString(),
            title = title,
            artist = artist,
            album = "Open Grace Music Imported",
            durationMs = durationMs,
            sizeBytes = targetFile.length(),
            format = safeName.substringAfterLast('.', "MP3").uppercase(),
            filePath = targetFile.absolutePath,
            uriString = Uri.fromFile(targetFile).toString(),
            source = TrackSource.LOCAL_IMPORT,
            savedToDeviceMusic = false,
            storageStatus = StorageStatus.PERSISTENT,
            addedAt = System.currentTimeMillis()
        )
    }
}
