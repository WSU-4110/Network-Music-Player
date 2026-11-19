package com.example.networkmusicplayer.filemanagement

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import com.example.networkmusicplayer.model.SmbConfig
import com.example.networkmusicplayer.filemanagement.MusicLibraryScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.EnumSet

data class SmbConfig(
    val host: String,
    val shareName: String,
    val username: String,
    val password: String,
    val domain: String = ""
)


class ReadOnlySmbRepository {

    private fun buildAuthContext(config: SmbConfig): AuthenticationContext {
        return if (config.isGuest) {
            AuthenticationContext.guest()
        } else {
            AuthenticationContext(
                config.username.trim(),
                config.password.toCharArray(),
                config.domain.trim()
            )
        }
    }

    suspend fun listFiles(config: SmbConfig, folderPath: String = ""): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(config.isValid()) { "Invalid SMB server configuration." }

                SMBClient().use { client ->
                    client.connect(config.host.trim(), config.port).use { connection ->
                        val session = connection.authenticate(buildAuthContext(config))
                        val share = session.connectShare(config.sanitizedShareName()) as DiskShare

                        share.list(folderPath)
                            .map { it.fileName }
                            .filter { it != "." && it != ".." }
                    }
                }
            }
        }

    suspend fun downloadFile(
        config: SmbConfig,
        remoteFilePath: String,
        localDestination: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(config.isValid()) { "Invalid SMB server configuration." }

            SMBClient().use { client ->
                client.connect(config.host.trim(), config.port).use { connection ->
                    val session = connection.authenticate(buildAuthContext(config))
                    val share = session.connectShare(config.sanitizedShareName()) as DiskShare

                    val remoteFile = share.openFile(
                        remoteFilePath,
                        EnumSet.of(AccessMask.GENERIC_READ),
                        null,
                        SMB2ShareAccess.ALL,
                        SMB2CreateDisposition.FILE_OPEN,
                        null
                    )

                    remoteFile.use { smbFile ->
                        smbFile.inputStream.use { input ->
                            FileOutputStream(localDestination).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    localDestination
                }
            }
        }
    }
}