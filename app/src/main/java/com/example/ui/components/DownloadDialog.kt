package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.downloader.YouTubeStreamResolver
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardHover
import com.example.ui.theme.OnSageGreen
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenDeep
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle

@Composable
fun DownloadDialog(
    isDownloading: Boolean,
    downloadProgress: Float,
    downloadStatus: String,
    initialUrl: String? = null,
    onDismiss: () -> Unit,
    onStartDownload: (url: String, saveToMusic: Boolean, bitrate: Int) -> Unit,
    onOpenYouTubeBrowser: (targetUrl: String?) -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember(initialUrl) { mutableStateOf(initialUrl ?: "") }
    var rightsChecked by remember { mutableStateOf(true) }
    var saveToMusicFolder by remember { mutableStateOf(true) }
    var clipboardSuggestion by remember { mutableStateOf<String?>(null) }

    val detectedYtId = remember(urlInput) {
        YouTubeStreamResolver.extractYouTubeId(urlInput)
    }

    // Check clipboard for YouTube or media URLs
    LaunchedEffect(Unit) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val item = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
            if (!item.isNullOrEmpty() && (item.contains("youtube.com") || item.contains("youtu.be") || item.startsWith("http"))) {
                clipboardSuggestion = item
            }
        } catch (_: Exception) {}
    }

    Dialog(onDismissRequest = { if (!isDownloading) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .testTag("urlDialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Section Title matching Open Grace Music branding
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SageGreenDeep),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♪", color = SageGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Convert YouTube & Media to MP3",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Open Grace Music extracts and saves authentic music audio directly to your phone's Music folder for offline listening.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Label
                Text(
                    text = "YOUTUBE VIDEO OR DIRECT AUDIO URL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Input box with [ Paste ] button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardHover)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (urlInput.isEmpty()) {
                            Text(
                                text = "https://youtu.be/... or youtube.com link",
                                fontSize = 13.sp,
                                color = TextSubtle
                            )
                        }
                        BasicTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            textStyle = TextStyle(fontSize = 13.5.sp, color = TextLight),
                            cursorBrush = SolidColor(SageGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("urlInput"),
                            enabled = !isDownloading
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Explicit Paste Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCardBorder)
                            .clickable(enabled = !isDownloading) {
                                try {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                                    if (!text.isNullOrEmpty()) {
                                        urlInput = text
                                        rightsChecked = true
                                    }
                                } catch (_: Exception) {}
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("paste_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SageGreen
                            )
                        }
                    }
                }

                // If YouTube video recognized, show clear detected badge & preview
                if (detectedYtId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SageGreenDeep)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "✓ YouTube Video Detected: $detectedYtId",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreen
                                )
                                Text(
                                    text = "Ready to convert & save directly to library",
                                    fontSize = 10.5.sp,
                                    color = TextLight.copy(alpha = 0.85f)
                                )
                            }

                            // Optional Player Preview Button
                            OutlinedButton(
                                onClick = {
                                    val canonical = YouTubeStreamResolver.canonicalWatchUrl(detectedYtId)
                                    onOpenYouTubeBrowser(canonical)
                                },
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Preview", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Clipboard auto-detect chip if found
                if (!clipboardSuggestion.isNullOrEmpty() && urlInput.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SageGreenDeep)
                            .clickable {
                                urlInput = clipboardSuggestion ?: ""
                                rightsChecked = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 Paste from clipboard: ${clipboardSuggestion?.take(34)}...",
                            fontSize = 11.5.sp,
                            color = SageGreen,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // YouTube in-app browse & direct copy provision
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCardHover)
                        .clickable(enabled = !isDownloading) {
                            onOpenYouTubeBrowser(if (detectedYtId != null) YouTubeStreamResolver.canonicalWatchUrl(detectedYtId) else null)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = SageGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open YouTube (search, copy direct URL, 1-tap save)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SageGreen
                        )
                    }
                    Text("➔", color = SageGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Save to Phone's Music folder toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Save directly to phone Music folder",
                        fontSize = 12.5.sp,
                        color = TextMuted
                    )
                    Switch(
                        checked = saveToMusicFolder,
                        onCheckedChange = { saveToMusicFolder = it },
                        enabled = !isDownloading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnSageGreen,
                            checkedTrackColor = SageGreen,
                            uncheckedTrackColor = DarkCardHover
                        )
                    )
                }

                // Rights confirmation checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rightsChecked,
                        onCheckedChange = { rightsChecked = it },
                        enabled = !isDownloading,
                        colors = CheckboxDefaults.colors(
                            checkedColor = SageGreen,
                            checkmarkColor = OnSageGreen
                        )
                    )
                    Text(
                        text = "I own or have authorized access to download this media.",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }

                // Progress Indicator
                if (isDownloading) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = SageGreen,
                        trackColor = SageGreenDeep
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = downloadStatus,
                        fontSize = 11.5.sp,
                        color = SageGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dialog Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.5.dp, DarkCardBorder, RoundedCornerShape(999.dp))
                            .clickable(enabled = !isDownloading) { onDismiss() }
                            .padding(horizontal = 18.dp, vertical = 9.dp)
                            .testTag("urlCancel"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    val isEnabled = !isDownloading && urlInput.isNotBlank() && rightsChecked
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isEnabled) SageGreen else SageGreen.copy(alpha = 0.35f))
                            .clickable(enabled = isEnabled) {
                                val cleanUrl = if (detectedYtId != null) {
                                    YouTubeStreamResolver.canonicalWatchUrl(detectedYtId)
                                } else {
                                    urlInput.trim()
                                }
                                onStartDownload(cleanUrl, saveToMusicFolder, 192)
                            }
                            .padding(horizontal = 22.dp, vertical = 10.dp)
                            .testTag("urlOk"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = OnSageGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Convert to MP3",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnabled) OnSageGreen else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
