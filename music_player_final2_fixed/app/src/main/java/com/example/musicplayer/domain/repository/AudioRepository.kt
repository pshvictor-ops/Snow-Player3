package com.example.musicplayer.domain.repository

import com.example.musicplayer.domain.model.AudioItem
import com.example.musicplayer.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    suspend fun getLocalAudioFiles(): Flow<Result<List<AudioItem>>>
}
