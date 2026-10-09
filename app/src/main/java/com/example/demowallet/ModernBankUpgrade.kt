@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Transfer fee model (simulator: free under N50k, else N10). */
fun renmonieTransferFeeNaira(amountNaira: Long): Long {
    return when {
        amountNaira <= 0L -> 0L
        amountNaira < 50_000L -> 0L
        else -> 10L
    }
}

/** Monthly spend / receive from local transactions (kobo amounts). */
data class MonthlyMoneySummary(
    val sentKobo: Long,
    val receivedKobo: Long,
    val transferCount: Int
)

fun summarizeThisMonth(transactions: List<Transaction>): MonthlyMoneySummary {
    var sent = 0L
    var received = 0L
    var count = 0
    transactions.forEach { tx ->
        if (tx.isCredit) received += tx.amount else {
            sent += tx.amount
            count += 1
        }
    }
    return MonthlyMoneySummary(sent, received, count)
}

@Composable
fun MonthlyInsightCard(
    transactions: List<Transaction>,
    onOpenInsights: () -> Unit = {}
) {
    val summary = remember(transactions) { summarizeThisMonth(transactions) }
    val total = (summary.sentKobo + summary.receivedKobo).coerceAtLeast(1L)
    val sentRatio = (summary.sentKobo.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onOpenInsights),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RenCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChart, null, tint = RenPurple, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("This month", color = RenText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text("Insights ›", color = RenPurple, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Spent", color = RenMuted, fontSize = 11.sp)
                    Text(naira(summary.sentKobo), color = RenText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Received", color = RenMuted, fontSize = 11.sp)
                    Text(naira(summary.receivedKobo), color = RenGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { sentRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = RenPurple,
                trackColor = RenCard2,
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${summary.transferCount} transfers this period",
                color = RenMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun UpcomingBillsStrip(onBills: () -> Unit = {}) {
    val bills = listOf(
        Triple("Electricity", "Due soon", Icons.Default.Bolt),
        Triple("Airtime", "Top up", Icons.Default.PhoneAndroid),
        Triple("Cable TV", "Optional", Icons.Default.Tv)
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Bills & top-ups", color = RenText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                "See all ›",
                color = RenPurple,
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onBills)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            bills.forEach { (title, subtitle, icon) ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onBills),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RenCard)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RenPurple.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = RenPurple, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(title, color = RenText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(subtitle, color = RenMuted, fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetPotCard(
    context: Context,
    balanceKobo: Long
) {
    val prefs = context.getSharedPreferences("renmonie_budget", Context.MODE_PRIVATE)
    var potKobo by remember {
        mutableStateOf(prefs.getLong("pot_kobo", 0L))
    }
    val goalKobo = 50_000_00L
    val progress = (potKobo.toFloat() / goalKobo.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RenCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Savings, null, tint = RenGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Savings pot", color = RenText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(naira(potKobo), color = RenText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Goal ${naira(goalKobo)}", color = RenMuted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = RenGreen,
                trackColor = RenCard2,
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(500L, 1000L, 2000L).forEach { nairaAmt ->
                    Card(
                        modifier = Modifier.clickable {
                            val add = nairaAmt * 100L
                            if (balanceKobo >= add) {
                                potKobo += add
                                prefs.edit().putLong("pot_kobo", potKobo).apply()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RenCard2)
                    ) {
                        Text(
                            "+₦$nairaAmt",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = RenPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Text(
                "Demo pot — does not move real money",
                color = RenMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun TransferFeePreview(amountNaira: Long) {
    if (amountNaira <= 0L) return
    val fee = renmonieTransferFeeNaira(amountNaira)
    val total = amountNaira + fee
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RenCard2)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Amount", color = RenMuted, fontSize = 12.sp)
                Text("₦%,d".format(amountNaira), color = RenText, fontSize = 12.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Fee", color = RenMuted, fontSize = 12.sp)
                Text(if (fee == 0L) "Free" else "₦%,d".format(fee), color = RenText, fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total debit", color = RenText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("₦%,d".format(total), color = RenPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DeviceActivityCard() {
    val rows = listOf(
        "This phone · Active now",
        "Last unlock · Just now",
        "App version · RenMonie simulator"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RenCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = RenPurple, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Device activity", color = RenText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(10.dp))
            rows.forEach { line ->
                Text(line, color = RenMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
    }
}

@Composable
fun TrustBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RenPurple.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Shield, null, tint = RenPurple, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Transfers are protected by your PIN. RenMonie is a UI simulator — no real bank ledger.",
                color = RenMuted,
                fontSize = 11.sp
            )
        }
    }
}
