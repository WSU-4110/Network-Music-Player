
package com.example.networkmusicplayer

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class MusicPlayerFacade(context: Context) {

    private val player: ExoPlayer =
        ExoPlayer.Builder(context).build()

    init {
        val uris: List<Uri> = listOf(
            Uri.parse(
                "android.resource://${context.packageName}/${R.raw.dirty_harry}"
            ),
            Uri.parse(
                "android.resource://${context.packageName}/${R.raw.feel_good_inc}"
            )
        )

        player.setMediaItems(
            uris.map { MediaItem.fromUri(it) }
        )
        player.prepare()
    }

    fun playSong() {
        player.play()
    }

    fun pauseSong() {
        player.pause()
    }

    fun nextSong() {
        player.seekToNextMediaItem()
    }

    fun prevSong() {
        player.seekToPreviousMediaItem()
    }

    fun release() {
        player.release()
    }
}
