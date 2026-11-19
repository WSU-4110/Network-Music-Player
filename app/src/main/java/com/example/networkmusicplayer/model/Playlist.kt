package com.example.networkmusicplayer.model

import com.example.networkmusicplayer.filemanagement.Track
import java.util.UUID

data class Playlist(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val tracks: List<Track> = emptyList()
)
