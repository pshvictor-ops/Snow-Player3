package com.example.musicplayer.di

import android.content.Context
import androidx.room.Room
import com.example.musicplayer.data.local.FavoriteDao
import com.example.musicplayer.data.local.MusicDatabase
import com.example.musicplayer.data.local.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMusicDatabase(
        @ApplicationContext context: Context
    ): MusicDatabase =
        Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music_database"
        ).build()

    @Provides
    @Singleton
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao =
        database.playlistDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: MusicDatabase): FavoriteDao =
        database.favoriteDao()
}
