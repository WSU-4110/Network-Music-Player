package com.example.networkmusicplayer.model

enum class RepeatMode {
    NONE,
    ALL,
    ONE
}

enum class NavTab {
    LIBRARY,
    NETWORK,
    SEARCH
}

enum class LibraryFilter {
    ALL,
    FAVORITES,
    LOCAL,
    NETWORK
}

data class PlayerUiState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progressMs: Long = 0L,
    val volume: Float = 0.8f,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val queue: List<Track> = emptyList(),
    val currentQueueIndex: Int = 0,
    val activeTab: NavTab = NavTab.LIBRARY,
    val libraryFilter: LibraryFilter = LibraryFilter.ALL,
    val searchQuery: String = "",
    val isExpandedPlayerVisible: Boolean = false,
    val isQueueSheetVisible: Boolean = false,
    val isEqualizerSheetVisible: Boolean = false,
    val eqPreset: String = "Flat",
    val bassBoost: Float = 0.5f,
    val trebleBoost: Float = 0.5f,
    val networkServers: List<NetworkServer> = emptyList(),
    val allTracks: List<Track> = emptyList()
)

