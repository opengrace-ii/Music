package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.player.LoopMode
import com.example.player.PlayerState
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMedium
import com.example.ui.theme.InkSoft
import com.example.ui.theme.PeachDark
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerDialog(
    playerState: PlayerState,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleLoop: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSaveToDeviceMusic: (Track) -> Unit,
    onSelectQueueItem: (Int) -> Unit
) {
    val track = playerState.currentTrack ?: return
    var showQueueSheet by remember { mutableStateOf(false) }

    // Vinyl spinning animation when audio is active
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CreamBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("full_player_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Player",
                            tint = InkDark
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PLAYING FROM OGM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeachDark,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (track.savedToDeviceMusic) "● Saved to Music Folder" else "● Offline Ready",
                            fontSize = 10.sp,
                            color = InkSoft
                        )
                    }

                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "View Queue",
                            tint = InkDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vinyl Record / Album Disc
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1611))
                        .border(4.dp, Color(0xFF38261B), CircleShape)
                        .rotate(if (playerState.isPlaying) rotation else 0f),
                    contentAlignment = Alignment.Center
                ) {
                    // Vinyl grooves rings
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .border(1.dp, Color(0xFF2C1E16), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(1.dp, Color(0xFF2C1E16), CircleShape)
                    )

                    // Center album artwork / label
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(CircleShape)
                            .background(PeachPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!track.thumbnailUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = track.thumbnailUrl,
                                contentDescription = "Album Art",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = track.initials,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // Center spindle hole
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(CreamBackground)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Track Title & Artist
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = track.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        fontSize = 14.sp,
                        color = InkSoft,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick action chips (Export to Music folder, Speed)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!track.savedToDeviceMusic) {
                            FilterChip(
                                selected = false,
                                onClick = { onSaveToDeviceMusic(track) },
                                label = { Text("Save to Music Folder", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FolderSpecial,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    labelColor = InkDark
                                )
                            )
                        }

                        // Playback Speed Toggle
                        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                        val nextSpeed = speeds[(speeds.indexOf(playerState.playbackSpeed) + 1) % speeds.size]
                        FilterChip(
                            selected = playerState.playbackSpeed != 1.0f,
                            onClick = { onSetSpeed(nextSpeed) },
                            label = { Text("${playerState.playbackSpeed}x", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                selectedContainerColor = PeachSoft,
                                labelColor = InkDark
                            )
                        )
                    }
                }

                // Seekbar & Times
                Column(modifier = Modifier.fillMaxWidth()) {
                    var isUserSeeking by remember { mutableStateOf(false) }
                    var sliderPos by remember { mutableFloatStateOf(0f) }

                    Slider(
                        value = if (isUserSeeking) sliderPos else playerState.progress,
                        onValueChange = {
                            isUserSeeking = true
                            sliderPos = it
                        },
                        onValueChangeFinished = {
                            isUserSeeking = false
                            val newMs = (sliderPos * playerState.durationMs).toLong()
                            onSeek(newMs)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = PeachPrimary,
                            activeTrackColor = PeachPrimary,
                            inactiveTrackColor = PeachSoft
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("full_player_seek_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = playerState.formattedCurrentTime,
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                        Text(
                            text = playerState.formattedDuration,
                            fontSize = 12.sp,
                            color = InkSoft
                        )
                    }
                }

                // Main Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playerState.isShuffle) PeachPrimary else InkSoft,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Previous Button
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = InkDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Main Big Play/Pause Button
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(PeachPrimary)
                            .testTag("full_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Next Button
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = InkDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Repeat Mode Button
                    IconButton(
                        onClick = onCycleLoop,
                        modifier = Modifier.size(44.dp)
                    ) {
                        val (icon, tint) = when (playerState.loopMode) {
                            LoopMode.OFF -> Icons.Default.Repeat to InkSoft
                            LoopMode.ALL -> Icons.Default.Repeat to PeachPrimary
                            LoopMode.ONE -> Icons.Default.RepeatOne to PeachPrimary
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repeat Mode",
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Queue Bottom Sheet
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Playing Queue (${playerState.queue.size} songs)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    itemsIndexed(playerState.queue) { idx, qTrack ->
                        val isCurrent = idx == playerState.currentIndex
                        Card(
                            onClick = {
                                onSelectQueueItem(idx)
                                showQueueSheet = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) PeachSoft else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) PeachPrimary else InkSoft,
                                    modifier = Modifier.width(28.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = qTrack.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = InkDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = qTrack.artist,
                                        fontSize = 11.sp,
                                        color = InkSoft,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = qTrack.formattedDuration,
                                    fontSize = 11.sp,
                                    color = InkMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
