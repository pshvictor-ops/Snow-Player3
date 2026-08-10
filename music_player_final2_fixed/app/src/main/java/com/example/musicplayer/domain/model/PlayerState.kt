package com.example.musicplayer.domain.model

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val currentTrack: AudioItem? = null
)

sealed interface PlayerErrorState {
    data object None : PlayerErrorState
    data class Error(val errorCode: Int, val errorMessage: String) : PlayerErrorState
}
