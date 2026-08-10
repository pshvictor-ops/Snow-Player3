package com.example.musicplayer.domain.model

import android.net.Uri

data class AudioItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val albumArtUri: Uri?,
    val lyrics: Lyrics? = null
)

data class Lyrics(
    val rawText: String?,
    val timedLyrics: List<TimedLyricLine> = emptyList()
)

data class TimedLyricLine(
    val timestampMs: Long,
    val text: String
)
