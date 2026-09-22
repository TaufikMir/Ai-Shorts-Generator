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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AppScreen(val label: String, val iconFilled: ImageVector, val iconOutlined: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Home, Icons.Outlined.Home),
    CREATE("Create", Icons.Filled.AddCircle, Icons.Filled.AddCircle),
    PROJECTS("Projects", Icons.Filled.Folder, Icons.Outlined.Folder),
    SHORTS("Shorts", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    BILLING("Billing", Icons.Filled.CreditCard, Icons.Outlined.CreditCard),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings);
}

@Composable
fun AppTopBar(
    currentCreditsUsed: Int,
    totalCredits: Int,
    planName: String,
    onNavigateToCreate: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onToggleLandingPage: () -> Unit,
    onToggleAdmin: () -> Unit,
    isAdminVisible: Boolean,
    isLandingVisible: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceDark,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleLandingPage() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CyanGlow, PurpleGlow)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SlowMotionVideo,
                            contentDescription = "AI Shorts Factory logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Shorts Factory",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x3300F2FE)
                            ) {
                                Text(
                                    text = "PRO",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanGlow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Long-form to Vertical Shorts",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Actions: Landing, Admin, and User Profile
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleLandingPage,
                        modifier = Modifier.size(34.dp).testTag("top_bar_landing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Landing Page View",
                            tint = if (isLandingVisible) CyanGlow else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleAdmin,
                        modifier = Modifier.size(34.dp).testTag("top_bar_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Dashboard",
                            tint = if (isAdminVisible) PinkAccent else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-row: Credits & Primary "Create Short" CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Credits Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.clickable { onNavigateToBilling() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Credits",
                            tint = CyanGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$currentCreditsUsed / $totalCredits Shorts used",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Primary CTA: CREATE SHORT
                Button(
                    onClick = onNavigateToCreate,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanGlow,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("create_short_cta_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create Short",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        AppScreen.values().forEach { screen ->
            val isSelected = currentScreen == screen
            val isCreateTab = screen == AppScreen.CREATE

            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    if (isCreateTab) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CyanGlow, PurpleGlow)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Create Short",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) screen.iconFilled else screen.iconOutlined,
                            contentDescription = screen.label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyanGlow,
                    selectedTextColor = CyanGlow,
                    indicatorColor = Color(0x3300F2FE),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
