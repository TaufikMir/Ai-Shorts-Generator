package com.example.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class YouTubeVideoDetails(
    val videoId: String,
    val canonicalUrl: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val maxResThumbnailUrl: String,
    val durationSeconds: Int,
    val estimatedSizeMb: Float,
    val category: String = "YouTube Video",
    val description: String = ""
) {
    fun formatDuration(): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

data class YouTubePreset(
    val title: String,
    val channel: String,
    val videoId: String,
    val url: String,
    val durationSeconds: Int,
    val category: String,
    val description: String
)

object YouTubeImportHelper {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    val CURATED_PRESETS = listOf(
        YouTubePreset(
            title = "Sam Altman & Lex Fridman on GPT-5, Q*, and Future of AI",
            channel = "Lex Fridman",
            videoId = "jvqFAi7vkBc",
            url = "https://www.youtube.com/watch?v=jvqFAi7vkBc",
            durationSeconds = 7200,
            category = "AI / TECH PODCAST",
            description = "Deep conversation on OpenAI roadmap, superintelligence, compute scaling, and AI agents."
        ),
        YouTubePreset(
            title = "MKBHD: The Real Truth About AI Hardware & Gadgets",
            channel = "Marques Brownlee",
            videoId = "Yf-P_Wk7OQk",
            url = "https://www.youtube.com/watch?v=Yf-P_Wk7OQk",
            durationSeconds = 960,
            category = "TECH REVIEW",
            description = "Honest breakdown of handheld AI devices, prompt latency, battery efficiency, and software limits."
        ),
        YouTubePreset(
            title = "Alex Hormozi: How To Get High-Paying Clients Fast",
            channel = "Alex Hormozi",
            videoId = "6rZ1Hn36z04",
            url = "https://www.youtube.com/watch?v=6rZ1Hn36z04",
            durationSeconds = 1440,
            category = "BUSINESS & SALES",
            description = "High-leverage outreach strategies, irresistible offer mechanics, and closing high ticket deals."
        ),
        YouTubePreset(
            title = "Andrew Huberman: Master Focus & Optimize Dopamine Loops",
            channel = "Huberman Lab",
            videoId = "QmOF0crdyRU",
            url = "https://www.youtube.com/watch?v=QmOF0crdyRU",
            durationSeconds = 5400,
            category = "HEALTH & SCIENCE",
            description = "Neurobiology protocols for peak mental stamina, task initiation, and avoiding cognitive burnout."
        )
    )

    private val YOUTUBE_PATTERNS = listOf(
        // youtu.be/<id> with optional query, hash, or trailing slash
        Regex("""(?:https?://)?(?:[a-zA-Z0-9_-]+\.)?youtu\.be/([a-zA-Z0-9_-]{11})(?:[?#/].*)?"""),
        // youtube.com/watch?v=<id> (handles all subdomains, params before/after, trailing slash, hashes)
        Regex("""(?:https?://)?(?:[a-zA-Z0-9_-]+\.)?youtube\.com/watch/?\?(?:[^#\s]*&)?v=([a-zA-Z0-9_-]{11})(?:[&#/].*)?"""),
        // youtube.com/(shorts|live|embed|v)/<id>
        Regex("""(?:https?://)?(?:[a-zA-Z0-9_-]+\.)?youtube\.com/(?:shorts|live|embed|v)/([a-zA-Z0-9_-]{11})(?:[?#/].*)?"""),
        // General query param v=<id> across any youtube URL
        Regex("""[?&]v=([a-zA-Z0-9_-]{11})(?:[&#/\s]|$)"""),
        // Standalone 11-character video ID with optional quotes or angle brackets
        Regex("""^[<"'\(\[]?([a-zA-Z0-9_-]{11})[>"'\)\]]?$""")
    )

    /**
     * Extracts an 11-character YouTube video ID from arbitrary user input.
     * Returns null if no valid ID could be identified.
     */
    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
            .removePrefix("<").removeSuffix(">")
            .removePrefix("\"").removeSuffix("\"")
            .removePrefix("'").removeSuffix("'")
            .trim()
        if (trimmed.isBlank()) return null

        for (regex in YOUTUBE_PATTERNS) {
            val match = regex.find(trimmed)
            if (match != null && match.groupValues.size >= 2) {
                val candidate = match.groupValues[1]
                if (candidate.length == 11) {
                    return candidate
                }
            }
        }
        return null
    }

    /**
     * Checks if the user's input string matches any valid YouTube URL pattern.
     */
    fun isValidYouTubeUrl(input: String): Boolean {
        return extractVideoId(input) != null
    }

    /**
     * Converts any YouTube video ID to a standard canonical watch URL.
     */
    fun toCanonicalUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"

    /**
     * Cleans up raw user input or shared text into a canonical YouTube watch URL.
     */
    fun cleanUrlOrExtract(input: String): String {
        val id = extractVideoId(input)
        return if (id != null) toCanonicalUrl(id) else input.trim()
    }

    /**
     * Asynchronously resolves YouTube video metadata via public oEmbed API with graceful fallbacks.
     */
    suspend fun resolveVideoDetails(input: String): Result<YouTubeVideoDetails> = withContext(Dispatchers.IO) {
        val videoId = extractVideoId(input)
            ?: return@withContext Result.failure(
                IllegalArgumentException("Invalid YouTube URL. Please provide a standard YouTube video, Shorts, Live stream, or youtu.be link.")
            )

        val canonicalUrl = toCanonicalUrl(videoId)
        val thumbHq = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
        val thumbMax = "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"

        // Check if matching curated preset for richer default info
        val preset = CURATED_PRESETS.find { it.videoId == videoId }

        // Attempt live metadata fetch via YouTube's open oEmbed API
        try {
            val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val request = Request.Builder()
                .url(oembedUrl)
                .header("User-Agent", "AI-Shorts-Factory/1.0 (Android)")
                .build()

            val response = httpClient.newCall(request).execute()
            response.use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val title = json.optString("title", preset?.title ?: "YouTube Video: $videoId").trim()
                        val author = json.optString("author_name", preset?.channel ?: "YouTube Creator").trim()
                        val duration = preset?.durationSeconds ?: 1500 // ~25 minutes default estimate
                        val sizeMb = preset?.durationSeconds?.let { (it * 0.22f).coerceAtLeast(35f) } ?: 330.0f

                        return@withContext Result.success(
                            YouTubeVideoDetails(
                                videoId = videoId,
                                canonicalUrl = canonicalUrl,
                                title = if (title.isNotBlank()) title else (preset?.title ?: "YouTube Video: $videoId"),
                                channelTitle = if (author.isNotBlank()) author else (preset?.channel ?: "YouTube Creator"),
                                thumbnailUrl = thumbHq,
                                maxResThumbnailUrl = thumbMax,
                                durationSeconds = duration,
                                estimatedSizeMb = sizeMb,
                                category = preset?.category ?: "YOUTUBE IMPORT",
                                description = preset?.description ?: "Direct video stream imported from YouTube for viral vertical shorts extraction."
                            )
                        )
                    }
                } else if (resp.code == 404) {
                    return@withContext Result.failure(
                        IllegalArgumentException("YouTube video was not found (HTTP 404). Please verify that the link or video ID exists and the video is public.")
                    )
                }
            }
        } catch (_: Exception) {
            // Network fallback below (offline testing or timeout)
        }

        // Offline / fallback resolution
        val title = preset?.title ?: "YouTube Stream - ID: $videoId"
        val author = preset?.channel ?: "YouTube Channel"
        val duration = preset?.durationSeconds ?: 1800
        val sizeMb = (duration * 0.22f).coerceAtLeast(40f)

        Result.success(
            YouTubeVideoDetails(
                videoId = videoId,
                canonicalUrl = canonicalUrl,
                title = title,
                channelTitle = author,
                thumbnailUrl = thumbHq,
                maxResThumbnailUrl = thumbMax,
                durationSeconds = duration,
                estimatedSizeMb = sizeMb,
                category = preset?.category ?: "YOUTUBE IMPORT",
                description = preset?.description ?: "YouTube video stream imported directly for AI transcript and viral clip extraction."
            )
        )
    }
}
