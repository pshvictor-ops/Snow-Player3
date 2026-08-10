package com.example.musicplayer.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "audioId"])
data class PlaylistTrackEntity(
    val playlistId: Long,
    val audioId: Long,
    val title: String,
    val artist: String,
    val uri: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val audioId: Long,
    val title: String,
    val artist: String,
    val uri: String
)

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId")
    fun getTracksForPlaylist(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToPlaylist(track: PlaylistTrackEntity)

    @Delete
    suspend fun removeTrackFromPlaylist(track: PlaylistTrackEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE audioId = :audioId)")
    fun isFavorite(audioId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE audioId = :audioId")
    suspend fun deleteFavorite(audioId: Long)
}

@Database(entities = [PlaylistEntity::class, PlaylistTrackEntity::class, FavoriteEntity::class], version = 1, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
}
