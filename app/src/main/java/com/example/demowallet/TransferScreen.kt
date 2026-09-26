@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demowallet.ui.theme.RenMonieBackground
import com.example.demowallet.ui.theme.RenMonieBlue
import com.example.demowallet.ui.theme.RenMonieBrightBlue
import com.example.demowallet.ui.theme.RenMonieDivider
import com.example.demowallet.ui.theme.RenMonieError
import com.example.demowallet.ui.theme.RenMonieNavyCard
import com.example.demowallet.ui.theme.RenMonieSuccess
import com.example.demowallet.ui.theme.RenMonieSurface
import com.example.demowallet.ui.theme.RenMonieText
import com.example.demowallet.ui.theme.RenMonieTextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    balance: Long,
    onBack: () -> Unit,
    onTransfer: (
        bank: String,
        recipient: String,
        account: String,
        amount: Long,
        narration: String
    ) -> Unit,
    onPendingTransfer: (
        bank: String,
        recipient: String,
        account: String,
        amount: Long,
        narration: String
    ) -> Unit = { bank, recipient, account, amount, narration ->
        onTransfer(bank, recipient, account, amount, narration)
    },
    onRequirePin: (() -> Unit) -> Unit = { action -> action() }
) {
    var selectedBank by rememberSaveable { mutableStateOf("") }
    var accountNumber by rememberSaveable { mutableStateOf("") }
    var recipientName by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var narration by rememberSaveable { mutableStateOf("") }

    var showBankSelector by rememberSaveable { mutableStateOf(false) }
    var showReview by rememberSaveable { mutableStateOf(false) }
    var showPendingReview by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var verificationMessage by rememberSaveable { mutableStateOf("") }
    var verifyingName by remember { mutableStateOf(false) }
    var nameVerified by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showProcessing by remember { mutableStateOf(false) }
    var bankOptions by remember { mutableStateOf<List<BankOption>>(emptyList()) }
    var liveBankDirectoryReady by remember { mutableStateOf(false) }
    var bankDirectoryLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(context) {
        bankDirectoryLoading = true
        val result = BankDirectoryClient.load(context)
        bankOptions = result.banks
        liveBankDirectoryReady = result.banks.isNotEmpty()
        bankDirectoryLoading = false
        if (result.banks.isEmpty()) {
            verificationMessage = result.message
        }
    }

    val amountValue = amount.toLongOrNull() ?: 0L
    val availableBalance = balance

    Scaffold(
        containerColor = RenMonieBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Transfer",
                            color = RenMonieText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Send money securely",
                            color = RenMonieTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = RenMonieText
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 30.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                TransferBalanceCard(balance = availableBalance)
            }

            item {
                SecurityNotice()
            }

            item {
                SectionTitle("Saved beneficiaries")
            }

            item {
                val savedBeneficiaries = listOf(
                    Triple("John Doe", "0123456789", "GTBank"),
                    Triple("Jane Smith", "0987654321", "Access Bank"),
                    Triple("Chidi Okafor", "2034567890", "Zenith Bank")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    savedBeneficiaries.forEach { (name, acct, bank) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedBank = bank
                                    accountNumber = acct
                                    recipientName = ""
                                    verificationMessage = ""
                                    errorMessage = ""
                                    nameVerified = false
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = RenMonieSurface)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RenPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(1),
                                        color = RenPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = name.split(" ").first(),
                                    color = RenMonieText,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = bank.take(6),
                                    color = RenMonieTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle("Recipient")
            }

            item {
                BankSelector(
                    selectedBank = selectedBank,
                    onClick = {
                        showBankSelector = true
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = {
                        accountNumber = it
                            .filter { char -> char.isDigit() }
                            .take(10)
                        errorMessage = ""
                        verificationMessage = ""
                        nameVerified = false
                        recipientName = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Account number") },
                    placeholder = { Text("Enter 10-digit account number") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
    LaunchedEffect(accountNumber, selectedBank) {
        nameVerified = false
        recipientName = ""
        verificationMessage = ""

        if (accountNumber.length == 10 && selectedBank.isNotBlank()) {
            val selectedBankOption = bankOptions.firstOrNull {
                it.name == selectedBank
            }

            if (!liveBankDirectoryReady || selectedBankOption == null) {
                verificationMessage =
                    "Live bank directory is unavailable. Please try again."
                verifyingName = false
            } else {
                verifyingName = true

                val result = AccountVerificationClient.verifyAccount(
                    context = context,
                    accountNumber = accountNumber,
                    bankCode = selectedBankOption.code
                )

                if (result.verified) {
                    recipientName = result.accountName
                    nameVerified = true
                    verificationMessage = result.message
                } else {
                    verificationMessage = result.message
                }

                verifyingName = false
            }
        } else {
            verifyingName = false
        }
    }

    NameEnquiryBanner(
        verifying = verifyingName,
        verifiedName = if (nameVerified) recipientName else null,
        error = verificationMessage.takeIf { it.isNotBlank() }
    )
}

            item {
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = nameVerified,
                    label = { Text("Verified recipient name") },
                    placeholder = { Text("Name will appear automatically after verification") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (nameVerified) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = RenMonieSuccess
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                SectionTitle("Amount")
            }

            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                            .filter { char -> char.isDigit() }
                            .take(9)
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Amount")
                    },
                    placeholder = {
                        Text("0")
                    },
                    prefix = {
                        Text(
                            text = "₦ ",
                            color = RenMonieText,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                QuickAmounts(
                    onSelected = {
                        amount = it.toString()
                        errorMessage = ""
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = narration,
                    onValueChange = {
                        narration = it.take(80)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Narration")
                    },
                    placeholder = {
                        Text("What is this transfer for?")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            if (errorMessage.isNotEmpty()) {
                item {
                    ErrorCard(errorMessage)
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        when {
                            selectedBank.isBlank() -> {
                                errorMessage = "Please select a bank"
                            }

                            accountNumber.length != 10 -> {
                                errorMessage = "Enter a valid 10-digit account number"
                            }

                           verifyingName -> {
    errorMessage = "Please wait for account verification to finish"
}

!nameVerified -> {
    errorMessage = "Please verify the recipient account before continuing"
}

recipientName.trim().length < 2 -> {
    errorMessage = "Enter the recipient name"
}
                            amountValue > availableBalance -> {
                                errorMessage = "Insufficient balance"
                            }

                            else -> {
                                if (isSubmitting) return@Button
                                errorMessage = ""
                                showReview = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RenMonieBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(9.dp))

                    Text(
                        text = "Review transfer",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showBankSelector) {
        BankSelectorDialog(
            banks = bankOptions,
            loading = bankDirectoryLoading,
            onDismiss = {
                showBankSelector = false
            },
            onSelected = {
                selectedBank = it
                showBankSelector = false
                errorMessage = ""
                verificationMessage = ""
                nameVerified = false
                recipientName = ""
            }
        )
    }

    if (showReview) {
        TransferReviewDialog(
            bank = selectedBank,
            account = accountNumber,
            recipient = recipientName,
            amount = amountValue,
            narration = narration,
            onDismiss = {
                showReview = false
            },
            onConfirm = {
                if (!isSubmitting) {
                    showReview = false
                    onRequirePin {
                        isSubmitting = true
                        showProcessing = true
                    }
                }
            },
            onPending = {
                if (!isSubmitting) {
                    showReview = false
                    showPendingReview = true
                }
            }
        )
    }

    if (showPendingReview) {
        PendingTransferDialog(
            bank = selectedBank,
            account = accountNumber,
            recipient = recipientName,
            amount = amountValue,
            narration = narration,
            onDismiss = {
                showPendingReview = false
            },
            onConfirm = {
                showPendingReview = false

                onRequirePin {
                    onPendingTransfer(
                        selectedBank,
                    recipientName.trim(),
                    accountNumber,
                    amountValue,
                    narration.trim().ifBlank {
                        "RenMonie transfer"
                    }
                )
            }
        )
    }

    if (showProcessing) {
        ProcessingDialog(
            title = "Sending money…",
            subtitle = "Do not close the app"
        )
        LaunchedEffect(showProcessing) {
            val bankCode = bankOptions.firstOrNull { it.name == selectedBank }?.code
            if (bankCode == null) {
                showProcessing = false
                isSubmitting = false
                errorMessage = "Bank details are unavailable. Please select the bank again."
            } else {
                val result = TransferClient.submit(
                    context = context,
                    amountNaira = amountValue,
                    bankCode = bankCode,
                    accountNumber = accountNumber,
                    accountName = recipientName.trim(),
                    narration = narration.trim()
                )
                showProcessing = false
                isSubmitting = false
                if (result.ok) {
                    val status = result.status.uppercase()
                    if (status == "PENDING" || status == "PENDING_AUTHORIZATION" ||
                        status == "AWAITING_PROCESSING" || status == "IN_PROGRESS") {
                        onPendingTransfer(selectedBank, recipientName.trim(), accountNumber, amountValue,
                            narration.trim().ifBlank { "RenMonie transfer" })
                    } else {
                        onTransfer(selectedBank, recipientName.trim(), accountNumber, amountValue,
                            narration.trim().ifBlank { "RenMonie transfer" })
                    }
                } else {
                    errorMessage = result.message
                }
            }
        }
    }
}

@Composable
private fun TransferBalanceCard(
    balance: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = RenMonieNavyCard
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = RenMonieBlue.copy(alpha = 0.15f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = RenMonieBrightBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "AVAILABLE BALANCE",
                        color = RenMonieTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = transferNaira(balance),
                        color = RenMonieText,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Divider(
                color = RenMonieDivider.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = RenMonieSuccess,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    text = "Available for transfer",
                    color = RenMonieTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SecurityNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = RenMonieSuccess.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = RenMonieSuccess,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = "Every transfer is protected by your transaction PIN.",
                color = RenMonieTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        color = RenMonieText,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 3.dp)
    )
}

@Composable
private fun BankSelector(
    selectedBank: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = RenMonieSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(13.dp),
                color = RenMonieBlue.copy(alpha = 0.12f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = RenMonieBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Bank / financial institution",
                    color = RenMonieTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = selectedBank.ifBlank { "Select bank" },
                    color = if (selectedBank.isBlank()) {
                        RenMonieTextSecondary
                    } else {
                        RenMonieText
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = RenMonieTextSecondary
            )
        }
    }
}

@Composable
private fun QuickAmounts(
    onSelected: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        listOf(1000L, 5000L, 10000L, 20000L).forEach { amount ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSelected(amount)
                    },
                shape = RoundedCornerShape(11.dp),
                color = RenMonieBlue.copy(alpha = 0.09f)
            ) {
                Box(
                    modifier = Modifier.padding(
                        vertical = 10.dp,
                        horizontal = 4.dp
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₦${formatQuickAmount(amount)}",
                        color = RenMonieBrightBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = RenMonieError.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = RenMonieError
            )

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = message,
                color = RenMonieText,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun BankSelectorDialog(
    banks: List<BankOption>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }

    val filteredBanks = remember(search, banks) {
        banks.filter {
            it.name.contains(
                search.trim(),
                ignoreCase = true
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RenMonieSurface,
        title = {
            Text(
                text = "Select bank",
                color = RenMonieText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search bank")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (loading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = RenMonieBrightBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Updating bank list…",
                            color = RenMonieTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 330.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(filteredBanks) { bank ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelected(bank.name)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = CircleShape,
                                color = RenMonieBlue.copy(alpha = 0.12f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bank.name.take(1),
                                        color = RenMonieBrightBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = bank.name,
                                color = RenMonieText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = RenMonieBrightBlue
                )
            }
        }
    )
}

@Composable
private fun TransferReviewDialog(
    bank: String,
    account: String,
    recipient: String,
    amount: Long,
    narration: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onPending: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RenMonieSurface,
        title = {
            Column {
                Text(
                    text = "Review transfer",
                    color = RenMonieText,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Please confirm the details",
                    color = RenMonieTextSecondary,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ReviewRow("Bank", bank)
                ReviewRow("Recipient", recipient)
                ReviewRow("Account", account)
                ReviewRow("Amount", transferNaira(amount))

                if (narration.isNotBlank()) {
                    ReviewRow("Narration", narration)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                    color = RenMonieBlue.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = RenMonieBrightBlue,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "You will be asked for your transaction PIN.",
                            color = RenMonieTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Edit",
                    color = RenMonieTextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RenMonieBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Confirm",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun PendingTransferDialog(
    bank: String,
    account: String,
    recipient: String,
    amount: Long,
    narration: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RenMonieSurface,
        title = {
            Text(
                text = "Pending transfer",
                color = RenMonieText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "This transfer will be recorded as pending.",
                    color = RenMonieTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                ReviewRow("Bank", bank)
                ReviewRow("Recipient", recipient)
                ReviewRow("Account", account)
                ReviewRow("Amount", transferNaira(amount))

                if (narration.isNotBlank()) {
                    ReviewRow("Narration", narration)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = RenMonieTextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RenMonieBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun ReviewRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = RenMonieTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(82.dp)
        )

        Text(
            text = value,
            color = RenMonieText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun transferNaira(kobo: Long): String {
    val naira = kobo / 100L
    return "₦" + String.format(
        Locale.US,
        "%,d",
        naira
    )
}

private fun formatQuickAmount(amount: Long): String {
    return when {
        amount >= 1000000L -> "${amount / 1000000L}M"
        amount >= 1000L -> "${amount / 1000L}k"
        else -> amount.toString()
    }
}