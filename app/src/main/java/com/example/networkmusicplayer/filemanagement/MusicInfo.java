package com.example.networkmusicplayer.filemanagement;

import android.graphics.Bitmap;

public class MusicInfo {

    public final String title;
    public final String artist;
    public final String album;
    public final long durationMs;
    public final String mimeType;
    public final Bitmap albumArt;

    public MusicInfo(
            String title,
            String artist,
            String album,
            long durationMs,
            String mimeType,
            Bitmap albumArt
    ) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.durationMs = durationMs;
        this.mimeType = mimeType;
        this.albumArt = albumArt;
    }
}