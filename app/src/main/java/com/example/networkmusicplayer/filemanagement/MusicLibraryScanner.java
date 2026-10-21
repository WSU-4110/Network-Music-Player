package com.example.networkmusicplayer.filemanagement;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MusicLibraryScanner {

    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "m4a", "wav", "flac");

    private MusicLibraryScanner() {}

    //Scans a normal filesystem directory.
    //Use only for directories the app can access.
    public static List<MusicInfo> scanDirectory(Context context, File directory) {
        List<MusicInfo> songs = new ArrayList<>();

        if (directory == null || !directory.isDirectory() || !directory.canRead()) {
            return songs;
        }

        scanFileDirectory(context, directory, songs);
        return songs;
    }

    //Scans a directory selected using ACTION_OPEN_DOCUMENT_TREE.
    //The app must have permission to access the selected tree.
    public static List<MusicInfo> scanTree(Context context, Uri treeUri) {
        List<MusicInfo> songs = new ArrayList<>();

        if (treeUri == null) {
            return songs;
        }

        DocumentFile root = DocumentFile.fromTreeUri(context, treeUri);

        if (root != null && root.isDirectory()) {
            scanDocumentDirectory(context, root, songs);
        }

        return songs;
    }

    private static void scanFileDirectory(Context context, File directory, List<MusicInfo> songs) {
        File[] entries = directory.listFiles();

        if (entries == null) {
            return;
        }

        for (File entry : entries) {
            if (entry.isDirectory()) {
                scanFileDirectory(context, entry, songs);
            }
            else if (entry.isFile() && isAudioFile(entry.getName())) {
                try {
                    Uri uri = Uri.fromFile(entry);
                    MusicInfo info = MusicParser.parse(context, uri);
                    songs.add(info);
                }
                catch (Exception e) {
                    // Skip unreadable or unsupported files.
                }
            }
        }
    }

    private static void scanDocumentDirectory(Context context, DocumentFile directory, List<MusicInfo> songs) {
        DocumentFile[] entries = directory.listFiles();

        for (DocumentFile entry : entries) {
            if (entry.isDirectory()) {
                scanDocumentDirectory(context, entry, songs);
            }
            else if (entry.isFile() &&
                    isAudioFile(entry.getName())) {
                try {
                    MusicInfo info = MusicParser.parse(context, entry.getUri());

                    songs.add(info);
                }
                catch (Exception e) {
                    // Continue scanning remaining files.
                }
            }
        }
    }

    private static boolean isAudioFile(String name) {
        if (name == null) {
            return false;
        }

        int dot = name.lastIndexOf('.');

        if (dot < 0 || dot == name.length() - 1) {
            return false;
        }

        String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);

        return AUDIO_EXTENSIONS.contains(extension);
    }
}
