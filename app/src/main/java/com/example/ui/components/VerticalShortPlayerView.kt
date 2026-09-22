package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaptionPosition
import com.example.data.model.CaptionStyle
import com.example.data.model.CaptionWord
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.SurfaceCard
import kotlinx.coroutines.delay

@Composable
fun VerticalShortPlayerView(
    modifier: Modifier = Modifier,
    shortTitle: String,
    hookText: String,
    transcript: String,
    captionWords: List<CaptionWord>,
    captionStyle: CaptionStyle,
    captionPosition: CaptionPosition,
    captionFontSize: Int = 24,
    showCaptions: Boolean = true,
    highlightWords: Boolean = true,
    emojiEmphasis: Boolean = true,
    framingMode: String = "CENTER_SPEAKER",
    cropOffsetRatio: Float = 0.5f,
    durationSeconds: Int = 45,
    onFrameScrub: ((Float) -> Unit)? = null
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentProgress by remember { mutableFloatStateOf(0.15f) }
    var activeWordIndex by remember { mutableIntStateOf(0) }

    // Simulation loop for video playback & word synchronizing
    LaunchedEffect(isPlaying, captionWords.size) {
        if (!isPlaying || captionWords.isEmpty()) return@LaunchedEffect
        while (isPlaying) {
            delay(280)
            activeWordIndex = (activeWordIndex + 1) % captionWords.size
            currentProgress = (activeWordIndex.toFloat() / captionWords.size.toFloat()).coerceIn(0f, 1f)
            onFrameScrub?.invoke(currentProgress)
        }
    }

    // Dynamic scale pulse for active viral word
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video Header badge bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color(0xFF10B981) else Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "9:16 VERTICAL PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanGlow,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x3300F2FE)
            ) {
                Text(
                    text = framingMode.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanGlow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 9:16 Aspect Ratio Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E1B4B),
                            Color(0xFF090D16)
                        )
                    )
                )
                .border(2.dp, Brush.linearGradient(listOf(CyanGlow, PurpleGlow, PinkAccent)), RoundedCornerShape(16.dp))
                .clickable { isPlaying = !isPlaying }
                .testTag("vertical_short_preview_canvas"),
            contentAlignment = Alignment.Center
        ) {
            // Simulated video stage visuals based on framingMode
            when (framingMode) {
                "BLURRED_BG" -> {
                    // Blurred backdrop simulation with sharp 16:9 inner block
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.radialGradient(listOf(Color(0x664F46E5), Color(0x22000000))))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Speaker audio waveform",
                            tint = CyanGlow,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                "SMART_CROP" -> {
                    // Smart crop visualization
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF312E81),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                    )
                }
                else -> {
                    // Center Speaker with dynamic focal tracking offset
                    val horizontalShiftDp = ((cropOffsetRatio - 0.5f) * 80).dp
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.offset(x = horizontalShiftDp)
                    ) {
                        // Speaker avatar placeholder with audio wave aura
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(CyanGlow, PurpleGlow)))
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🎙️",
                                    fontSize = 32.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x88000000)
                        ) {
                            Text(
                                text = if (cropOffsetRatio in 0.48f..0.52f) "Speaker Centered (50%)"
                                else "Speaker Offset (${(cropOffsetRatio * 100).toInt()}%)",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Top Video Watermark / Brand Hook Header
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC000000)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ $hookText",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            // CAPTIONS OVERLAY
            if (showCaptions && captionWords.isNotEmpty()) {
                val captionAlignment = when (captionPosition) {
                    CaptionPosition.TOP -> Alignment.TopCenter
                    CaptionPosition.CENTER -> Alignment.Center
                    CaptionPosition.BOTTOM -> Alignment.BottomCenter
                }

                val verticalPadding = when (captionPosition) {
                    CaptionPosition.TOP -> 52.dp
                    CaptionPosition.CENTER -> 0.dp
                    CaptionPosition.BOTTOM -> 54.dp
                }

                Box(
                    modifier = Modifier
                        .align(captionAlignment)
                        .padding(top = if (captionPosition == CaptionPosition.TOP) verticalPadding else 0.dp)
                        .padding(bottom = if (captionPosition == CaptionPosition.BOTTOM) verticalPadding else 0.dp)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RenderStyledCaptions(
                        style = captionStyle,
                        words = captionWords,
                        activeIndex = activeWordIndex,
                        fontSize = captionFontSize,
                        highlightWords = highlightWords,
                        emojiEmphasis = emojiEmphasis,
                        pulseScale = pulseScale
                    )
                }
            }

            // Play/Pause Floating Overlay
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC000000))
                        .border(2.dp, CyanGlow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Bottom Audio Track & Controls Row
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x99000000)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Audio track",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Original Audio • High Quality AAC",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 8.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x99000000)
                ) {
                    Text(
                        text = "1080x1920",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playback Scrubbing & Control Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.size(36.dp).testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = CyanGlow
                )
            }

            Slider(
                value = currentProgress,
                onValueChange = {
                    currentProgress = it
                    activeWordIndex = ((captionWords.size - 1) * it).toInt().coerceIn(0, (captionWords.size - 1).coerceAtLeast(0))
                    onFrameScrub?.invoke(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .testTag("video_progress_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = CyanGlow,
                    activeTrackColor = CyanGlow,
                    inactiveTrackColor = Color(0xFF334155)
                )
            )

            Text(
                text = "${(currentProgress * durationSeconds).toInt()}s / ${durationSeconds}s",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun RenderStyledCaptions(
    style: CaptionStyle,
    words: List<CaptionWord>,
    activeIndex: Int,
    fontSize: Int,
    highlightWords: Boolean,
    emojiEmphasis: Boolean,
    pulseScale: Float
) {
    // Show a window of 4-6 words around active word for clean reading
    val windowSize = 5
    val startIndex = (activeIndex - 1).coerceAtLeast(0)
    val endIndex = (startIndex + windowSize).coerceAtMost(words.size)
    val displayWords = words.subList(startIndex, endIndex)

    when (style) {
        CaptionStyle.CLEAN -> {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x88000000),
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayWords.forEach { word ->
                        val isActive = highlightWords && words.indexOf(word) == activeIndex
                        Text(
                            text = "${word.word} ",
                            fontSize = fontSize.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isActive) CyanGlow else Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        CaptionStyle.BOLD -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xEE0F172A),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFACC15))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayWords.forEach { word ->
                        val isActive = highlightWords && words.indexOf(word) == activeIndex
                        Text(
                            text = "${word.word.uppercase()} ",
                            fontSize = (fontSize + 2).sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = if (isActive) Color(0xFFFACC15) else Color.White
                        )
                    }
                }
            }
        }

        CaptionStyle.PODCAST -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xDD1E1B4B)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayWords.forEach { word ->
                        val isActive = highlightWords && words.indexOf(word) == activeIndex
                        Text(
                            text = "${word.word} ",
                            fontSize = fontSize.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) PinkAccent else Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        CaptionStyle.MINIMAL -> {
            Row(
                modifier = Modifier
                    .background(Color(0x55000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayWords.forEach { word ->
                    val isActive = highlightWords && words.indexOf(word) == activeIndex
                    Text(
                        text = "${word.word} ",
                        fontSize = (fontSize - 2).sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isActive) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }

        CaptionStyle.GAMING -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE090D16))
                    .border(2.dp, Brush.horizontalGradient(listOf(CyanGlow, Color(0xFFFF0055))), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayWords.forEach { word ->
                        val isActive = highlightWords && words.indexOf(word) == activeIndex
                        Text(
                            text = "${word.word} ",
                            fontSize = fontSize.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isActive) CyanGlow else Color(0xFFFF0055)
                        )
                    }
                }
            }
        }

        CaptionStyle.VIRAL_STYLE -> {
            // Large focal active word with emoji & bounce
            val currentWord = words.getOrNull(activeIndex)
            if (currentWord != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                ) {
                    if (emojiEmphasis && currentWord.emoji != null) {
                        Text(
                            text = currentWord.emoji,
                            fontSize = 32.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFACC15),
                        shadowElevation = 8.dp
                    ) {
                        Text(
                            text = " ${currentWord.word.uppercase()} ",
                            fontSize = (fontSize + 6).sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
