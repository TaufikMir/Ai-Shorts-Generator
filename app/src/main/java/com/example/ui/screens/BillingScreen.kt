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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CreditTransactionEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.PricingPlan
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
fun BillingScreen(
    userAccount: UserAccountEntity?,
    transactions: List<CreditTransactionEntity>,
    onUpgradePlan: (PricingPlan) -> Unit
) {
    val context = LocalContext.current
    var planToUpgrade by remember { mutableStateOf<PricingPlan?>(null) }

    val currentPlanId = userAccount?.planId ?: "creator"
    val creditsUsed = userAccount?.creditsUsed ?: 37
    val creditsTotal = userAccount?.creditsTotal ?: 100
    val progress = if (creditsTotal > 0) creditsUsed.toFloat() / creditsTotal.toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Subscription & Credits",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Manage your monthly quota, credit consumption, and subscription tier.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Current Usage Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("billing_usage_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, Brush.linearGradient(listOf(CyanGlow, PurpleGlow)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Allowance",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${currentPlanId.uppercase()} PLAN",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x3300F2FE)
                        ) {
                            Text(
                                text = "${(creditsTotal - creditsUsed).coerceAtLeast(0)} Credits Available",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$creditsUsed / $creditsTotal Shorts used this month",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanGlow
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = CyanGlow,
                        trackColor = Color(0xFF232A42)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Credits are idempotent & automatically refunded if rendering encounters an error.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Subscription Plans Section Header
        item {
            Text(
                text = "Available Subscription Plans",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Choose the plan that fits your video publishing frequency.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Plans list
        items(PricingPlan.values()) { plan ->
            val isCurrent = currentPlanId.equals(plan.id, ignoreCase = true)
            PlanCard(
                plan = plan,
                isCurrent = isCurrent,
                onSelectPlan = {
                    if (!isCurrent) planToUpgrade = plan
                }
            )
        }

        // Transaction History Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Credit Activity Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No credit transactions recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        } else {
            items(transactions) { tx ->
                CreditTransactionRow(tx = tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Upgrade Confirmation Dialog
    if (planToUpgrade != null) {
        val plan = planToUpgrade!!
        AlertDialog(
            onDismissRequest = { planToUpgrade = null },
            title = {
                Text("Confirm Upgrade to ${plan.name}")
            },
            text = {
                Column {
                    Text(
                        text = "Upgrade to ${plan.name} at ₹${plan.priceInr}/month?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your allowance will immediately increase to ${plan.monthlyShorts} Shorts per month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpgradePlan(plan)
                        planToUpgrade = null
                        Toast.makeText(context, "Upgraded to ${plan.name}!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
                ) {
                    Text("Confirm Upgrade", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { planToUpgrade = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun PlanCard(
    plan: PricingPlan,
    isCurrent: Boolean,
    onSelectPlan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_card_${plan.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.isPopular) Color(0xFF161B2E) else SurfaceCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (plan.isPopular) 2.dp else 1.dp,
            if (plan.isPopular) CyanGlow else if (isCurrent) GreenSuccess else BorderDark
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    if (plan.isPopular) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PinkAccent
                        ) {
                            Text(
                                text = "POPULAR",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                if (isCurrent) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x3310B981)
                    ) {
                        Text(
                            text = "CURRENT PLAN",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${plan.priceInr}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = " / month",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = "${plan.monthlyShorts} Shorts per month",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = CyanGlow
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                plan.features.forEach { feat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feat,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSelectPlan,
                enabled = !isCurrent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (plan.isPopular) CyanGlow else SurfaceElevated,
                    contentColor = if (plan.isPopular) Color.Black else TextPrimary,
                    disabledContainerColor = Color(0x3310B981),
                    disabledContentColor = GreenSuccess
                )
            ) {
                Text(
                    text = if (isCurrent) "Current Plan" else if (plan.priceInr == 0) "Downgrade to Free" else "Upgrade to ${plan.name}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CreditTransactionRow(tx: CreditTransactionEntity) {
    val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
    val isPositive = tx.amount > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr • Balance after: ${tx.balanceAfter} credits",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isPositive) Color(0x3310B981) else Color(0x33EF4444)
            ) {
                Text(
                    text = if (isPositive) "+${tx.amount}" else "${tx.amount}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isPositive) GreenSuccess else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
