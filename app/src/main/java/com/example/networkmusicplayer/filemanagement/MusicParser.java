package com.example.networkmusicplayer.filemanagement;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

//Class used for parsing music data from selected song
public class MusicParser {

    public static Track parse(Context context, Uri uri) throws Exception {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(context, uri);

            String title = getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_TITLE);

            String artist = getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_ARTIST);

            String album = getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_ALBUM);

            String duration = getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_DURATION);

            String mimeType = getMetadata(retriever, MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

            long durationMs = -1L;

            if (duration != null) {
                try {
                    durationMs = Long.parseLong(duration);
                }
                catch (NumberFormatException e) {
                    // Duration unavailable or invalid.
                }
            }

            byte[] imageBytes = retriever.getEmbeddedPicture();
            Bitmap artwork = null;

            if (imageBytes != null) {
                artwork = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }

            Track track = new Track();
            if (title != null)
                track.setTitle(title);
            if (artist != null)
                track.setArtist(artist);
            if (album != null)
                track.setAlbum(album);
            track.setDurationMs(durationMs);
            track.setAlbumArt(artwork);
            return track;

        }
        finally {
            retriever.release();
        }
    }

    private static String getMetadata(MediaMetadataRetriever retriever, int key) {
        String value = retriever.extractMetadata(key);

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
