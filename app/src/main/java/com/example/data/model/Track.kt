package com.example.data.model

enum class StorageStatus(val label: String) {
    SAVED_MUSIC_FOLDER("● IN MUSIC FOLDER"),
    PERSISTENT("● OFFLINE READY"),
    DEVICE_LOCAL("◐ DEVICE CACHED")
}

enum class TrackSource(val label: String) {
    YOUTUBE("YouTube"),
    DIRECT_URL("Direct URL"),
    LOCAL_IMPORT("Imported"),
    DEVICE_MUSIC("Device Music")
}

data class Track(
    val id: String,
    val title: String,
    val artist: String = "Open Grace Music",
    val album: String = "OGM Library",
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val format: String = "MP3",
    val filePath: String = "",
    val uriString: String = "",
    val source: TrackSource = TrackSource.YOUTUBE,
    val savedToDeviceMusic: Boolean = false,
    val storageStatus: StorageStatus = StorageStatus.PERSISTENT,
    val addedAt: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null,
    val bitrateKbps: Int = 192,
    val isFavorite: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "$minutes:${seconds.toString().padStart(2, '0')}"
        }

    val formattedSize: String
        get() {
            return when {
                sizeBytes >= 1_000_000 -> String.format("%.1f MB", sizeBytes / 1_000_000.0)
                sizeBytes >= 1_000 -> String.format("%d KB", sizeBytes / 1000)
                sizeBytes > 0 -> "$sizeBytes B"
                else -> "—"
            }
        }

    val initials: String
        get() {
            val words = title.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return if (words.isEmpty()) "OGM"
            else words.take(2).map { it.first().uppercaseChar() }.joinToString("")
        }
}
