package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.downloader.AudioDownloader
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
    onOpenYouTubeBrowser: () -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember(initialUrl) { mutableStateOf(initialUrl ?: "") }
    var rightsChecked by remember { mutableStateOf(true) }
    var saveToMusicFolder by remember { mutableStateOf(true) }
    var clipboardSuggestion by remember { mutableStateOf<String?>(null) }

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
                // Section Title matching web app in image 4
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
                        text = "Convert media to MP3",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Open Grace Music converts YouTube music videos and authorized media URLs into real MP3 files, saved directly to your phone for offline listening.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Label
                Text(
                    text = "AUTHORIZED DIRECT AUDIO/VIDEO OR YOUTUBE URL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Input box with [ Paste ] button matching web UI
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
                                text = "https://example.org/audio.mp3 or YouTube link",
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

                    // Explicit Paste Button matching web app
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
                        Text(
                            text = "Paste",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SageGreen
                        )
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
                            text = "📋 Paste link from clipboard: ${clipboardSuggestion?.take(36)}...",
                            fontSize = 11.5.sp,
                            color = SageGreen,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // YouTube in-app browse & copy provision
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCardHover)
                        .clickable(enabled = !isDownloading) { onOpenYouTubeBrowser() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📺 Open YouTube in Open Grace Music (sign in, copy URL, 1-tap save)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SageGreen
                    )
                    Text("➔", color = SageGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Format & Quality indicators matching web app
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("OUTPUT FORMAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCardHover)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text("MP3 - High quality", fontSize = 11.5.sp, color = TextLight)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("STORAGE DESTINATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCardHover)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text("Phone Music folder", fontSize = 11.5.sp, color = SageGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Save to Phone's Music folder toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Save to phone system Music folder",
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
                        text = "I own this media or have permission to download and convert it.",
                        fontSize = 12.sp,
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

                Spacer(modifier = Modifier.height(16.dp))

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
                            .padding(horizontal = 20.dp, vertical = 9.dp)
                            .testTag("urlCancel"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.5.sp,
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
                                onStartDownload(urlInput, saveToMusicFolder, 192)
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
                                fontSize = 13.5.sp,
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
