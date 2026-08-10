package com.example.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.domain.model.AudioItem
import com.example.musicplayer.domain.model.PlayerState

@Composable
fun FullPlayerScreen(
    playerState: PlayerState,
    playbackSpeed: Float,
    sleepTimerRemaining: Long?,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSetSleepTimer: (Long) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back")
                }
                Text("Now Playing", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showLyrics = !showLyrics }) {
                    Icon(Icons.Default.Lyrics, contentDescription = "Toggle Lyrics")
                }
            }

            if (showLyrics && track != null) {
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()) {
                    com.example.musicplayer.ui.components.LyricsView(
                        lyrics = track.lyrics,
                        currentPosition = playerState.currentPosition
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .size(280.dp)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = track?.title ?: "No Track Playing",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = playerState.currentPosition.toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..playerState.duration.toFloat().coerceAtLeast(1f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(playerState.currentPosition), style = MaterialTheme.typography.bodySmall)
                    Text(formatTime(playerState.duration), style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showSpeedDialog = true }) {
                    Icon(Icons.Default.Speed, contentDescription = "Speed")
                }
                IconButton(onClick = { /* Previous */ }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                FloatingActionButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause"
                    )
                }
                IconButton(onClick = { /* Next */ }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
                IconButton(onClick = { showTimerDialog = true }) {
                    Icon(Icons.Default.Timer, contentDescription = "Sleep Timer")
                }
            }
        }
    }

    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed") },
            text = {
                Column {
                    listOf(0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        TextButton(
                            onClick = {
                                onSpeedChange(speed)
                                showSpeedDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${speed}x", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text("Close") }
            }
        )
    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Sleep Timer") },
            text = {
                Column {
                    listOf(15L to "15 minutes", 30L to "30 minutes", 60L to "1 hour").forEach { (mins, label) ->
                        TextButton(
                            onClick = {
                                onSetSleepTimer(mins * 60 * 1000L)
                                showTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label)
                        }
                    }
                    if (sleepTimerRemaining != null) {
                        TextButton(
                            onClick = {
                                onCancelSleepTimer()
                                showTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel Timer", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerDialog = false }) { Text("Close") }
            }
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
