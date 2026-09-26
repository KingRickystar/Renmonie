package com.example.demowallet

/**
 * RenMonie entry point.
 *
 * Structure (readability):
 * - AppColors.kt / AppConstants.kt / Models.kt — shared types
 * - RenMonieStorage.kt / MoneyFormat.kt / WalletUtils.kt / DemoHistory.kt — data
 * - HomeUi.kt, TransferScreen.kt, AirtimeDataScreens.kt, HistoryMoreScreens.kt,
 *   AuthScreens.kt, BankingScreen.kt, UssdScreen.kt, ReceiptUi.kt,
 *   Phase23Extras.kt, RenMonieUpgradeScreens.kt — UI screens
 * - This file — Activity + theme + app state / navigation
 */

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        setContent {
            RenMonieTheme {
                RenMonieApp()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "RenMonie wallet notifications"
            }

            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun RenMonieTheme(
    content: @Composable () -> Unit
) {
    androidx.compose.material3.MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = RenPurple,
            secondary = RenViolet,
            background = RenDark,
            surface = RenCard,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = RenText,
            onSurface = RenText,
            error = RenRed
        ),
        content = content
    )
}

@Composable
fun RenMonieApp() {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        RenThemeStore.load(context)
    }

    /*
     * BALANCE IS NOW STORED INTERNALLY IN KOBO.
     *
     * Example:
     * ₦150,000.00 = 15,000,000 kobo
     * ₦150,000.50 = 15,000,050 kobo
     */

    var balanceKobo by rememberSaveable {
        mutableStateOf(
            RenMonieStorage.loadBalanceKobo(context)
        )
    }

    var transactions by remember {
        mutableStateOf(
            DemoHistory.ensureSeeded(context)
        )
    }

    LaunchedEffect(transactions) {
        val pending = transactions.filter { it.status == STATUS_PENDING && it.reference.isNotBlank() }
        if (pending.isNotEmpty()) {
            delay(12_000L)
            pending.forEach { transaction ->
                val result = TransferClient.status(context, transaction.reference)
                if (!result.ok || result.status.isBlank()) return@forEach
                val remote = result.status.uppercase()
                val newStatus = when (remote) {
                    "SUCCESS", "COMPLETED" -> STATUS_SUCCESSFUL
                    "FAILED", "EXPIRED", "REVERSED" -> STATUS_FAILED
                    else -> STATUS_PENDING
                }
                if (newStatus != transaction.status) {
                    transactions = transactions.map { current ->
                        if (current.id == transaction.id) current.copy(status = newStatus) else current
                    }
                    if (newStatus == STATUS_FAILED) {
                        balanceKobo += transaction.amount
                        showWalletNotification("Transfer failed", "The transfer to " + transaction.recipient + " was not completed. Your money was returned.")
                    } else if (newStatus == STATUS_SUCCESSFUL) {
                        showWalletNotification("Transfer successful", naira(transaction.amount) + " transfer to " + transaction.recipient + " is complete.")
                    }
                    saveWallet()
                }
            }
        }
    }

    var currentScreen by rememberSaveable {
        mutableStateOf(SCREEN_SPLASH)
    }

    var selectedReceipt by remember {
        mutableStateOf<ReceiptData?>(null)
    }

    var showSuccessAnimation by remember {
        mutableStateOf(false)
    }
    var successTitle by remember { mutableStateOf("Transfer successful") }
    var successSubtitle by remember { mutableStateOf("Your money is on its way") }
    var successAmountLabel by remember { mutableStateOf("") }
    var pendingReceipt by remember {
        mutableStateOf<ReceiptData?>(null)
    }

    var pinDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var pendingSecureAction by remember {
        mutableStateOf<(() -> Unit)?>(null)
    }


    fun showSuccessThenReceipt(
        receipt: ReceiptData,
        title: String = "Transfer successful",
        subtitle: String = "Your money is on its way",
        amountLabel: String = ""
    ) {
        pendingReceipt = receipt
        successTitle = title
        successSubtitle = subtitle
        successAmountLabel = amountLabel
        showSuccessAnimation = true
        currentScreen = SCREEN_HOME
    }

    fun runSecureAction(action: () -> Unit) {
        if (!RenMonieStorage.hasPin(context)) {
            Toast.makeText(
                context,
                "Create a transaction PIN in Security first",
                Toast.LENGTH_SHORT
            ).show()
            currentScreen = SCREEN_SECURITY
            return
        }

        if (RenMonieStorage.isPinLocked(context)) {
            Toast.makeText(
                context,
                "PIN is temporarily locked. Try again later.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        pendingSecureAction = action
        pinDialogVisible = true
    }

    /*
     * The existing TransferScreen works with whole-naira Long values.
     * We therefore expose the display balance as naira when opening it.
     */
    val balanceNaira: Long = balanceKobo / 100L

    fun saveWallet() {
        RenMonieStorage.saveBalanceKobo(
            context,
            balanceKobo
        )

        RenMonieStorage.saveTransactions(
            context,
            transactions
        )
    }

    fun addTransaction(
        transaction: Transaction
    ) {
        transactions =
            (listOf(transaction) + transactions).take(120)

        saveWallet()
    }

    fun showWalletNotification(
        title: String,
        message: String
    ) {
        NotificationStore.add(context, title, message, type = "success")


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(
            context,
            MainActivity::class.java
        )

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        if (
                            Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.M
                        ) {
                            PendingIntent.FLAG_IMMUTABLE
                        } else {
                            0
                        }
            )

        val notification =
            NotificationCompat.Builder(
                context,
                NOTIFICATION_CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .build()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.notify(
            Random.nextInt(1000, 999999),
            notification
        )
    }

    fun addFunds(amountNaira: Long) {
        if (amountNaira <= 0L) {
            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        val amountKobo = amountNaira * 100L
        balanceKobo += amountKobo
        val transaction = Transaction(
            id = generateTransactionId(),
            recipient = "RenMonie Wallet",
            account = "8094821482",
            bank = "RenMonie",
            amount = amountKobo,
            narration = "Wallet top-up",
            date = currentDateTime(),
            type = "Credit",
            isCredit = true,
            status = STATUS_SUCCESSFUL,
            reference = reference
        )
        addTransaction(transaction)
        val masked = "****4821"
        showWalletNotification(
            "Credit Alert",
            "You have received ${naira(amountKobo)} to Acc: $masked (RenMonie Personal). Available balance: ${naira(balanceKobo)}. View in RenMonie."
        )
        Toast.makeText(context, "Funds added successfully", Toast.LENGTH_SHORT).show()
        selectedReceipt = transaction.toReceiptData()
        currentScreen = SCREEN_HOME
    }




    /*
     * Existing TransferScreen sends the amount as whole naira.
     *
     * We convert it to kobo before touching the wallet.
     */
    fun makeTransfer(
        bank: String,
        recipient: String,
        account: String,
        amount: Long,
        narration: String,
        reference: String = ""
    ) {

        if (amount <= 0L) {
            Toast.makeText(
                context,
                "Enter a valid amount",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val amountKobo = amount * 100L

        if (amountKobo > balanceKobo) {
            Toast.makeText(
                context,
                "Insufficient balance",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (account.length != 10) {
            Toast.makeText(
                context,
                "Enter a valid 10-digit account number",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val transaction = Transaction(
            id = generateTransactionId(),
            recipient = recipient,
            account = account,
            bank = bank,
            amount = amountKobo,
            narration = narration.ifBlank {
                "Transfer"
            },
            date = currentDateTime(),
            type = "Transfer",
            isCredit = false,
            status = STATUS_SUCCESSFUL
        )

        balanceKobo -= amountKobo

        addTransaction(transaction)

        val masked = if (account.length >= 4) "****${account.takeLast(4)}" else account
        val bal = naira(balanceKobo)
        showWalletNotification(
            "Debit Alert",
            "You transferred ${naira(amountKobo)} to $recipient. Acc: $masked ($bank). Available balance: $bal. View in RenMonie."
        )

        Toast.makeText(
            context,
            "Transfer successful",
            Toast.LENGTH_SHORT
        ).show()

        showSuccessThenReceipt(
            receipt = transaction.toReceiptData(),
            title = "Transfer successful",
            subtitle = "Sent to $recipient",
            amountLabel = naira(amountKobo)
        )
    }

fun makePendingTransfer(
    bank: String,
    recipient: String,
    account: String,
    amount: Long,
    narration: String
) {

    if (amount <= 0L) {
        Toast.makeText(
            context,
            "Enter a valid amount",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val amountKobo = amount * 100L

    if (amountKobo > balanceKobo) {
        Toast.makeText(
            context,
            "Insufficient balance",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    if (account.length != 10) {
        Toast.makeText(
            context,
            "Enter a valid 10-digit account number",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val transaction = Transaction(
        id = generateTransactionId(),
        recipient = recipient,
        account = account,
        bank = bank,
        amount = amountKobo,
        narration = narration.ifBlank {
            "Transfer"
        },
        date = currentDateTime(),
        type = "Transfer",
        isCredit = false,
        status = STATUS_PENDING,
        reference = reference
    )

    /*
     * Reserve/deduct the amount from the
     * local wallet when the transfer is
     * created as pending.
     */
    balanceKobo -= amountKobo

    addTransaction(transaction)

    showWalletNotification(
        "Transfer pending",
        "${naira(amountKobo)} transfer to $recipient is pending"
    )

    Toast.makeText(
        context,
        "Transfer is pending",
        Toast.LENGTH_SHORT
    ).show()

    selectedReceipt =
        transaction.toReceiptData()

    currentScreen = SCREEN_HOME
}
  
    fun buyAirtime(
        network: String,
        phone: String,
        amount: Long
    ) {

        if (phone.length < 10) {
            Toast.makeText(
                context,
                "Enter a valid phone number",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (amount <= 0L) {
            Toast.makeText(
                context,
                "Enter a valid amount",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val amountKobo = amount * 100L

        if (amountKobo > balanceKobo) {
            Toast.makeText(
                context,
                "Insufficient balance",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val transaction = Transaction(
            id = generateTransactionId(),
            recipient = phone,
            account = phone,
            bank = network,
            amount = amountKobo,
            narration = "$network Airtime",
            date = currentDateTime(),
            type = "Airtime",
            isCredit = false,
            status = STATUS_SUCCESSFUL
        )

        balanceKobo -= amountKobo

        addTransaction(transaction)

        showWalletNotification(
            "Debit Alert",
            "Airtime ${naira(amountKobo)} to $phone ($network). Available balance: ${naira(balanceKobo)}. View in RenMonie."
        )

        Toast.makeText(
            context,
            "Airtime purchase successful",
            Toast.LENGTH_SHORT
        ).show()

        showSuccessThenReceipt(
            receipt = transaction.toReceiptData(),
            title = "Airtime successful",
            subtitle = "Sent to $phone",
            amountLabel = naira(amountKobo)
        )
    }

    fun buyData(
        network: String,
        bundle: String,
        amount: Long
    ) {

        if (amount <= 0L) {
            Toast.makeText(
                context,
                "Invalid data bundle",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val amountKobo = amount * 100L

        if (amountKobo > balanceKobo) {
            Toast.makeText(
                context,
                "Insufficient balance",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val transaction = Transaction(
            id = generateTransactionId(),
            recipient = network,
            account = network,
            bank = network,
            amount = amountKobo,
            narration = "$network $bundle Data",
            date = currentDateTime(),
            type = "Data",
            isCredit = false,
            status = STATUS_SUCCESSFUL
        )

        balanceKobo -= amountKobo

        addTransaction(transaction)

        showWalletNotification(
            "Debit Alert",
            "Data $bundle for ${naira(amountKobo)}. Available balance: ${naira(balanceKobo)}. View in RenMonie."
        )

        Toast.makeText(
            context,
            "Data purchase successful",
            Toast.LENGTH_SHORT
        ).show()

        selectedReceipt =
            transaction.toReceiptData()
    }

    fun payBill(
        service: String,
        customer: String,
        amount: Long
    ) {
        if (amount <= 0L) {
            Toast.makeText(context, "Enter a valid bill amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amountKobo = amount * 100L
        if (amountKobo > balanceKobo) {
            Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = generateTransactionId(),
            recipient = customer,
            account = customer,
            bank = service,
            amount = amountKobo,
            narration = "$service payment",
            date = currentDateTime(),
            type = "Bill Payment",
            isCredit = false,
            status = STATUS_SUCCESSFUL
        )

        balanceKobo -= amountKobo
        addTransaction(transaction)
        showWalletNotification(
            "Debit Alert",
            "Bill payment ${naira(amountKobo)} to $service. Available balance: ${naira(balanceKobo)}. View in RenMonie."
        )
        Toast.makeText(
            context,
            "Payment successful",
            Toast.LENGTH_SHORT
        ).show()
        selectedReceipt = transaction.toReceiptData()
    }

    /*
     * Secret transaction-status controller.
     *
     * This only changes the local transaction record.
     * It does NOT interact with real banks.
     */
    fun changeTransactionStatus(
        transactionId: String,
        newStatus: String
    ) {

        transactions =
            transactions.map { transaction ->

                if (transaction.id == transactionId) {
                    transaction.copy(
                        status = newStatus
                    )
                } else {
                    transaction
                }
            }

        saveWallet()

        val changed =
            transactions.firstOrNull {
                it.id == transactionId
            }

        if (changed != null) {
            selectedReceipt =
                changed.toReceiptData()
        }

        Toast.makeText(
            context,
            "Transaction marked $newStatus",
            Toast.LENGTH_SHORT
        ).show()
    }

    when (currentScreen) {

        SCREEN_SPLASH -> {
            SplashScreen(
                onFinished = {
                    currentScreen =
                        if (RenMonieStorage.hasPin(context)) SCREEN_LOGIN
                        else SCREEN_HOME
                }
            )
        }

        SCREEN_LOGIN -> {
            LoginScreen(
                context = context,
                onUnlocked = { currentScreen = SCREEN_HOME },
                onSetupPin = { currentScreen = SCREEN_SECURITY }
            )
        }

        SCREEN_HOME -> {

            HomeScreen(
                balance = balanceKobo,
                transactions = transactions,
                onTransfer = {
                    currentScreen =
                        SCREEN_TRANSFER
                },
                onAirtime = {
                    currentScreen =
                        SCREEN_AIRTIME
                },
                onData = {
                    currentScreen =
                        SCREEN_DATA
                },
                onUssd = {
                    currentScreen = SCREEN_USSD
                },
                onHistory = {
                    currentScreen =
                        SCREEN_HISTORY
                },
                onMore = {
                    currentScreen =
                        SCREEN_MORE
                },
                onCard = {
                    currentScreen = SCREEN_CARDS
                },
                onSettings = {
                    currentScreen = SCREEN_SECURITY
                },
                onAddMoney = {
                    currentScreen = SCREEN_ADD_MONEY
                },
                onNotifications = {
                    currentScreen = SCREEN_NOTIFICATIONS
                },
                onReceipt = {
                    selectedReceipt =
                        it.toReceiptData()
                }
            )
        }
        

        SCREEN_ADD_MONEY -> {
            AddMoneyScreen(
                onBack = { currentScreen = SCREEN_HOME },
                onConfirm = { amount ->
                    runSecureAction {
                        addFunds(amount)
                    }
                }
            )
        }

        SCREEN_USSD -> {
            UssdScreen(
                balanceNaira = balanceNaira,
                onBack = { currentScreen = SCREEN_HOME },
                onTransfer = { account, amount, narration ->
                    makeTransfer(
                        bank = "USSD Bank",
                        recipient = "USSD Recipient",
                        account = account,
                        amount = amount,
                        narration = narration
                    )
                },
                onAirtime = { phone, amount ->
                    buyAirtime(
                        network = "USSD",
                        phone = phone,
                        amount = amount
                    )
                },
                onRequirePin = { action ->
                    runSecureAction(action)
                }
            )
        }

        SCREEN_BILLS -> {
            BillsScreen(
                balance = balanceNaira,
                onBack = { currentScreen = SCREEN_HOME },
                onPay = { service, customer, amount ->
                    runSecureAction {
                        payBill(service, customer, amount)
                    }
                }
            )
        }

        SCREEN_ACCOUNT -> {
            AccountScreen(
                context = context,
                onBack = { currentScreen = SCREEN_MORE }
            )
        }

        SCREEN_NOTIFICATIONS -> {
            NotificationCentreScreen(
                context = context,
                onBack = { currentScreen = SCREEN_HOME }
            )
        }

        SCREEN_ABOUT -> {
            AboutScreen(onBack = { currentScreen = SCREEN_MORE })
        }

        SCREEN_THEME -> {
            ThemeSettingsScreen(
                context = context,
                onBack = { currentScreen = SCREEN_MORE }
            )
        }

        SCREEN_BANKING, SCREEN_BENEFICIARIES, SCREEN_SAVINGS, SCREEN_INSIGHTS -> {
            BankingScreen(
                context = context,
                balance = balanceNaira,
                onBack = { currentScreen = SCREEN_MORE }
            )
        }

        SCREEN_CARDS -> {
            BankingScreen(
                context = context,
                balance = balanceNaira,
                onBack = { currentScreen = SCREEN_HOME },
                startPage = "cards"
            )
        }

        SCREEN_SECURITY -> {

    SecurityScreen(
        context = context,

        onBack = {
            currentScreen =
                SCREEN_MORE
        }
    )
}

        SCREEN_TRANSFER -> {

    TransferScreen(
        balance = balanceNaira,

        onBack = {
            currentScreen = SCREEN_HOME
        },

        onTransfer = {
            bank,
            recipient,
            account,
            amount,
            narration,
            reference ->

            makeTransfer(
                bank,
                recipient,
                account,
                amount,
                narration,
                reference
            )
        },

        onPendingTransfer = {
            bank,
            recipient,
            account,
            amount,
            narration,
            reference ->

            makePendingTransfer(
                bank,
                recipient,
                account,
                amount,
                narration
            )
        },

        onRequirePin = { action ->
            runSecureAction(action)
        }
    )
}

        SCREEN_AIRTIME -> {

            AirtimeScreen(
                balance = balanceNaira,
                onBack = {
                    currentScreen =
                        SCREEN_HOME
                },
                onPurchase = {
                        network,
                        phone,
                        amount ->

                    runSecureAction {
                        buyAirtime(
                            network,
                            phone,
                            amount
                        )
                    }
                }
            )
        }

        SCREEN_DATA -> {

            DataScreen(
                balance = balanceNaira,
                onBack = {
                    currentScreen =
                        SCREEN_HOME
                },
                onPurchase = {
                        network,
                        bundle,
                        amount ->

                    runSecureAction {
                        buyData(
                            network,
                            bundle,
                            amount
                        )
                    }
                }
            )
        }

        SCREEN_HISTORY -> {

            HistoryScreen(
                transactions = transactions,
                onBack = {
                    currentScreen =
                        SCREEN_HOME
                },
                onReceipt = {
                    selectedReceipt =
                        it.toReceiptData()
                }
            )
        }

        SCREEN_MORE -> {

    MoreScreen(
        balance = balanceKobo,
        transactions = transactions,

        onBack = {
            currentScreen =
                SCREEN_HOME
        },

        onSecurity = {
            currentScreen =
                SCREEN_SECURITY
        },

        onTransfer = {
            currentScreen =
                SCREEN_TRANSFER
        },

        onAirtime = {
            currentScreen =
                SCREEN_AIRTIME
        },

        onData = {
            currentScreen =
                SCREEN_DATA
        },

        onHistory = {
            currentScreen =
                SCREEN_HISTORY
        },

        onSecretControl = {
            currentScreen =
                SCREEN_CONTROL
        },

        onBills = {
            currentScreen = SCREEN_BILLS
        },

        onAccount = {
            currentScreen = SCREEN_ACCOUNT
        },

        onTheme = {
            currentScreen = SCREEN_THEME
        },

        onBeneficiaries = {
            currentScreen = SCREEN_BENEFICIARIES
        },

        onCards = {
            currentScreen = SCREEN_CARDS
        },

        onSavings = {
            currentScreen = SCREEN_SAVINGS
        },

        onInsights = {
            currentScreen = SCREEN_INSIGHTS
        },

        onBanking = {
            currentScreen = SCREEN_BANKING
        },

        onUssd = {
            currentScreen = SCREEN_USSD
        },

        onAbout = {
            currentScreen = SCREEN_ABOUT
        },

        onLogout = {
            currentScreen = SCREEN_LOGIN
        },

        onNotifications = {
            currentScreen = SCREEN_NOTIFICATIONS
        }
    )
}

        SCREEN_CONTROL -> {

            TransactionControlScreen(
                transactions = transactions,
                onBack = {
                    currentScreen =
                        SCREEN_MORE
                },
                onStatusChange = {
                    transactionId,
                    status ->

                    changeTransactionStatus(
                        transactionId,
                        status
                    )
                },
                onReceipt = {
                    selectedReceipt =
                        it.toReceiptData()
                }
            )
        }
    }

    if (pinDialogVisible) {
        PinVerificationDialog(
            context = context,
            onVerified = {
                pinDialogVisible = false
                val action = pendingSecureAction
                pendingSecureAction = null
                action?.invoke()
            },
            onDismiss = {
                pinDialogVisible = false
                pendingSecureAction = null
            }
        )
    }

    if (showSuccessAnimation) {
        TransferSuccessAnimation(
            title = successTitle,
            subtitle = successSubtitle,
            amountLabel = successAmountLabel,
            onFinished = {
                showSuccessAnimation = false
                selectedReceipt = pendingReceipt
                pendingReceipt = null
            }
        )
    }

    selectedReceipt?.let { receipt ->
        ReceiptDialog(
            receipt = receipt,
            onDismiss = {
                selectedReceipt = null
            }
        )
    }

@Composable
private fun TransactionDetailsDialog(transaction: Transaction, onDismiss: () -> Unit) {
    val masked = if (transaction.account.length >= 4) "******" + transaction.account.takeLast(4) else transaction.account
    val reference = transaction.reference.ifBlank { transaction.id }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RenCard,
        title = { Text("Transaction Details", color = RenText, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ReceiptAmount(transaction.amount, transaction.isCredit)
                Spacer(Modifier.height(14.dp))
                ReceiptDetail("Status", transaction.status)
                ReceiptDetail("Reference", reference)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val clipboard = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("RenMonie transaction reference", reference))
                        Toast.makeText(LocalContext.current, "Reference copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy Reference")
                }
                OutlinedButton(
                    onClick = {
                        val receiptText = buildString {
                            appendLine("RenMonie Transaction Receipt")
                            appendLine("-----------------------------")
                            appendLine("Amount: " + naira(transaction.amount))
                            appendLine("Status: " + transaction.status)
                            appendLine("Reference: " + reference)
                            appendLine("Recipient: " + transaction.recipient)
                            appendLine("Bank: " + transaction.bank)
                            appendLine("Account: " + masked)
                            appendLine("Account verification: Verified • " + transaction.recipient)
                            appendLine("Date & time: " + transaction.date)
                            appendLine("Type: " + transaction.type)
                            appendLine("Narration: " + transaction.narration.ifBlank { "Transfer" })
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "RenMonie Transaction Receipt")
                            putExtra(Intent.EXTRA_TEXT, receiptText)
                        }
                        LocalContext.current.startActivity(
                            Intent.createChooser(shareIntent, "Share receipt")
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share Receipt")
                }
                ReceiptDetail("Recipient", transaction.recipient)
                ReceiptDetail("Bank", transaction.bank)
                ReceiptDetail("Account", masked)
                ReceiptDetail("Account verification", "Verified • " + transaction.recipient)
                ReceiptDetail("Date & time", transaction.date)
                ReceiptDetail("Type", transaction.type)
                ReceiptDetail("Narration", transaction.narration.ifBlank { "Transfer" })
            }
        },
        confirmButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = RenPurple)) { Text("Done") } }
    )
}
}
