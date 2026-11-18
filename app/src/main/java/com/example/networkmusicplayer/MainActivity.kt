package com.example.networkmusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.networkmusicplayer.ui.NetworkMusicPlayerApp
import com.example.networkmusicplayer.ui.theme.NetworkMusicPlayerTheme
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaMetadata
import com.example.networkmusicplayer.filemanagement.MusicLibraryScanner
import com.example.networkmusicplayer.filemanagement.NetworkServer
import com.example.networkmusicplayer.filemanagement.Track
import com.example.networkmusicplayer.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private val viewModel: MusicPlayerViewModel by viewModels()
    private val audioPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted ->
            if (granted) {
                val musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                viewModel.scanAudioFiles("Local Storage", "Local", musicDirectory)
            }
            else {
                Toast.makeText(
                    this,
                    "Audio permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun checkAudioPermission() {

        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        }
        else{
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            val musicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            viewModel.scanAudioFiles("Local Storage", "Local", musicDirectory)
        }
        else {
            audioPermissionLauncher.launch(permission)
        }
    }

    private fun observeAudioFiles() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState
                    .map { it.allTracks }
                    .distinctUntilChanged()
                    .collect { tracks ->
                        Log.d("NMP", "Updating player...");
                        addSongsToPlayer(tracks)

                    }
            }
        }
    }

    private fun addSongsToPlayer(allTracks: List<Track>) {

        if (allTracks.isEmpty()) return

        val mediaItems = allTracks.map { track ->

            MediaItem.Builder()
                .setUri(File(track.sourceUrl).toUri())
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .build()
                )
                .build()
        }

        player?.setMediaItems(mediaItems)
        player?.prepare()
        Log.d("NMP", "Added songs to player");
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicLibraryScanner.Init(this.applicationContext)
        enableEdgeToEdge()
        observeAudioFiles()
        checkAudioPermission()
        setContent {
            NetworkMusicPlayerTheme {
                //Pass button functionality calls to NetworkMusicPlayerApp
                NetworkMusicPlayerApp(
                    viewModel = viewModel,
                    onPlaySong = { playSong() },
                    onPauseSong = { pauseSong() },
                    onNextSong = { nextSong() },
                    onPrevSong = { prevSong() }
                )
            }
        }
        //Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        player?.prepare()
    }

    //functions for player buttons
    private fun playSong() {
        player?.play()
    }

    fun pauseSong() {
        player?.pause()
    }

    fun nextSong() {
        player?.seekToNextMediaItem()
    }

    fun prevSong() {
        player?.seekToPreviousMediaItem()
    }

    //Cleanup player after the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NetworkMusicPlayerTheme {
        Greeting("Android")
    }
}

//HI!!!!!