package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.util.UploadErrorType
import com.example.data.util.VideoUploadValidator
import com.example.data.util.VideoValidationResult
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private val AmberWarning = Color(0xFFF59E0B)

/**
 * User-friendly Alert Dialog for granular video upload errors:
 * - Specifically addresses File Size Limit Exceeded (2.0 GB limit)
 * - Specifically addresses Unsupported Format errors (rejects .avi, .mkv, etc., guides to MP4/MOV/WebM)
 * - Provides actionable recovery steps, format specifications, and one-tap re-pick.
 */
@Composable
fun UploadErrorAlertDialog(
    result: VideoValidationResult,
    onDismiss: () -> Unit,
    onPickAnother: () -> Unit,
    onShowFormatSpecs: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("upload_error_alert_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (result.isFileSizeError) AmberWarning else RedError
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header with themed icon and dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Themed Avatar Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (result.isFileSizeError) AmberWarning.copy(alpha = 0.2f)
                                    else RedError.copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    if (result.isFileSizeError) AmberWarning.copy(alpha = 0.5f)
                                    else RedError.copy(alpha = 0.5f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (result.errorType) {
                                    UploadErrorType.FILE_SIZE_LIMIT_EXCEEDED -> Icons.Default.Storage
                                    UploadErrorType.FILE_EMPTY -> Icons.Default.FolderOff
                                    UploadErrorType.UNSUPPORTED_FORMAT -> Icons.Default.Block
                                    UploadErrorType.DURATION_TOO_SHORT,
                                    UploadErrorType.DURATION_TOO_LONG -> Icons.Default.Timer
                                    else -> Icons.Default.Warning
                                },
                                contentDescription = null,
                                tint = if (result.isFileSizeError) AmberWarning else RedError,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (result.isFileSizeError) AmberWarning.copy(alpha = 0.15f)
                                        else RedError.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = when (result.errorType) {
                                        UploadErrorType.FILE_SIZE_LIMIT_EXCEEDED -> "FILE SIZE LIMIT EXCEEDED"
                                        UploadErrorType.UNSUPPORTED_FORMAT -> "UNSUPPORTED FORMAT"
                                        UploadErrorType.FILE_EMPTY -> "CORRUPT OR EMPTY FILE"
                                        UploadErrorType.DURATION_TOO_SHORT -> "DURATION TOO SHORT"
                                        UploadErrorType.DURATION_TOO_LONG -> "DURATION EXCEEDED"
                                        else -> "UPLOAD ERROR"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (result.isFileSizeError) AmberWarning else RedError,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.errorTitle ?: "Video Upload Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("dialog_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Error Description
                Text(
                    text = result.errorMessage ?: "The selected video could not be processed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Granular Breakdown Section:
                // Case A: File Size Exceeded -> Show Granular Size Comparison
                if (result.errorType == UploadErrorType.FILE_SIZE_LIMIT_EXCEEDED) {
                    FileSizeComparisonBox(result = result)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Case B: Unsupported Format -> Show Accepted vs Rejected Matrix
                if (result.errorType == UploadErrorType.UNSUPPORTED_FORMAT) {
                    FormatMatrixComparisonBox(result = result)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Technical Diagnostics
                result.technicalDetails?.let { technical ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF151C2C),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Technical Diagnostics",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanGlow
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = technical,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Actionable Resolution Tips
                if (result.resolutionTips.isNotEmpty()) {
                    Text(
                        text = "RECOMMENDED RESOLUTION STEPS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        result.resolutionTips.forEachIndexed { index, tip ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232D48))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (result.isFileSizeError) AmberWarning.copy(alpha = 0.2f)
                                                else CyanGlow.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (result.isFileSizeError) AmberWarning else CyanGlow,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onPickAnother()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (result.isFileSizeError) AmberWarning else RedError,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_pick_another_button")
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Another Video", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Text("Dismiss", color = TextPrimary)
                    }
                }
            }
        }
    }
}

/**
 * Granular visual size comparison component for File Size Limit Exceeded errors.
 */
@Composable
fun FileSizeComparisonBox(result: VideoValidationResult) {
    val maxMb = VideoUploadValidator.MAX_FILE_SIZE_MB
    val excessMb = result.sizeExcessMb

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E1711),
        border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("file_size_comparison_box")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "File Size Breakdown",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmberWarning
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberWarning.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+${String.format("%.0f", excessMb)} MB OVER LIMIT",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = AmberWarning,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two-column metric boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Current Size
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF291B15),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Your Video Size", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format("%.1f", result.fileSizeMb)} MB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = AmberWarning
                        )
                        Text(
                            text = "(${String.format("%.2f", result.fileSizeMb / 1024f)} GB)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Max Limit
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF131D33),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Maximum Allowed", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "2,048 MB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = GreenSuccess
                        )
                        Text(
                            text = "(2.0 GB Limit)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Overflow Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF232D48))
            ) {
                // Max limit portion
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(8.dp)
                        .background(GreenSuccess)
                )
                // Overflow red/amber portion
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(AmberWarning.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(8.dp)
                        .background(GreenSuccess)
                )
            }
        }
    }
}

/**
 * Granular format comparison matrix for Unsupported Format errors.
 */
@Composable
fun FormatMatrixComparisonBox(result: VideoValidationResult) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF241417),
        border = androidx.compose.foundation.BorderStroke(1.dp, RedError.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("format_matrix_comparison_box")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Rejected format row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(RedError),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Rejected Format Container", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = "${result.detectedFormat} (${result.fileName})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = RedError
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Supported Ingest Formats", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            // Supported format items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormatPillSupported("MP4", "H.264 / HEVC", Modifier.weight(1f))
                FormatPillSupported("MOV", "Apple ProRes", Modifier.weight(1f))
                FormatPillSupported("WebM", "VP9 / AV1", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FormatPillSupported(title: String, codec: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF13221C),
        border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
            }
            Text(codec, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        }
    }
}
