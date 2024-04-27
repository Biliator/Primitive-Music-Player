package com.example.hudebni_prehravac.room

import androidx.room.TypeConverter
import com.example.hudebni_prehravac.models.Music
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromString(value: String): ArrayList<Music> {
        val listType = object : TypeToken<ArrayList<Music>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<Music>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}