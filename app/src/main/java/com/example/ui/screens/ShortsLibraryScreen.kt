package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ShortEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ShortsFilter(val label: String) {
    ALL("All"),
    PROCESSING("Processing"),
    READY("Ready"),
    EXPORTED("Exported"),
    FAVORITES("Favorites")
}

@Composable
fun ShortsLibraryScreen(
    shorts: List<ShortEntity>,
    onOpenShortStudio: (Long) -> Unit,
    onDeleteShort: (Long) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onExportQuick: (Long) -> Unit,
    onOpenExport: ((Long) -> Unit)? = null
) {
    var selectedFilter by remember { mutableStateOf(ShortsFilter.ALL) }

    val filteredShorts = remember(shorts, selectedFilter) {
        when (selectedFilter) {
            ShortsFilter.ALL -> shorts
            ShortsFilter.PROCESSING -> shorts.filter { it.status == "PROCESSING" }
            ShortsFilter.READY -> shorts.filter { it.status == "READY" }
            ShortsFilter.EXPORTED -> shorts.filter { it.status == "EXPORTED" }
            ShortsFilter.FAVORITES -> shorts.filter { it.isFavorite }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Shorts Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Manage, edit captions, generate SEO, and export your 9:16 vertical Shorts.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Filters row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ShortsFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    val count = when (filter) {
                        ShortsFilter.ALL -> shorts.size
                        ShortsFilter.PROCESSING -> shorts.count { it.status == "PROCESSING" }
                        ShortsFilter.READY -> shorts.count { it.status == "READY" }
                        ShortsFilter.EXPORTED -> shorts.count { it.status == "EXPORTED" }
                        ShortsFilter.FAVORITES -> shorts.count { it.isFavorite }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = "${filter.label} ($count)",
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
                        modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                    )
                }
            }
        }

        if (filteredShorts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Shorts found for '${selectedFilter.label}'",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        } else {
            items(filteredShorts) { short ->
                ShortLibraryItemCard(
                    short = short,
                    onOpenStudio = { onOpenShortStudio(short.id) },
                    onDelete = { onDeleteShort(short.id) },
                    onToggleFavorite = { onToggleFavorite(short.id, short.isFavorite) },
                    onExport = {
                        if (onOpenExport != null) {
                            onOpenExport(short.id)
                        } else {
                            onExportQuick(short.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ShortLibraryItemCard(
    short: ShortEntity,
    onOpenStudio: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExport: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(short.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenStudio() }
            .testTag("short_library_card_${short.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 9:16 Mini Preview Tile
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                            )
                        )
                        .border(1.dp, CyanGlow.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = CyanGlow,
                        modifier = Modifier.size(24.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xCC000000),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "${short.durationSeconds}s",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title, Project & Badges
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (short.status) {
                                "EXPORTED" -> Color(0x3310B981)
                                "PROCESSING" -> Color(0x33F59E0B)
                                else -> Color(0x333B82F6)
                            }
                        ) {
                            Text(
                                text = short.status,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = when (short.status) {
                                    "EXPORTED" -> GreenSuccess
                                    "PROCESSING" -> Color(0xFFF59E0B)
                                    else -> CyanGlow
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(28.dp).testTag("fav_button_${short.id}")
                        ) {
                            Icon(
                                imageVector = if (short.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (short.isFavorite) PinkAccent else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = short.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "From: ${short.projectTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Style: ${short.captionStyle} • Score ${short.internalAiScore} • $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Edit, Export, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp).testTag("delete_short_${short.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onExport,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (short.status == "EXPORTED") Color(0x3310B981) else SurfaceElevated,
                        contentColor = if (short.status == "EXPORTED") GreenSuccess else TextPrimary
                    ),
                    modifier = Modifier.height(34.dp).testTag("quick_export_${short.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (short.status == "EXPORTED") "Export Kit" else "Export MP4",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onOpenStudio,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanGlow,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(34.dp).testTag("edit_short_studio_${short.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit Studio",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
