package com.example.musicplayer.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackParameters
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayer.domain.model.AudioItem
import com.example.musicplayer.domain.model.PlayerErrorState
import com.example.musicplayer.domain.model.PlayerState
import com.example.musicplayer.service.MusicService
import com.example.musicplayer.util.LyricsParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _errorState = MutableStateFlow<PlayerErrorState>(PlayerErrorState.None)
    val errorState: StateFlow<PlayerErrorState> = _errorState.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining.asStateFlow()

    private var mediaController: MediaController? = null
    private var sleepTimerJob: Job? = null
    private var positionUpdateJob: Job? = null

    init {
        initializeController()
        observeServiceErrors()
    }

    private fun initializeController() {
        viewModelScope.launch {
            try {
                val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
                mediaController = MediaController.Builder(context, sessionToken).buildAsync().await()
                
                mediaController?.addListener(object : androidx.media3.common.Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.update { it.copy(isPlaying = isPlaying) }
                        if (isPlaying) startPositionUpdates() else stopPositionUpdates()
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        updatePlayerPositionAndDuration()
                    }
                })
            } catch (e: Exception) {
                _errorState.value = PlayerErrorState.Error(-1, "Failed to connect to MediaService: ${e.localizedMessage}")
            }
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    _playerState.update {
                        it.copy(
                            currentPosition = controller.currentPosition.coerceAtLeast(0L),
                            duration = controller.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(500L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
    }

    private fun updatePlayerPositionAndDuration() {
        mediaController?.let { controller ->
            _playerState.update {
                it.copy(
                    duration = controller.duration.coerceAtLeast(0L),
                    currentPosition = controller.currentPosition.coerceAtLeast(0L)
                )
            }
        }
    }

    private fun observeServiceErrors() {
        viewModelScope.launch {
            MusicService.playerErrors.collect { error ->
                _errorState.value = PlayerErrorState.Error(
                    errorCode = error.errorCode,
                    errorMessage = error.errorCodeName ?: error.localizedMessage ?: "Unknown playback error"
                )
            }
        }
    }

    fun playAudio(audioItem: AudioItem) {
        mediaController?.let { controller ->
            val parsedLyrics = LyricsParser.parse(audioItem.lyrics?.rawText)
            val enrichedItem = audioItem.copy(lyrics = parsedLyrics)

            val mediaItem = androidx.media3.common.MediaItem.Builder()
                .setUri(audioItem.uri)
                .setMediaId(audioItem.id.toString())
                .setTag(enrichedItem)
                .build()
            
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
            _playerState.update { it.copy(currentTrack = enrichedItem) }
        }
    }

    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _playerState.update { it.copy(currentPosition = position) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        mediaController?.setPlaybackParameters(PlaybackParameters(speed))
    }

    fun startSleepTimer(durationMillis: Long) {
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = durationMillis
        sleepTimerJob = viewModelScope.launch {
            var remaining = durationMillis
            while (remaining > 0) {
                delay(1000L)
                remaining -= 1000L
                _sleepTimerRemaining.value = remaining
            }
            mediaController?.pause()
            _sleepTimerRemaining.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemaining.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionUpdates()
        sleepTimerJob?.cancel()
        mediaController?.let {
            MediaController.releaseFuture(it.asFuture())
            mediaController = null
        }
    }
}
