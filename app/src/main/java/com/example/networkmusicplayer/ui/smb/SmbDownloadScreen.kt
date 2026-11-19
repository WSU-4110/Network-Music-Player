package com.example.networkmusicplayer.ui.smb

import com.example.networkmusicplayer.model.SmbConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun SmbDownloadScreen(
    viewModel: SmbViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var host by remember { mutableStateOf("192.168.1.100") }
    var shareName by remember { mutableStateOf("Public") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val currentConfig = SmbConfig(
        host = host,
        shareName = shareName,
        isGuest = true
        //username = username,
        // password = password
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("SMB Server Connection", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("Server IP / Host") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = shareName, onValueChange = { shareName = it }, label = { Text("Share Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.connectAndFetchFiles(currentConfig) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect & List Files")
        }

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }

        uiState.statusMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.files) { fileName ->
                ListItem(
                    headlineContent = { Text(fileName) },
                    supportingContent = { Text("Tap to download") },
                    modifier = Modifier.clickable {
                        // Saves directly to app-specific external downloads folder
                        val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                        viewModel.downloadFile(currentConfig, fileName, downloadDir)

                    }
                )
            }
        }
    }
}