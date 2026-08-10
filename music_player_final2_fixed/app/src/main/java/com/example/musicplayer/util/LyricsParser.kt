package com.example.musicplayer.util

import com.example.musicplayer.domain.model.Lyrics
import com.example.musicplayer.domain.model.TimedLyricLine
import java.util.regex.Pattern

object LyricsParser {
    private val timeTagPattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\]")

    fun parse(rawText: String?): Lyrics {
        if (rawText.isNullOrBlank()) {
            return Lyrics(rawText = null, timedLyrics = emptyList())
        }

        val timedLines = mutableListOf<TimedLyricLine>()
        val lines = rawText.lines()

        for (line in lines) {
            val matcher = timeTagPattern.matcher(line)
            val timestamps = mutableListOf<Long>()
            var lastIndex = 0

            while (matcher.find()) {
                val minutes = matcher.group(1)?.toLong() ?: 0L
                val seconds = matcher.group(2)?.toLong() ?: 0L
                val millisPart = matcher.group(3) ?: "0"
                val millis = if (millisPart.length == 2) millisPart.toLong() * 10 else millisPart.toLong()

                val totalMs = (minutes * 60 * 1000) + (seconds * 1000) + millis
                timestamps.add(totalMs)
                lastIndex = matcher.end()
            }

            if (timestamps.isNotEmpty()) {
                val text = line.substring(lastIndex).trim()
                for (timestamp in timestamps) {
                    timedLines.add(TimedLyricLine(timestampMs = timestamp, text = text))
                }
            }
        }

        val sortedTimedLines = timedLines.sortedBy { it.timestampMs }
        return Lyrics(rawText = rawText, timedLyrics = sortedTimedLines)
    }
}
