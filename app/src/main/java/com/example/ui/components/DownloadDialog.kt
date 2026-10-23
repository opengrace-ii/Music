package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.downloader.AudioDownloader
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkSoft
import com.example.ui.theme.PeachDark
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft

@Composable
fun DownloadDialog(
    isDownloading: Boolean,
    downloadProgress: Float,
    downloadStatus: String,
    onDismiss: () -> Unit,
    onStartDownload: (url: String, saveToMusic: Boolean, bitrate: Int) -> Unit,
    onOpenYouTubeBrowser: () -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    var rightsChecked by remember { mutableStateOf(true) }
    var saveToMusicFolder by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = { if (!isDownloading) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .testTag("urlDialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Dialog Title
                Text(
                    text = "Import from direct URL or YouTube",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Dialog Note
                Text(
                    text = "Paste a direct link to an audio or video file (ends in .mp3, .m4a, .ogg, .wav, .flac, .webm, .mp4) or any YouTube music link to convert into MP3.",
                    fontSize = 12.5.sp,
                    color = InkSoft,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Input URL box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFFE8D5C8), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (urlInput.isEmpty()) {
                        Text(
                            text = "https://example.org/audio/song.mp3",
                            fontSize = 14.sp,
                            color = Color(0xFFAFA096)
                        )
                    }
                    BasicTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        textStyle = TextStyle(fontSize = 14.sp, color = InkDark),
                        cursorBrush = SolidColor(PeachPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("urlInput"),
                        enabled = !isDownloading
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // In-App YouTube Browser Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PeachSoft)
                        .clickable(enabled = !isDownloading) { onOpenYouTubeBrowser() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📺 Open YouTube in OGM (sign in & 1-tap download)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PeachDark
                    )
                    Text("➔", color = PeachDark, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Demo track chips
                Text(
                    text = "Quick test tracks:",
                    fontSize = 11.sp,
                    color = InkSoft
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AudioDownloader.DEMO_TRACKS.forEach { demo ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CreamBackground)
                                .clickable(enabled = !isDownloading) {
                                    urlInput = demo.directStreamUrl
                                    rightsChecked = true
                                }
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = demo.title.substringBefore(" (").take(12),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save to Phone's Music folder toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save to device Music folder",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkDark
                        )
                        Text(
                            text = "Play in OGM and phone's native music player",
                            fontSize = 10.5.sp,
                            color = InkSoft
                        )
                    }
                    Switch(
                        checked = saveToMusicFolder,
                        onCheckedChange = { saveToMusicFolder = it },
                        enabled = !isDownloading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PeachPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rights confirmation checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rightsChecked,
                        onCheckedChange = { rightsChecked = it },
                        enabled = !isDownloading,
                        colors = CheckboxDefaults.colors(checkedColor = PeachPrimary)
                    )
                    Text(
                        text = "I own this file or have permission to download and keep it.",
                        fontSize = 12.sp,
                        color = InkSoft,
                        lineHeight = 16.sp
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
                        color = PeachPrimary,
                        trackColor = PeachSoft
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = downloadStatus,
                        fontSize = 11.5.sp,
                        color = PeachDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dialog Actions (Cancel ghost pill, Import primary pill)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Ghost Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .border(2.dp, PeachPrimary, RoundedCornerShape(999.dp))
                            .clickable(enabled = !isDownloading) { onDismiss() }
                            .padding(horizontal = 20.dp, vertical = 9.dp)
                            .testTag("urlCancel"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PeachPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Import Primary Pill Button
                    val isEnabled = !isDownloading && urlInput.isNotBlank() && rightsChecked
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isEnabled) PeachPrimary else PeachPrimary.copy(alpha = 0.45f))
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
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Import",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
