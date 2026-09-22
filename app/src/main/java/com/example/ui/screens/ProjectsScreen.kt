package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectEntity
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    onCreateNewProject: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onDeleteProject: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Projects",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${projects.size} active source video projects",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onCreateNewProject,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black),
                    modifier = Modifier.testTag("create_new_project_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (projects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No projects yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Click 'New Project' to upload your first video", color = TextSecondary)
                    }
                }
            }
        } else {
            items(projects) { project ->
                ProjectDetailedCard(
                    project = project,
                    onOpen = { onOpenProject(project.id) },
                    onDelete = { onDeleteProject(project.id) }
                )
            }
        }
    }
}

@Composable
fun ProjectDetailedCard(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(project.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("project_detailed_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(android.graphics.Color.parseColor(project.thumbnailColorHex))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Project icon",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = project.sourceFileName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x3310B981)
                ) {
                    Text(
                        text = project.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Generated Shorts: ${project.shortsCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanGlow,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${project.sourceDurationSeconds / 60}m ${project.sourceDurationSeconds % 60}s • ${project.sourceFileSizeMb.toInt()} MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Uploaded $dateStr",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("delete_project_${project.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onOpen,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = CyanGlow),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.height(34.dp).testTag("open_project_${project.id}")
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanGlow)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Shorts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
