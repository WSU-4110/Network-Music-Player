package com.example.networkmusicplayer.model

data class SmbConfig(
    val host: String = "",
    val port: Int = 445,
    val shareName: String = "",
    val username: String = "",
    val password: String = "",
    val domain: String = "",
    val isGuest: Boolean = true
) {
    /**
     * Checks that the minimum required fields to open an SMB connection are filled.
     */
    fun isValid(): Boolean {
        val hasServerInfo = host.isNotBlank() && shareName.isNotBlank() && port in 1..65535
        val hasCredentials = isGuest || username.isNotBlank()
        return hasServerInfo && hasCredentials
    }

    /**
     * Strips accidental leading/trailing slashes from share names (e.g., "/Public/" -> "Public").
     */
    fun sanitizedShareName(): String = shareName.trim().trim('/', '\\')
}