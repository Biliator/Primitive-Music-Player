package com.example.hudebni_prehravac.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hudebni_prehravac.models.Music

@Entity(tableName = "playlist_table")
data class PlayList(
    @PrimaryKey(autoGenerate = true) val id: Int,
    var playListName: String,
    var musics: ArrayList<Music>
)