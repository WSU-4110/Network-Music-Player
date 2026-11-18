package com.example.networkmusicplayer.filemanagement;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

//Scanner that scans local directories for music files
public final class MusicLibraryScanner {

    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "m4a", "wav", "flac", "ogg");
    private static MusicLibraryScanner instance;
    private Context m_MainContext;

    public static MusicLibraryScanner getInstance() {
        if (instance == null)
            instance = new MusicLibraryScanner();
        return instance;
    }

    public static void Init(Context context) {
        if (instance != null)
            return;
        instance = new MusicLibraryScanner(context);
    }

    private MusicLibraryScanner() {}

    private MusicLibraryScanner(Context context) {
        this.m_MainContext = context;
    }

    //Scans a normal filesystem directory.
    //Use only for directories the app can access.
    public List<Track> scanDirectory(File directory) {
        if (!checkMusicPermission()) {
            Log.d("NMP", "No Permission");
            return Collections.emptyList();
        }
        Log.d("NMP", "Scanning Directory...");
        List<Track> songs = new ArrayList<>();

        if (directory == null || !directory.isDirectory() || !directory.canRead()) {
            Log.d("NMP", "Directory Unreadable");
            return songs;
        }

        scanFileDirectory(directory, songs);
        return songs;
    }

    //Scans a directory selected using ACTION_OPEN_DOCUMENT_TREE.
    //The app must have permission to access the selected tree.
    public List<Track> scanTree(Context context, Uri treeUri) {
        List<Track> songs = new ArrayList<>();

        if (treeUri == null) {
            return songs;
        }

        DocumentFile root = DocumentFile.fromTreeUri(context, treeUri);

        if (root != null && root.isDirectory()) {
            scanDocumentDirectory(root, songs);
        }

        return songs;
    }

    private void scanFileDirectory(File directory, List<Track> songs) {
        File[] entries = directory.listFiles();

        if (entries == null) {
            Log.d("NMP", "No Entries");
            return;
        }

        for (File entry : entries) {
            Log.d("NMP", entry.getName());
            if (entry.isDirectory()) {
                scanFileDirectory(entry, songs);
            }
            else if (entry.isFile() && isAudioFile(entry.getName())) {
                try {
                    Uri uri = Uri.fromFile(entry);
                    Track info = MusicParser.parse(m_MainContext, uri);
                    info.setSourceUrl(entry.getPath());
                    songs.add(info);
                    Log.d("NMP", "Song Added");
                }
                catch (Exception e) {
                    Log.d("NMP", "Caught Exception");
                    // Skip unreadable or unsupported files.
                }
            }
        }
    }

    private void scanDocumentDirectory(DocumentFile directory, List<Track> songs) {
        DocumentFile[] entries = directory.listFiles();

        for (DocumentFile entry : entries) {
            if (entry.isDirectory()) {
                scanDocumentDirectory(entry, songs);
            }
            else if (entry.isFile() &&
                    isAudioFile(entry.getName())) {
                try {
                    Track info = MusicParser.parse(m_MainContext, entry.getUri());

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

    private boolean checkMusicPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(m_MainContext, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}
