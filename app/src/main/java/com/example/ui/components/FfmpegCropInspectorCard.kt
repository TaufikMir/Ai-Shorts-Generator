package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoFramingMode
import com.example.data.util.FfmpegCropConfig
import com.example.data.util.FfmpegCropPipeline
import com.example.data.util.FfmpegPipelineProgress
import com.example.data.util.ResolutionPreset
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Visual FFmpeg Automated Center-Cropping and Framing Pipeline Inspector.
 * Provides interactive visual overlays of the 16:9 widescreen input and 9:16 vertical crop window,
 * macroblock-aligned crop telemetry, offset adjustments, and live FFmpeg command terminal execution.
 */
@Composable
fun FfmpegCropInspectorCard(
    modifier: Modifier = Modifier,
    initialFramingMode: VideoFramingMode = VideoFramingMode.CENTER_SPEAKER,
    onFramingModeChanged: ((VideoFramingMode) -> Unit)? = null,
    onOffsetChanged: ((Float) -> Unit)? = null,
    clipTitle: String = "Podcast Clip #1",
    startTimeSec: Float = 0f,
    durationSec: Float = 30f
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFramingMode by remember { mutableStateOf(initialFramingMode) }
    var horizontalOffsetRatio by remember { mutableFloatStateOf(0.5f) } // 0.5 = exact center
    var selectedPreset by remember { mutableStateOf(FfmpegCropPipeline.RESOLUTION_PRESETS.first()) }

    // Test run pipeline state
    var showPipelineRunnerDialog by remember { mutableStateOf(false) }
    var pipelineProgress by remember { mutableStateOf<FfmpegPipelineProgress?>(null) }
    var isPipelineRunning by remember { mutableStateOf(false) }

    // Calculate live geometry & filter complex
    val geometry = remember(selectedPreset, selectedFramingMode, horizontalOffsetRatio) {
        FfmpegCropPipeline.calculateGeometry(
            sourceWidth = selectedPreset.width,
            sourceHeight = selectedPreset.height,
            framingMode = selectedFramingMode,
            horizontalOffsetRatio = horizontalOffsetRatio
        )
    }

    val ffmpegConfig = remember(selectedPreset, selectedFramingMode, horizontalOffsetRatio, startTimeSec, durationSec) {
        FfmpegCropConfig(
            inputPath = "source_horizontal_${selectedPreset.width}x${selectedPreset.height}.mp4",
            outputPath = "short_9x16_${selectedFramingMode.name.lowercase()}.mp4",
            sourceWidth = selectedPreset.width,
            sourceHeight = selectedPreset.height,
            startTimeSec = startTimeSec,
            durationSec = durationSec,
            framingMode = selectedFramingMode,
            horizontalOffsetRatio = horizontalOffsetRatio
        )
    }

    val ffmpegCommand = remember(ffmpegConfig) {
        FfmpegCropPipeline.buildCommand(ffmpegConfig)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseReticle")
    val reticlePulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticlePulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ffmpeg_crop_inspector_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanGlow.copy(alpha = 0.15f))
                            .border(1.dp, CyanGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = "FFmpeg Crop Engine",
                            tint = CyanGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FFmpeg 9:16 Video Pipeline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x3300F2FE)
                            ) {
                                Text(
                                    text = "AUTOMATED",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanGlow
                                )
                            }
                        }
                        Text(
                            text = "Automated center-cropping & framing from horizontal source",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        showPipelineRunnerDialog = true
                        isPipelineRunning = true
                        coroutineScope.launch {
                            FfmpegCropPipeline.executePipeline(ffmpegConfig, context)
                                .onEach { prog ->
                                    pipelineProgress = prog
                                    if (prog.isComplete) {
                                        isPipelineRunning = false
                                    }
                                }
                                .launchIn(this)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("run_ffmpeg_pipeline_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Pipeline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual 16:9 Source Canvas with 9:16 Crop Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0B0F19))
                    .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOURCE: ${selectedPreset.width}x${selectedPreset.height} (16:9 Horizontal)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "CROP: ${geometry.cropWidth}x${geometry.cropHeight} ➔ 1080x1920 (9:16)",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanGlow,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 16:9 Horizontal Frame Container
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF090D16))
                                )
                            )
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val canvasWidth = maxWidth
                        val canvasHeight = maxHeight

                        // 9:16 Aspect crop width relative to 16:9 canvas height
                        val cropBoxWidth = canvasHeight * (9f / 16f)
                        val maxAvailableMargin = (canvasWidth - cropBoxWidth).coerceAtLeast(0.dp)
                        val cropBoxLeftOffset = maxAvailableMargin * horizontalOffsetRatio

                        // Background horizontal scene representation
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left speaker visual indicator
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PurpleGlow.copy(alpha = 0.3f))
                                        .border(1.dp, PurpleGlow.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎙️", fontSize = 16.sp)
                                }
                                Text("Host (30%)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                            }

                            // Center speaker visual indicator
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(CyanGlow.copy(alpha = 0.35f))
                                        .border(2.dp, CyanGlow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎯", fontSize = 20.sp)
                                }
                                Text("Center Focus (50%)", style = MaterialTheme.typography.labelSmall, color = CyanGlow, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }

                            // Right speaker visual indicator
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PinkAccent.copy(alpha = 0.3f))
                                        .border(1.dp, PinkAccent.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💬", fontSize = 16.sp)
                                }
                                Text("Guest (70%)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                            }
                        }

                        // Left Cropped Margin (translucent darkened scrim)
                        if (cropBoxLeftOffset > 0.dp) {
                            Box(
                                modifier = Modifier
                                    .width(cropBoxLeftOffset)
                                    .fillMaxHeight()
                                    .background(Color(0xBB000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "-${geometry.leftCroppedPixels}px",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Right Cropped Margin (translucent darkened scrim)
                        val rightOffset = cropBoxLeftOffset + cropBoxWidth
                        val rightWidth = (canvasWidth - rightOffset).coerceAtLeast(0.dp)
                        if (rightWidth > 0.dp) {
                            Box(
                                modifier = Modifier
                                    .offset(x = rightOffset)
                                    .width(rightWidth)
                                    .fillMaxHeight()
                                    .background(Color(0xBB000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "-${geometry.rightCroppedPixels}px",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Active 9:16 Crop Window Overlay
                        Box(
                            modifier = Modifier
                                .offset(x = cropBoxLeftOffset)
                                .width(cropBoxWidth)
                                .fillMaxHeight()
                                .border(2.dp, CyanGlow, RoundedCornerShape(4.dp))
                                .background(
                                    if (selectedFramingMode == VideoFramingMode.BLURRED_BG)
                                        Color(0x3300F2FE)
                                    else
                                        Color.Transparent
                                )
                                .testTag("active_9x16_crop_window"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Rule of thirds subtle grid
                            Column(modifier = Modifier.fillMaxSize()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CyanGlow.copy(alpha = 0.25f)))
                                Spacer(modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CyanGlow.copy(alpha = 0.25f)))
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            // Center speaker reticle crosshair
                            Box(
                                modifier = Modifier
                                    .size(32.dp * reticlePulse)
                                    .border(1.5.dp, CyanGlow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CyanGlow)
                                )
                            }

                            // Top Tag
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xDD000000)
                            ) {
                                Text(
                                    text = "9:16 VERTICAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanGlow,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            // Bottom Caption safe zone
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(0.9f)
                                    .padding(bottom = 6.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xAA1E293B)
                            ) {
                                Text(
                                    text = "Caption Safe Zone",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Framing Mode Selector Pills
            Text("Framing Strategy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VideoFramingMode.values().forEach { mode ->
                    val isSelected = selectedFramingMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFramingMode = mode
                            onFramingModeChanged?.invoke(mode)
                        },
                        label = {
                            Text(
                                text = mode.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanGlow)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E2640),
                            selectedLabelColor = CyanGlow,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BorderDark,
                            selectedBorderColor = CyanGlow
                        ),
                        modifier = Modifier.testTag("framing_chip_${mode.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Focal Offset Slider (Automated Center Lock)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Automated Center-Speaker Offset", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text(
                    text = "${(horizontalOffsetRatio * 100).toInt()}% " +
                            when {
                                horizontalOffsetRatio < 0.45f -> "(Left Focus)"
                                horizontalOffsetRatio > 0.55f -> "(Right Focus)"
                                else -> "(Exact Center)"
                            },
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanGlow,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = horizontalOffsetRatio,
                onValueChange = {
                    horizontalOffsetRatio = it
                    onOffsetChanged?.invoke(it)
                },
                valueRange = 0.0f..1.0f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = CyanGlow,
                    activeTrackColor = CyanGlow,
                    inactiveTrackColor = Color(0xFF232A42)
                ),
                modifier = Modifier.testTag("crop_offset_slider")
            )

            // Preset Quick Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        horizontalOffsetRatio = 0.30f
                        onOffsetChanged?.invoke(0.30f)
                    },
                    modifier = Modifier.weight(1f).height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (horizontalOffsetRatio in 0.28f..0.32f) Color(0xFF1E2640) else SurfaceElevated),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("Left (30%)", fontSize = 11.sp, color = if (horizontalOffsetRatio in 0.28f..0.32f) CyanGlow else TextSecondary)
                }
                Button(
                    onClick = {
                        horizontalOffsetRatio = 0.50f
                        onOffsetChanged?.invoke(0.50f)
                    },
                    modifier = Modifier.weight(1f).height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (horizontalOffsetRatio in 0.48f..0.52f) Color(0xFF1E2640) else SurfaceElevated),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("Center (50%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (horizontalOffsetRatio in 0.48f..0.52f) CyanGlow else TextSecondary)
                }
                Button(
                    onClick = {
                        horizontalOffsetRatio = 0.70f
                        onOffsetChanged?.invoke(0.70f)
                    },
                    modifier = Modifier.weight(1f).height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (horizontalOffsetRatio in 0.68f..0.72f) Color(0xFF1E2640) else SurfaceElevated),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("Right (70%)", fontSize = 11.sp, color = if (horizontalOffsetRatio in 0.68f..0.72f) CyanGlow else TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Test Source Resolution Presets
            Text("Source Video Format", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FfmpegCropPipeline.RESOLUTION_PRESETS) { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        modifier = Modifier
                            .clickable { selectedPreset = preset }
                            .clip(RoundedCornerShape(8.dp)),
                        color = if (isSelected) Color(0xFF1E2640) else SurfaceDark,
                        border = BorderStroke(1.dp, if (isSelected) CyanGlow else BorderDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CyanGlow else TextPrimary
                            )
                            Text(
                                text = "${preset.width}x${preset.height}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macroblock & Crop Geometry Metrics Grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceDark,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("FFmpeg Crop Geometry & Macroblock Alignment", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyanGlow)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem("Source Size", "${geometry.sourceWidth}x${geometry.sourceHeight}")
                        MetricItem("9:16 Crop Box", "${geometry.cropWidth}x${geometry.cropHeight}")
                        MetricItem("Left Crop", "-${geometry.leftCroppedPixels} px")
                        MetricItem("Right Crop", "-${geometry.rightCroppedPixels} px")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // FFmpeg Filter Graph & Command Terminal
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF070A11),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generated FFmpeg Filter Complex", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(ffmpegCommand))
                                Toast.makeText(context, "Full FFmpeg command copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy command", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "-filter_complex \"${geometry.filterComplex}\"",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8),
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Encoder: H.264 (libx264) | Preset: veryfast | CRF: 21 | Audio: AAC 192k 48kHz | Output: 1080x1920 @ 60fps (9:16)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    // FFmpeg Pipeline Execution Dialog
    if (showPipelineRunnerDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isPipelineRunning) showPipelineRunnerDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = CyanGlow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FFmpeg 9:16 Crop Pipeline Run", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val prog = pipelineProgress
                    if (prog != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = prog.stage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "${prog.percent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = CyanGlow)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { prog.percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CyanGlow,
                            trackColor = Color(0xFF1E2640)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Frames: ${prog.currentFrame} / ${prog.totalEstimatedFrames}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(text = "FPS: ${String.format(java.util.Locale.US, "%.1f", prog.fps)}", style = MaterialTheme.typography.labelSmall, color = GreenSuccess, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF070A11),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = prog.logOutput,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        if (prog.isComplete) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x3310B981),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("1080x1920 9:16 vertical short successfully rendered!", style = MaterialTheme.typography.labelSmall, color = GreenSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        CircularProgressIndicator(color = CyanGlow, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPipelineRunnerDialog = false },
                    enabled = !isPipelineRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
                ) {
                    Text(if (isPipelineRunning) "Processing..." else "Close")
                }
            },
            containerColor = SurfaceDark,
            textContentColor = TextPrimary
        )
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
    }
}
