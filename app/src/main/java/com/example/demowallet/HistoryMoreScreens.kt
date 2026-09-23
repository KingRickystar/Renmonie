@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

import android.content.Context
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
fun HistoryScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onReceipt: (Transaction) -> Unit
) {

    var search by rememberSaveable {
        mutableStateOf("")
    }

    var filter by rememberSaveable {
        mutableStateOf("All")
    }

    val filtered =
        transactions.filter { transaction ->

            val matchesSearch =
                search.isBlank() ||
                        transaction.recipient
                            .contains(
                                search,
                                ignoreCase = true
                            ) ||
                        transaction.type
                            .contains(
                                search,
                                ignoreCase = true
                            ) ||
                        transaction.bank
                            .contains(
                                search,
                                ignoreCase = true
                            ) ||
                        transaction.status
                            .contains(
                                search,
                                ignoreCase = true
                            )

            val matchesFilter =
                when (filter) {

                    "Money In" ->
                        transaction.isCredit

                    "Money Out" ->
                        !transaction.isCredit

                    "Pending" ->
                        transaction.status ==
                                STATUS_PENDING

                    "Failed" ->
                        transaction.status ==
                                STATUS_FAILED

                    else ->
                        true
                }

            matchesSearch &&
                    matchesFilter
        }

    Scaffold(
        containerColor = RenDark,
        topBar = {

            TopAppBar(
                title = {

                    Text(
                        text =
                            "Transaction History",
                        fontWeight =
                            FontWeight.Bold
                    )
                },
                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Back"
                        )
                    }
                },
                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor =
                                RenDark,
                            titleContentColor =
                                RenText,
                            navigationIconContentColor =
                                RenText
                        )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                placeholder = {
                    Text(
                        "Search transactions"
                    )
                },
                leadingIcon = {

                    Icon(
                        imageVector =
                            Icons.Default.Search,
                        contentDescription =
                            null
                    )
                },
                singleLine = true,
                shape =
                    RoundedCornerShape(14.dp),
                colors =
                    renTextFieldColors()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {

                listOf(
                    "All",
                    "Money In",
                    "Money Out",
                    "Pending"
                ).forEach { option ->

                    val active =
                        filter == option

                    OutlinedButton(
                        onClick = {
                            filter = option
                        },
                        modifier =
                            Modifier.weight(1f),
                        shape =
                            RoundedCornerShape(11.dp),
                        contentPadding =
                            androidx.compose.foundation
                                .layout
                                .PaddingValues(
                                    horizontal = 2.dp
                                ),
                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    containerColor =
                                        if (active) {
                                            RenPurple.copy(
                                                alpha = 0.18f
                                            )
                                        } else {
                                            RenCard
                                        },
                                    contentColor =
                                        if (active) {
                                            RenPurple
                                        } else {
                                            RenText
                                        }
                                )
                    ) {

                        Text(
                            text = option,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (filtered.isEmpty()) {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.History,
                            contentDescription =
                                null,
                            tint = RenMuted,
                            modifier =
                                Modifier.size(45.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(
                            text =
                                "No transactions found",
                            color = RenText,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        filtered,
                        key = {
                            it.id
                        }
                    ) { transaction ->

                        TransactionCard(
                            transaction =
                                transaction,
                            onClick = {
                                onReceipt(
                                    transaction
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoreScreen(
    balance: Long,
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onTransfer: () -> Unit,
    onAirtime: () -> Unit,
    onData: () -> Unit,
    onHistory: () -> Unit,
    onSecretControl: () -> Unit,
    onSecurity: () -> Unit,
    onBills: () -> Unit = {},
    onAccount: () -> Unit = {},
    onTheme: () -> Unit = {},
    onBeneficiaries: () -> Unit = {},
    onCards: () -> Unit = {},
    onSavings: () -> Unit = {},
    onInsights: () -> Unit = {},
    onBanking: () -> Unit = {},
    onUssd: () -> Unit = {},
    onAbout: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNotifications: () -> Unit = {}
) {

    /*
     * Seven taps on the profile card unlocks
     * the local transaction control screen.
     */
    var profileTaps by rememberSaveable {
        mutableStateOf(0)
    }

    Scaffold(
        containerColor = RenDark,
        topBar = {

            TopAppBar(
                title = {

                    Text(
                        text = "More",
                        fontWeight =
                            FontWeight.Bold
                    )
                },
                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Back"
                        )
                    }
                },
                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor =
                                RenDark,
                            titleContentColor =
                                RenText,
                            navigationIconContentColor =
                                RenText
                        )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            item {

                ProfileCard(
                    onTap = {

                        profileTaps++

                        if (profileTaps >= 7) {

                            profileTaps = 0

                            onSecretControl()
                        }
                    }
                )
            }

            item {

                MoreSectionTitle(
                    "Payments & Services"
                )
            }

            item {

                MoreMenuItem(
                    icon =
                        Icons.Default.Send,
                    title =
                        "Transfer Money",
                    subtitle =
                        "Send money from your wallet",
                    onClick = onTransfer
                )
            }

            item {

                MoreMenuItem(
                    icon =
                        Icons.Default.Phone,
                    title =
                        "Buy Airtime",
                    subtitle =
                        "Recharge any network",
                    onClick = onAirtime
                )
            }

            item {

                MoreMenuItem(
                    icon =
                        Icons.Default.DataUsage,
                    title =
                        "Buy Data",
                    subtitle =
                        "Purchase data bundles",
                    onClick = onData
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.AccountBalance,
                    title = "Bills & Services",
                    subtitle = "Electricity, TV, internet & more",
                    onClick = onBills
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Phone,
                    title = "USSD (*556#)",
                    subtitle = "Banking without internet",
                    onClick = onUssd
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.AccountBalance,
                    title = "Banking Hub",
                    subtitle = "Accounts, cards, savings & more",
                    onClick = onBanking
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Person,
                    title = "Beneficiaries",
                    subtitle = "Saved recipients for quick transfer",
                    onClick = onBeneficiaries
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Wallet,
                    title = "Cards",
                    subtitle = "Virtual & physical cards, freeze",
                    onClick = onCards
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.AccountBalance,
                    title = "Savings",
                    subtitle = "Goals & savings pots",
                    onClick = onSavings
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.History,
                    title = "Spending Insights",
                    subtitle = "See where your money goes",
                    onClick = onInsights
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Person,
                    title = "Account & Login",
                    subtitle = "Personal and account details",
                    onClick = onAccount
                )
            }

            item {

                MoreMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Theme & Appearance",
                    subtitle = "Choose your RenMonie colour",
                    onClick = onTheme
                )
            }

            item {

                MoreMenuItem(
                    icon =
                        Icons.Default.History,
                    title =
                        "Transaction History",
                    subtitle =
                        "${transactions.size} transaction(s)",
                    onClick = onHistory
                )
            }

            item {

                MoreSectionTitle(
                    "Account"
                )
            }

            item {

                MoreMenuItem(
                    icon =
                        Icons.Default.Person,
                    title = "Profile",
                    subtitle =
                        "Manage your personal details",
                    onClick = {}
                )
            }

            item {

                MoreMenuItem(
                    icon =
                        Icons.Default.Lock,
                    title = "Security",
                    subtitle =
                        "PIN and wallet security",
                    onClick = onSecurity
                )
            }

            item {

                MoreMenuItem(
                    icon =
                        Icons.Default.Settings,
                    title = "Settings",
                    subtitle =
                        "App preferences",
                    onClick = {}
                )
            }

            item {

                MoreMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Transaction and security alerts",
                    onClick = onNotifications
                )
            }

            item {
                MoreMenuItem(
                    icon =
                        Icons.Default.Info,
                    title =
                        "About RenMonie",
                    subtitle =
                        "Wallet information",
                    onClick = onAbout
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Log out",
                    subtitle = "Lock the app with your PIN",
                    onClick = onLogout
                )
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

