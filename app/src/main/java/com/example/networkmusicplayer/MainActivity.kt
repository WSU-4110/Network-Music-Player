package com.example.networkmusicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.networkmusicplayer.ui.theme.NetworkMusicPlayerTheme
import com.example.networkmusicplayer.R
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import android.net.Uri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetworkMusicPlayerTheme {
                MusicPlayerApp(onPlaySong = { playSong() }, onPauseSong = { pauseSong() })
            }
        }
    }

    //temporary
    val songUri = Uri.parse("android.resource://NetworkMusicPlayer/raw/dirty_harry.flac")
    private val player = ExoPlayer.Builder(this).build()
    private val mediaItem = MediaItem.fromUri(songUri)

    fun playSong() {
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun pauseSong() {
        player.pause()
    }
}

@PreviewScreenSizes
@Composable
fun MusicPlayerApp(onPlaySong: () -> Unit = {}, onPauseSong: () -> Unit = {}) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        //swap between the two screens
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(modifier = Modifier.padding(innerPadding), onPlayClick = onPlaySong, onPauseClick = onPauseSong)
                AppDestinations.FILES -> FileScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
//home screen for playing music and such
@Composable
fun HomeScreen(modifier: Modifier = Modifier, onPlayClick: () -> Unit, onPauseClick: () -> Unit) {
//pad to center
    Column(
        modifier = Modifier.fillMaxSize(),
        //.padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //make this modifiable to current song playing
        SongTitle(
            songName = "None",
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            //play button
            Button(onClick = onPlayClick) {
                Text("Play")
            }
            Button(onClick = onPauseClick) {
                Text("Pause")
            }
        }
    }
}
//screen for the file manager
@Composable
fun FileScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("put files here :)")
    }
}

//buttons for swapping between the two screens
enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FILES("Files", R.drawable.ic_account_box)
}
//display current song playing
@Composable
fun SongTitle(songName: String, modifier: Modifier = Modifier) {
    Text(
        text = "Current song: $songName",
        modifier = modifier
    )
}


/* I LOW KEY FORGOT WHAT THIS PART WAS FOR BUT I'M LIKE 99% SURE IT CAN BE DELETED
@Preview(showBackground = true)
@Composable
fun TitlePreview() {
    Music_PlayerTheme {

    }
}
*/