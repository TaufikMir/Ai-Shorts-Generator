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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.local.ProjectEntity
import com.example.data.local.ShortEntity
import com.example.data.local.UserAccountEntity
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

@Composable
fun DashboardScreen(
    userAccount: UserAccountEntity?,
    projects: List<ProjectEntity>,
    shorts: List<ShortEntity>,
    onNavigateToCreate: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToShorts: () -> Unit,
    onOpenShortStudio: (Long) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onOpenExport: ((Long) -> Unit)? = null
) {
    val creditsUsed = userAccount?.creditsUsed ?: 37
    val creditsTotal = userAccount?.creditsTotal ?: 100
    val progress = if (creditsTotal > 0) creditsUsed.toFloat() / creditsTotal.toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Hero Welcome & Primary Action Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.linearGradient(listOf(CyanGlow.copy(alpha = 0.5f), PurpleGlow.copy(alpha = 0.3f)))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0x3300F2FE)
                        ) {
                            Text(
                                text = "✨ AI VIDEO WORKFLOW",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceCard
                        ) {
                            Text(
                                text = "${userAccount?.planId?.uppercase() ?: "CREATOR"} PLAN",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PurpleGlow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Turn Long Videos Into Viral Shorts",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Upload 1 video → AI finds best moments → creates 9:16 Shorts with hooks, viral captions & SEO tags.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToCreate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("create_new_short_hero_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanGlow,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create New Short",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Quick Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Projects",
                    value = "${projects.size}",
                    subtext = "Active videos",
                    icon = Icons.Default.Folder,
                    tint = CyanGlow
                )
                MetricMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Shorts Ready",
                    value = "${shorts.size}",
                    subtext = "9:16 generated",
                    icon = Icons.Default.VideoLibrary,
                    tint = PurpleGlow
                )
                MetricMiniCard(
                    modifier = Modifier.weight(1f),
                    label = "Credits Left",
                    value = "${(creditsTotal - creditsUsed).coerceAtLeast(0)}",
                    subtext = "Allowance",
                    icon = Icons.Default.AutoAwesome,
                    tint = GreenSuccess
                )
            }
        }

        // Usage Meter Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Credit Usage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$creditsUsed / $creditsTotal Shorts used",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanGlow
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CyanGlow,
                        trackColor = Color(0xFF232A42)
                    )
                }
            }
        }

        // High Retention Suggested Moments (Shorts carousel)
        if (shorts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suggested Shorts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "View All (${shorts.size}) →",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanGlow,
                        modifier = Modifier.clickable { onNavigateToShorts() }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(shorts.take(6)) { shortItem ->
                        ShortThumbnailCard(
                            short = shortItem,
                            onClick = { onOpenShortStudio(shortItem.id) },
                            onExport = if (onOpenExport != null) { { onOpenExport(shortItem.id) } } else null
                        )
                    }
                }
            }
        }

        // Recent Projects Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Projects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "All Projects →",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanGlow,
                    modifier = Modifier.clickable { onNavigateToProjects() }
                )
            }
        }

        if (projects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No projects yet. Click 'Create New Short' above!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(projects.take(4)) { project ->
                ProjectItemRow(
                    project = project,
                    onOpen = { onNavigateToProjects() },
                    onDelete = { onDeleteProject(project.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MetricMiniCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ShortThumbnailCard(
    short: ShortEntity,
    onClick: () -> Unit,
    onExport: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .testTag("short_card_${short.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // 9:16 Mock Thumbnail Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                        )
                    )
                    .border(1.dp, Color(0x3300F2FE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play preview",
                    tint = CyanGlow,
                    modifier = Modifier.size(32.dp)
                )

                // AI internal score badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "Score ${short.internalAiScore}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Yellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }

                // Duration badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${short.durationSeconds}s",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = short.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = short.captionStyle,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanGlow,
                    fontSize = 10.sp
                )

                if (onExport != null) {
                    IconButton(
                        onClick = onExport,
                        modifier = Modifier.size(24.dp).testTag("quick_export_icon_${short.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Kit",
                            tint = if (short.status == "EXPORTED") GreenSuccess else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectItemRow(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(project.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("project_row_${project.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(android.graphics.Color.parseColor(project.thumbnailColorHex))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Project folder",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${project.shortsCount} Shorts • ${project.sourceFileSizeMb.toInt()} MB • $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x3310B981)
                ) {
                    Text(
                        text = project.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete project",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
