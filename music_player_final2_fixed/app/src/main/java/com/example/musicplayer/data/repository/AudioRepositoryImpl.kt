package com.example.musicplayer.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.musicplayer.di.IoDispatcher
import com.example.musicplayer.domain.model.AudioItem
import com.example.musicplayer.domain.model.Lyrics
import com.example.musicplayer.domain.model.Result
import com.example.musicplayer.domain.repository.AudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AudioRepository {

    override suspend fun getLocalAudioFiles(): Flow<Result<List<AudioItem>>> = flow {
        emit(Result.Loading)
        try {
            val audioList = mutableListOf<AudioItem>()
            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
            )

            val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
            val selectionArgs = arrayOf("30000")
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Title"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val duration = cursor.getLong(durationColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val filePath = cursor.getString(dataColumn)

                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val albumArtUri = ContentUris.withAppendedId(
                        android.net.Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    val lyrics = extractLyricsFromFile(filePath)

                    audioList.add(
                        AudioItem(
                            id = id,
                            uri = uri,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            albumArtUri = albumArtUri,
                            lyrics = lyrics
                        )
                    )
                }
            }

            emit(Result.Success(audioList))
        } catch (e: Exception) {
            emit(Result.Error(e, "Failed to scan local audio files: ${e.localizedMessage}"))
        }
    }.flowOn(ioDispatcher)

    private fun extractLyricsFromFile(filePath: String?): Lyrics? {
        if (filePath == null) return null
        try {
            val audioFile = File(filePath)
            val lrcFile = File(audioFile.parent, audioFile.nameWithoutExtension + ".lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                val rawText = lrcFile.readText()
                return Lyrics(rawText = rawText)
            }
        } catch (e: Exception) {
            // Ignore lyrics extraction errors
        }
        return null
    }
}
