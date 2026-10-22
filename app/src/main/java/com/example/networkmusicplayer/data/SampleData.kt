package com.example.networkmusicplayer.data

import androidx.compose.ui.graphics.Color
import com.example.networkmusicplayer.model.NetworkServer
import com.example.networkmusicplayer.model.Track

object SampleData {
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

    val sampleTracks = listOf(
        Track(
            id = "t1",
            title = "Dirty Harry",
            artist = "Gorillaz",
            album = "Demon Days",
            durationMs = 230000L,
            coverColor = Color(0xFF673AB7),
            isNetworkTrack = true,
            sourceUrl = "http://192.168.1.120/Music/Midnight_Horizon.flac",
            isFavorite = true,
            bitrate = "FLAC 24-bit / 96kHz"
        ),
        Track(
            id = "t2",
            title = "Feel Good Inc",
            artist = "Gorillaz",
            album = "Demon Days",
            durationMs = 184000L,
            coverColor = Color(0xFFE91E63),
            isNetworkTrack = false,
            sourceUrl = null,
            isFavorite = false,
            bitrate = "320 kbps MP3"
        ) /*
        Track(
            id = "t3",
            title = "Ocean Waves & Chill",
            artist = "Lofi Lounge",
            album = "Midnight Stream",
            durationMs = 240000L,
            coverColor = Color(0xFF00BCD4),
            isNetworkTrack = true,
            sourceUrl = "http://stream.networkmusic.local:8080/lofi.mp3",
            isFavorite = true,
            bitrate = "256 kbps AAC"
        ),
        Track(
            id = "t4",
            title = "Acoustic Serenade",
            artist = "Clara Vance",
            album = "Unplugged Journeys",
            durationMs = 198000L,
            coverColor = Color(0xFFFF9800),
            isNetworkTrack = false,
            sourceUrl = null,
            isFavorite = true,
            bitrate = "320 kbps MP3"
        ),
        Track(
            id = "t5",
            title = "Galactic Odyssey",
            artist = "Starlight Ensemble",
            album = "Cosmic Dreams",
            durationMs = 312000L,
            coverColor = Color(0xFF3F51B5),
            isNetworkTrack = true,
            sourceUrl = "http://192.168.1.120/Music/Galactic_Odyssey.wav",
            isFavorite = false,
            bitrate = "FLAC 24-bit / 192kHz"
        ),
        Track(
            id = "t6",
            title = "Urban Rhythm",
            artist = "Metro Beatmasters",
            album = "City Lights",
            durationMs = 165000L,
            coverColor = Color(0xFF4CAF50),
            isNetworkTrack = false,
            sourceUrl = null,
            isFavorite = false,
            bitrate = "320 kbps MP3"
        ),
        Track(
            id = "t7",
            title = "Velvet Evening Jazz",
            artist = "The Midnight Trio",
            album = "Blue Note Room",
            durationMs = 275000L,
            coverColor = Color(0xFF9C27B0),
            isNetworkTrack = true,
            sourceUrl = "http://192.168.1.120/Music/Velvet_Evening_Jazz.flac",
            isFavorite = true,
            bitrate = "FLAC 16-bit / 44.1kHz"
        ) */
    )
}
