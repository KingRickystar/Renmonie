@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BankingPurple = Color(0xFF6C3BFF)
private val BankingViolet = Color(0xFF8B5CF6)
private val BankingDark = Color(0xFF101016)
private val BankingCard = Color(0xFF191923)
private val BankingCard2 = Color(0xFF22222E)
private val BankingText = Color(0xFFF7F7FA)
private val BankingMuted = Color(0xFFAAAAB8)
private val BankingGreen = Color(0xFF2ECC71)
private val BankingRed = Color(0xFFFF5C67)
private val BankingOrange = Color(0xFFFFA726)
private val BankingBlue = Color(0xFF42A5F5)

private fun bankingNaira(amount: Long): String {
    return "₦" + NumberFormat
        .getNumberInstance(Locale.US)
        .format(amount)
}

private data class BankingBeneficiary(
    val name: String,
    val accountNumber: String,
    val bank: String
)

private data class BankingStatementItem(
    val title: String,
    val amount: Long,
    val credit: Boolean,
    val status: String,
    val date: String
)

private enum class BankingPage {
    HOME,
    ACCOUNT,
    HISTORY,
    STATEMENT,
    BENEFICIARIES,
    SAVINGS,
    CARDS,
    STATISTICS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingScreen(
    context: Context,
    balance: Long,
    onBack: () -> Unit,
    startPage: String = "home"
) {
    var page by remember {
        mutableStateOf(
            when (startPage.lowercase()) {
                "cards" -> BankingPage.CARDS
                "savings" -> BankingPage.SAVINGS
                "beneficiaries" -> BankingPage.BENEFICIARIES
                else -> BankingPage.HOME
            }
        )
    }

    when (page) {
        BankingPage.HOME -> BankingHomeScreen(
            balance = balance,
            onBack = onBack,
            onAccount = { page = BankingPage.ACCOUNT },
            onHistory = { page = BankingPage.HISTORY },
            onStatement = { page = BankingPage.STATEMENT },
            onBeneficiaries = { page = BankingPage.BENEFICIARIES },
            onSavings = { page = BankingPage.SAVINGS },
            onCards = { page = BankingPage.CARDS },
            onStatistics = { page = BankingPage.STATISTICS }
        )

        BankingPage.ACCOUNT -> BankingAccountDetailsScreen(
            context = context,
            balance = balance,
            onBack = { page = BankingPage.HOME }
        )

        BankingPage.HISTORY -> BankingHistoryScreen(
            onBack = { page = BankingPage.HOME }
        )

        BankingPage.STATEMENT -> BankingStatementScreen(
            balance = balance,
            onBack = { page = BankingPage.HOME }
        )

        BankingPage.BENEFICIARIES -> BankingBeneficiariesScreen(
            onBack = { page = BankingPage.HOME }
        )

        BankingPage.SAVINGS -> BankingSavingsScreen(
            balance = balance,
            onBack = { page = BankingPage.HOME }
        )

        BankingPage.CARDS -> BankingCardsScreen(
            onBack = {
                if (startPage.lowercase() == "cards") onBack()
                else page = BankingPage.HOME
            }
        )

        BankingPage.STATISTICS -> BankingStatisticsScreen(
            balance = balance,
            onBack = { page = BankingPage.HOME }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingHomeScreen(
    balance: Long,
    onBack: () -> Unit,
    onAccount: () -> Unit,
    onHistory: () -> Unit,
    onStatement: () -> Unit,
    onBeneficiaries: () -> Unit,
    onSavings: () -> Unit,
    onCards: () -> Unit,
    onStatistics: () -> Unit
) {
    Scaffold(
        containerColor = BankingDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Banking",
                        color = BankingText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = BankingText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BankingDark
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BankingDark)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingPurple
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Available Balance",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            bankingNaira(balance),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "RenMonie MFB",
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            item {
                Text(
                    "Banking Services",
                    color = BankingText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                BankingMenuItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Account Details",
                    subtitle = "View your RenMonie account information",
                    onClick = onAccount
                )
            }

            item {
                BankingMenuItem(
                    icon = Icons.Default.Assessment,
                    title = "Transaction History",
                    subtitle = "View transfers, airtime and data transactions",
                    onClick = onHistory
                )
            }

            item {
                BankingMenuItem(
                    icon = Icons.Default.Description,
                    title = "Account Statement",
                    subtitle = "View your account statement",
                    onClick = onStatement
                )
            }

            item {
                BankingMenuItem(
                    icon = Icons.Default.Person,
                    title = "Beneficiaries",
                    subtitle = "Manage saved recipients",
                    onClick = onBeneficiaries
                )
            }

            item {
                BankingMenuItem(
                    icon = Icons.Default.Savings,
                    title = "Savings",
                    subtitle = "Save money and track your goals",
                    onClick = onSavings
                )
            }

            item {
                BankingMenuItem(
                    icon = Icons.Default.CreditCard,
                    title = "Cards",
                    subtitle = "Manage your RenMonie virtual card",
                    onClick = onCards
                )
            }

            item {
                BankingMenuItem(
                    icon = Icons.Default.PieChart,
                    title = "Financial Statistics",
                    subtitle = "Understand your income and spending",
                    onClick = onStatistics
                )
            }

            item {
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun BankingMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BankingCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        BankingPurple.copy(alpha = 0.18f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = BankingViolet
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    color = BankingText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    subtitle,
                    color = BankingMuted,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Default.MoreVert,
                contentDescription = null,
                tint = BankingMuted
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingAccountDetailsScreen(
    context: Context,
    balance: Long,
    onBack: () -> Unit
) {
    val accountNumber = "1024587391"
    val accountName = "Patrick Ugochukwu Onyekwere"

    Scaffold(
        containerColor = BankingDark,
        topBar = {
            BankingTopBar(
                title = "Account Details",
                onBack = onBack
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BankingDark)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingPurple
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Text(
                            "RenMonie MFB",
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            accountName,
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Savings Account",
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                BankingInfoCard(
                    title = "Account Number",
                    value = accountNumber,
                    actionText = "COPY",
                    onAction = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE)
                                    as ClipboardManager

                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                "Account Number",
                                accountNumber
                            )
                        )

                        Toast.makeText(
                            context,
                            "Account number copied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            item {
                BankingInfoCard(
                    title = "Account Name",
                    value = accountName
                )
            }

            item {
                BankingInfoCard(
                    title = "Bank",
                    value = "RenMonie Microfinance Bank"
                )
            }

            item {
                BankingInfoCard(
                    title = "Account Type",
                    value = "Savings"
                )
            }

            item {
                BankingInfoCard(
                    title = "Available Balance",
                    value = bankingNaira(balance)
                )
            }

            item {
                BankingInfoCard(
                    title = "Account Status",
                    value = "Active"
                )
            }
        }
    }
}

@Composable
private fun BankingInfoCard(
    title: String,
    value: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BankingCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    color = BankingMuted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    value,
                    color = BankingText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            if (actionText != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(
                        actionText,
                        color = BankingViolet,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingHistoryScreen(
    onBack: () -> Unit
) {
    val transactions = remember {
        listOf(
            BankingStatementItem(
                "Transfer to GTBank",
                25000,
                false,
                "Successful",
                "11 Sep 2026"
            ),
            BankingStatementItem(
                "Airtime Purchase",
                2000,
                false,
                "Successful",
                "10 Sep 2026"
            ),
            BankingStatementItem(
                "Money Received",
                50000,
                true,
                "Successful",
                "09 Sep 2026"
            ),
            BankingStatementItem(
                "Data Purchase",
                5000,
                false,
                "Successful",
                "08 Sep 2026"
            )
        )
    }

    var search by remember { mutableStateOf("") }

    val filtered = transactions.filter {
        it.title.contains(search, ignoreCase = true) ||
                it.status.contains(search, ignoreCase = true)
    }

    Scaffold(
        containerColor = BankingDark,
        topBar = {
            BankingTopBar(
                title = "Transaction History",
                onBack = onBack
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BankingDark)
                .padding(padding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = BankingMuted
                    )
                },
                placeholder = {
                    Text(
                        "Search transactions",
                        color = BankingMuted
                    )
                }
            )

            Spacer(Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { item ->
                    BankingTransactionRow(item)
                }
            }
        }
    }
}

@Composable
private fun BankingTransactionRow(
    item: BankingStatementItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BankingCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (item.credit)
                            BankingGreen.copy(alpha = 0.15f)
                        else
                            BankingRed.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.credit)
                        Icons.Default.ArrowDownward
                    else
                        Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (item.credit)
                        BankingGreen
                    else
                        BankingRed
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.title,
                    color = BankingText,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    "${item.date} • ${item.status}",
                    color = BankingMuted,
                    fontSize = 12.sp
                )
            }

            Text(
                (if (item.credit) "+" else "-") +
                        bankingNaira(item.amount),
                color = if (item.credit)
                    BankingGreen
                else
                    BankingText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingStatementScreen(
    balance: Long,
    onBack: () -> Unit
) {
    val items = remember {
        listOf(
            BankingStatementItem(
                "Money Received",
                50000,
                true,
                "Successful",
                "09 Sep 2026"
            ),
            BankingStatementItem(
                "Transfer",
                25000,
                false,
                "Successful",
                "10 Sep 2026"
            ),
            BankingStatementItem(
                "Airtime",
                2000,
                false,
                "Successful",
                "10 Sep 2026"
            ),
            BankingStatementItem(
                "Data",
                5000,
                false,
                "Successful",
                "08 Sep 2026"
            )
        )
    }

    val credits = items
        .filter { it.credit }
        .sumOf { it.amount }

    val debits = items
        .filter { !it.credit }
        .sumOf { it.amount }

    Scaffold(
        containerColor = BankingDark,
        topBar = {
            BankingTopBar(
                title = "Account Statement",
                onBack = onBack
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BankingDark)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingCard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            "September 2026 Statement",
                            color = BankingText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(Modifier.height(15.dp))

                        StatementValue(
                            "Opening Balance",
                            bankingNaira(balance + debits - credits)
                        )

                        StatementValue(
                            "Total Credits",
                            bankingNaira(credits),
                            BankingGreen
                        )

                        StatementValue(
                            "Total Debits",
                            bankingNaira(debits),
                            BankingRed
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = BankingMuted.copy(alpha = 0.2f)
                        )

                        StatementValue(
                            "Closing Balance",
                            bankingNaira(balance),
                            BankingText
                        )
                    }
                }
            }

            item {
                Text(
                    "Transactions",
                    color = BankingText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(items) {
                BankingTransactionRow(it)
            }
        }
    }
}

@Composable
private fun StatementValue(
    title: String,
    value: String,
    valueColor: Color = BankingText
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            color = BankingMuted
        )

        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingBeneficiariesScreen(
    onBack: () -> Unit
) {
    val beneficiaries = remember {
        mutableStateListOf(
            BankingBeneficiary(
                "John Doe",
                "0123456789",
                "GTBank"
            ),
            BankingBeneficiary(
                "Jane Smith",
                "0987654321",
                "Access Bank"
            )
        )
    }

    var showAdd by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BankingDark,
        topBar = {
            BankingTopBar(
                title = "Beneficiaries",
                onBack = onBack,
                action = {
                    IconButton(onClick = { showAdd = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = BankingText
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (beneficiaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No beneficiaries saved",
                    color = BankingMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BankingDark)
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    beneficiaries,
                    key = {
                        it.accountNumber
                    }
                ) { beneficiary ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BankingCard
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        BankingPurple.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = BankingViolet
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    beneficiary.name,
                                    color = BankingText,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    beneficiary.accountNumber,
                                    color = BankingMuted
                                )

                                Text(
                                    beneficiary.bank,
                                    color = BankingMuted,
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    beneficiaries.remove(beneficiary)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = BankingRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = {
                showAdd = false
            },
            title = {
                Text("Add Beneficiary")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Recipient name")
                        },
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = account,
                        onValueChange = {
                            if (it.length <= 10 &&
                                it.all { char -> char.isDigit() }
                            ) {
                                account = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Account number")
                        },
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bank,
                        onValueChange = { bank = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Bank")
                        },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (
                            name.isNotBlank() &&
                            account.length == 10 &&
                            bank.isNotBlank()
                        ) {
                            beneficiaries.add(
                                BankingBeneficiary(
                                    name,
                                    account,
                                    bank
                                )
                            )

                            name = ""
                            account = ""
                            bank = ""
                            showAdd = false
                        }
                    }
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdd = false
                    }
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingSavingsScreen(
    balance: Long,
    onBack: () -> Unit
) {
    var savings by remember { mutableStateOf(25000L) }
    var target by remember { mutableStateOf(100000L) }
    var showAdd by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }

    val progress =
        if (target > 0)
            (savings.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        else 0f

    Scaffold(
        containerColor = BankingDark,
        topBar = {
            BankingTopBar(
                title = "Savings",
                onBack = onBack
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BankingDark)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingPurple
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Text(
                            "Savings Balance",
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            bankingNaira(savings),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(18.dp))

                        Text(
                            "Goal: ${bankingNaira(target)}",
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(7.dp))

                        Text(
                            "${(progress * 100).toInt()}% completed",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        showAdd = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("Add to Savings")
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        if (savings >= 5000L) {
                            savings -= 5000L
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Withdraw ₦5,000")
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingCard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            "Savings Information",
                            color = BankingText,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Available wallet balance: ${bankingNaira(balance)}",
                            color = BankingMuted
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            "Keep saving to reach your target.",
                            color = BankingMuted
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = {
                showAdd = false
            },
            title = {
                Text("Add to Savings")
            },
            text = {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (
                            it.all { char -> char.isDigit() }
                        ) {
                            amountText = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Amount")
                    },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount =
                            amountText.toLongOrNull() ?: 0L

                        if (amount > 0 && amount <= balance) {
                            savings += amount
                            amountText = ""
                            showAdd = false
                        }
                    }
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdd = false
                    }
                ) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingCardsScreen(
    onBack: () -> Unit
) {
    var cardVisible by remember { mutableStateOf(false) }
    var cvvVisible by remember { mutableStateOf(false) }
    var frozen by remember { mutableStateOf(false) }

    val cardNumber = "5399 2048 7612 4821"
    val cvv = "482"

    Scaffold(
        containerColor = BankingDark,
        topBar = {
            BankingTopBar(
                title = "Cards",
                onBack = onBack
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BankingDark)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingPurple
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "RENMONIE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(Modifier.height(35.dp))

                        Text(
                            if (cardVisible)
                                cardNumber
                            else
                                "•••• •••• •••• 4821",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "VALID THRU",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )

                                Text(
                                    "09/30",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    "CARD HOLDER",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )

                                Text(
                                    "PATRICK ONYEKWERE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingCard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Card Number",
                                    color = BankingMuted
                                )

                                Text(
                                    if (cardVisible)
                                        cardNumber
                                    else
                                        "•••• •••• •••• 4821",
                                    color = BankingText,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    cardVisible = !cardVisible
                                }
                            ) {
                                Icon(
                                    if (cardVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = BankingViolet
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = BankingMuted.copy(alpha = 0.2f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "CVV",
                                    color = BankingMuted
                                )

                                Text(
                                    if (cvvVisible) cvv else "•••",
                                    color = BankingText,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    cvvVisible = !cvvVisible
                                }
                            ) {
                                Icon(
                                    if (cvvVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = BankingViolet
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        frozen = !frozen
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        if (frozen)
                            "Unfreeze Card"
                        else
                            "Freeze Card"
                    )
                }
            }

            item {
                Text(
                    if (frozen)
                        "Your virtual card is currently frozen."
                    else
                        "Your virtual card is active.",
                    color = if (frozen)
                        BankingRed
                    else
                        BankingGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingStatisticsScreen(
    balance: Long,
    onBack: () -> Unit
) {
    val income = 150000L
    val transfers = 55000L
    val airtime = 7000L
    val data = 12000L
    val totalSpending = transfers + airtime + data

    Scaffold(
        containerColor = BankingDark,
        topBar = {
            BankingTopBar(
                title = "Financial Statistics",
                onBack = onBack
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BankingDark)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                BankingStatCard(
                    title = "Total Income",
                    amount = income,
                    icon = Icons.Default.ArrowDownward,
                    iconColor = BankingGreen
                )
            }

            item {
                BankingStatCard(
                    title = "Total Spending",
                    amount = totalSpending,
                    icon = Icons.Default.ArrowUpward,
                    iconColor = BankingRed
                )
            }

            item {
                BankingStatCard(
                    title = "Transfers",
                    amount = transfers,
                    icon = Icons.Default.AccountBalance,
                    iconColor = BankingViolet
                )
            }

            item {
                BankingStatCard(
                    title = "Airtime",
                    amount = airtime,
                    icon = Icons.Default.Person,
                    iconColor = BankingOrange
                )
            }

            item {
                BankingStatCard(
                    title = "Data",
                    amount = data,
                    icon = Icons.Default.Search,
                    iconColor = BankingBlue
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingCard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            "Financial Overview",
                            color = BankingText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        FinancialBar(
                            title = "Transfers",
                            amount = transfers,
                            maximum = totalSpending,
                            color = BankingViolet
                        )

                        FinancialBar(
                            title = "Airtime",
                            amount = airtime,
                            maximum = totalSpending,
                            color = BankingOrange
                        )

                        FinancialBar(
                            title = "Data",
                            amount = data,
                            maximum = totalSpending,
                            color = BankingBlue
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BankingCard2
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            "Current Balance",
                            color = BankingMuted
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            bankingNaira(balance),
                            color = BankingText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BankingStatCard(
    title: String,
    amount: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BankingCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        iconColor.copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    title,
                    color = BankingMuted,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    bankingNaira(amount),
                    color = BankingText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
private fun FinancialBar(
    title: String,
    amount: Long,
    maximum: Long,
    color: Color
) {
    val progress =
        if (maximum > 0)
            (amount.toFloat() / maximum.toFloat()).coerceIn(0f, 1f)
        else 0f

    Column(
        modifier = Modifier.padding(vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                color = BankingMuted
            )

            Text(
                bankingNaira(amount),
                color = BankingText,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(5.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankingTopBar(
    title: String,
    onBack: () -> Unit,
    action: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                title,
                color = BankingText,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = BankingText
                )
            }
        },
        actions = {
            action?.invoke()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BankingDark
        )
    )
}