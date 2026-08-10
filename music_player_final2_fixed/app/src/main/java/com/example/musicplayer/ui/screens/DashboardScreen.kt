package com.example.musicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.domain.model.AudioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    audioList: List<AudioItem>,
    onItemClick: (AudioItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tracks", "Albums", "Artists", "Favorites")
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(audioList, searchQuery) {
        if (searchQuery.isBlank()) audioList
        else audioList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search music...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> TrackListContent(audioList = filteredList, onItemClick = onItemClick)
            1 -> PlaceholderContent(title = "Albums View")
            2 -> PlaceholderContent(title = "Artists View")
            3 -> PlaceholderContent(title = "Favorites View")
        }
    }
}

@Composable
fun TrackListContent(
    audioList: List<AudioItem>,
    onItemClick: (AudioItem) -> Unit
) {
    if (audioList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No audio files found.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(audioList) { item ->
                ListItem(
                    headlineContent = { Text(item.title, maxLines = 1) },
                    supportingContent = { Text(item.artist, maxLines = 1) },
                    leadingContent = {
                        Icon(Icons.Default.MusicNote, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onItemClick(item) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun PlaceholderContent(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}
