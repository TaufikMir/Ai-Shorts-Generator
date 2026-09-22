package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun LandingPageScreen(
    onCloseLanding: () -> Unit,
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Sticky Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(CyanGlow, PurpleGlow))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Shorts Factory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onGetStarted,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp).testTag("landing_get_started_header_button")
                    ) {
                        Text("Open App", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onCloseLanding, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x3300F2FE),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✨", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next-Gen AI Shorts Creation Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Turn Long Videos Into Shorts Automatically",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Upload 1 video → AI finds the best moments → creates multiple vertical Shorts → adds captions, hooks & titles → 1-click export.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("landing_primary_cta_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Your First Short (Free)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // How It Works Steps
            item {
                Text(
                    text = "How It Works (5 Seamless Steps)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "From raw podcast or webinar file to viral TikTok, Reel & Short:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            val steps = listOf(
                Triple("1. Upload Long Video", "Drag & drop MP4, MOV, or WebM video up to 2 hours in duration.", Icons.Default.Upload),
                Triple("2. AI Finds Best Moments", "Scans audio transcript for high curiosity hooks, humor, drama & key lessons.", Icons.Default.AutoAwesome),
                Triple("3. Smart 9:16 Vertical Crop", "Automatically tracks faces and centers active speakers in vertical frame.", Icons.Default.Crop),
                Triple("4. Dynamic Synced Captions", "Word-by-word highlighted text in 6 viral styles with emoji accents.", Icons.Default.Subtitles),
                Triple("5. 1-Click 1080p Export", "Download ready-to-publish MP4 with generated high-CTR titles & hashtags.", Icons.Default.Download)
            )

            items(steps.size) { index ->
                val (title, desc, icon) = steps[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x3300F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // Features Checklist Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Brush.linearGradient(listOf(CyanGlow, PurpleGlow)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Everything You Need to Scale Views", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        val perks = listOf(
                            "AI Hook Generator: 3 viral options per clip",
                            "6 Caption Styles: Clean, Bold, Podcast, Minimal, Gaming & Viral-Style",
                            "SEO Suite: 5 high-CTR titles, hashtags, keywords & pinned comment",
                            "Thumbnail Suite: Frame grabber & bold text overlays",
                            "Credit Protection: 100% automatic refund if any render fails",
                            "Multi-Platform Ready: YouTube Shorts, Instagram Reels & TikTok"
                        )
                        perks.forEach { perk ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(perk, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Bottom CTA
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
                    ) {
                        Text("Start Creating Shorts Now →", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
