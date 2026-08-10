package com.example.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.musicplayer.data.repository.AudioRepositoryImpl
import com.example.musicplayer.domain.model.AudioItem
import com.example.musicplayer.domain.model.Result
import com.example.musicplayer.ui.components.MiniPlayer
import com.example.musicplayer.ui.permissions.PermissionScreen
import com.example.musicplayer.ui.screens.DashboardScreen
import com.example.musicplayer.ui.screens.FullPlayerScreen
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import com.example.musicplayer.ui.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val musicViewModel: MusicViewModel by viewModels()

    @Inject
    lateinit var audioRepository: AudioRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionScreen {
                        MainContent(musicViewModel, audioRepository)
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent(
    viewModel: MusicViewModel,
    repository: AudioRepositoryImpl
) {
    var audioList by remember { mutableStateOf<List<AudioItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFullPlayer by remember { mutableStateOf(false) }

    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        repository.getLocalAudioFiles().collectLatest { result ->
            when (result) {
                is Result.Loading -> isLoading = true
                is Result.Success -> {
                    isLoading = false
                    audioList = result.data
                }
                is Result.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Scaffold(
                bottomBar = {
                    Column {
                        MiniPlayer(
                            currentTrack = playerState.currentTrack,
                            isPlaying = playerState.isPlaying,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onPlayerClick = { showFullPlayer = true }
                        )
                    }
                }
            ) { padding ->
                DashboardScreen(
                    audioList = audioList,
                    onItemClick = { track ->
                        viewModel.playAudio(track)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        if (showFullPlayer) {
            FullPlayerScreen(
                playerState = playerState,
                playbackSpeed = playbackSpeed,
                sleepTimerRemaining = sleepTimerRemaining,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onSetSleepTimer = { viewModel.startSleepTimer(it) },
                onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                onBackClick = { showFullPlayer = false }
            )
        }
    }
}
