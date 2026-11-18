package com.example.networkmusicplayer.ui.smb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkmusicplayer.model.SmbConfig
import com.example.networkmusicplayer.data.repository.ReadOnlySmbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class SmbUiState(
    val isLoading: Boolean = false,
    val files: List<String> = emptyList(),
    val statusMessage: String? = null
)

class SmbViewModel(
    private val repository: ReadOnlySmbRepository = ReadOnlySmbRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmbUiState())
    val uiState: StateFlow<SmbUiState> = _uiState.asStateFlow()

    fun connectAndFetchFiles(config: SmbConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            repository.listFiles(config)
                .onSuccess { fileList ->
                    _uiState.update { it.copy(isLoading = false, files = fileList) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, statusMessage = "Connection failed: ${error.message}")
                    }
                }
        }
    }

    fun downloadFile(config: SmbConfig, fileName: String, destinationDir: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Downloading $fileName...") }
            val targetFile = File(destinationDir, fileName)

            repository.downloadFile(config, fileName, targetFile)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, statusMessage = "Downloaded to ${targetFile.absolutePath}")
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, statusMessage = "Download error: ${error.message}")
                    }
                }
        }
    }
}