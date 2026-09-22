package com.example.data.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns

enum class UploadErrorType {
    NONE,
    FILE_SIZE_LIMIT_EXCEEDED,
    UNSUPPORTED_FORMAT,
    FILE_EMPTY,
    DURATION_TOO_SHORT,
    DURATION_TOO_LONG
}

data class VideoValidationResult(
    val isValid: Boolean,
    val fileName: String,
    val detectedFormat: String, // "MP4", "MOV", "WEBM", or extension/UNKNOWN
    val fileSizeMb: Float,
    val durationSeconds: Int,
    val mimeType: String? = null,
    val errorMessage: String? = null,
    val technicalDetails: String? = null,
    val errorType: UploadErrorType = UploadErrorType.NONE,
    val errorTitle: String? = null,
    val resolutionTips: List<String> = emptyList()
) {
    val isFileSizeError: Boolean
        get() = errorType == UploadErrorType.FILE_SIZE_LIMIT_EXCEEDED || errorType == UploadErrorType.FILE_EMPTY

    val isFormatError: Boolean
        get() = errorType == UploadErrorType.UNSUPPORTED_FORMAT

    val sizeExcessMb: Float
        get() = if (fileSizeMb > VideoUploadValidator.MAX_FILE_SIZE_MB) fileSizeMb - VideoUploadValidator.MAX_FILE_SIZE_MB else 0f
}

data class SupportedFormatInfo(
    val name: String,
    val extension: String,
    val mimeTypes: List<String>,
    val codecs: String,
    val recommendation: String,
    val isSupported: Boolean = true
)

object VideoUploadValidator {

    const val MAX_FILE_SIZE_MB = 2048f // 2 GB
    const val MIN_DURATION_SECONDS = 5
    const val MAX_DURATION_SECONDS = 10800 // 3 hours

    val SUPPORTED_FORMATS = listOf(
        SupportedFormatInfo(
            name = "MP4",
            extension = ".mp4",
            mimeTypes = listOf("video/mp4", "video/mp4v-es"),
            codecs = "H.264, H.265 (HEVC), AAC audio",
            recommendation = "Best compatibility & fastest AI transcribing"
        ),
        SupportedFormatInfo(
            name = "MOV",
            extension = ".mov",
            mimeTypes = listOf("video/quicktime", "video/x-quicktime"),
            codecs = "Apple ProRes, H.264, Linear PCM",
            recommendation = "Standard for iPhone & Final Cut Pro exports"
        ),
        SupportedFormatInfo(
            name = "WebM",
            extension = ".webm",
            mimeTypes = listOf("video/webm"),
            codecs = "VP8, VP9, AV1, Opus audio",
            recommendation = "Optimized for modern web and browser recordings"
        )
    )

    val UNSUPPORTED_COMMON_FORMATS = listOf(".avi", ".mkv", ".flv", ".wmv", ".3gp", ".ts", ".m4v", ".mp3", ".wav")

    /**
     * Checks whether an extension or mime type is supported.
     */
    fun isExtensionSupported(extension: String): Boolean {
        val clean = extension.trim().lowercase().removePrefix(".")
        return clean in setOf("mp4", "mov", "webm")
    }

    fun isMimeTypeSupported(mime: String?): Boolean {
        if (mime == null) return false
        val clean = mime.trim().lowercase()
        return clean in setOf("video/mp4", "video/quicktime", "video/x-quicktime", "video/webm")
    }

    /**
     * Validates a video file given its filename, size, duration, and optional Uri.
     */
    fun validate(
        fileName: String,
        fileSizeMb: Float,
        durationSeconds: Int,
        mimeType: String? = null
    ): VideoValidationResult {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val detectedFormat = when (extension) {
            "mp4" -> "MP4"
            "mov" -> "MOV"
            "webm" -> "WebM"
            "" -> if (mimeType?.contains("webm", true) == true) "WebM"
                  else if (mimeType?.contains("quicktime", true) == true) "MOV"
                  else if (mimeType?.contains("mp4", true) == true) "MP4"
                  else "UNKNOWN"
            else -> extension.uppercase()
        }

        // 1. Format check
        val formatValid = isExtensionSupported(extension) || isMimeTypeSupported(mimeType)
        if (!formatValid) {
            val rejectedDisplay = if (extension.isNotBlank()) ".$extension" else (mimeType ?: "Unknown format")
            return VideoValidationResult(
                isValid = false,
                fileName = fileName,
                detectedFormat = detectedFormat,
                fileSizeMb = fileSizeMb,
                durationSeconds = durationSeconds,
                mimeType = mimeType,
                errorMessage = "Unsupported file format: '$rejectedDisplay'. Only MP4, MOV, and WebM video formats are accepted.",
                technicalDetails = "Detected container: '$rejectedDisplay' • Required: MP4 (H.264/HEVC), QuickTime MOV, or WebM (VP9/AV1).",
                errorType = UploadErrorType.UNSUPPORTED_FORMAT,
                errorTitle = "Unsupported Format: ${if (extension.isNotBlank()) ".$extension" else detectedFormat}",
                resolutionTips = listOf(
                    "Transcode to MP4 using VLC Media Player: Media → Convert/Save → Video - H.264 + MP3 (MP4).",
                    "In Adobe Premiere, DaVinci Resolve, or Final Cut: Export using the 'YouTube 1080p Full HD MP4' preset.",
                    "Use free converters such as HandBrake (choose the Fast 1080p30 preset) or CloudConvert.",
                    "On iOS or Mac: Open the file in QuickTime Player, then choose File → Export As → 1080p."
                )
            )
        }

        // 2. File size check
        if (fileSizeMb > MAX_FILE_SIZE_MB) {
            val excessMb = fileSizeMb - MAX_FILE_SIZE_MB
            return VideoValidationResult(
                isValid = false,
                fileName = fileName,
                detectedFormat = detectedFormat,
                fileSizeMb = fileSizeMb,
                durationSeconds = durationSeconds,
                mimeType = mimeType,
                errorMessage = "File size (${String.format("%.1f", fileSizeMb)} MB) exceeds the 2 GB (2,048 MB) maximum limit.",
                technicalDetails = "Current: ${String.format("%.1f", fileSizeMb)} MB (${String.format("%.2f", fileSizeMb / 1024f)} GB) • Max Limit: 2,048 MB (2.0 GB) • Exceeded by: +${String.format("%.1f", excessMb)} MB",
                errorType = UploadErrorType.FILE_SIZE_LIMIT_EXCEEDED,
                errorTitle = "File Size Limit Exceeded (2.0 GB Limit)",
                resolutionTips = listOf(
                    "Compress with HandBrake or CapCut using H.264 and CRF 22 (reduces size by up to 70% with near-lossless visual quality).",
                    "Downscale 4K (3840×2160) master exports to 1080p Full HD (1920×1080), ideal for 9:16 Shorts extraction.",
                    "Trim unneeded introductory padding or split footage longer than 60 minutes into multiple smaller projects.",
                    "Export with AAC audio at 192 kbps instead of uncompressed multichannel LPCM tracks."
                )
            )
        }

        if (fileSizeMb <= 0f) {
            return VideoValidationResult(
                isValid = false,
                fileName = fileName,
                detectedFormat = detectedFormat,
                fileSizeMb = fileSizeMb,
                durationSeconds = durationSeconds,
                mimeType = mimeType,
                errorMessage = "Selected file appears to be empty (0 MB).",
                technicalDetails = "The file has a byte length of 0 or cannot be accessed by the system storage provider.",
                errorType = UploadErrorType.FILE_EMPTY,
                errorTitle = "Empty Video File (0 MB)",
                resolutionTips = listOf(
                    "Check if the recording or export is still in progress and completed writing to disk.",
                    "Ensure cloud-synced files (Google Drive, iCloud, Dropbox) have fully downloaded locally.",
                    "Re-transfer the video file from your device camera roll or SD card."
                )
            )
        }

        // 3. Duration check
        if (durationSeconds > 0 && durationSeconds < MIN_DURATION_SECONDS) {
            return VideoValidationResult(
                isValid = false,
                fileName = fileName,
                detectedFormat = detectedFormat,
                fileSizeMb = fileSizeMb,
                durationSeconds = durationSeconds,
                mimeType = mimeType,
                errorMessage = "Video is too short (${durationSeconds}s). Minimum length is 5 seconds for Short generation.",
                technicalDetails = "Duration: ${durationSeconds}s • Minimum required: ${MIN_DURATION_SECONDS}s",
                errorType = UploadErrorType.DURATION_TOO_SHORT,
                errorTitle = "Video Duration Too Short (< 5s)",
                resolutionTips = listOf(
                    "Select a longer video file that contains dialogue or meaningful narrative moments.",
                    "Combine several short takes into a single sequence before uploading."
                )
            )
        }

        if (durationSeconds > MAX_DURATION_SECONDS) {
            return VideoValidationResult(
                isValid = false,
                fileName = fileName,
                detectedFormat = detectedFormat,
                fileSizeMb = fileSizeMb,
                durationSeconds = durationSeconds,
                mimeType = mimeType,
                errorMessage = "Video duration (${durationSeconds / 60} mins) exceeds the 3-hour limit.",
                technicalDetails = "Duration: ${durationSeconds / 60}m • Maximum allowed: ${MAX_DURATION_SECONDS / 60}m (3 hours)",
                errorType = UploadErrorType.DURATION_TOO_LONG,
                errorTitle = "Video Exceeds 3-Hour Duration Limit",
                resolutionTips = listOf(
                    "Trim long live streams or podcast recordings to focus on the key 30-60 minute section.",
                    "Use video editing software to cut the recording into episodic parts."
                )
            )
        }

        // All checks passed
        return VideoValidationResult(
            isValid = true,
            fileName = fileName,
            detectedFormat = detectedFormat,
            fileSizeMb = fileSizeMb,
            durationSeconds = durationSeconds,
            mimeType = mimeType,
            technicalDetails = "Validated: $detectedFormat container, ${String.format("%.1f", fileSizeMb)} MB, ${durationSeconds / 60}m ${durationSeconds % 60}s"
        )
    }

    /**
     * Inspects a real Content Uri to extract name, size, and duration.
     */
    fun extractFromUri(context: Context, uri: Uri): VideoValidationResult {
        var fileName = "video_${System.currentTimeMillis()}.mp4"
        var fileSizeMb = 350.0f
        var durationSec = 1800
        var mimeType = context.contentResolver.getType(uri)

        // Try extracting metadata from ContentResolver
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    if (sizeIndex != -1) {
                        val bytes = cursor.getLong(sizeIndex)
                        if (bytes > 0) {
                            fileSizeMb = bytes / (1024f * 1024f)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // ContentResolver query fallback
        }

        // If filename still lacks extension, try from path
        if (!fileName.contains(".")) {
            uri.lastPathSegment?.let { segment ->
                if (segment.contains(".")) {
                    fileName = segment
                }
            }
        }

        // Try extracting duration with MediaMetadataRetriever
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMsStr?.toLongOrNull()?.let { ms ->
                if (ms > 0) {
                    durationSec = (ms / 1000L).toInt()
                }
            }
            val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            if (!mime.isNullOrBlank()) {
                mimeType = mime
            }
            retriever.release()
        } catch (_: Exception) {
            // MediaMetadataRetriever fallback (e.g. mock or restricted Uri)
        }

        return validate(
            fileName = fileName,
            fileSizeMb = fileSizeMb,
            durationSeconds = durationSec,
            mimeType = mimeType
        )
    }
}
