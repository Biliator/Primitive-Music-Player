package com.example.hudebni_prehravac.models

data class Music(
        var path: String,
        var title: String,
        val artist: String,
        val album: String,
        val duration: String,
        val size: String,
        val id: String,
        var art: ByteArray?,
        var fav: Boolean = false
)