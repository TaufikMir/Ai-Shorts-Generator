package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ShortEntity
import com.example.data.model.VideoFramingMode
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportScreen(
    short: ShortEntity,
    onBack: () -> Unit,
    onSaveShort: (ShortEntity) -> Unit,
    onExportRender: suspend (Long, (Int) -> Unit) -> String,
    onOpenStudio: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    // Local mutable copy state
    var selectedTitle by remember { mutableStateOf(short.title) }
    var descriptionText by remember { mutableStateOf(short.description) }
    var hashtagsText by remember {
        mutableStateOf(
            if (short.youtubeHashtags.isNotBlank()) short.youtubeHashtags
            else "#shorts #podcast #tech #viral #shortsfeed #creators"
        )
    }
    var pinnedCommentText by remember {
        mutableStateOf(
            if (short.pinnedComment.isNotBlank()) short.pinnedComment
            else "What do you think about this take? Comment below! 👇"
        )
    }

    // Export progress state
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableIntStateOf(0) }
    var currentRenderStep by remember { mutableStateOf("") }
    var exportedFilePath by remember { mutableStateOf(short.exportedPath) }
    var isDownloadedLocally by remember { mutableStateOf(short.exportedPath != null) }

    // Quick-copy feedback flags for buttons
    var copiedKitRecently by remember { mutableStateOf(false) }
    var copiedTitleRecently by remember { mutableStateOf(false) }
    var copiedDescriptionRecently by remember { mutableStateOf(false) }
    var copiedHashtagsRecently by remember { mutableStateOf(false) }
    var copiedPinnedRecently by remember { mutableStateOf(false) }

    // Suggested alternative titles
    val titleList = remember(short.titleSuggestions, selectedTitle) {
        val parsed = short.titleSuggestions.split("|").filter { it.isNotBlank() }
        if (parsed.isNotEmpty()) parsed else listOf(
            short.title,
            "Mind-Blowing Truth About ${short.projectTitle} 🤯",
            "This Changes Everything: ${short.hook}",
            "Why Nobody Talks About This... #Shorts"
        )
    }

    // Estimated file size based on duration (4.5 Mbps H.264 1080p60 + 192k audio)
    val estimatedSizeMb = remember(short.durationSeconds) {
        String.format(Locale.US, "%.1f", (short.durationSeconds * 0.58f).coerceAtLeast(8.5f))
    }

    fun copyToClipboard(text: String, label: String) {
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareShortMetadata(context: Context, title: String, description: String, hashtags: String) {
        val shareText = "$title\n\n$description\n\n$hashtags"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Short Content Kit")
        context.startActivity(shareIntent)
    }

    fun triggerExport() {
        coroutineScope.launch {
            isExporting = true
            exportProgress = 0
            currentRenderStep = "Configuring 1080x1920 9:16 FFmpeg pipeline..."

            try {
                val path = onExportRender(short.id) { progress ->
                    exportProgress = progress
                    currentRenderStep = when {
                        progress < 25 -> "Analyzing source video & calculating center crop..."
                        progress < 55 -> "Cropping to 9:16 vertical & Lanczos upscaling..."
                        progress < 80 -> "Burning in word-synced animated subtitles..."
                        progress < 95 -> "Encoding H.264 High Profile 60fps MP4..."
                        else -> "Finalizing MP4 container with faststart flags..."
                    }
                }
                exportedFilePath = path
                isDownloadedLocally = true
                currentRenderStep = "Render complete! Ready for download."

                // Persist exported status to database
                val updated = short.copy(
                    title = selectedTitle,
                    description = descriptionText,
                    youtubeHashtags = hashtagsText,
                    pinnedComment = pinnedCommentText,
                    status = "EXPORTED",
                    exportedPath = path
                )
                onSaveShort(updated)
                Toast.makeText(context, "1080x1920 MP4 rendered successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.localizedMessage ?: "Unknown"}", Toast.LENGTH_LONG).show()
            } finally {
                isExporting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("export_screen")
    ) {
        // TOP APP BAR
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp).testTag("export_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Export & Content Kit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (exportedFilePath != null) Color(0x3310B981) else Color(0x3306B6D4)
                        ) {
                            Text(
                                text = if (exportedFilePath != null) "EXPORTED" else "1080x1920 MP4",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (exportedFilePath != null) GreenSuccess else CyanGlow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = "From project: ${short.projectTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (onOpenStudio != null) {
                    OutlinedButton(
                        onClick = onOpenStudio,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanGlow),
                        modifier = Modifier.height(34.dp).testTag("open_studio_button"),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. RENDER & DOWNLOAD CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, if (exportedFilePath != null) GreenSuccess.copy(alpha = 0.5f) else BorderDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(CyanGlow, PurpleGlow))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "1080x1920 MP4 Video",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Duration: ${short.durationSeconds}s • Approx $estimatedSizeMb MB • 60 FPS",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (exportedFilePath != null) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Ready",
                                    tint = GreenSuccess,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Technical Specifications Grid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Resolution", color = TextSecondary, fontSize = 11.sp)
                                    Text("1080 × 1920 (9:16 Vertical)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Video Codec", color = TextSecondary, fontSize = 11.sp)
                                    Text("H.264 High Profile (libx264, CRF 21)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Framing Strategy", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        try { VideoFramingMode.valueOf(short.framingMode).displayName } catch (e: Exception) { "Center Speaker Crop" },
                                        color = CyanGlow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Audio Track", color = TextSecondary, fontSize = 11.sp)
                                    Text("AAC 192kbps 48kHz Stereo", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtitles", color = TextSecondary, fontSize = 11.sp)
                                    Text("Burned-in ${short.captionStyle}", color = PinkAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        // RENDER PROGRESS INDICATOR
                        if (isExporting) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E2640),
                                border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Rendering Vertical Short...",
                                            fontWeight = FontWeight.Bold,
                                            color = CyanGlow,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "$exportProgress%",
                                            fontWeight = FontWeight.Black,
                                            color = CyanGlow,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { exportProgress / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = CyanGlow,
                                        trackColor = Color(0xFF131B2E)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentRenderStep,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // EXPORT SUCCESS FILE INFO
                        if (exportedFilePath != null && !isExporting) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x1A10B981),
                                border = BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GreenSuccess,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "1080x1920 MP4 Rendered & Ready",
                                            fontWeight = FontWeight.Bold,
                                            color = GreenSuccess,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = exportedFilePath ?: "exports/Short_${short.id}_1080x1920.mp4",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PRIMARY DOWNLOAD / RENDER BUTTONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (exportedFilePath != null) {
                                        Toast.makeText(
                                            context,
                                            "Downloading 1080x1920 MP4 to device storage (${exportedFilePath})...",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        isDownloadedLocally = true
                                    } else {
                                        triggerExport()
                                    }
                                },
                                enabled = !isExporting,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("download_mp4_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (exportedFilePath != null) GreenSuccess else CyanGlow,
                                    contentColor = Color.Black
                                )
                            ) {
                                if (isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Rendering...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (exportedFilePath != null) "Download MP4 (1080p)" else "Render & Download MP4",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (exportedFilePath != null) {
                                IconButton(
                                    onClick = { triggerExport() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceElevated)
                                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                        .testTag("re_render_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Re-render",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. 1-TAP ALL-IN-ONE PUBLISHING KIT BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val allInOneKit = buildString {
                                appendLine("--- TITLE ---")
                                appendLine(selectedTitle)
                                appendLine()
                                appendLine("--- DESCRIPTION ---")
                                appendLine(descriptionText)
                                appendLine()
                                appendLine("--- HASHTAGS ---")
                                appendLine(hashtagsText)
                                appendLine()
                                appendLine("--- PINNED COMMENT ---")
                                appendLine(pinnedCommentText)
                            }
                            copyToClipboard(allInOneKit, "Complete Publishing Kit")
                            copiedKitRecently = true
                            coroutineScope.launch {
                                delay(2500)
                                copiedKitRecently = false
                            }
                        }
                        .testTag("copy_all_kit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F38)),
                    border = BorderStroke(1.dp, PurpleGlow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PurpleGlow, PinkAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (copiedKitRecently) Icons.Default.Check else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (copiedKitRecently) "Copied Complete Kit!" else "1-Tap Copy All-In-One Kit",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = if (copiedKitRecently) GreenSuccess else Color.White
                            )
                            Text(
                                text = "Copies Title + Description + Hashtags + Pinned Comment formatted for instant posting",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                shareShortMetadata(context, selectedTitle, descriptionText, hashtagsText)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark)
                                .testTag("share_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = CyanGlow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 3. QUICK COPY: TITLE SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Title, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Title", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Button(
                                onClick = {
                                    copyToClipboard(selectedTitle, "Title")
                                    copiedTitleRecently = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        copiedTitleRecently = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (copiedTitleRecently) GreenSuccess else SurfaceElevated,
                                    contentColor = if (copiedTitleRecently) Color.Black else CyanGlow
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("copy_title_button")
                            ) {
                                Icon(
                                    imageVector = if (copiedTitleRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (copiedTitleRecently) "Copied" else "Copy Title",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = selectedTitle,
                            onValueChange = {
                                selectedTitle = it
                                onSaveShort(short.copy(title = it))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanGlow,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ideal length: 40-70 characters for YouTube & TikTok",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${selectedTitle.length} chars",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedTitle.length in 30..80) CyanGlow else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Alternative title recommendations
                        if (titleList.size > 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Alternative Viral Angles:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            titleList.forEach { altTitle ->
                                if (altTitle != selectedTitle) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SurfaceDark,
                                        border = BorderStroke(1.dp, BorderDark),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable {
                                                selectedTitle = altTitle
                                                onSaveShort(short.copy(title = altTitle))
                                                Toast.makeText(context, "Set as active title!", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = altTitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { copyToClipboard(altTitle, "Alternative Title") },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Alt Title",
                                                    tint = CyanGlow,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. QUICK COPY: DESCRIPTION SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Description", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Button(
                                onClick = {
                                    copyToClipboard(descriptionText, "Description")
                                    copiedDescriptionRecently = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        copiedDescriptionRecently = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (copiedDescriptionRecently) GreenSuccess else SurfaceElevated,
                                    contentColor = if (copiedDescriptionRecently) Color.Black else CyanGlow
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("copy_description_button")
                            ) {
                                Icon(
                                    imageVector = if (copiedDescriptionRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (copiedDescriptionRecently) "Copied" else "Copy Description",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = descriptionText,
                            onValueChange = {
                                descriptionText = it
                                onSaveShort(short.copy(description = it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanGlow,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${descriptionText.length} characters • Includes SEO keywords and video summary",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // 5. QUICK COPY: HASHTAGS SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Viral Hashtags", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Button(
                                onClick = {
                                    copyToClipboard(hashtagsText, "Hashtags")
                                    copiedHashtagsRecently = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        copiedHashtagsRecently = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (copiedHashtagsRecently) GreenSuccess else SurfaceElevated,
                                    contentColor = if (copiedHashtagsRecently) Color.Black else CyanGlow
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("copy_hashtags_button")
                            ) {
                                Icon(
                                    imageVector = if (copiedHashtagsRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (copiedHashtagsRecently) "Copied" else "Copy All Hashtags",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = hashtagsText,
                            onValueChange = {
                                hashtagsText = it
                                onSaveShort(short.copy(youtubeHashtags = it))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanGlow,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = CyanGlow,
                                unfocusedTextColor = CyanGlow,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive individual hashtag chips
                        val tagChips = remember(hashtagsText) {
                            hashtagsText.split(" ").filter { it.startsWith("#") && it.length > 1 }
                        }

                        if (tagChips.isNotEmpty()) {
                            Text(
                                text = "Tap any individual hashtag to copy:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                tagChips.forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E2640),
                                        border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable {
                                            copyToClipboard(tag, tag)
                                        }
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CyanGlow,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. QUICK COPY: PINNED COMMENT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, tint = PinkAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pinned Engagement Comment", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Button(
                                onClick = {
                                    copyToClipboard(pinnedCommentText, "Pinned Comment")
                                    copiedPinnedRecently = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        copiedPinnedRecently = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (copiedPinnedRecently) GreenSuccess else SurfaceElevated,
                                    contentColor = if (copiedPinnedRecently) Color.Black else PinkAccent
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("copy_pinned_comment_button")
                            ) {
                                Icon(
                                    imageVector = if (copiedPinnedRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (copiedPinnedRecently) "Copied" else "Copy",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = pinnedCommentText,
                            onValueChange = {
                                pinnedCommentText = it
                                onSaveShort(short.copy(pinnedComment = it))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkAccent,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pin this comment immediately after uploading to boost algorithm comments & retention",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // 7. PLATFORM COMPLIANCE GUIDE
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDark,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Platform Publishing Checklist",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("YouTube Shorts", color = TextSecondary, fontSize = 11.sp)
                            Text("100 char title • #Shorts in title • 60s max", color = TextMuted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TikTok", color = TextSecondary, fontSize = 11.sp)
                            Text("2,200 char desc • 3-5 trending tags", color = TextMuted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Instagram Reels", color = TextSecondary, fontSize = 11.sp)
                            Text("Clean 9:16 vertical • 30 hashtags max", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
