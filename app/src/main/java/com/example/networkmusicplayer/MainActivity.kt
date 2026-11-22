package com.example.networkmusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.networkmusicplayer.filemanagement.MusicLibraryScanner
import com.example.networkmusicplayer.filemanagement.Track
import com.example.networkmusicplayer.ui.NetworkMusicPlayerApp
import com.example.networkmusicplayer.ui.smb.SmbDownloadScreen
import com.example.networkmusicplayer.ui.theme.NetworkMusicPlayerTheme
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

    private fun observePlayerState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { Triple(it.currentQueueIndex, it.isPlaying, it.currentTrack) }
                    .distinctUntilChanged()
                    .collect { (index, isPlaying, track) ->
                        if (track != null) {
                            val currentPlayerIndex = player?.currentMediaItemIndex ?: -1
                            if (currentPlayerIndex != index && index >= 0) {
                                player?.seekToDefaultPosition(index)
                            }
                            if (isPlaying) {
                                if (player?.isPlaying == false) {
                                    player?.play()
                                }
                            } else {
                                if (player?.isPlaying == true) {
                                    player?.pause()
                                }
                            }
                        }
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

    //For Sprint 2 Presentation, scans downloads folder on click of library page button
    private fun updateLibrary()
    {
        val downloadDir = File("/storage/emulated/0/Android/data/com.example.networkmusicplayer/files/Download")
        val scanner = MusicLibraryScanner.getInstance()
        val songs: List<Track> = scanner.scanDirectory(downloadDir)
        addSongsToPlayer(songs)
        viewModel.updateScreen(songs)
        val hi = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicLibraryScanner.Init(this.applicationContext)
        enableEdgeToEdge()

        // Initialize ExoPlayer first
        player = ExoPlayer.Builder(this).build()
        player?.prepare()
        player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                if (viewModel.uiState.value.isPlaying || viewModel.uiState.value.currentTrack != null) {
                    player?.currentMediaItemIndex?.let { index ->
                        val queue = viewModel.uiState.value.queue
                        if (index >= 0 && index < queue.size) {
                            val track = queue[index]
                            if (viewModel.uiState.value.currentTrack?.id != track.id) {
                                viewModel.playTrack(track)
                            }
                        }
                    }
                }
            }
        })

        observeAudioFiles()
        observePlayerState()
        checkAudioPermission()

        setContent {
            NetworkMusicPlayerTheme {
                //Pass button functionality calls to NetworkMusicPlayerApp

                NetworkMusicPlayerApp(
                    viewModel = viewModel,
                    onPlaySong = { playSong() },
                    onPauseSong = { pauseSong() },
                    onNextSong = { nextSong() },
                    onPrevSong = { prevSong() },
                    onUpdateClick = { updateLibrary() }

                )

            }
        }
    }
    

    //functions for player buttons
    private fun playSong() {
        val state = viewModel.uiState.value
        if (state.currentTrack == null && state.queue.isNotEmpty()) {
            viewModel.playTrack(state.queue.first())
        } else {
            player?.play()
            if (!state.isPlaying) {
                viewModel.togglePlayPause()
            }
        }
    }

    fun pauseSong() {
        player?.pause()
        if (viewModel.uiState.value.isPlaying) {
            viewModel.togglePlayPause()
        }
    }

    fun nextSong() {
        viewModel.skipToNext()
        val index = viewModel.uiState.value.currentQueueIndex
        if (index >= 0) {
            player?.seekToDefaultPosition(index)
            player?.play()
        }
    }

    fun prevSong() {
        viewModel.skipToPrevious()
        val index = viewModel.uiState.value.currentQueueIndex
        if (index >= 0) {
            player?.seekToDefaultPosition(index)
            player?.play()
        }
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