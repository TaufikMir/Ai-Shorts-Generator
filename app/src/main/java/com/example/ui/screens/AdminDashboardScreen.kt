package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProcessingJobEntity
import com.example.ui.theme.BgDark
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    jobs: List<ProcessingJobEntity>,
    onCloseAdmin: () -> Unit,
    onAddCredits: (Int) -> Unit
) {
    val context = LocalContext.current

    // Sample admin metrics as specified in prompt
    val totalUsers = 1420
    val activeUsers = 840
    val paidUsers = 312
    val monthlyRevenueInr = "₹2,49,288"
    val videosUploaded = 4820
    val shortsGenerated = 18450
    val processingFailures = 14
    val storageUsage = "1.84 TB / 10 TB"
    val aiUsageTokens = "42.8M Tokens"

    val sampleJobs = listOf(
        ProcessingJobEntity(
            id = 101,
            projectId = 1,
            jobType = "ANALYZE_VIDEO",
            status = "COMPLETED",
            progress = 100,
            errorMessage = null,
            createdAt = System.currentTimeMillis() - 3600000L * 4
        ),
        ProcessingJobEntity(
            id = 102,
            projectId = 2,
            jobType = "RENDER_SHORT_9_16",
            status = "COMPLETED",
            progress = 100,
            errorMessage = null,
            createdAt = System.currentTimeMillis() - 3600000L * 2
        ),
        ProcessingJobEntity(
            id = 103,
            projectId = 3,
            jobType = "GENERATE_CAPTIONS",
            status = "FAILED",
            progress = 45,
            errorMessage = "Upstream socket timeout during audio chunking (Auto-refunded)",
            createdAt = System.currentTimeMillis() - 1800000L
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseAdmin) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Admin", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Ops Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = PinkAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x33FF2A6D)
                        ) {
                            Text(
                                text = "SUPERADMIN",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PinkAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Real-time SaaS Telemetry, Job Queue & Infrastructure",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High Level SaaS Financials & Users
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Total Users",
                        value = "$totalUsers",
                        subtext = "$activeUsers active",
                        icon = Icons.Default.People,
                        tint = CyanGlow
                    )
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Paid Users",
                        value = "$paidUsers",
                        subtext = "SaaS subscribers",
                        icon = Icons.Default.AttachMoney,
                        tint = GreenSuccess
                    )
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Monthly ARR",
                        value = monthlyRevenueInr,
                        subtext = "Gross billings",
                        icon = Icons.Default.AttachMoney,
                        tint = PinkAccent
                    )
                }
            }

            // Infrastructure Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Uploaded Videos",
                        value = "$videosUploaded",
                        subtext = "Source files",
                        icon = Icons.Default.Cloud,
                        tint = PurpleGlow
                    )
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Shorts Created",
                        value = "$shortsGenerated",
                        subtext = "Rendered 9:16",
                        icon = Icons.Default.VideoLibrary,
                        tint = CyanGlow
                    )
                    MetricMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Failures",
                        value = "$processingFailures",
                        subtext = "0.08% error rate",
                        icon = Icons.Default.ErrorOutline,
                        tint = if (processingFailures > 0) Color(0xFFF59E0B) else GreenSuccess
                    )
                }
            }

            // Cloud Resource Usage Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cloud Compute & AI Usage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Object Storage (S3 / Cloud Storage):", color = TextSecondary, fontSize = 12.sp)
                            Text(storageUsage, color = CyanGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gemini 3.5 Flash Token Consumption:", color = TextSecondary, fontSize = 12.sp)
                            Text(aiUsageTokens, color = PurpleGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Video Render Workers (FFmpeg Cluster):", color = TextSecondary, fontSize = 12.sp)
                            Text("8/8 Active Nodes • Healthy", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Quick Admin Action: Grant Credits to Test User
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Admin Credit Tools",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Directly adjust user allowance or grant promotional credits:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    onAddCredits(25)
                                    Toast.makeText(context, "+25 Credits Granted", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = CyanGlow),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+25 Credits", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    onAddCredits(100)
                                    Toast.makeText(context, "+100 Credits Granted", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+100 Credits", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Job Queue & Tasks
            item {
                Text(
                    text = "Background Job Queue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(sampleJobs) { job ->
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
                            Text(
                                text = "Job #${job.id}: ${job.jobType}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (job.status) {
                                    "COMPLETED" -> Color(0x3310B981)
                                    "FAILED" -> Color(0x33EF4444)
                                    else -> Color(0x33F59E0B)
                                }
                            ) {
                                Text(
                                    text = job.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (job.status) {
                                        "COMPLETED" -> GreenSuccess
                                        "FAILED" -> RedError
                                        else -> Color(0xFFF59E0B)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        if (job.errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Error: ${job.errorMessage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = RedError,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Retrying Job #${job.id} on priority node...", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = CyanGlow),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry Failed Job", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
