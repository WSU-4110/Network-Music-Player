package com.example.networkmusicplayer.filemanagement;
import com.example.networkmusicplayer.filemanagement.Track;
import android.content.Context;
import android.content.pm.PackageManager;

import static org.junit.Assert.*;

import androidx.test.core.app.ApplicationProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MusicLibraryScannerTest {

    @Test public void testScan() {
        Context ctx = ApplicationProvider.getApplicationContext();
        File downloadDir = new File("/storage/emulated/0/Android/data/com.example.networkmusicplayer/files/Download");
        MusicLibraryScanner.Init(ctx);
        var scanner = MusicLibraryScanner.getInstance();
        List<Track> songs = new ArrayList<>();
        songs = scanner.scanDirectory(downloadDir);
        assertFalse(!songs.isEmpty());
    }

}