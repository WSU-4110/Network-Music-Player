package com.example.networkmusicplayer.filemanagement

import java.io.File

class NetworkServer(
    var id: Int = -1,
    var name: String = "None",
    var protocol: String = "Local", // e.g., "HTTP/HTTPS", "SMB/NAS", "DLNA/UPnP"
    var address: String = "",
    var isConnected: Boolean = true,
    var myTracks: List<Track> = emptyList(),
    var trackCount: Int = 0
) {
    fun copy() : NetworkServer {
        val server = NetworkServer()
        server.id = this.id
        server.name = this.name
        server.protocol = this.protocol
        server.address = this.address
        server.isConnected = this.isConnected
        server.myTracks = this.myTracks
        server.trackCount = this.trackCount
        return server
    }

    companion object {
        var NetworkServerID: Int = 0
        fun BuildFromDirectory(name: String, protocol: String, directory: File): NetworkServer {
            val server = NetworkServer()
            server.id = NetworkServerID++
            server.name = name
            server.protocol = protocol
            server.address = directory.path

            server.myTracks += MusicLibraryScanner.getInstance().scanDirectory(directory)
            server.trackCount = server.myTracks.size
            return server
        }
    }
}
