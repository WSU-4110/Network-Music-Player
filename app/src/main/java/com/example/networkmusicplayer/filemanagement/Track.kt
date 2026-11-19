package com.example.networkmusicplayer.filemanagement

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import kotlin.random.nextLong

class Track() {
    var id: Int = -1
    var title: String = ""
    var artist: String = "Unknown artist"
    var album: String = "Unknown album"
    var durationMs: Long = -1
    var coverColor: Color = Color.Red
    var isNetworkTrack: Boolean = false
    var sourceUrl: String? = null
    var isFavorite: Boolean = false
    var bitrate: String = "        "
    var albumArt: Bitmap? = null

    constructor(a: Int = -1) : this() {
        id = Track.TrackID++
        coverColor = Color(Random.nextLong())
    }

    fun copy() : Track {
        val track = Track();
        track.id = this.id;
        track.title = this.title;
        track.artist = this.artist;
        track.album = this.album;
        track.durationMs = this.durationMs;
        track.coverColor = this.coverColor;
        track.isNetworkTrack = this.isNetworkTrack;
        track.sourceUrl = this.sourceUrl;
        track.isFavorite = this.isFavorite;
        track.bitrate = this.bitrate;
        track.albumArt = this.albumArt;

        return track;
    }

    companion object {
        var TrackID: Int = 0
    }
}

