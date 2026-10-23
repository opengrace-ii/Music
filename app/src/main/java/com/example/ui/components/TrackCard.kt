package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StorageStatus
import com.example.data.model.Track
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkSoft
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusAmberBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg

@Composable
fun TrackCard(
    track: Track,
    isPlayingThis: Boolean,
    onPlayClick: () -> Unit,
    onConvertToMp3: () -> Unit,
    onSaveToMusicClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMp3 = track.format.equals("MP3", ignoreCase = true) || track.title.endsWith(".mp3", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x14783C14), spotColor = Color(0x14783C14))
            .testTag("track_card_${track.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Cover (54x54, initials or image, rounded 12dp, peach-soft background)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PeachSoft),
                contentAlignment = Alignment.Center
            ) {
                if (!track.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = track.thumbnailUrl,
                        contentDescription = "Cover for ${track.title}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = track.initials,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PeachPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Track Body (Title, duration, size, format chip, status chip)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = track.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = track.formattedDuration,
                        fontSize = 11.5.sp,
                        color = InkSoft
                    )
                    Text(
                        text = "·",
                        fontSize = 11.5.sp,
                        color = InkSoft
                    )
                    Text(
                        text = track.formattedSize,
                        fontSize = 11.5.sp,
                        color = InkSoft
                    )

                    // MP3 Chip
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEFE9E4))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MP3",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkSoft
                        )
                    }

                    // Status Chip: ● PERSISTENT / ◐ DEVICE
                    val isPersistent = track.storageStatus == StorageStatus.PERSISTENT || track.savedToDeviceMusic
                    val chipBg = if (isPersistent) StatusGreenBg else StatusAmberBg
                    val chipColor = if (isPersistent) StatusGreen else StatusAmber
                    val chipText = if (isPersistent) "● PERSISTENT" else "◐ DEVICE"

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(chipBg)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipColor
                        )
                    }
                }
            }

            // Track Actions (Vertical stack of mini-btns: Play, To MP3, Save, X)
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Play Button (accent peach mini-btn)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PeachPrimary)
                        .clickable { onPlayClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("play_button_${track.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPlayingThis) "❚❚ Pause" else "▶ Play",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // To MP3 Button (shown if non-mp3)
                if (!isMp3) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CreamBackground)
                            .clickable { onConvertToMp3() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⇄ To MP3",
                            color = InkDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Save Button (export to music folder)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CreamBackground)
                            .clickable { onSaveToMusicClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("save_button_${track.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⤓ Save",
                            color = InkDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Delete Button (✕)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CreamBackground)
                            .clickable { onDeleteClick() }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                            .testTag("delete_button_${track.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = InkSoft,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
