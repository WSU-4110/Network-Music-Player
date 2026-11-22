package com.example.networkmusicplayer

import androidx.compose.ui.graphics.Color
import com.example.networkmusicplayer.model.Playlist
import com.example.networkmusicplayer.model.RepeatMode
import com.example.networkmusicplayer.model.Track
import com.example.networkmusicplayer.viewmodel.MusicPlayerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Tests for Sprint 2 Features in MusicPlayerViewModel & Playlist Model.
 */
class MusicPlayerViewModelTest {

    private lateinit var viewModel: MusicPlayerViewModel

    private val testTrack1 = Track(
        id = "test-1",
        title = "Test Song 1",
        artist = "Artist A",
        album = "Album A",
        durationMs = 200000L,
        coverColor = Color.Red
    )

    private val testTrack2 = Track(
        id = "test-2",
        title = "Test Song 2",
        artist = "Artist B",
        album = "Album B",
        durationMs = 180000L,
        coverColor = Color.Blue
    )

    @Before
    fun setUp() {
        viewModel = MusicPlayerViewModel()
    }

    // =========================================================================
    // METHOD 1: createPlaylist(name)
    // =========================================================================
    @Test
    fun testCreatePlaylist_addsNewPlaylistToState() {
        val initialCount = viewModel.uiState.value.playlists.size
        val playlistName = "Workout Jams"

        viewModel.createPlaylist(playlistName)

        val updatedPlaylists = viewModel.uiState.value.playlists
        assertEquals(initialCount + 1, updatedPlaylists.size)
        val createdPlaylist = updatedPlaylists.last()
        assertEquals("Workout Jams", createdPlaylist.name)
        assertTrue(createdPlaylist.tracks.isEmpty())
    }

    // =========================================================================
    // METHOD 2: addTrackToPlaylist(playlistId, track)
    // =========================================================================
    @Test
    fun testAddTrackToPlaylist_appendsSongToTargetPlaylist() {
        viewModel.createPlaylist("Favorites Mix")
        val playlistId = viewModel.uiState.value.playlists.last().id

        viewModel.addTrackToPlaylist(playlistId, testTrack1)

        val updatedPlaylist = viewModel.uiState.value.playlists.first { it.id == playlistId }
        assertEquals(1, updatedPlaylist.tracks.size)
        assertEquals("Test Song 1", updatedPlaylist.tracks.first().title)
    }

    // =========================================================================
    // METHOD 3: removeTrackFromPlaylist(playlistId, trackId)
    // =========================================================================
    @Test
    fun testRemoveTrackFromPlaylist_deletesSongFromPlaylist() {
        viewModel.createPlaylist("Party Playlist")
        val playlistId = viewModel.uiState.value.playlists.last().id
        viewModel.addTrackToPlaylist(playlistId, testTrack1)
        viewModel.addTrackToPlaylist(playlistId, testTrack2)

        // Verify initial size
        var playlist = viewModel.uiState.value.playlists.first { it.id == playlistId }
        assertEquals(2, playlist.tracks.size)

        // Remove track 1
        viewModel.removeTrackFromPlaylist(playlistId, testTrack1.id)

        playlist = viewModel.uiState.value.playlists.first { it.id == playlistId }
        assertEquals(1, playlist.tracks.size)
        assertEquals(testTrack2.id, playlist.tracks.first().id)
    }

    // =========================================================================
    // METHOD 4: deletePlaylist(playlistId)
    // =========================================================================
    @Test
    fun testDeletePlaylist_removesPlaylistFromState() {
        viewModel.createPlaylist("Temporary Playlist")
        val playlistId = viewModel.uiState.value.playlists.last().id

        viewModel.deletePlaylist(playlistId)

        val remainingPlaylists = viewModel.uiState.value.playlists
        assertFalse(remainingPlaylists.any { it.id == playlistId })
    }

    // =========================================================================
    // METHOD 5: toggleRepeat()
    // =========================================================================
    @Test
    fun testToggleRepeat_cyclesThroughRepeatModes() {
        assertEquals(RepeatMode.NONE, viewModel.uiState.value.repeatMode)

        viewModel.toggleRepeat()
        assertEquals(RepeatMode.ALL, viewModel.uiState.value.repeatMode)

        viewModel.toggleRepeat()
        assertEquals(RepeatMode.ONE, viewModel.uiState.value.repeatMode)

        viewModel.toggleRepeat()
        assertEquals(RepeatMode.NONE, viewModel.uiState.value.repeatMode)
    }

    // =========================================================================
    // METHOD 6: Playlist.totalDurationMs (Computed Model Property)
    // =========================================================================
    @Test
    fun testPlaylistTotalDuration_calculatesSumOfTrackDurations() {
        val playlist = Playlist(
            id = "p-test",
            name = "Duration Test",
            tracks = listOf(testTrack1, testTrack2) // 200000 + 180000 = 380000
        )

        assertEquals(380000L, playlist.totalDurationMs)
    }
}
