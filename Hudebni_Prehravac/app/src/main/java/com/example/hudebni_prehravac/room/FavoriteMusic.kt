package com.example.hudebni_prehravac.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_table")
data class FavoriteMusic (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val musicId: String
)