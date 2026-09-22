package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ShortEntity
import com.example.data.model.CaptionPosition
import com.example.data.model.CaptionStyle
import com.example.data.model.CaptionWord
import com.example.data.model.VideoFramingMode
import com.example.data.repository.AiService
import com.example.ui.components.FfmpegCropInspectorCard
import com.example.ui.components.VerticalShortPlayerView
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TranscriptEntity
import com.example.data.model.TimestampedSegment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

enum class StudioTab(val label: String) {
    PLAYER_FRAMING("Framing & Player"),
    CAPTIONS("Captions & Style"),
    HOOKS("Hooks (3 Options)"),
    SEO("SEO & Titles"),
    TRANSCRIPT("Full Transcript"),
    THUMBNAIL("Thumbnail"),
    EXPORT("Export (1080p)")
}

@Composable
fun ShortStudioScreen(
    short: ShortEntity,
    aiService: AiService,
    onBack: () -> Unit,
    onSaveShort: (ShortEntity) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onExportRender: suspend (Long, (Int) -> Unit) -> String,
    getTranscriptByProjectId: ((Long) -> Flow<TranscriptEntity?>)? = null,
    onRetranscribe: ((Long) -> Unit)? = null,
    onOpenExportScreen: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(StudioTab.PLAYER_FRAMING) }

    // Live editable state
    var shortTitle by remember { mutableStateOf(short.title) }
    var currentHook by remember { mutableStateOf(short.hook) }
    var framingMode by remember { mutableStateOf(short.framingMode) }
    var cropOffsetRatio by remember { mutableFloatStateOf(0.5f) }
    var captionStyle by remember {
        mutableStateOf(
            try { CaptionStyle.valueOf(short.captionStyle) } catch (e: Exception) { CaptionStyle.VIRAL_STYLE }
        )
    }
    var captionPosition by remember {
        mutableStateOf(
            try { CaptionPosition.valueOf(short.captionPosition) } catch (e: Exception) { CaptionPosition.BOTTOM }
        )
    }
    var captionFontSize by remember { mutableIntStateOf(short.captionFontSize) }
    var showCaptions by remember { mutableStateOf(short.showCaptions) }
    var highlightWords by remember { mutableStateOf(short.highlightWords) }
    var emojiEmphasis by remember { mutableStateOf(short.emojiEmphasis) }
    var transcriptText by remember { mutableStateOf(short.transcript) }

    // SEO state
    var titleSuggestions by remember {
        mutableStateOf(short.titleSuggestions.split("|").filter { it.isNotBlank() })
    }
    var selectedTitle by remember { mutableStateOf(short.title) }
    var descriptionText by remember { mutableStateOf(short.description) }
    var youtubeHashtags by remember { mutableStateOf(short.youtubeHashtags) }
    var searchKeywords by remember { mutableStateOf(short.searchKeywords) }
    var pinnedComment by remember { mutableStateOf(short.pinnedComment) }

    // Thumbnail state
    var thumbnailFrameSec by remember { mutableIntStateOf(short.thumbnailFrameSecond) }
    var thumbnailOverlayText by remember { mutableStateOf(short.thumbnailOverlayText.ifBlank { short.hook }) }

    // Export state
    var exportProgress by remember { mutableIntStateOf(0) }
    var isExporting by remember { mutableStateOf(false) }
    var exportedDownloadPath by remember { mutableStateOf(short.exportedPath) }

    // Generate hook options
    val suggestedHooks = remember(short.id) {
        aiService.generateHooks(short.hook, short.transcript)
    }

    // Parsed words for animated caption rendering
    var parsedWords by remember { mutableStateOf<List<CaptionWord>>(emptyList()) }
    LaunchedEffect(transcriptText) {
        parsedWords = aiService.parseTranscriptIntoWords(transcriptText, startMs = (short.startTimeSeconds * 1000).toLong())
    }

    val projectTranscript by (getTranscriptByProjectId?.invoke(short.projectId) ?: flowOf(null))
        .collectAsStateWithLifecycle(null)
    var transcriptSearchQuery by remember { mutableStateOf("") }

    val transcriptSegments = remember(projectTranscript?.segmentsJson) {
        val json = projectTranscript?.segmentsJson
        if (json != null) {
            TimestampedSegment.parseSegmentsJson(json)
        } else emptyList()
    }

    val filteredTranscriptSegments = remember(transcriptSegments, transcriptSearchQuery) {
        if (transcriptSearchQuery.isBlank()) transcriptSegments
        else transcriptSegments.filter {
            it.text.contains(transcriptSearchQuery, ignoreCase = true) ||
            it.speaker.contains(transcriptSearchQuery, ignoreCase = true)
        }
    }

    fun saveCurrentChanges() {
        val updated = short.copy(
            title = selectedTitle,
            hook = currentHook,
            framingMode = framingMode,
            captionStyle = captionStyle.name,
            captionPosition = captionPosition.name,
            captionFontSize = captionFontSize,
            showCaptions = showCaptions,
            highlightWords = highlightWords,
            emojiEmphasis = emojiEmphasis,
            transcript = transcriptText,
            titleSuggestions = titleSuggestions.joinToString("|"),
            description = descriptionText,
            youtubeHashtags = youtubeHashtags,
            searchKeywords = searchKeywords,
            pinnedComment = pinnedComment,
            thumbnailFrameSecond = thumbnailFrameSec,
            thumbnailOverlayText = thumbnailOverlayText
        )
        onSaveShort(updated)
        Toast.makeText(context, "Short settings saved!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Studio Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("studio_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Column {
                        Text(
                            text = "Short Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${short.durationSeconds}s • Internal Score ${short.internalAiScore}/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanGlow,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleFavorite(short.id, short.isFavorite) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (short.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (short.isFavorite) PinkAccent else TextMuted
                        )
                    }

                    Button(
                        onClick = { saveCurrentChanges() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("save_short_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Scrollable Tabs Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = SurfaceDark,
            contentColor = CyanGlow,
            edgePadding = 12.dp
        ) {
            StudioTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.label,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Main Tab Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TAB 1: PLAYER & FRAMING
            if (selectedTab == StudioTab.PLAYER_FRAMING) {
                item {
                    VerticalShortPlayerView(
                        shortTitle = selectedTitle,
                        hookText = currentHook,
                        transcript = transcriptText,
                        captionWords = parsedWords,
                        captionStyle = captionStyle,
                        captionPosition = captionPosition,
                        captionFontSize = captionFontSize,
                        showCaptions = showCaptions,
                        highlightWords = highlightWords,
                        emojiEmphasis = emojiEmphasis,
                        framingMode = framingMode,
                        cropOffsetRatio = cropOffsetRatio,
                        durationSeconds = short.durationSeconds
                    )
                }

                item {
                    val currentMode = try {
                        VideoFramingMode.valueOf(framingMode)
                    } catch (e: Exception) {
                        VideoFramingMode.CENTER_SPEAKER
                    }

                    FfmpegCropInspectorCard(
                        initialFramingMode = currentMode,
                        onFramingModeChanged = { newMode ->
                            framingMode = newMode.name
                        },
                        onOffsetChanged = { newOffset ->
                            cropOffsetRatio = newOffset
                        },
                        clipTitle = selectedTitle,
                        startTimeSec = short.startTimeSeconds.toFloat(),
                        durationSec = short.durationSeconds.toFloat()
                    )
                }

                item {
                    Text(
                        text = "Vertical 9:16 Framing Strategy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose how the widescreen original video is converted into vertical format:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                items(VideoFramingMode.values()) { mode ->
                    val isSelected = framingMode == mode.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { framingMode = mode.name }
                            .testTag("framing_mode_${mode.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E2640) else SurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyanGlow else BorderDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyanGlow else SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = mode.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = CyanGlow)
                            }
                        }
                    }
                }
            }

            // TAB 2: CAPTIONS & STYLE
            if (selectedTab == StudioTab.CAPTIONS) {
                item {
                    // Quick Mini Preview
                    VerticalShortPlayerView(
                        shortTitle = selectedTitle,
                        hookText = currentHook,
                        transcript = transcriptText,
                        captionWords = parsedWords,
                        captionStyle = captionStyle,
                        captionPosition = captionPosition,
                        captionFontSize = captionFontSize,
                        showCaptions = showCaptions,
                        highlightWords = highlightWords,
                        emojiEmphasis = emojiEmphasis,
                        framingMode = framingMode,
                        durationSeconds = short.durationSeconds
                    )
                }

                item {
                    Text(
                        text = "Caption Presets (6 Styles)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CaptionStyle.values()) { style ->
                            val isSelected = captionStyle == style
                            FilterChip(
                                selected = isSelected,
                                onClick = { captionStyle = style },
                                label = {
                                    Text(
                                        text = style.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanGlow,
                                    selectedLabelColor = Color.Black,
                                    containerColor = SurfaceCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) CyanGlow else BorderDark
                                ),
                                modifier = Modifier.testTag("style_${style.name.lowercase()}")
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Caption Controls",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Show Captions Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Display Captions", color = TextPrimary)
                                Switch(
                                    checked = showCaptions,
                                    onCheckedChange = { showCaptions = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanGlow)
                                )
                            }

                            // Word-by-word Highlight Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Word-by-Word Highlight", color = TextPrimary)
                                Switch(
                                    checked = highlightWords,
                                    onCheckedChange = { highlightWords = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanGlow)
                                )
                            }

                            // Emoji Emphasis Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Emoji Burst Emphasis (🔥 💥 🛑)", color = TextPrimary)
                                Switch(
                                    checked = emojiEmphasis,
                                    onCheckedChange = { emojiEmphasis = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyanGlow)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Font size slider
                            Text(
                                text = "Font Size: $captionFontSize sp",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Slider(
                                value = captionFontSize.toFloat(),
                                onValueChange = { captionFontSize = it.toInt() },
                                valueRange = 16f..36f,
                                steps = 10,
                                colors = SliderDefaults.colors(thumbColor = CyanGlow, activeTrackColor = CyanGlow)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Position selector
                            Text("Caption Position", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CaptionPosition.values().forEach { pos ->
                                    val isSelected = captionPosition == pos
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CyanGlow else SurfaceDark,
                                        modifier = Modifier
                                            .clickable { captionPosition = pos }
                                            .weight(1f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) CyanGlow else BorderDark
                                        )
                                    ) {
                                        Text(
                                            text = pos.name,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = if (isSelected) Color.Black else TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Edit Transcript directly
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Transcript & Subtitles Editor",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Edit verbatim words below. Timestamps & captions will re-sync dynamically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = transcriptText,
                                onValueChange = { transcriptText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .testTag("transcript_editor_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // TAB 3: HOOK GENERATOR
            if (selectedTab == StudioTab.HOOKS) {
                item {
                    Text(
                        text = "AI Hook Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "The first 3 seconds decide whether viewers swipe or watch. Pick the strongest hook:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                items(suggestedHooks) { hook ->
                    val isSelected = currentHook == hook.hookText
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentHook = hook.hookText }
                            .testTag("hook_option_${hook.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E2640) else SurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyanGlow else BorderDark
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x3300F2FE)
                                ) {
                                    Text(
                                        text = hook.category.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanGlow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GreenSuccess
                                    ) {
                                        Text(
                                            text = "ACTIVE HOOK",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "\"${hook.hookText}\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "⚡ ${hook.engagementNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Custom Hook Input
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Or Write Custom Hook",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = currentHook,
                                onValueChange = { currentHook = it },
                                modifier = Modifier.fillMaxWidth().testTag("custom_hook_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SurfaceDark,
                                    unfocusedContainerColor = SurfaceDark
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // TAB 4: SEO GENERATOR
            if (selectedTab == StudioTab.SEO) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SEO & Metadata Suite",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Optimized titles, hashtags, description, and pinned comment",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                val regenerated = aiService.regenerateSeo(selectedTitle, transcriptText)
                                titleSuggestions = regenerated.titleSuggestions
                                descriptionText = regenerated.description
                                youtubeHashtags = regenerated.youtubeHashtags.joinToString(" ")
                                searchKeywords = regenerated.searchKeywords.joinToString(", ")
                                pinnedComment = regenerated.pinnedComment
                                Toast.makeText(context, "Regenerated fresh SEO package!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = CyanGlow),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.testTag("regenerate_seo_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Regenerate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Text(
                        text = "5 High-CTR Title Suggestions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(titleSuggestions) { titleOption ->
                    val isSelected = selectedTitle == titleOption
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTitle = titleOption },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E2640) else SurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyanGlow else BorderDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = titleOption,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = CyanGlow)
                            }
                        }
                    }
                }

                // Description Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Short Description", fontWeight = FontWeight.Bold, color = TextPrimary)
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(descriptionText))
                                        Toast.makeText(context, "Copied description!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyanGlow, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = descriptionText,
                                onValueChange = { descriptionText = it },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
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
                        }
                    }
                }

                // Hashtags & Keywords
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("YouTube & TikTok Hashtags", fontWeight = FontWeight.Bold, color = TextPrimary)
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(youtubeHashtags))
                                        Toast.makeText(context, "Copied hashtags!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyanGlow, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = youtubeHashtags,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Search Keywords", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = searchKeywords, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }

                // Pinned comment
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Suggested Pinned Comment", fontWeight = FontWeight.Bold, color = TextPrimary)
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(pinnedComment))
                                        Toast.makeText(context, "Copied pinned comment!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyanGlow, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(text = pinnedComment, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // TAB 5: THUMBNAIL GENERATOR
            if (selectedTab == StudioTab.THUMBNAIL) {
                item {
                    Text(
                        text = "Thumbnail Suite & Frame Grabber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Select high-expression frame and customize bold title banner for click-through rate:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                item {
                    // Thumbnail Preview Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF311042), Color(0xFF0F172A))
                                )
                            )
                            .border(2.dp, CyanGlow, RoundedCornerShape(16.dp))
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎙️", fontSize = 56.sp)

                        // Top frame timestamp
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xCC000000),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Frame: 00:0${thumbnailFrameSec}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanGlow,
                                fontSize = 9.sp
                            )
                        }

                        // Bold Thumbnail Text Overlay
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFACC15),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = thumbnailOverlayText.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Frame Scrubber", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = "Scrub video to choose optimal speaker facial expression at second $thumbnailFrameSec",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Slider(
                                value = thumbnailFrameSec.toFloat(),
                                onValueChange = { thumbnailFrameSec = it.toInt() },
                                valueRange = 0f..short.durationSeconds.toFloat(),
                                colors = SliderDefaults.colors(thumbColor = CyanGlow, activeTrackColor = CyanGlow)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Thumbnail Overlay Text", fontWeight = FontWeight.Bold, color = TextPrimary)
                            OutlinedTextField(
                                value = thumbnailOverlayText,
                                onValueChange = { thumbnailOverlayText = it },
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
                        }
                    }
                }
            }

            // TAB 5: FULL TRANSCRIPT SERVICE & SEGMENTS
            if (selectedTab == StudioTab.TRANSCRIPT) {
                val currentTranscript = projectTranscript
                val segments = transcriptSegments
                val filteredSegments = filteredTranscriptSegments

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Full Audio Transcript",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Asynchronous neural speech-to-text service output",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        if (onRetranscribe != null) {
                            Button(
                                onClick = {
                                    onRetranscribe(short.projectId)
                                    Toast.makeText(context, "Transcription worker enqueued in background!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, BorderDark),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Re-transcribe", tint = CyanGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Re-run Worker", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                            }
                        }
                    }
                }

                if (currentTranscript != null) {
                    // Audio & Speech Telemetry Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, BorderDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Audio Telemetry & Metadata",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Surface(
                                        color = GreenSuccess.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${(currentTranscript.confidenceScore * 100).toInt()}% Confidence",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = GreenSuccess
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TranscriptMetricChip(
                                        modifier = Modifier.weight(1f),
                                        label = "Duration",
                                        value = "${currentTranscript.durationSeconds / 60}m ${currentTranscript.durationSeconds % 60}s"
                                    )
                                    TranscriptMetricChip(
                                        modifier = Modifier.weight(1f),
                                        label = "Word Count",
                                        value = "${currentTranscript.wordCount} words"
                                    )
                                    TranscriptMetricChip(
                                        modifier = Modifier.weight(1f),
                                        label = "Pacing",
                                        value = "${currentTranscript.speechRateWpm} WPM"
                                    )
                                    TranscriptMetricChip(
                                        modifier = Modifier.weight(1f),
                                        label = "Language",
                                        value = currentTranscript.language
                                    )
                                }

                                if (currentTranscript.keyTopics.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Topics: ${currentTranscript.keyTopics}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Executive Summary
                    if (currentTranscript.summary.isNotBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(1.dp, BorderDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Executive Summary",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentTranscript.summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    // Clip Range Indicator
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CyanGlow.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Current Short: [${short.startTimeSeconds}s - ${short.endTimeSeconds}s] (${short.durationSeconds}s)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanGlow
                                    )
                                    Text(
                                        text = "Segments matching this clip range are highlighted below with a cyan indicator.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Search & Export Bar
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = transcriptSearchQuery,
                                onValueChange = { transcriptSearchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search dialogue or speakers...", color = TextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
                                },
                                trailingIcon = {
                                    if (transcriptSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { transcriptSearchQuery = "" }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val fullText = segments.joinToString("\n\n") { "[${it.formatTimestamp()}] ${it.speaker}: ${it.text}" }
                                        clipboardManager.setText(AnnotatedString(fullText))
                                        Toast.makeText(context, "Full transcript copied with timestamps!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                    border = BorderStroke(1.dp, BorderDark)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Text", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                }

                                Button(
                                    onClick = {
                                        val srtBuilder = StringBuilder()
                                        segments.forEachIndexed { idx, seg ->
                                            srtBuilder.append("${idx + 1}\n")
                                            val startM = seg.startTimeSeconds / 60
                                            val startS = seg.startTimeSeconds % 60
                                            val endM = seg.endTimeSeconds / 60
                                            val endS = seg.endTimeSeconds % 60
                                            srtBuilder.append(String.format("00:%02d:%02d,000 --> 00:%02d:%02d,000\n", startM, startS, endM, endS))
                                            srtBuilder.append("${seg.speaker}: ${seg.text}\n\n")
                                        }
                                        clipboardManager.setText(AnnotatedString(srtBuilder.toString()))
                                        Toast.makeText(context, "SubRip (.SRT) subtitles copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.4f))
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export .SRT", style = MaterialTheme.typography.labelMedium, color = CyanGlow)
                                }
                            }
                        }
                    }

                    // List of Segments
                    items(filteredSegments, key = { it.id }) { seg ->
                        val isOverlapping = seg.startTimeSeconds < short.endTimeSeconds && seg.endTimeSeconds > short.startTimeSeconds

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOverlapping) SurfaceDark else SurfaceCard
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isOverlapping) CyanGlow.copy(alpha = 0.7f) else BorderDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = BgDark,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, BorderDark)
                                        ) {
                                            Text(
                                                text = seg.formatTimestamp(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyanGlow,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        val speakerColor = if (seg.speaker.contains("Host", true) || seg.speaker.contains("1")) PurpleGlow else GreenSuccess
                                        Surface(
                                            color = speakerColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = seg.speaker,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = speakerColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (isOverlapping) {
                                            Surface(
                                                color = CyanGlow.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "In This Short",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CyanGlow,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(seg.text))
                                            Toast.makeText(context, "Segment text copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy segment", tint = TextMuted, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = seg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isOverlapping) TextPrimary else TextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                } else {
                    // Fallback when transcript is still being generated or not yet persisted
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(1.dp, BorderDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Transcript Processing Service",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Full timestamped dialogue is processed in the background. Here is the excerpt detected for this specific Short:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = BgDark,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderDark)
                                ) {
                                    Text(
                                        text = short.transcript,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                                if (onRetranscribe != null) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            onRetranscribe(short.projectId)
                                            Toast.makeText(context, "Asynchronous transcription worker started!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = BgDark, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Launch Background Worker", color = BgDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 6: EXPORT & DOWNLOAD
            if (selectedTab == StudioTab.EXPORT) {
                item {
                    Text(
                        text = "Export & Render",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Render production-grade 1080x1920 MP4 vertical video with embedded captions & audio:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Export Specifications", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Resolution:", color = TextSecondary, fontSize = 12.sp)
                                Text("1080 x 1920 (9:16 Vertical)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Framing Strategy:", color = TextSecondary, fontSize = 12.sp)
                                Text(
                                    text = try { VideoFramingMode.valueOf(framingMode).displayName } catch (e: Exception) { "Center Speaker" },
                                    color = CyanGlow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Video Codec:", color = TextSecondary, fontSize = 12.sp)
                                Text("H.264 High Profile 60fps", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Audio Format:", color = TextSecondary, fontSize = 12.sp)
                                Text("AAC 192kbps 48kHz", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Burned-in Captions:", color = TextSecondary, fontSize = 12.sp)
                                Text(captionStyle.displayName, color = CyanGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (isExporting) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Rendering Vertical Video... $exportProgress%",
                                    fontWeight = FontWeight.Bold,
                                    color = CyanGlow
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { exportProgress / 100f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = CyanGlow,
                                    trackColor = Color(0xFF232A42)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Burning animated captions and encoding 1080p MP4...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                if (exportedDownloadPath != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ready to Download!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exportedDownloadPath ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Saved video to your device Downloads folder!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download MP4 (1080p)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isExporting = true
                                exportProgress = 0
                                try {
                                    val path = onExportRender(short.id) { prog ->
                                        exportProgress = prog
                                    }
                                    exportedDownloadPath = path
                                    Toast.makeText(context, "Rendering complete!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Render failed: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        enabled = !isExporting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("render_export_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isExporting) "Rendering Video..." else "Render & Export Short",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                // Quick Actions: Titles, Descriptions, Hashtags
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Quick Copy Content Kit",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1-tap actions to copy ready-to-publish metadata for Shorts, Reels, and TikTok",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Copy Title Quick Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Title", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(selectedTitle, style = MaterialTheme.typography.bodySmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(selectedTitle))
                                        Toast.makeText(context, "Title copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = CyanGlow),
                                    modifier = Modifier.height(32.dp).testTag("studio_copy_title_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Copy Description Quick Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Description", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(descriptionText, style = MaterialTheme.typography.bodySmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(descriptionText))
                                        Toast.makeText(context, "Description copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = CyanGlow),
                                    modifier = Modifier.height(32.dp).testTag("studio_copy_desc_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Copy Hashtags Quick Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Hashtags", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(youtubeHashtags, style = MaterialTheme.typography.bodySmall, color = CyanGlow, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(youtubeHashtags))
                                        Toast.makeText(context, "Hashtags copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = CyanGlow),
                                    modifier = Modifier.height(32.dp).testTag("studio_copy_hashtags_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (onOpenExportScreen != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onOpenExportScreen(short.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("open_full_export_screen_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PurpleGlow,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Dedicated Export & Content Screen", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun TranscriptMetricChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        color = BgDark,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

