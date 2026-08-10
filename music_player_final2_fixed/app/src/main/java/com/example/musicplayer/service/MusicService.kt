package com.example.musicplayer.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@AndroidEntryPoint
class MusicService : MediaSessionService(), Player.Listener {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private var mediaSession: MediaSession? = null

    companion object {
        private val _playerErrors = MutableSharedFlow<PlaybackException>()
        val playerErrors: SharedFlow<PlaybackException> = _playerErrors.asSharedFlow()
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        exoPlayer.addListener(this)
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        CoroutineScope(Dispatchers.Main).launch {
            _playerErrors.emit(error)
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer.removeListener(this)
        super.onDestroy()
    }
}
