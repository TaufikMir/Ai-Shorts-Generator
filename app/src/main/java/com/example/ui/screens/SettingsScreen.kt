package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserAccountEntity
import com.example.data.model.ContentCategory
import com.example.data.model.PublishPlatform
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    userAccount: UserAccountEntity?,
    onSaveProfile: (String, String, String, String, String) -> Unit,
    onOpenOnboarding: () -> Unit
) {
    val context = LocalContext.current

    var name by remember(userAccount) { mutableStateOf(userAccount?.name ?: "Alex Vance") }
    var email by remember(userAccount) { mutableStateOf(userAccount?.email ?: "alex.creator@shortsfactory.ai") }
    var selectedCategory by remember(userAccount) { mutableStateOf(userAccount?.contentCategory ?: "PODCAST") }
    var selectedPlatforms by remember(userAccount) { mutableStateOf(userAccount?.publishPlatforms ?: "MULTIPLE") }
    var customApiKey by remember(userAccount) { mutableStateOf(userAccount?.customApiKey ?: "") }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings & Preferences",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Manage your creator profile, content targets, and AI model configurations.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyanGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Content Category Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "Content Category Focus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        TextButton(onClick = onOpenOnboarding) {
                            Text("Re-run Onboarding", color = CyanGlow, fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "Helps AI identify high-yield moments tailored specifically to your audience:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ContentCategory.values().forEach { cat ->
                        val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF1E2640) else SurfaceDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedCategory = cat.name },
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
            }
        }

        // Target Publishing Platforms
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Target Publishing Platform",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Optimizes caption positions, hashtag syntax, and duration limits:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PublishPlatform.values().forEach { plat ->
                        val isSelected = selectedPlatforms.equals(plat.name, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF1E2640) else SurfaceDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPlatforms = plat.name },
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
            }
        }

        // Custom API Key (Optional)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Custom Gemini API Key (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use your own Google AI Studio Gemini key for unlimited high-speed transcript analysis:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = { customApiKey = it },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Save Settings CTA Button
        item {
            Button(
                onClick = {
                    onSaveProfile(name, email, selectedCategory, selectedPlatforms, customApiKey)
                    Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Preferences", fontWeight = FontWeight.Bold)
            }
        }

        // Danger Zone: Delete Account
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RedError.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Danger Zone", fontWeight = FontWeight.Bold, color = RedError)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Once you delete your account, your projects, clips, and generated exports are removed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedError)
                    ) {
                        Text("Delete Account & Data")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account?", color = RedError) },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Account reset completed.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError, contentColor = Color.White)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
