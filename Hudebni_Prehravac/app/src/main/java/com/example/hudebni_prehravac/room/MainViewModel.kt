package com.example.hudebni_prehravac.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    val getPlayList: LiveData<List<PlayList>>
    val getFavorite: LiveData<List<FavoriteMusic>>
    private val repository: Repositiory

    init {
        val musicDao = AppDatabase.getDatabase(application).musicDao()
        repository = Repositiory(musicDao)
        getPlayList = repository.getAll
        getFavorite = repository.getFav
    }

    fun addPlaylist(playlist: PlayList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPlayList(playlist)
        }
    }

    fun updatePlaylist(playlist: PlayList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePlayList(playlist)
        }
    }

    fun removePlaylist(playlist: PlayList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlayList(playlist)
        }
    }

    fun insertFavorite(favorite: FavoriteMusic) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFavorite(favorite)
        }
    }

    fun updateFavorite(favorite: FavoriteMusic) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavorite(favorite)
        }
    }

    fun removeFavorite(favorite: FavoriteMusic) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFavorite(favorite)
        }
    }
}