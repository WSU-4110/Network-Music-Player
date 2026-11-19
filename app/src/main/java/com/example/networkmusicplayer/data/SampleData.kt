package com.example.networkmusicplayer.data

import com.example.networkmusicplayer.filemanagement.NetworkServer
import com.example.networkmusicplayer.filemanagement.Track
import com.example.networkmusicplayer.model.Playlist

//Temporary sample data for songs and servers
object SampleData {
    val samplePlaylists = emptyList<Playlist>()
    val sampleServers = listOf(
        NetworkServer(
            id = 1,
            name = "Home NAS Audio Drive",
            protocol = "SMB / Local Network",
            address = "192.168.1.120/Music",
            isConnected = true,
            trackCount = 4820
        ),
        NetworkServer(
            id = 2,
            name = "Hi-Res Stream Node",
            protocol = "HTTP / FLAC Stream",
            address = "http://stream.networkmusic.local:8080",
            isConnected = true,
            trackCount = 1250
        ),
        NetworkServer(
            id = 3,
            name = "DLNA Media Server",
            protocol = "UPnP / DLNA",
            address = "dlna://media-server.local:1900",
            isConnected = false,
            trackCount = 3100
        )
    )

    val sampleTracks = emptyList<Track>()
}
