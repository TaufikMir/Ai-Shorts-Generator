package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.util.YouTubeImportHelper
import com.example.data.util.YouTubePreset
import com.example.data.util.YouTubeVideoDetails
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val YouTubeRed = Color(0xFFFF0000)

@Composable
fun YouTubeImportSection(
    onImportConfirmed: (YouTubeVideoDetails, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var urlInput by remember { mutableStateOf("") }
    var customTitle by remember { mutableStateOf("") }
    var resolvedDetails by remember { mutableStateOf<YouTubeVideoDetails?>(null) }
    var isResolving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun triggerResolve(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            resolvedDetails = null
            errorMessage = null
            return
        }

        if (!YouTubeImportHelper.isValidYouTubeUrl(trimmed)) {
            errorMessage = "Please enter a valid YouTube video URL (e.g. youtube.com/watch?v=... or youtu.be/...)"
            resolvedDetails = null
            return
        }

        errorMessage = null
        isResolving = true

        coroutineScope.launch {
            val result = YouTubeImportHelper.resolveVideoDetails(trimmed)
            isResolving = false
            result.onSuccess { details ->
                resolvedDetails = details
                errorMessage = null
                if (customTitle.isBlank()) {
                    customTitle = details.title
                }
            }.onFailure { err ->
                errorMessage = err.message ?: "Failed to resolve YouTube video details"
                resolvedDetails = null
            }
        }
    }

    // Auto trigger when URL changes if it looks like a complete link
    LaunchedEffect(urlInput) {
        if (urlInput.contains("youtu") && YouTubeImportHelper.isValidYouTubeUrl(urlInput)) {
            triggerResolve(urlInput)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Input Box Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("youtube_input_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "YouTube",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Import from YouTube Link",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Paste standard video, Shorts, or podcast link",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Paste from clipboard button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) {
                                    urlInput = text
                                    triggerResolve(text)
                                }
                            }
                            .testTag("paste_youtube_button"),
                        color = Color(0x3338BDF8),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = CyanGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // URL Text Field
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        if (it.isBlank()) {
                            resolvedDetails = null
                            errorMessage = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("youtube_url_input"),
                    placeholder = {
                        Text(
                            "https://www.youtube.com/watch?v=...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = if (resolvedDetails != null) YouTubeRed else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isResolving) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 4.dp),
                                    strokeWidth = 2.dp,
                                    color = YouTubeRed
                                )
                            } else if (urlInput.isNotBlank()) {
                                IconButton(onClick = {
                                    urlInput = ""
                                    resolvedDetails = null
                                    errorMessage = null
                                    customTitle = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    )
                )

                // Error Message
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = RedError,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = RedError
                        )
                    }
                }
            }
        }

        // Preview Card if video is resolved
        AnimatedVisibility(visible = resolvedDetails != null) {
            resolvedDetails?.let { details ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("youtube_preview_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Video Thumbnail with Overlays
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = details.thumbnailUrl,
                                    contentDescription = details.title,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Dark gradient vignette
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.3f),
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                )

                                // Stream Quality Badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(GreenSuccess)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "1080p HD • YouTube Stream",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Duration pill
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = details.formatDuration(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Details Body
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = YouTubeRed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = details.category.uppercase(),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = YouTubeRed
                                        )
                                    }

                                    Text(
                                        text = details.channelTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = details.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Telemetry row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    YouTubeStatChip(
                                        icon = Icons.Default.Timer,
                                        label = "Duration",
                                        value = details.formatDuration(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    YouTubeStatChip(
                                        icon = Icons.Default.Storage,
                                        label = "Est. Stream",
                                        value = "${String.format("%.0f", details.estimatedSizeMb)} MB",
                                        modifier = Modifier.weight(1f)
                                    )
                                    YouTubeStatChip(
                                        icon = Icons.Default.Speed,
                                        label = "Ingest",
                                        value = "Direct TLS",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Custom Project Title Input
                                OutlinedTextField(
                                    value = customTitle,
                                    onValueChange = { customTitle = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("youtube_project_title_input"),
                                    label = { Text("Project Title") },
                                    placeholder = { Text("Customize project title for shorts") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = YouTubeRed,
                                        unfocusedBorderColor = BorderDark,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = SurfaceDark,
                                        unfocusedContainerColor = SurfaceDark
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Confirm Import Button
                                Button(
                                    onClick = {
                                        onImportConfirmed(details, customTitle.ifBlank { null })
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("import_youtube_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = YouTubeRed,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Import Video & Generate Shorts",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Curated Trending YouTube Videos for 1-click testing
        Text(
            text = "Or Test With Popular YouTube Videos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Click any preset to import its direct stream immediately:",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            YouTubeImportHelper.CURATED_PRESETS.forEach { preset ->
                YouTubePresetCard(
                    preset = preset,
                    isSelected = resolvedDetails?.videoId == preset.videoId,
                    onClick = {
                        urlInput = preset.url
                        triggerResolve(preset.url)
                    }
                )
            }
        }
    }
}

@Composable
private fun YouTubeStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
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

@Composable
private fun YouTubePresetCard(
    preset: YouTubePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("youtube_preset_${preset.videoId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceElevated else SurfaceCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) YouTubeRed else BorderDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail preview
            Box(
                modifier = Modifier
                    .size(width = 88.dp, height = 54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = "https://img.youtube.com/vi/${preset.videoId}/hqdefault.jpg",
                    contentDescription = preset.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                // Red play icon overlay
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(YouTubeRed.copy(alpha = 0.9f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = preset.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${preset.durationSeconds / 60} mins",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = preset.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Channel: ${preset.channel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) YouTubeRed else Color(0x22FFFFFF)
            ) {
                Text(
                    text = if (isSelected) "Selected" else "Try",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else TextPrimary
                )
            }
        }
    }
}
