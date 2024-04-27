package com.example.hudebni_prehravac.room

import androidx.lifecycle.LiveData

class Repositiory(private val musicDao: MusicDao) {
    val getAll: LiveData<List<PlayList>> = musicDao.getPlaylists()
    suspend fun insertPlayList(playList: PlayList) {
        musicDao.insertPlayList(playList)
    }
    suspend fun deletePlayList(playList: PlayList) {
        musicDao.deletePlayList(playList)
    }
    suspend fun updatePlayList(playList: PlayList) {
        musicDao.updatePlayList(playList)
    }

    val getFav: LiveData<List<FavoriteMusic>> = musicDao.getFavorite()
    suspend fun insertFavorite(music: FavoriteMusic) {
        musicDao.insertFavorite(music)
    }
    suspend fun deleteFavorite(music: FavoriteMusic) {
        musicDao.deleteFavorite(music)
    }
    suspend fun updateFavorite(music: FavoriteMusic) {
        musicDao.updateFavorite(music)
    }
}