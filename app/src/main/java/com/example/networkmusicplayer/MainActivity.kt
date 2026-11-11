
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

class MainActivity : ComponentActivity() {

    private lateinit var musicPlayer: MusicPlayerFacade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        musicPlayer = MusicPlayerFacade(this)

        setContent {
            NetworkMusicPlayerTheme {
                NetworkMusicPlayerApp(
                    onPlaySong = { musicPlayer.playSong() },
                    onPauseSong = { musicPlayer.pauseSong() },
                    onNextSong = { musicPlayer.nextSong() },
                    onPrevSong = { musicPlayer.prevSong() }
                )
            }
        }
    }

    override fun onDestroy() {
        musicPlayer.release()
        super.onDestroy()
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
