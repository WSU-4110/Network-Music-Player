package com.example.networkmusicplayer.model

data class Playlist(
    val name: String,
    val tracks: MutableList<Track> = mutableListOf()
)