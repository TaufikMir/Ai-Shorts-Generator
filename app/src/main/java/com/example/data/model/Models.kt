package com.example.data.model

enum class CaptionStyle(val displayName: String, val description: String) {
    CLEAN("Clean", "Minimalist white text with subtle shadow"),
    BOLD("Bold", "High contrast uppercase yellow on dark box"),
    PODCAST("Podcast", "Warm gradient with highlighted active word"),
    MINIMAL("Minimal", "Clean sans-serif lower-third subtitles"),
    GAMING("Gaming", "Cyberpunk neon cyan & magenta border"),
    VIRAL_STYLE("Viral-Style", "Bouncing word-by-word with emoji bursts")
}

enum class CaptionPosition(val displayName: String) {
    TOP("Top (Header)"),
    CENTER("Center (Focal)"),
    BOTTOM("Bottom (Standard)")
}

enum class VideoFramingMode(val displayName: String, val description: String) {
    CENTER_SPEAKER("Center Speaker", "Smart face & subject tracking centered"),
    SMART_CROP("Smart Crop 9:16", "Dynamic focal crop maintaining key action"),
    BLURRED_BG("Blurred Backdrop", "Original aspect ratio with 9:16 blurred background")
}

enum class ProcessingStage(val stepNumber: Int, val label: String, val detail: String) {
    IDLE(0, "Ready", "Awaiting video upload"),
    UPLOADING(1, "Uploading", "Validating and transferring video to ingest pipeline..."),
    TRANSCRIBING(2, "Transcribing", "Speech-to-text generating high accuracy transcript..."),
    FINDING_MOMENTS(3, "Finding moments", "Analyzing hooks, surprise, humor, and story payoffs..."),
    CREATING_CLIPS(4, "Creating clips", "Applying smart framing, speaker centering, and crop..."),
    ADDING_CAPTIONS(5, "Adding captions", "Syncing word timestamps, styles, and emoji accents..."),
    READY(6, "Ready", "Shorts generated successfully!"),
    FAILED(-1, "Failed", "Processing encountered an issue")
}

enum class PricingPlan(
    val id: String,
    val priceInr: Int,
    val monthlyShorts: Int,
    val features: List<String>,
    val isPopular: Boolean = false
) {
    FREE(
        id = "free",
        priceInr = 0,
        monthlyShorts = 3,
        features = listOf(
            "3 Shorts / month",
            "Basic captions",
            "Standard export (720p)",
            "AI titles",
            "Single project"
        )
    ),
    STARTER(
        id = "starter",
        priceInr = 299,
        monthlyShorts = 30,
        features = listOf(
            "30 Shorts / month",
            "Premium captions",
            "HD export (1080p)",
            "AI hooks (3 options)",
            "SEO generator",
            "Priority queue"
        )
    ),
    CREATOR(
        id = "creator",
        priceInr = 799,
        monthlyShorts = 100,
        features = listOf(
            "100 Shorts / month",
            "Advanced animated captions",
            "HD 1080x1920 60fps export",
            "AI hooks & viral scores",
            "SEO generator & hashtags",
            "Thumbnail tools & frame grabber",
            "Custom fonts & branding"
        ),
        isPopular = true
    ),
    PRO(
        id = "pro",
        priceInr = 1499,
        monthlyShorts = 250,
        features = listOf(
            "250 Shorts / month",
            "Priority processing queue",
            "Advanced customization & presets",
            "Thumbnail generation suite",
            "Unlimited active projects",
            "Fast cloud rendering",
            "Dedicated support"
        )
    ),
    AGENCY(
        id = "agency",
        priceInr = 3999,
        monthlyShorts = 1000,
        features = listOf(
            "1,000 Shorts / month",
            "Multiple workspaces & teams",
            "Multi-account publishing",
            "Custom branding & watermarks",
            "Highest priority processing",
            "Dedicated account manager",
            "REST API access"
        )
    )
}

enum class ContentCategory(val label: String, val iconName: String) {
    PODCAST("Podcast", "Mic"),
    YOUTUBE("YouTube / Vlogs", "Video"),
    GAMING("Gaming / Streaming", "Gamepad"),
    EDUCATION("Education / Tech", "School"),
    BUSINESS("Business / SaaS", "TrendingUp"),
    COMMENTARY("Commentary / News", "Chat"),
    COMEDY("Comedy / Entertainment", "Laugh"),
    OTHER("Other Creator", "Star")
}

enum class PublishPlatform(val label: String) {
    YOUTUBE_SHORTS("YouTube Shorts"),
    INSTAGRAM_REELS("Instagram Reels"),
    TIKTOK("TikTok"),
    MULTIPLE("All Platforms")
}

data class CaptionWord(
    val word: String,
    val startMs: Long,
    val endMs: Long,
    val isHighlighted: Boolean = false,
    val emoji: String? = null
)

data class SuggestedHook(
    val id: String,
    val hookText: String,
    val category: String, // Curiosity, Mistake, Revelation, Direct
    val engagementNote: String
)

data class SeoMetadata(
    val titleSuggestions: List<String>,
    val selectedTitle: String,
    val description: String,
    val youtubeHashtags: List<String>,
    val searchKeywords: List<String>,
    val pinnedComment: String
)
