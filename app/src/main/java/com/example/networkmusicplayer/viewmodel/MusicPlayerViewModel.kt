package com.example.networkmusicplayer.viewmodel

import android.os.Environment
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.networkmusicplayer.filemanagement.NetworkServer
import com.example.networkmusicplayer.filemanagement.Track
import com.example.networkmusicplayer.model.LibraryFilter
import com.example.networkmusicplayer.model.NavTab
import com.example.networkmusicplayer.model.PlayerUiState
import com.example.networkmusicplayer.model.RepeatMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.collections.minus


class MusicPlayerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null


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

    private fun handleTrackCompletion() {
        val state = _uiState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                _uiState.update { it.copy(progressMs = 0L) }
            }
            RepeatMode.ALL, RepeatMode.NONE -> {
                if (state.currentQueueIndex < state.queue.size - 1) {
                    skipToNext()
                } else if (state.repeatMode == RepeatMode.ALL && state.queue.isNotEmpty()) {
                    val first = state.queue.first()
                    _uiState.update {
                        it.copy(
                            currentTrack = first,
                            currentQueueIndex = 0,
                            progressMs = 0L
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isPlaying = false,
                            progressMs = 0L
                        )
                    }
                }
            }
        }
    }

    fun playTrack(track: Track) {
        val queue = if (_uiState.value.queue.isEmpty()) _uiState.value.allTracks else _uiState.value.queue
        val index = queue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }

        _uiState.update { state ->
            state.copy(
                queue = queue,
                currentTrack = track,
                currentQueueIndex = index,
                isPlaying = true,
                progressMs = 0L
            )
        }
        startPlaybackTimer()
    }

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

    fun seekTo(positionMs: Long) {
        _uiState.update { state ->
            val duration = state.currentTrack?.durationMs ?: 0L
            val clamped = positionMs.coerceIn(0L, duration)
            state.copy(progressMs = clamped)
        }
    }

    fun skipToNext() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return

        val nextIndex = if (state.isShuffleEnabled) {
            state.queue.indices.random()
        } else {
            (state.currentQueueIndex + 1) % state.queue.size
        }

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

    fun toggleShuffle() {
        _uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
    }

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

    fun toggleFavorite(trackId: Int) {
        _uiState.update { state ->
            val updatedAll = state.allTracks.map {
                if (it.id == trackId) {
                    it.isFavorite = !it.isFavorite
                    it.copy()
                }
                else it
            }

            val updatedQueue = state.queue.map {
                if (it.id == trackId) {
                    it.isFavorite = !it.isFavorite
                    it.copy()
                }
                else it
            }

            val updatedCurrent = if (state.currentTrack?.id == trackId) {
                state.currentTrack.isFavorite = !state.currentTrack.isFavorite
                state.currentTrack.copy()
            }
            else {
                state.currentTrack?.copy()
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

    fun addNetworkServer(name: String, address: String, protocol: String) {
        val file = File(address)
        if (!file.exists() || !file.isDirectory)
            return

        scanAudioFiles(name, protocol, file)
    }

    fun toggleServerConnection(serverId: Int) {
        var networkServer = NetworkServer()
        _uiState.update { state ->
            val updated = state.networkServers.map { server ->
                if (server.id == serverId) {
                    server.isConnected = !server.isConnected
                    networkServer = server
                    server.copy()
                }
                else server
            }
            val newTracks = if (networkServer.isConnected)
                (state.allTracks + networkServer.myTracks).sortedBy { it.id }
            else
                (state.allTracks - networkServer.myTracks.toSet()).sortedBy { it.id }

            state.copy(
                networkServers = updated,
                allTracks = newTracks,
                queue = newTracks
            )
        }
    }

    fun scanAudioFiles(name: String, protocol: String, directory: File) {
        if (_uiState.value.isLoading)
            return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            try {
                val server = NetworkServer.BuildFromDirectory(name, protocol, directory)
                _uiState.update { state ->
                    val newTracks = state.allTracks + server.myTracks
                    state.copy(
                        networkServers = state.networkServers + server,
                        allTracks = newTracks,
                        queue = newTracks,
                        isLoading = false
                    )
                }
            }
            catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}
