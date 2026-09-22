package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        ShortEntity::class,
        CreditTransactionEntity::class,
        UserAccountEntity::class,
        ProcessingJobEntity::class,
        TranscriptEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun shortDao(): ShortDao
    abstract fun creditDao(): CreditDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun processingJobDao(): ProcessingJobDao
    abstract fun transcriptDao(): TranscriptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_shorts_factory.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val user = UserAccountEntity(
                id = 1,
                name = "Alex Vance",
                email = "alex.creator@shortsfactory.ai",
                avatarInitial = "A",
                planId = "creator",
                creditsUsed = 37,
                creditsTotal = 100,
                contentCategory = "PODCAST",
                publishPlatforms = "MULTIPLE",
                isOnboarded = true
            )
            db.userAccountDao().insertOrUpdate(user)

            // Initial credit transactions
            db.creditDao().insertTransaction(
                CreditTransactionEntity(
                    timestamp = System.currentTimeMillis() - 86400000L * 15,
                    amount = 100,
                    type = "MONTHLY_GRANT",
                    description = "Creator Plan Monthly Allowance Renewal",
                    balanceAfter = 100
                )
            )
            db.creditDao().insertTransaction(
                CreditTransactionEntity(
                    timestamp = System.currentTimeMillis() - 86400000L * 5,
                    amount = -20,
                    type = "RENDER_SHORT",
                    description = "Generated 20 vertical Shorts for 'The Future of AI Hardware' podcast",
                    balanceAfter = 80
                )
            )
            db.creditDao().insertTransaction(
                CreditTransactionEntity(
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    amount = -17,
                    type = "RENDER_SHORT",
                    description = "Generated 17 Shorts for 'How I Built a \$10M SaaS' interview",
                    balanceAfter = 63
                )
            )

            // Seed sample Project 1
            val p1Id = db.projectDao().insertProject(
                ProjectEntity(
                    id = 1,
                    title = "The Future of AI Hardware & Robotics",
                    sourceFileName = "ai_hardware_keynote_full.mp4",
                    sourceFileSizeMb = 482.4f,
                    sourceDurationSeconds = 1840,
                    status = "READY",
                    shortsCount = 3,
                    thumbnailColorHex = "#1E1B4B"
                )
            )

            // Seed sample Project 2
            val p2Id = db.projectDao().insertProject(
                ProjectEntity(
                    id = 2,
                    title = "How We Scaled From 0 to \$1M ARR Without Funding",
                    sourceFileName = "founder_interview_ep42.mp4",
                    sourceFileSizeMb = 720.0f,
                    sourceDurationSeconds = 2450,
                    status = "READY",
                    shortsCount = 2,
                    thumbnailColorHex = "#064E3B"
                )
            )

            // Seed Shorts for Project 1
            db.shortDao().insertShort(
                ShortEntity(
                    id = 1,
                    projectId = p1Id,
                    projectTitle = "The Future of AI Hardware & Robotics",
                    title = "The Brutal Truth About Next-Gen Chips",
                    startTimeSeconds = 142,
                    endTimeSeconds = 194,
                    durationSeconds = 52,
                    hook = "Nobody warned us about what happens when chips hit 1nm...",
                    selectionReason = "High surprise factor, controversial industry statement, strong hook",
                    internalAiScore = 94,
                    framingMode = "CENTER_SPEAKER",
                    captionStyle = "VIRAL_STYLE",
                    captionPosition = "BOTTOM",
                    captionFontSize = 24,
                    showCaptions = true,
                    highlightWords = true,
                    emojiEmphasis = true,
                    transcript = "Nobody warned us about what happens when chips hit 1 nanometer. Quantum tunneling kicks in, and traditional silicon physics completely breaks down! You can't just shrink transistors anymore without quantum errors multiplying by 10x.",
                    titleSuggestions = "Nobody warned me about 1nm chips!|The brutal truth about silicon physics|Why 1nm chips will break the internet|Silicon has hit a physical brick wall|The quantum trap nobody is talking about",
                    description = "Why 1nm silicon chips are facing impossible quantum limits and what robotics engineers are doing next. Watch till the end!",
                    youtubeHashtags = "#AIHardware #TechNews #Silicon #Engineering #Shorts",
                    searchKeywords = "silicon chips, AI hardware, quantum tunneling, transistors, nvidia, future tech",
                    pinnedComment = "Which approach wins: Optical computing or 3D stacking? Drop your take below! 👇",
                    isFavorite = true,
                    status = "READY"
                )
            )

            db.shortDao().insertShort(
                ShortEntity(
                    id = 2,
                    projectId = p1Id,
                    projectTitle = "The Future of AI Hardware & Robotics",
                    title = "Why Humanoid Robots Are Already Here",
                    startTimeSeconds = 520,
                    endTimeSeconds = 565,
                    durationSeconds = 45,
                    hook = "Stop thinking humanoid robots are 10 years away.",
                    selectionReason = "Story payoff, high emotional reaction, clear demonstration",
                    internalAiScore = 88,
                    framingMode = "SMART_CROP",
                    captionStyle = "BOLD",
                    captionPosition = "CENTER",
                    captionFontSize = 26,
                    showCaptions = true,
                    highlightWords = true,
                    emojiEmphasis = true,
                    transcript = "Stop thinking humanoid robots are ten years away. When you look at actuators and battery density today, the limiting factor isn't hardware anymore. It's real-time multimodal spatial intelligence.",
                    titleSuggestions = "Robots aren't 10 years away—they're here|The secret reason humanoid robots are delayed|Hardware is solved. Here's what isn't.|Why robot brains are harder than robot bodies|The real timeline for domestic humanoid robots",
                    description = "The honest state of humanoid robotics in 2026. Hardware is ready, spatial models are catching up fast.",
                    youtubeHashtags = "#Robotics #Humanoids #TechTrends #ArtificialIntelligence #Reels",
                    searchKeywords = "humanoid robots, robotics actuators, AI spatial models, future tech",
                    pinnedComment = "Would you trust a humanoid robot in your kitchen? Yes or No? 🤖",
                    isFavorite = false,
                    status = "EXPORTED",
                    exportedPath = "exports/Short_Robots_Here_1080p.mp4"
                )
            )

            // Seed Short for Project 2
            db.shortDao().insertShort(
                ShortEntity(
                    id = 3,
                    projectId = p2Id,
                    projectTitle = "How We Scaled From 0 to \$1M ARR Without Funding",
                    title = "The One Mistake That Almost Killed My Startup",
                    startTimeSeconds = 88,
                    endTimeSeconds = 136,
                    durationSeconds = 48,
                    hook = "I made one huge mistake in month 3 that almost cost us everything.",
                    selectionReason = "High curiosity gap, vulnerable founder lesson, practical takeaway",
                    internalAiScore = 96,
                    framingMode = "BLURRED_BG",
                    captionStyle = "PODCAST",
                    captionPosition = "BOTTOM",
                    captionFontSize = 22,
                    showCaptions = true,
                    highlightWords = true,
                    emojiEmphasis = false,
                    transcript = "I made one huge mistake in month three that almost cost us everything. We built 14 features our users never asked for before charging our first dollar. Never build in the dark without customer pre-orders.",
                    titleSuggestions = "I made one huge mistake in my startup|Never build features before this step|How I lost \$50k building what nobody wanted|The #1 rule of bootstrapping a SaaS|Why 90% of founders fail before first revenue",
                    description = "The single biggest bootstrap mistake and how we reversed it to reach \$1M ARR. Real founder advice.",
                    youtubeHashtags = "#Startups #Entrepreneurship #SaaS #BusinessTips #TikTok",
                    searchKeywords = "bootstrap startup, founder advice, saas growth, product market fit",
                    pinnedComment = "What's the hardest lesson you learned in business? Let's discuss in the replies!",
                    isFavorite = true,
                    status = "READY"
                )
            )

            // Seed initial full transcripts generated by background worker
            val p1Segments = listOf(
                com.example.data.model.TimestampedSegment(
                    id = 1,
                    startTimeSeconds = 0,
                    endTimeSeconds = 35,
                    speaker = "Host",
                    text = "Welcome to the Keynote. Today we're diving straight into the silicon and hardware bottlenecks of modern machine learning models.",
                    confidence = 0.99f
                ),
                com.example.data.model.TimestampedSegment(
                    id = 2,
                    startTimeSeconds = 36,
                    endTimeSeconds = 85,
                    speaker = "Guest",
                    text = "If you think AI chips are moving fast right now, wait until you see what happens next year. The thermal architecture has fundamentally hit a wall.",
                    confidence = 0.98f
                ),
                com.example.data.model.TimestampedSegment(
                    id = 3,
                    startTimeSeconds = 86,
                    endTimeSeconds = 142,
                    speaker = "Host",
                    text = "Everyone is obsessed with GPUs, but nobody is discussing the interconnect memory bandwidth that actually limits token generation throughput.",
                    confidence = 0.99f
                )
            )

            db.transcriptDao().insertTranscript(
                TranscriptEntity(
                    id = 1,
                    projectId = p1Id,
                    jobId = 1,
                    fullText = p1Segments.joinToString(" ") { it.text },
                    language = "en-US",
                    durationSeconds = 1840,
                    wordCount = 380,
                    confidenceScore = 0.988f,
                    speechRateWpm = 152,
                    segmentsJson = com.example.data.model.TimestampedSegment.toJsonString(p1Segments),
                    summary = "In-depth keynote addressing AI hardware bottlenecks, thermal constraints, and memory bandwidth scaling for multi-billion parameter networks.",
                    keyTopics = "Semiconductors, AI Hardware, Memory Bandwidth, Thermal Limits"
                )
            )

            db.processingJobDao().insertJob(
                ProcessingJobEntity(
                    id = 1,
                    projectId = p1Id,
                    jobType = "TRANSCRIBE_AUDIO",
                    status = "COMPLETED",
                    progress = 100,
                    createdAt = System.currentTimeMillis() - 86400000L * 5,
                    updatedAt = System.currentTimeMillis() - 86400000L * 5 + 12000L
                )
            )
        }
    }
}
