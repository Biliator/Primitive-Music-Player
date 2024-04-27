package com.example.hudebni_prehravac.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MusicDao {
    @Query("SELECT * FROM playlist_table")
    fun getPlaylists(): LiveData<List<PlayList>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayList(playList: PlayList)

    @Delete
    suspend fun deletePlayList(playList: PlayList)

    @Update
    fun updatePlayList(playList: PlayList)

    @Query("SELECT * FROM favorite_table")
    fun getFavorite(): LiveData<List<FavoriteMusic>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(fav: FavoriteMusic)

    @Delete
    suspend fun deleteFavorite(fav: FavoriteMusic)

    @Update
    fun updateFavorite(fav: FavoriteMusic)
}