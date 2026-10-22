package com.example.networkmusicplayer.model

import androidx.compose.ui.graphics.Color

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val coverColor: Color,
    val isNetworkTrack: Boolean = false,
    val sourceUrl: String? = null,
    val isFavorite: Boolean = false,
    val bitrate: String? = "320 kbps MP3"
)

data class NetworkServer(
    val id: String,
    val name: String,
    val protocol: String, // e.g., "HTTP/HTTPS", "SMB/NAS", "DLNA/UPnP"
    val address: String,
    val isConnected: Boolean = true,
    val trackCount: Int = 0
)
