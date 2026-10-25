package com.example.networkmusicplayer

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

class MainActivity : ComponentActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetworkMusicPlayerTheme {
                NetworkMusicPlayerApp(
                    onPlaySong = { playSong() },
                    onPauseSong = { pauseSong() },
                    onNextSong = { nextSong() },
                    onPrevSong = { prevSong() })
            }
        }
        //Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // Load a set song from res/raw (temporary)
        val uris: List<Uri> = listOf(
            Uri.parse("android.resource://$packageName/${R.raw.dirty_harry}"),
            Uri.parse("android.resource://$packageName/${R.raw.feel_good_inc}")
        )

        player?.setMediaItems(uris.map {MediaItem.fromUri(it)})
        player?.prepare()
    }
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