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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.player.PlayerState
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkSoft
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft

@Composable
fun NowPlayingBar(
    playerState: PlayerState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSeek: (Long) -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack ?: return
    var isSeeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("now_playing_bar"),
        color = Color(0xF5FFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3E2D5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: np-info (Thumb, title, subtitle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBarClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PeachPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    if (!track.thumbnailUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = "Cover",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "♪",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${playerState.formattedDuration} · ${if (track.savedToDeviceMusic) "saved in music ✓" else "offline ✓"}",
                        fontSize = 11.sp,
                        color = InkSoft,
                        maxLines = 1
                    )
                }
            }

            // Row 2: np-controls (Shuffle 🔀, Prev ⏮, Play 52dp circle, Next ⏭)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onToggleShuffle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔀",
                        fontSize = 17.sp,
                        color = if (playerState.isShuffle) PeachPrimary else InkDark
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Previous Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onPrevious() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏮",
                        fontSize = 18.sp,
                        color = InkDark
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Big Circular Play Button (52dp, Peach, white icon)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(8.dp, CircleShape, ambientColor = PeachPrimary, spotColor = PeachPrimary)
                        .clip(CircleShape)
                        .background(PeachPrimary)
                        .clickable { onTogglePlayPause() }
                        .testTag("now_playing_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (playerState.isPlaying) "❚❚" else "▶",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Next Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onNext() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏭",
                        fontSize = 18.sp,
                        color = InkDark
                    )
                }
            }

            // Row 3: np-seek (currentTime, slider, durationTime)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playerState.formattedCurrentTime,
                    fontSize = 11.sp,
                    color = InkSoft,
                    modifier = Modifier.width(32.dp)
                )

                Slider(
                    value = if (isSeeking) seekFraction else playerState.progress,
                    onValueChange = {
                        isSeeking = true
                        seekFraction = it
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        val targetMs = (seekFraction * playerState.durationMs).toLong()
                        onSeek(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = PeachPrimary,
                        activeTrackColor = PeachPrimary,
                        inactiveTrackColor = PeachSoft
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .padding(horizontal = 4.dp)
                )

                Text(
                    text = playerState.formattedDuration,
                    fontSize = 11.sp,
                    color = InkSoft,
                    modifier = Modifier.width(32.dp)
                )
            }
        }
    }
}
