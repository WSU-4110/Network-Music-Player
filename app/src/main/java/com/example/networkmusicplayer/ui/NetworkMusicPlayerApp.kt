package com.example.networkmusicplayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.networkmusicplayer.model.NavTab
import com.example.networkmusicplayer.ui.components.AddToPlaylistDialog
import com.example.networkmusicplayer.ui.components.CreatePlaylistDialog
import com.example.networkmusicplayer.ui.components.EqualizerSheet
import com.example.networkmusicplayer.ui.components.ExpandedPlayerSheet
import com.example.networkmusicplayer.ui.components.MiniPlayer
import com.example.networkmusicplayer.ui.components.PlaylistDetailSheet
import com.example.networkmusicplayer.ui.components.QueueBottomSheet
import com.example.networkmusicplayer.ui.screens.LibraryScreen
import com.example.networkmusicplayer.ui.screens.NetworkScreen
import com.example.networkmusicplayer.ui.screens.SearchScreen
import com.example.networkmusicplayer.ui.theme.NetworkMusicPlayerTheme
import com.example.networkmusicplayer.viewmodel.MusicPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkMusicPlayerApp(
    viewModel: MusicPlayerViewModel = viewModel(),
    onPlaySong: () -> Unit = {},
    onPauseSong: () -> Unit = {},
    onNextSong: () -> Unit = {},
    onPrevSong: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Network Music Player",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column {
                // Persistent Mini Player above Navigation Bar
                MiniPlayer(
                    track = uiState.currentTrack,
                    isPlaying = uiState.isPlaying,
                    progressMs = uiState.progressMs,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onPlayClick = onPlaySong,
                    onPauseClick = onPauseSong,
                    onNextClick = { viewModel.skipToNext(); onNextSong() },
                    onPlayerExpand = { viewModel.setExpandedPlayerVisible(true) }
                )

                // Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    NavigationBarItem(
                        selected = uiState.activeTab == NavTab.LIBRARY,
                        onClick = { viewModel.setNavTab(NavTab.LIBRARY) },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == NavTab.NETWORK,
                        onClick = { viewModel.setNavTab(NavTab.NETWORK) },
                        icon = { Icon(Icons.Default.CloudQueue, contentDescription = "Network Hub") },
                        label = { Text("Network") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == NavTab.SEARCH,
                        onClick = { viewModel.setNavTab(NavTab.SEARCH) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                NavTab.LIBRARY -> LibraryScreen(
                    tracks = uiState.allTracks,
                    playlists = uiState.playlists,
                    currentTrack = uiState.currentTrack,
                    isPlaying = uiState.isPlaying,
                    onPlayClick = onPlaySong,
                    activeFilter = uiState.libraryFilter,
                    onFilterSelect = { viewModel.setLibraryFilter(it) },
                    onTrackSelect = { viewModel.playTrack(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onPlayAll = {
                        uiState.allTracks.firstOrNull()?.let { viewModel.playTrack(it) }
                    },
                    onShuffleAll = {
                        if (uiState.allTracks.isNotEmpty()) {
                            viewModel.playTrack(uiState.allTracks.random())
                        }
                    },
                    onCreatePlaylistClick = { viewModel.setCreatePlaylistDialogVisible(true) },
                    onPlaylistSelect = { viewModel.selectPlaylist(it) },
                    onPlayPlaylist = { viewModel.playPlaylist(it); onPlaySong() },
                    onShufflePlaylist = { viewModel.shufflePlaylist(it); onPlaySong() },
                    onDeletePlaylist = { viewModel.deletePlaylist(it) },
                    onAddToQueue = { viewModel.addToQueue(it) },
                    onPlayNext = { viewModel.playNextInQueue(it) },
                    onAddToPlaylist = { viewModel.setTrackToAddToPlaylist(it) }
                )

                NavTab.NETWORK -> NetworkScreen(
                    servers = uiState.networkServers,
                    onToggleServer = { viewModel.toggleServerConnection(it) },
                    onAddServer = { name, address, protocol ->
                        viewModel.addNetworkServer(name, address, protocol)
                    }
                )

                NavTab.SEARCH -> SearchScreen(
                    tracks = uiState.allTracks,
                    currentTrack = uiState.currentTrack,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onTrackSelect = { viewModel.playTrack(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onAddToQueue = { viewModel.addToQueue(it) },
                    onPlayNext = { viewModel.playNextInQueue(it) },
                    onAddToPlaylist = { viewModel.setTrackToAddToPlaylist(it) }
                )
            }
        }
    }

    // Modal Expanded Player Sheet
    if (uiState.isExpandedPlayerVisible) {
        ExpandedPlayerSheet(
            track = uiState.currentTrack,
            isPlaying = uiState.isPlaying,
            progressMs = uiState.progressMs,
            volume = uiState.volume,
            repeatMode = uiState.repeatMode,
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onNextClick = { viewModel.skipToNext(); onNextSong() },
            onPreviousClick = { viewModel.skipToPrevious(); onPrevSong() },
            onSeek = { viewModel.seekTo(it) },
            onRepeatToggle = { viewModel.toggleRepeat() },
            onFavoriteToggle = { viewModel.toggleFavorite(it) },
            onVolumeChange = { viewModel.setVolume(it) },
            onQueueClick = { viewModel.setQueueSheetVisible(true) },
            onEqualizerClick = { viewModel.setEqualizerSheetVisible(true) },
            onDismiss = { viewModel.setExpandedPlayerVisible(false) },
            onPlayClick = onPlaySong,
            onPauseClick = onPauseSong
        )
    }

    // Queue Bottom Sheet
    if (uiState.isQueueSheetVisible) {
        QueueBottomSheet(
            queue = uiState.queue,
            currentQueueIndex = uiState.currentQueueIndex,
            onTrackSelect = {
                viewModel.playTrack(it)
                viewModel.setQueueSheetVisible(false)
            },
            onRemoveTrack = { viewModel.removeFromQueue(it) },
            onClearQueue = { viewModel.clearQueue() },
            onMoveUp = { viewModel.moveQueueItemUp(it) },
            onMoveDown = { viewModel.moveQueueItemDown(it) },
            onDismiss = { viewModel.setQueueSheetVisible(false) }
        )
    }

    // Equalizer Sheet
    if (uiState.isEqualizerSheetVisible) {
        EqualizerSheet(
            activePreset = uiState.eqPreset,
            bassBoost = uiState.bassBoost,
            trebleBoost = uiState.trebleBoost,
            onPresetSelect = { viewModel.setEqPreset(it) },
            onBassChange = { viewModel.setBassBoost(it) },
            onTrebleChange = { viewModel.setTrebleBoost(it) },
            onDismiss = { viewModel.setEqualizerSheetVisible(false) }
        )
    }

    // Create Playlist Dialog
    if (uiState.isCreatePlaylistDialogVisible) {
        CreatePlaylistDialog(
            onConfirm = { name -> viewModel.createPlaylist(name) },
            onDismiss = { viewModel.setCreatePlaylistDialogVisible(false) }
        )
    }

    // Add Track to Playlist Dialog
    uiState.trackToAddToPlaylist?.let { track ->
        AddToPlaylistDialog(
            track = track,
            playlists = uiState.playlists,
            onSelectPlaylist = { playlistId ->
                viewModel.addTrackToPlaylist(playlistId, track)
            },
            onCreateNewPlaylistClick = {
                viewModel.setTrackToAddToPlaylist(null)
                viewModel.setCreatePlaylistDialogVisible(true)
            },
            onDismiss = { viewModel.setTrackToAddToPlaylist(null) }
        )
    }

    // Playlist Details Sheet
    uiState.selectedPlaylist?.let { playlist ->
        PlaylistDetailSheet(
            playlist = playlist,
            currentTrack = uiState.currentTrack,
            onTrackSelect = { viewModel.playTrack(it); onPlaySong() },
            onRemoveTrack = { trackId -> viewModel.removeTrackFromPlaylist(playlist.id, trackId) },
            onPlayPlaylist = { viewModel.playPlaylist(playlist.id); onPlaySong() },
            onShufflePlaylist = { viewModel.shufflePlaylist(playlist.id); onPlaySong() },
            onDismiss = { viewModel.selectPlaylist(null) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NetworkMusicPlayerAppPreview() {
    NetworkMusicPlayerTheme {
        NetworkMusicPlayerApp()
    }
}
