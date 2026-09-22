package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ContentCategory
import com.example.data.model.PublishPlatform
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingDialog(
    initialCategory: String = "PODCAST",
    initialPlatform: String = "MULTIPLE",
    onComplete: (category: String, platforms: String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedPlatform by remember { mutableStateOf(initialPlatform) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("onboarding_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CyanGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Personalization",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x3300F2FE)
                    ) {
                        Text(
                            text = "Step $step of 2",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanGlow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (step == 1) {
                    Text(
                        text = "What type of content do you create?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "We tune hook detection and viral markers to your genre:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ContentCategory.values().size) { index ->
                            val cat = ContentCategory.values()[index]
                            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF1E2640) else SurfaceCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategory = cat.name }
                                    .testTag("onboard_cat_${cat.name.lowercase()}"),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) CyanGlow else BorderDark
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cat.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CyanGlow)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
                    ) {
                        Text("Next Step →", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Where do you publish most?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "We adapt caption heights to avoid being blocked by feed UI overlays:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PublishPlatform.values().forEach { plat ->
                            val isSelected = selectedPlatform.equals(plat.name, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF1E2640) else SurfaceCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPlatform = plat.name }
                                    .testTag("onboard_plat_${plat.name.lowercase()}"),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) CyanGlow else BorderDark
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = plat.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CyanGlow)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { step = 1 },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = TextPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = { onComplete(selectedCategory, selectedPlatform) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black),
                            modifier = Modifier.weight(2f).testTag("onboard_finish_button")
                        ) {
                            Text("Finish Setup ⚡", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
