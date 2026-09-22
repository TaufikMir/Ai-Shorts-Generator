package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProcessingStage
import com.example.data.repository.ShortsRepository
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * The ordered sequence of progress stages required by the video processing pipeline:
 * Uploading → Transcribing → Finding moments → Creating clips → Adding captions → Ready
 */
val PIPELINE_PROGRESS_STAGES = listOf(
    ProcessingStage.UPLOADING,
    ProcessingStage.TRANSCRIBING,
    ProcessingStage.FINDING_MOMENTS,
    ProcessingStage.CREATING_CLIPS,
    ProcessingStage.ADDING_CAPTIONS,
    ProcessingStage.READY
)

fun getStageIcon(stage: ProcessingStage): ImageVector = when (stage) {
    ProcessingStage.UPLOADING -> Icons.Default.CloudUpload
    ProcessingStage.TRANSCRIBING -> Icons.Default.GraphicEq
    ProcessingStage.FINDING_MOMENTS -> Icons.Default.AutoAwesome
    ProcessingStage.CREATING_CLIPS -> Icons.Default.Movie
    ProcessingStage.ADDING_CAPTIONS -> Icons.Default.Subtitles
    ProcessingStage.READY -> Icons.Default.CheckCircle
    else -> Icons.Default.CheckCircle
}

/**
 * Visual Status Indicator displaying the sequential progress stages of video processing:
 * 'Uploading → Transcribing → Finding moments → Creating clips → Adding captions → Ready'
 * with distinct loading animations (pulsing glow, rotating rings, flowing connectors, live progress bar).
 */
@Composable
fun VideoProcessingProgressIndicator(
    status: ShortsRepository.CreationPipelineStatus.Running,
    onCancelUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Loading Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pipeline_loading")

    // Pulsing scale for current active stage icon
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stage_pulse"
    )

    // Pulsing halo alpha for active glow
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stage_glow"
    )

    // Continuous 360 rotation for in-progress spinner
    val spinnerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_rotation"
    )

    // Smooth animated linear progress
    val animatedProgress by animateFloatAsState(
        targetValue = status.progressPercent / 100f,
        animationSpec = tween(durationMillis = 320, easing = LinearEasing),
        label = "linear_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pipeline_progress_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(listOf(CyanGlow.copy(alpha = 0.8f), PurpleGlow.copy(alpha = 0.8f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Pipeline Title & Overall Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanGlow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PROCESSING PIPELINE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = CyanGlow,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stage ${status.stage.stepNumber} of 6",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = status.stage.label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                // Prominent Percentage
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${status.progressPercent}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = CyanGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Overall Progress Bar with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2438))
                    .testTag("pipeline_progress_bar")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(CyanGlow, PurpleGlow)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // VISUAL STATUS INDICATOR:
            // 'Uploading → Transcribing → Finding moments → Creating clips → Adding captions → Ready'
            Text(
                text = "PIPELINE PROGRESS STAGES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp),
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )

            // Horizontal Stepper Bar with Arrows and Live Loading Animations
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                        .testTag("pipeline_stepper_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PIPELINE_PROGRESS_STAGES.forEachIndexed { index, stage ->
                        val isPast = stage.stepNumber < status.stage.stepNumber
                        val isCurrent = stage == status.stage
                        val isFuture = stage.stepNumber > status.stage.stepNumber

                        // Stage Node Pill
                        StageNode(
                            stage = stage,
                            isPast = isPast,
                            isCurrent = isCurrent,
                            isFuture = isFuture,
                            pulseScale = pulseScale,
                            glowAlpha = glowAlpha,
                            spinnerRotation = spinnerRotation
                        )

                        // Arrow Connector (unless last stage)
                        if (index < PIPELINE_PROGRESS_STAGES.lastIndex) {
                            Spacer(modifier = Modifier.width(6.dp))
                            StageArrowConnector(
                                isPast = isPast,
                                isCurrentTransition = isPast && PIPELINE_PROGRESS_STAGES[index + 1] == status.stage,
                                glowAlpha = glowAlpha
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Active Dynamic Message Banner with Animated Spinner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Animated Pulse Ring Spinner
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(CyanGlow.copy(alpha = glowAlpha * 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(spinnerRotation),
                            strokeWidth = 2.5.dp,
                            color = CyanGlow,
                            trackColor = Color.Transparent
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = status.currentMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        if (status.uploadSubStep.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = status.uploadSubStep,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Upload Telemetry (if in upload stage)
            if (status.isUploadPhase && status.totalSizeMb > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                UploadTelemetryGrid(
                    uploadedMb = status.uploadProgressMb,
                    totalMb = status.totalSizeMb,
                    speedMbPerSec = status.uploadSpeedMbPerSec,
                    etaSec = status.etaSeconds
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Detailed Sequential Stage Breakdown
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PIPELINE_PROGRESS_STAGES.forEach { stage ->
                    val isPast = stage.stepNumber < status.stage.stepNumber
                    val isCurrent = stage == status.stage
                    val isFuture = stage.stepNumber > status.stage.stepNumber

                    StageDetailedRow(
                        stage = stage,
                        isPast = isPast,
                        isCurrent = isCurrent,
                        isFuture = isFuture,
                        pulseScale = pulseScale,
                        glowAlpha = glowAlpha,
                        spinnerRotation = spinnerRotation
                    )
                }
            }

            // Cancel Upload Option
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCancelUpload,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel_upload_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancel Processing & Abort", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Individual Node inside the Horizontal Pipeline Stepper.
 */
@Composable
private fun StageNode(
    stage: ProcessingStage,
    isPast: Boolean,
    isCurrent: Boolean,
    isFuture: Boolean,
    pulseScale: Float,
    glowAlpha: Float,
    spinnerRotation: Float
) {
    val nodeBackgroundColor by animateColorAsState(
        targetValue = when {
            isPast -> GreenSuccess
            isCurrent -> Color(0xFF0F2642)
            else -> Color(0xFF192033)
        },
        label = "nodeBg"
    )

    val borderColor = when {
        isPast -> GreenSuccess
        isCurrent -> CyanGlow
        else -> BorderDark
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isCurrent) CyanGlow.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .border(
                width = if (isCurrent) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("stage_indicator_${stage.name}")
    ) {
        // Icon / Spinner container
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(if (isCurrent) pulseScale else 1.0f)
                .clip(CircleShape)
                .background(nodeBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            when {
                isPast -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
                isCurrent -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(spinnerRotation),
                        strokeWidth = 2.5.dp,
                        color = CyanGlow,
                        trackColor = Color(0x3300F2FE)
                    )
                    Icon(
                        imageVector = getStageIcon(stage),
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(11.dp)
                    )
                }
                else -> {
                    Text(
                        text = "${stage.stepNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(7.dp))

        // Label
        Column {
            Text(
                text = stage.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isCurrent) FontWeight.Black else if (isPast) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isCurrent -> CyanGlow
                    isPast -> TextPrimary
                    else -> TextMuted
                },
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Animated Directional Arrow Connector (→) between Stages.
 */
@Composable
private fun StageArrowConnector(
    isPast: Boolean,
    isCurrentTransition: Boolean,
    glowAlpha: Float
) {
    val arrowTint = when {
        isCurrentTransition -> CyanGlow.copy(alpha = glowAlpha)
        isPast -> GreenSuccess
        else -> BorderDark
    }

    Box(
        modifier = Modifier.padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "→",
            tint = arrowTint,
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * Detailed Row for the Vertical Pipeline Checklist.
 */
@Composable
private fun StageDetailedRow(
    stage: ProcessingStage,
    isPast: Boolean,
    isCurrent: Boolean,
    isFuture: Boolean,
    pulseScale: Float,
    glowAlpha: Float,
    spinnerRotation: Float
) {
    val rowBorderColor = when {
        isCurrent -> CyanGlow.copy(alpha = 0.6f)
        isPast -> BorderDark
        else -> Color(0xFF1E2338)
    }

    val rowBgColor = when {
        isCurrent -> Color(0xFF112036)
        isPast -> SurfaceCard
        else -> SurfaceDark
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = rowBgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, rowBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stage_row_${stage.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stage Indicator Icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .scale(if (isCurrent) pulseScale else 1.0f)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPast -> GreenSuccess
                            isCurrent -> Color(0xFF003847)
                            else -> Color(0xFF1E2438)
                        }
                    )
                    .border(
                        1.dp,
                        if (isCurrent) CyanGlow else if (isPast) GreenSuccess else BorderDark,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isPast -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    isCurrent -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(spinnerRotation),
                            strokeWidth = 2.dp,
                            color = CyanGlow,
                            trackColor = Color.Transparent
                        )
                        Icon(
                            imageVector = getStageIcon(stage),
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = getStageIcon(stage),
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Stage Label & Description
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stage.stepNumber}. ${stage.label}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                        color = when {
                            isCurrent -> CyanGlow
                            isPast -> TextPrimary
                            else -> TextSecondary
                        }
                    )

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isPast -> GreenSuccess.copy(alpha = 0.15f)
                            isCurrent -> CyanGlow.copy(alpha = 0.2f)
                            else -> Color(0xFF1E2438)
                        }
                    ) {
                        Text(
                            text = when {
                                isPast -> "COMPLETED"
                                isCurrent -> "IN PROGRESS"
                                else -> "QUEUED"
                            },
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isPast -> GreenSuccess
                                isCurrent -> CyanGlow
                                else -> TextMuted
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stage.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) TextSecondary else TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Upload Telemetry Card showing Speed, Transferred Size, and ETA.
 */
@Composable
fun UploadTelemetryGrid(
    uploadedMb: Float,
    totalMb: Float,
    speedMbPerSec: Float,
    etaSec: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131D33),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Speed
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Speed", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                Text(
                    text = "${String.format("%.1f", speedMbPerSec)} MB/s",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Transferred Size
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = PurpleGlow, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Transferred", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                Text(
                    text = "${String.format("%.0f", uploadedMb)} / ${String.format("%.0f", totalMb)} MB",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // ETA
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Time Left", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                Text(
                    text = "${etaSec}s",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}
