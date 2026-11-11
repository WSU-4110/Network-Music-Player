package com.example.networkmusicplayer.model

object PlaylistManager {

    private val playlists = mutableListOf<Playlist>()

    fun createPlaylist(name: String) {
        playlists.add(Playlist(name))
    }

    fun getPlaylists(): List<Playlist> {
        return playlists
    }

    fun addTrackToPlaylist(
        playlist: Playlist,
        track: Track
    ) {
        playlist.tracks.add(track)
    }
}