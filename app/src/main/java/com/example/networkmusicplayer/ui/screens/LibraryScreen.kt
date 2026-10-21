package com.example.networkmusicplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.networkmusicplayer.data.SampleData
import com.example.networkmusicplayer.model.LibraryFilter
import com.example.networkmusicplayer.model.Track
import com.example.networkmusicplayer.ui.components.TrackListItem
import com.example.networkmusicplayer.ui.theme.TemplateAppTheme

@Composable
fun LibraryScreen(
    tracks: List<Track>,
    currentTrack: Track?,
    activeFilter: LibraryFilter,
    onFilterSelect: (LibraryFilter) -> Unit,
    onTrackSelect: (Track) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTracks = when (activeFilter) {
        LibraryFilter.ALL -> tracks
        LibraryFilter.FAVORITES -> tracks.filter { it.isFavorite }
        LibraryFilter.LOCAL -> tracks.filter { !it.isNetworkTrack }
        LibraryFilter.NETWORK -> tracks.filter { it.isNetworkTrack }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Filter Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LibraryFilter.entries) { filter ->
                val label = when (filter) {
                    LibraryFilter.ALL -> "All Tracks (${tracks.size})"
                    LibraryFilter.FAVORITES -> "Favorites ❤️"
                    LibraryFilter.LOCAL -> "Local Only"
                    LibraryFilter.NETWORK -> "Network Streams 🌐"
                }
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { onFilterSelect(filter) },
                    label = { Text(text = label, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Play All / Shuffle All Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Play All", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onShuffleAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Shuffle", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tracks List
        if (filteredTracks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No tracks found in this category",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredTracks, key = { it.id }) { track ->
                    TrackListItem(
                        track = track,
                        isPlayingCurrent = currentTrack?.id == track.id,
                        onTrackClick = { onTrackSelect(track) },
                        onFavoriteToggle = { onFavoriteToggle(track.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    TemplateAppTheme {
        LibraryScreen(
            tracks = SampleData.sampleTracks,
            currentTrack = SampleData.sampleTracks.first(),
            activeFilter = LibraryFilter.ALL,
            onFilterSelect = {},
            onTrackSelect = {},
            onFavoriteToggle = {},
            onPlayAll = {},
            onShuffleAll = {}
        )
    }
}
