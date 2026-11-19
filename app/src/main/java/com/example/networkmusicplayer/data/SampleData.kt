package com.example.networkmusicplayer.data

import androidx.compose.ui.graphics.Color
import com.example.networkmusicplayer.model.NetworkServer
import com.example.networkmusicplayer.model.Playlist
import com.example.networkmusicplayer.model.Track

//Temporary sample data for songs and servers
object SampleData {
    val samplePlaylists = emptyList<Playlist>()
    val sampleServers = listOf(
        NetworkServer(
            id = "server-1",
            name = "Home NAS Audio Drive",
            protocol = "SMB / Local Network",
            address = "192.168.1.120/Music",
            isConnected = true,
            trackCount = 4820
        ),
        NetworkServer(
            id = "server-2",
            name = "Hi-Res Stream Node",
            protocol = "HTTP / FLAC Stream",
            address = "http://stream.networkmusic.local:8080",
            isConnected = true,
            trackCount = 1250
        ),
        NetworkServer(
            id = "server-3",
            name = "DLNA Media Server",
            protocol = "UPnP / DLNA",
            address = "dlna://media-server.local:1900",
            isConnected = false,
            trackCount = 3100
        )
    )

    val sampleTracks = emptyList<Track>()
}
