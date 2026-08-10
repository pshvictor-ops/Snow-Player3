package com.example.musicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.musicplayer.domain.model.Lyrics
import kotlinx.coroutines.launch

@Composable
fun LyricsView(
    lyrics: Lyrics?,
    currentPosition: Long,
    modifier: Modifier = Modifier
) {
    if (lyrics == null || (lyrics.timedLyrics.isEmpty() && lyrics.rawText.isNullOrBlank())) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No lyrics available", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val timedLines = lyrics.timedLyrics
    if (timedLines.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = lyrics.rawText ?: "",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val activeIndex = remember(currentPosition, timedLines) {
        timedLines.indexOfLast { it.timestampMs <= currentPosition }.coerceAtLeast(0)
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(activeIndex.coerceAtLeast(0))
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(timedLines) { index, line ->
            val isActive = index == activeIndex
            val textColor = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
            val textStyle = if (isActive) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            }

            Text(
                text = line.text,
                style = textStyle,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )
        }
    }
}
