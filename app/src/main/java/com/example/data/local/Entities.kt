package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sourceFileName: String,
    val sourceFileSizeMb: Float,
    val sourceDurationSeconds: Int,
    val status: String, // PROCESSING, READY, FAILED
    val shortsCount: Int = 0,
    val thumbnailColorHex: String = "#1E293B",
    val videoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shorts")
data class ShortEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val projectTitle: String,
    val title: String,
    val startTimeSeconds: Int,
    val endTimeSeconds: Int,
    val durationSeconds: Int,
    val hook: String,
    val selectionReason: String,
    val internalAiScore: Int, // 0 - 100 internal score
    // Framing & Composition
    val framingMode: String = "CENTER_SPEAKER",
    // Caption settings
    val captionStyle: String = "VIRAL_STYLE",
    val captionPosition: String = "BOTTOM",
    val captionFontSize: Int = 24,
    val showCaptions: Boolean = true,
    val highlightWords: Boolean = true,
    val emojiEmphasis: Boolean = true,
    val transcript: String,
    // SEO & Titles
    val titleSuggestions: String, // Pipe-separated or json string
    val description: String,
    val youtubeHashtags: String,
    val searchKeywords: String,
    val pinnedComment: String,
    // Thumbnail & Output
    val thumbnailFrameSecond: Int = 2,
    val thumbnailOverlayText: String = "",
    val isFavorite: Boolean = false,
    val status: String = "READY", // PROCESSING, READY, EXPORTED, FAILED
    val exportedPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "credit_transactions")
data class CreditTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Int, // e.g. -1 for render, +100 for plan renewal
    val type: String, // RENDER_SHORT, MONTHLY_GRANT, PURCHASE, REFUND
    val description: String,
    val balanceAfter: Int
)

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey val id: Long = 1,
    val name: String,
    val email: String,
    val avatarInitial: String = "C",
    val planId: String = "creator",
    val creditsUsed: Int = 37,
    val creditsTotal: Int = 100,
    val contentCategory: String = "PODCAST",
    val publishPlatforms: String = "MULTIPLE",
    val isOnboarded: Boolean = true,
    val customApiKey: String = ""
)

@Entity(tableName = "processing_jobs")
data class ProcessingJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val shortId: Long? = null,
    val jobType: String, // ANALYZE_VIDEO, RENDER_SHORT, GENERATE_SEO, TRANSCRIBE_AUDIO
    val status: String, // PENDING, PROCESSING, COMPLETED, FAILED
    val progress: Int = 0, // 0 - 100
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transcripts")
data class TranscriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val jobId: Long? = null,
    val fullText: String,
    val language: String = "en-US",
    val durationSeconds: Int,
    val wordCount: Int,
    val confidenceScore: Float = 0.98f, // 0.0 to 1.0
    val speechRateWpm: Int = 155, // words per minute
    val segmentsJson: String, // JSON array of TimestampedSegment
    val summary: String,
    val keyTopics: String, // comma-separated keywords
    val createdAt: Long = System.currentTimeMillis()
)

