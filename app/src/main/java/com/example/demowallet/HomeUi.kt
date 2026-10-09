@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun HomeScreen(
    balance: Long,
    transactions: List<Transaction>,
    onTransfer: () -> Unit,
    onAirtime: () -> Unit,
    onData: () -> Unit,
    onUssd: () -> Unit,
    onHistory: () -> Unit,
    onMore: () -> Unit,
    onCard: () -> Unit = {},
    onSettings: () -> Unit = {},
    onAddMoney: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onReceipt: (Transaction) -> Unit
) {
    var balanceVisible by rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current
    var isOffline by rememberSaveable { mutableStateOf(!isRenMonieOnline(context)) }

    DisposableEffect(context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOffline = !isRenMonieOnline(context)
            }

            override fun onLost(network: Network) {
                isOffline = !isRenMonieOnline(context)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    Scaffold(
        containerColor = RenDark,
        bottomBar = {
            RenBottomBar(
                selected = SCREEN_HOME,
                onHome = {},
                onCard = onCard,
                onServices = onMore,
                onSettings = onSettings
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(RenDark)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { HomeHeader(onNotifications = onNotifications) }
            item { OfflineStatusBanner(isOffline = isOffline) }
            item {
                BalanceCard(
                    balance = balance,
                    visible = balanceVisible,
                    onToggle = { balanceVisible = !balanceVisible },
                    onAddMoney = onAddMoney,
                    onHistory = onHistory
                )
            }
            item {
                QuickActions(
                    onTransfer = onTransfer,
                    onAirtime = onAirtime,
                    onData = onData,
                    onUssd = onUssd,
                    onMore = onMore
                )
            }
            item { FinancialOverview(transactions = transactions) }
            item { MonthlyInsightCard(transactions = transactions) }
            item { UpcomingBillsStrip() }
            item { BudgetPotCard(context = context, balanceKobo = balance) }
            item { TrustBanner() }
            item {
                SectionHeader(
                    title = "Recent transactions",
                    action = "See all",
                    onAction = onHistory
                )
            }
            if (transactions.isEmpty()) {
                item { EmptyTransactions() }
            } else {
                items(
                    items = transactions.take(5),
                    key = { it.id }
                ) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        onClick = { onReceipt(transaction) }
                    )
                }
            }
            item { SecurityCard() }
            item { DeviceActivityCard() }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun OfflineStatusBanner(isOffline: Boolean) {
    if (isOffline) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = RenOrange.copy(alpha = 0.12f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RenOrange.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = RenOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You're offline",
                        color = RenText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Saved wallet data is still available",
                        color = RenMuted,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RenOrange.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OFFLINE",
                        color = RenOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Online",
                tint = RenGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Online \u00b7 Secure connection",
                color = RenGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
