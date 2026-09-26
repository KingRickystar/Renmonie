@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

// R.drawable.ic_renmonie for home brand mark

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.runtime.DisposableEffect
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.demowallet.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    var isOffline by rememberSaveable {
        mutableStateOf(!isRenMonieOnline(context))
    }

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
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeHeader(onNotifications = onNotifications)
            }

            if (isOffline) {
                item { OfflineStatusBanner(isOffline = true) }
            }

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

            item {
                FinancialOverview(transactions = transactions)
            }

            item {
                SectionHeader(
                    title = "Recent activity",
                    action = "View all",
                    onAction = onHistory
                )
            }

            if (transactions.isEmpty()) {
                item { EmptyTransactions() }
            } else {
                items(transactions.take(5), key = { it.id }) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        onClick = { onReceipt(transaction) }
                    )
                }
            }

            item { SecurityCard() }
        }
    }
},
    onSettings: () -> Unit = {},
    onAddMoney: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onReceipt: (Transaction) -> Unit
) {

    var balanceVisible by rememberSaveable {
        mutableStateOf(true)
    }

    val context = LocalContext.current

    var isOffline by rememberSaveable {
        mutableStateOf(!isRenMonieOnline(context))
    }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            item {
                HomeHeader(onNotifications = onNotifications)
            }

            item {
                OfflineStatusBanner(isOffline = isOffline)
            }

            item {
                BalanceCard(
                    balance = balance,
                    visible = balanceVisible,
                    onToggle = {
                        balanceVisible =
                            !balanceVisible
                    },
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

            item {
                FinancialOverview(
                    transactions = transactions
                )
            }

            item {
                SectionHeader(
                    title = "Recent transactions",
                    action = "See all",
                    onAction = onHistory
                )
            }

            if (transactions.isEmpty()) {

                item {
                    EmptyTransactions()
                }

            } else {

                items(
                    transactions.take(5),
                    key = {
                        it.id
                    }
                ) { transaction ->

                    TransactionCard(
                        transaction = transaction,
                        onClick = {
                            onReceipt(transaction)
                        }
                    )
                }
            }

            item {
                SecurityCard()
            }

            item {
                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )
            }
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
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = RenOrange.copy(alpha = 0.14f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = RenOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(9.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "You're offline",
                        color = RenText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "Your saved wallet data is still available.",
                        color = RenMuted,
                        fontSize = 11.sp
                    )
                }
                Text(
                    "OFFLINE",
                    color = RenOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
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
                Icons.Default.CloudDone,
                contentDescription = "Online",
                tint = RenGreen,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                "Online",
                color = RenGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HomeHeader(onNotifications: () -> Unit = {}) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = CircleShape,
            color = RenPurple.copy(alpha = 0.16f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_renmonie),
                    contentDescription = "RenMonie",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                color = RenMuted,
                fontSize = 12.sp
            )
            Text(
                text = "Patrick 👋",
                color = RenText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            modifier = Modifier
                .size(42.dp)
                .clickable(onClick = onNotifications),
            shape = CircleShape,
            color = RenCard
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = RenText,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 18.dp)
    ) {
        // Brand row: R logo + RenMonie
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon (same as launcher drawable)
            Image(
                painter = painterResource(id = R.drawable.ic_renmonie),
                contentDescription = "RenMonie",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "RenMonie",
                color = RenText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onNotifications) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = RenText
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "$greeting,",
            color = RenMuted,
            fontSize = 13.sp
        )
        Text(
            text = "Patrick 👋",
            color = RenText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun BalanceCard(
    balance: Long,
    visible: Boolean,
    onToggle: () -> Unit,
    onAddMoney: () -> Unit = {},
    onHistory: () -> Unit = {}
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            RenPurple,
                            RenViolet,
                            Color(0xFF4C2AC7)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.16f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(9.dp).size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL BALANCE",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "RenMonie Personal",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (visible) "Hide balance" else "Show balance",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (visible) naira(balance) else "₦ ••••••",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = RenGreen.copy(alpha = 0.20f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFB8FFD2),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Account active",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "•  Personal account",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.14f))

            Spacer(modifier = Modifier.height(15.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACCOUNT",
                        color = Color.White.copy(alpha = 0.60f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "8094 •••• 4821",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString("809448214821"))
                        Toast.makeText(context, "Account number copied", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy account",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(13.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onAddMoney,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f)
                    ),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add money", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onHistory,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("History", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
},
    onHistory: () -> Unit = {}
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RenPurple)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Available Balance",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onToggle) {
                    Text(
                        text = if (visible) "HIDE" else "SHOW",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (visible) naira(balance) else "₦ ••••••",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RenMonie MFB",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "8094 •••• 4821",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy account",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    clipboard.setText(AnnotatedString("809448214821"))
                                    Toast.makeText(context, "Account number copied", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Active",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddMoney,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.22f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add Money", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onHistory,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.22f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("History", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun QuickActions(
    onTransfer: () -> Unit,
    onAirtime: () -> Unit,
    onData: () -> Unit,
    onUssd: () -> Unit,
    onMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Quick services",
            color = RenText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickAction(
                icon = Icons.Default.Send,
                title = "Transfer",
                accent = RenPurple,
                onClick = onTransfer,
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                icon = Icons.Default.Phone,
                title = "Airtime",
                accent = RenGreen,
                onClick = onAirtime,
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                icon = Icons.Default.DataUsage,
                title = "Data",
                accent = RenBlue,
                onClick = onData,
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                icon = Icons.Default.MoreHoriz,
                title = "More",
                accent = RenOrange,
                onClick = onMore,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUssd),
            shape = RoundedCornerShape(15.dp),
            color = RenCard
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = RenViolet.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = "USSD",
                            tint = RenViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("USSD banking", color = RenText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Access essential services quickly", color = RenMuted, fontSize = 10.sp)
                }
                Icon(Icons.Default.ChevronRight, null, tint = RenMuted)
            }
        }
    }
}

@Composable
fun QuickAction(
    icon: ImageVector,
    title: String,
    accent: Color = RenPurple,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(94.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = RenCard
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(43.dp),
                shape = RoundedCornerShape(13.dp),
                color = accent.copy(alpha = 0.13f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = title,
                color = RenText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FinancialOverview(
    transactions: List<Transaction>
) {

    val sent =
        transactions
            .filter {
                !it.isCredit
            }
            .sumOf {
                it.amount
            }

    val received =
        transactions
            .filter {
                it.isCredit
            }
            .sumOf {
                it.amount
            }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape =
            RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = RenCard
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = "Financial overview",
                color = RenText,
                fontWeight =
                    FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OverviewItem(
                    modifier =
                        Modifier.weight(1f),
                    icon =
                        Icons.Default.ArrowUpward,
                    title = "Sent",
                    amount = sent,
                    iconColor = RenRed
                )

                OverviewItem(
                    modifier =
                        Modifier.weight(1f),
                    icon =
                        Icons.Default.ArrowDownward,
                    title = "Received",
                    amount = received,
                    iconColor = RenGreen
                )

                OverviewItem(
                    modifier =
                        Modifier.weight(1f),
                    icon =
                        Icons.Default.History,
                    title = "Activity",
                    amount =
                        transactions.size.toLong(),
                    iconColor = RenPurple,
                    countMode = true
                )
            }
        }
    }
}

@Composable
fun OverviewItem(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    amount: Long,
    iconColor: Color,
    countMode: Boolean = false
) {

    Column(
        modifier = modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier =
                Modifier.size(22.dp)
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(
            text = title,
            color = RenMuted,
            fontSize = 11.sp
        )

        Text(
            text =
                if (countMode) {
                    amount.toString()
                } else {
                    naira(amount)
                },
            color = RenText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = RenText,
            fontSize = 17.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.weight(1f)
        )

        TextButton(
            onClick = onAction
        ) {

            Text(
                text = action,
                color = RenPurple
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = RenCard)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (transaction.isCredit) RenGreen.copy(alpha = 0.14f) else RenPurple.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (transaction.isCredit) RenGreen else RenPurple
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(transaction.recipient.ifBlank { transaction.type }, color = RenText, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(transaction.bank, color = RenMuted, fontSize = 11.sp)
                Text(transaction.date, color = RenMuted, fontSize = 10.sp)
                StatusBadge(transaction.status)
            }
            Text((if (transaction.isCredit) "+" else "-") + naira(transaction.amount), color = if (transaction.isCredit) RenGreen else RenText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun StatusBadge(
    status: String
) {

    val statusColor =
        when (status) {

            STATUS_PENDING ->
                RenOrange

            STATUS_FAILED ->
                RenRed

            else ->
                RenGreen
        }

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector =
                when (status) {

                    STATUS_PENDING ->
                        Icons.Default.Pending

                    STATUS_FAILED ->
                        Icons.Default.Error

                    else ->
                        Icons.Default.CheckCircle
                },
            contentDescription = null,
            tint = statusColor,
            modifier =
                Modifier.size(12.dp)
        )

        Spacer(
            modifier =
                Modifier.width(3.dp)
        )

        Text(
            text = status,
            color = statusColor,
            fontSize = 9.sp,
            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
fun EmptyTransactions() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = RenCard
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    Icons.Default.History,
                contentDescription = null,
                tint = RenMuted,
                modifier =
                    Modifier.size(38.dp)
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text = "No transactions yet",
                color = RenText,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Your wallet activity will appear here.",
                color = RenMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SecurityCard() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = RenCard
            )
    ) {

        Row(
            modifier =
                Modifier.padding(17.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    Icons.Default.Security,
                contentDescription = null,
                tint = RenGreen,
                modifier =
                    Modifier.size(27.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column {

                Text(
                    text =
                        "Your wallet is protected",
                    color = RenText,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Keep your PIN and account information private.",
                    color = RenMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun RenBottomBar(
    selected: String,
    onHome: () -> Unit,
    onCard: () -> Unit,
    onServices: () -> Unit,
    onSettings: () -> Unit
) {

    NavigationBar(
        containerColor = RenCard
    ) {

        NavigationBarItem(
            selected = selected == SCREEN_HOME,
            onClick = onHome,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = selected == SCREEN_CARDS,
            onClick = onCard,
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Card"
                )
            },
            label = { Text("Card") }
        )

        NavigationBarItem(
            selected = selected == SCREEN_MORE || selected == "services",
            onClick = onServices,
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Services"
                )
            },
            label = { Text("Services") }
        )

        NavigationBarItem(
            selected = selected == SCREEN_SECURITY || selected == SCREEN_THEME || selected == SCREEN_ACCOUNT,
            onClick = onSettings,
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") }
        )
    }
}

