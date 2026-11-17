package com.example.networkmusicplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkmusicplayer.data.SampleData
import com.example.networkmusicplayer.model.LibraryFilter
import com.example.networkmusicplayer.model.NavTab
import com.example.networkmusicplayer.model.NetworkServer
import com.example.networkmusicplayer.model.PlayerUiState
import com.example.networkmusicplayer.model.Playlist
import com.example.networkmusicplayer.model.RepeatMode
import com.example.networkmusicplayer.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * MusicPlayerViewModel - Sprint 2 Player Buttons & Playlist Management
 *
 * DESIGN PATTERNS USED:
 * 1. OBSERVER PATTERN (Reactive State Management):
 *    Exposes an immutable `StateFlow<PlayerUiState>` (`uiState`). The Jetpack Compose UI
 *    observes `uiState` reactively. Any player button or playlist modification updates
 *    the state, triggering seamless UI re-renders without manual listener registration.
 *
 * 2. COMMAND PATTERN (Player & Playlist Commands):
 *    Encapsulates player button actions (play, pause, skip, seek, shuffle, repeat) and
 *    playlist queue operations (add, play next, remove, clear, reorder) as distinct
 *    command methods executed on the ViewModel state.
 *
 * 3. STRATEGY PATTERN (Playback Navigation Strategy):
 *    Dynamically adjusts track navigation in `handleTrackCompletion()` and `skipToNext()`
 *    based on active `RepeatMode` (NONE, ALL, ONE) and `isShuffleEnabled` strategies.
 */
class MusicPlayerViewModel : ViewModel() {

    // Observer Pattern: Mutable StateFlow holds the single source of truth for Player state
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        val initialTracks = SampleData.sampleTracks
        val initialServers = SampleData.sampleServers
        val firstTrack = initialTracks.firstOrNull()

        _uiState.update { state ->
            state.copy(
                allTracks = initialTracks,
                queue = initialTracks,
                currentTrack = firstTrack,
                networkServers = initialServers,
                playlists = emptyList()
            )
        }
    }

    /**
     * Starts the simulated playback timer coroutine job.
     */
    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val currentState = _uiState.value
                if (currentState.isPlaying && currentState.currentTrack != null) {
                    val newProgress = currentState.progressMs + 1000L
                    if (newProgress >= currentState.currentTrack.durationMs) {
                        handleTrackCompletion()
                    } else {
                        _uiState.update { it.copy(progressMs = newProgress) }
                    }
                }
            }
        }
    }

    /**
     * Strategy Pattern: Handles track completion based on the current RepeatMode strategy.
     */
    private fun handleTrackCompletion() {
        val state = _uiState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                // Loop current single track indefinitely
                _uiState.update {
                    it.copy(
                        progressMs = 0L,
                        isPlaying = true
                    )
                }
                startPlaybackTimer()
            }
            RepeatMode.ALL -> {
                // Repeat all: loop back to track 0 if at end of queue
                val nextIndex = if (state.queue.isNotEmpty()) (state.currentQueueIndex + 1) % state.queue.size else 0
                val nextTrack = state.queue.getOrNull(nextIndex)
                _uiState.update {
                    it.copy(
                        currentTrack = nextTrack ?: it.currentTrack,
                        currentQueueIndex = nextIndex,
                        progressMs = 0L,
                        isPlaying = nextTrack != null
                    )
                }
                if (nextTrack != null) startPlaybackTimer()
            }
            RepeatMode.NONE -> {
                // Repeat off: advance if not at end of queue, else stop playback
                if (state.currentQueueIndex < state.queue.size - 1) {
                    val nextIndex = state.currentQueueIndex + 1
                    val nextTrack = state.queue[nextIndex]
                    _uiState.update {
                        it.copy(
                            currentTrack = nextTrack,
                            currentQueueIndex = nextIndex,
                            progressMs = 0L,
                            isPlaying = true
                        )
                    }
                    startPlaybackTimer()
                } else {
                    _uiState.update {
                        it.copy(
                            isPlaying = false,
                            progressMs = 0L
                        )
                    }
                    playbackJob?.cancel()
                }
            }
        }
    }

    // =========================================================================
    // PLAYER BUTTON COMMANDS (Command Pattern & Sprint 2 Integration)
    // =========================================================================

    /**
     * Play Button Command: Plays a specific track and starts playback timer.
     */
    fun playTrack(track: Track) {
        val queue = _uiState.value.queue
        val index = queue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }

        _uiState.update { state ->
            state.copy(
                currentTrack = track,
                currentQueueIndex = index,
                isPlaying = true,
                progressMs = 0L
            )
        }
        startPlaybackTimer()
    }

    /**
     * Play/Pause Toggle Command: Toggles playback state between playing and paused.
     */
    fun togglePlayPause() {
        val currentState = _uiState.value
        if (currentState.currentTrack == null && currentState.queue.isNotEmpty()) {
            playTrack(currentState.queue.first())
            return
        }

        val nextPlaying = !currentState.isPlaying
        _uiState.update { it.copy(isPlaying = nextPlaying) }
        if (nextPlaying) {
            startPlaybackTimer()
        } else {
            playbackJob?.cancel()
        }
    }

    /**
     * Seek Position Command: Clamps and seeks to position in milliseconds.
     */
    fun seekTo(positionMs: Long) {
        _uiState.update { state ->
            val duration = state.currentTrack?.durationMs ?: 0L
            val clamped = positionMs.coerceIn(0L, duration)
            state.copy(progressMs = clamped)
        }
    }

    /**
     * Skip Next Command: Advances sequentially to the next track in the queue.
     */
    fun skipToNext() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return

        val nextIndex = (state.currentQueueIndex + 1) % state.queue.size
        val nextTrack = state.queue[nextIndex]
        _uiState.update {
            it.copy(
                currentTrack = nextTrack,
                currentQueueIndex = nextIndex,
                progressMs = 0L,
                isPlaying = true
            )
        }
        startPlaybackTimer()
    }

    /**
     * Skip Previous Command: Seeks to start if > 3s, else moves to previous track.
     */
    fun skipToPrevious() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return

        if (state.progressMs > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (state.currentQueueIndex - 1 < 0) {
            state.queue.size - 1
        } else {
            state.currentQueueIndex - 1
        }

        val prevTrack = state.queue[prevIndex]
        _uiState.update {
            it.copy(
                currentTrack = prevTrack,
                currentQueueIndex = prevIndex,
                progressMs = 0L,
                isPlaying = true
            )
        }
        startPlaybackTimer()
    }

    /**
     * Repeat Toggle Command: Cycles repeat mode strategy (NONE -> ALL -> ONE).
     */
    fun toggleRepeat() {
        _uiState.update { state ->
            val nextMode = when (state.repeatMode) {
                RepeatMode.NONE -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.NONE
            }
            state.copy(repeatMode = nextMode)
        }
    }

    fun toggleFavorite(trackId: String) {
        _uiState.update { state ->
            val updatedAll = state.allTracks.map {
                if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it
            }
            val updatedQueue = state.queue.map {
                if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it
            }
            val updatedCurrent = if (state.currentTrack?.id == trackId) {
                state.currentTrack.copy(isFavorite = !state.currentTrack.isFavorite)
            } else {
                state.currentTrack
            }

            state.copy(
                allTracks = updatedAll,
                queue = updatedQueue,
                currentTrack = updatedCurrent
            )
        }
    }

    fun setVolume(volume: Float) {
        _uiState.update { it.copy(volume = volume.coerceIn(0f, 1f)) }
    }

    fun setNavTab(tab: NavTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setLibraryFilter(filter: LibraryFilter) {
        _uiState.update { it.copy(libraryFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setExpandedPlayerVisible(visible: Boolean) {
        _uiState.update { it.copy(isExpandedPlayerVisible = visible) }
    }

    fun setQueueSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isQueueSheetVisible = visible) }
    }

    fun setEqualizerSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isEqualizerSheetVisible = visible) }
    }

    fun setEqPreset(preset: String) {
        _uiState.update { it.copy(eqPreset = preset) }
    }

    fun setBassBoost(level: Float) {
        _uiState.update { it.copy(bassBoost = level.coerceIn(0f, 1f)) }
    }

    fun setTrebleBoost(level: Float) {
        _uiState.update { it.copy(trebleBoost = level.coerceIn(0f, 1f)) }
    }

    // =========================================================================
    // PLAYLIST & QUEUE MANAGEMENT COMMANDS (Sprint 2 Feature Integration)
    // =========================================================================

    /**
     * Adds a track to the end of the active playlist queue.
     */
    fun addToQueue(track: Track) {
        _uiState.update { state ->
            if (state.queue.any { it.id == track.id }) state
            else state.copy(queue = state.queue + track)
        }
    }

    fun playNextInQueue(track: Track) {
        _uiState.update { state ->
            val mutableQueue = state.queue.toMutableList()
            mutableQueue.removeAll { it.id == track.id }
            val insertIndex = (state.currentQueueIndex + 1).coerceAtMost(mutableQueue.size)
            mutableQueue.add(insertIndex, track)
            state.copy(queue = mutableQueue)
        }
    }

    fun removeFromQueue(trackId: String) {
        _uiState.update { state ->
            val updatedQueue = state.queue.filter { it.id != trackId }
            val newIndex = if (updatedQueue.isEmpty()) 0 else state.currentQueueIndex.coerceAtMost(updatedQueue.size - 1)
            val newCurrentTrack = updatedQueue.getOrNull(newIndex)
            state.copy(
                queue = updatedQueue,
                currentQueueIndex = newIndex,
                currentTrack = if (updatedQueue.isEmpty()) null else (newCurrentTrack ?: state.currentTrack),
                isPlaying = if (updatedQueue.isEmpty()) false else state.isPlaying
            )
        }
    }

    fun clearQueue() {
        _uiState.update { state ->
            state.copy(
                queue = emptyList(),
                currentQueueIndex = 0,
                currentTrack = null,
                isPlaying = false,
                progressMs = 0L
            )
        }
        playbackJob?.cancel()
    }

    fun moveQueueItemUp(index: Int) {
        _uiState.update { state ->
            if (index <= 0 || index >= state.queue.size) return@update state
            val list = state.queue.toMutableList()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            val newCurrentIndex = when (state.currentQueueIndex) {
                index -> index - 1
                index - 1 -> index
                else -> state.currentQueueIndex
            }
            state.copy(queue = list, currentQueueIndex = newCurrentIndex)
        }
    }

    fun moveQueueItemDown(index: Int) {
        _uiState.update { state ->
            if (index < 0 || index >= state.queue.size - 1) return@update state
            val list = state.queue.toMutableList()
            val item = list.removeAt(index)
            list.add(index + 1, item)
            val newCurrentIndex = when (state.currentQueueIndex) {
                index -> index + 1
                index + 1 -> index
                else -> state.currentQueueIndex
            }
            state.copy(queue = list, currentQueueIndex = newCurrentIndex)
        }
    }

    // =========================================================================
    // CUSTOM PLAYLIST FEATURE COMMANDS (Command & Observer Patterns)
    // =========================================================================

    /**
     * Creates a new named playlist and adds it to the user's playlist collection.
     */
    fun createPlaylist(name: String) {
        val newPlaylist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "My Playlist" },
            tracks = emptyList()
        )
        _uiState.update { state ->
            state.copy(
                playlists = state.playlists + newPlaylist,
                isCreatePlaylistDialogVisible = false
            )
        }
    }

    /**
     * Deletes a custom playlist by ID.
     */
    fun deletePlaylist(playlistId: String) {
        _uiState.update { state ->
            val updated = state.playlists.filter { it.id != playlistId }
            val updatedSelected = if (state.selectedPlaylist?.id == playlistId) null else state.selectedPlaylist
            state.copy(
                playlists = updated,
                selectedPlaylist = updatedSelected
            )
        }
    }

    /**
     * Adds a song/track to a specific playlist.
     */
    fun addTrackToPlaylist(playlistId: String, track: Track) {
        _uiState.update { state ->
            val updatedPlaylists = state.playlists.map { playlist ->
                if (playlist.id == playlistId) {
                    if (playlist.tracks.any { it.id == track.id }) playlist
                    else playlist.copy(tracks = playlist.tracks + track)
                } else playlist
            }
            val updatedSelected = if (state.selectedPlaylist?.id == playlistId) {
                updatedPlaylists.firstOrNull { it.id == playlistId }
            } else state.selectedPlaylist

            state.copy(
                playlists = updatedPlaylists,
                selectedPlaylist = updatedSelected,
                trackToAddToPlaylist = null
            )
        }
    }

    /**
     * Removes a song/track from a specific playlist.
     */
    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        _uiState.update { state ->
            val updatedPlaylists = state.playlists.map { playlist ->
                if (playlist.id == playlistId) {
                    playlist.copy(tracks = playlist.tracks.filter { it.id != trackId })
                } else playlist
            }
            val updatedSelected = if (state.selectedPlaylist?.id == playlistId) {
                updatedPlaylists.firstOrNull { it.id == playlistId }
            } else state.selectedPlaylist

            state.copy(
                playlists = updatedPlaylists,
                selectedPlaylist = updatedSelected
            )
        }
    }

    /**
     * Shuffles all songs in a specific playlist, sets the playback queue to the shuffled list,
     * and starts playing from the first song.
     */
    fun shufflePlaylist(playlistId: String) {
        val state = _uiState.value
        val playlist = state.playlists.firstOrNull { it.id == playlistId } ?: return
        if (playlist.tracks.isEmpty()) return

        val shuffledTracks = playlist.tracks.shuffled()
        val firstTrack = shuffledTracks.first()

        _uiState.update {
            it.copy(
                queue = shuffledTracks,
                currentQueueIndex = 0,
                currentTrack = firstTrack,
                isPlaying = true,
                progressMs = 0L
            )
        }
        startPlaybackTimer()
    }

    /**
     * Plays all songs in a playlist sequentially from the start.
     */
    fun playPlaylist(playlistId: String) {
        val state = _uiState.value
        val playlist = state.playlists.firstOrNull { it.id == playlistId } ?: return
        if (playlist.tracks.isEmpty()) return

        val firstTrack = playlist.tracks.first()
        _uiState.update {
            it.copy(
                queue = playlist.tracks,
                currentQueueIndex = 0,
                currentTrack = firstTrack,
                isPlaying = true,
                progressMs = 0L
            )
        }
        startPlaybackTimer()
    }

    fun setCreatePlaylistDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isCreatePlaylistDialogVisible = visible) }
    }

    fun setTrackToAddToPlaylist(track: Track?) {
        _uiState.update { it.copy(trackToAddToPlaylist = track) }
    }

    fun selectPlaylist(playlist: Playlist?) {
        _uiState.update { it.copy(selectedPlaylist = playlist) }
    }

    fun addNetworkServer(name: String, address: String, protocol: String) {
        val newServer = NetworkServer(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Custom Network Stream" },
            protocol = protocol.ifBlank { "HTTP/HTTPS Stream" },
            address = address.ifBlank { "http://localhost:8080" },
            isConnected = true,
            trackCount = (10..500).random()
        )
        _uiState.update { state ->
            state.copy(networkServers = state.networkServers + newServer)
        }
    }

    fun toggleServerConnection(serverId: String) {
        _uiState.update { state ->
            val updated = state.networkServers.map { server ->
                if (server.id == serverId) {
                    server.copy(isConnected = !server.isConnected)
                } else server
            }
            state.copy(networkServers = updated)
        }
    }
}
