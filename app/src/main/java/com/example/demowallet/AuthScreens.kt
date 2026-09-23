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
fun LoginScreen(
    context: Context,
    onUnlocked: () -> Unit,
    onSetupPin: () -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(RenMonieStorage.isPinLocked(context)) }

    LaunchedEffect(Unit) {
        if (!RenMonieStorage.hasPin(context)) {
            onUnlocked()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RenDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(RenPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "RenMonie",
                color = RenText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter your 4-digit PIN",
                color = RenMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) RenPurple
                                else RenCard2
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (error.isNotBlank()) {
                Text(error, color = RenRed, fontSize = 13.sp)
            }
            if (isLocked) {
                Text(
                    "Too many attempts. Wait 30 seconds.",
                    color = RenOrange,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Number pad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (key.isNotEmpty()) RenCard else Color.Transparent)
                                .clickable(enabled = key.isNotEmpty() && !isLocked) {
                                    when (key) {
                                        "⌫" -> {
                                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            error = ""
                                        }
                                        else -> {
                                            if (pin.length < 4) {
                                                pin += key
                                                if (pin.length == 4) {
                                                    if (RenMonieStorage.verifyPin(context, pin)) {
                                                        onUnlocked()
                                                    } else {
                                                        error = "Incorrect PIN. ${RenMonieStorage.remainingPinAttempts(context)} attempts left"
                                                        pin = ""
                                                        isLocked = RenMonieStorage.isPinLocked(context)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key.isNotEmpty()) {
                                Text(
                                    text = key,
                                    color = RenText,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onSetupPin) {
                Text("Forgot PIN? Go to Security", color = RenPurple)
            }
        }
    }
}

@Composable
fun SecurityScreen(
    context: Context,
    onBack: () -> Unit
) {

    var hasPin by remember {
        mutableStateOf(
            RenMonieStorage.hasPin(context)
        )
    }

    var pin by rememberSaveable {
        mutableStateOf("")
    }

    var confirmPin by rememberSaveable {
        mutableStateOf("")
    }

    var pinVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var confirmVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var message by rememberSaveable {
        mutableStateOf("")
    }

    var messageIsError by rememberSaveable {
        mutableStateOf(false)
    }

    var biometricEnabled by remember {
        mutableStateOf(SecurityPrefs.isBiometricEnabled(context))
    }

    Scaffold(
        containerColor = RenDark,

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Security",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = RenDark,
                        titleContentColor = RenText,
                        navigationIconContentColor = RenText
                    )
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(
                    rememberScrollState()
                ),

            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(20.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor = RenCard
                    )
            ) {

                Column(

                    modifier =
                        Modifier.padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = RenViolet,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            if (hasPin)
                                "Wallet PIN is enabled"
                            else
                                "Protect your wallet",

                        color = RenText,

                        fontSize = 20.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            if (hasPin)
                                "Change your 4-digit PIN below."
                            else
                                "Create a 4-digit PIN to protect wallet actions.",

                        color = RenMuted,

                        fontSize = 14.sp
                    )
                }
            }


            // =================================================
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = RenCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = RenPurple
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Biometric unlock",
                            color = RenText,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Use fingerprint / face to unlock (demo toggle)",
                            color = RenMuted,
                            fontSize = 12.sp
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = biometricEnabled,
                        onCheckedChange = {
                            biometricEnabled = it
                            SecurityPrefs.setBiometric(context, it)
                            NotificationStore.add(
                                context,
                                "Security",
                                if (it) "Biometric unlock enabled" else "Biometric unlock disabled",
                                type = "security"
                            )
                        },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedTrackColor = RenPurple
                        )
                    )
                }
            }

// CREATE / CHANGE PIN
            // =================================================

            OutlinedTextField(

                value = pin,

                onValueChange = { value ->

                    if (
                        value.length <= 4 &&
                        value.all {
                            it.isDigit()
                        }
                    ) {

                        pin = value
                        message = ""
                    }
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {

                    Text(
                        if (hasPin)
                            "New PIN"
                        else
                            "Create PIN"
                    )
                },

                placeholder = {
                    Text("4 digits")
                },

                singleLine = true,

                visualTransformation =
                    if (pinVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.NumberPassword
                    ),

                trailingIcon = {

                    TextButton(
                        onClick = {
                            pinVisible =
                                !pinVisible
                        }
                    ) {

                        Text(
                            if (pinVisible)
                                "HIDE"
                            else
                                "SHOW"
                        )
                    }
                }
            )


            // =================================================
            // CONFIRM PIN
            // =================================================

            OutlinedTextField(

                value = confirmPin,

                onValueChange = { value ->

                    if (
                        value.length <= 4 &&
                        value.all {
                            it.isDigit()
                        }
                    ) {

                        confirmPin = value
                        message = ""
                    }
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Confirm PIN")
                },

                placeholder = {
                    Text("Re-enter PIN")
                },

                singleLine = true,

                visualTransformation =
                    if (confirmVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.NumberPassword
                    ),

                trailingIcon = {

                    TextButton(
                        onClick = {
                            confirmVisible =
                                !confirmVisible
                        }
                    ) {

                        Text(
                            if (confirmVisible)
                                "HIDE"
                            else
                                "SHOW"
                        )
                    }
                }
            )


            // =================================================
            // MESSAGE
            // =================================================

            if (message.isNotBlank()) {

                Text(

                    text = message,

                    color =
                        if (messageIsError)
                            RenRed
                        else
                            RenGreen,

                    fontSize = 14.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }


            // =================================================
            // SAVE PIN
            // =================================================

            Button(

                onClick = {

                    when {

                        pin.length != 4 -> {

                            message =
                                "PIN must contain exactly 4 digits"

                            messageIsError = true
                        }

                        confirmPin.length != 4 -> {

                            message =
                                "Please confirm your 4-digit PIN"

                            messageIsError = true
                        }

                        pin != confirmPin -> {

                            message =
                                "PINs do not match"

                            messageIsError = true
                        }

                        else -> {

                            val saved =
                                RenMonieStorage.savePin(
                                    context,
                                    pin
                                )

                            if (saved) {

                                hasPin = true

                                message =
                                    "PIN saved successfully"

                                messageIsError = false

                                pin = ""
                                confirmPin = ""
                            }
                        }
                    }
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                shape =
                    RoundedCornerShape(14.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = RenPurple
                    )
            ) {

                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(

                    text =
                        if (hasPin)
                            "Change PIN"
                        else
                            "Create PIN",

                    fontWeight =
                        FontWeight.Bold
                )
            }


            // =================================================
            // DISABLE PIN
            // =================================================

            if (hasPin) {

                OutlinedButton(

                    onClick = {

                        RenMonieStorage.clearPin(
                            context
                        )

                        hasPin = false

                        pin = ""
                        confirmPin = ""

                        message =
                            "PIN protection disabled"

                        messageIsError = false
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp),

                    shape =
                        RoundedCornerShape(14.dp),

                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = RenRed
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Lock,
                        contentDescription = null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text = "Disable PIN"
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )
        }
    }
}

@Composable
fun ProfileCard(
    onTap: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            )
            .clickable(
                onClick = onTap
            ),
        shape =
            RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = RenCard
            )
    ) {

        Row(
            modifier =
                Modifier.padding(20.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .background(RenPurple),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "P",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.width(13.dp)
            )

            Column {

                Text(
                    text = "Patrick",
                    color = RenText,
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "RenMonie Wallet",
                    color = RenMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MoreSectionTitle(
    title: String
) {

    Text(
        text = title,
        modifier =
            Modifier.padding(
                start = 18.dp,
                top = 5.dp
            ),
        color = RenText,
        fontWeight =
            FontWeight.Bold,
        fontSize = 16.sp
    )
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            )
            .clickable(
                onClick = onClick
            ),
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = RenCard
            )
    ) {

        Row(
            modifier =
                Modifier.padding(16.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(43.dp)
                    .clip(
                        RoundedCornerShape(13.dp)
                    )
                    .background(
                        RenPurple.copy(
                            alpha = 0.13f
                        )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RenPurple
                )
            }

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    color = RenText,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text = subtitle,
                    color = RenMuted,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector =
                    Icons.Default.ArrowForward,
                contentDescription = null,
                tint = RenMuted
            )
        }
    }
}

/*
 * SECRET TRANSACTION CONTROL PAGE
 */
@Composable
fun TransactionControlScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onStatusChange: (
        transactionId: String,
        status: String
    ) -> Unit,
    onReceipt: (Transaction) -> Unit
) {

    Scaffold(
        containerColor = RenDark,
        topBar = {

            TopAppBar(
                title = {

                    Text(
                        text =
                            "Transaction Control",
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        ),
                    shape =
                        RoundedCornerShape(18.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                RenCard
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(18.dp)
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Settings,
                                contentDescription =
                                    null,
                                tint =
                                    RenOrange
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(9.dp)
                            )

                            Text(
                                text =
                                    "Local simulation control",
                                color = RenOrange,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "Use this page to change the displayed status of local RenMonie transactions.",
                            color = RenMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (transactions.isEmpty()) {

                item {

                    EmptyTransactions()
                }

            } else {

                items(
                    transactions,
                    key = {
                        it.id
                    }
                ) { transaction ->

                    ControlTransactionCard(
                        transaction =
                            transaction,
                        onStatusChange =
                            onStatusChange,
                        onReceipt = {
                            onReceipt(
                                transaction
                            )
                        }
                    )
                }
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
fun ControlTransactionCard(
    transaction: Transaction,
    onStatusChange: (
        transactionId: String,
        status: String
    ) -> Unit,
    onReceipt: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            ),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = RenCard
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            transaction.recipient
                                .ifBlank {
                                    transaction.type
                                },
                        color = RenText,
                        fontWeight =
                            FontWeight.Bold,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text =
                            transaction.type,
                        color = RenMuted,
                        fontSize = 11.sp
                    )

                    Text(
                        text =
                            naira(
                                transaction.amount
                            ),
                        color = RenText,
                        fontWeight =
                            FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                StatusBadge(
                    status =
                        transaction.status
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "Transaction ID: ${transaction.id}",
                color = RenMuted,
                fontSize = 9.sp
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {

                ControlStatusButton(
                    text = "Pending",
                    color = RenOrange,
                    active =
                        transaction.status ==
                                STATUS_PENDING,
                    onClick = {

                        onStatusChange(
                            transaction.id,
                            STATUS_PENDING
                        )
                    }
                )

                ControlStatusButton(
                    text = "Successful",
                    color = RenGreen,
                    active =
                        transaction.status ==
                                STATUS_SUCCESSFUL,
                    onClick = {

                        onStatusChange(
                            transaction.id,
                            STATUS_SUCCESSFUL
                        )
                    }
                )

                ControlStatusButton(
                    text = "Failed",
                    color = RenRed,
                    active =
                        transaction.status ==
                                STATUS_FAILED,
                    onClick = {

                        onStatusChange(
                            transaction.id,
                            STATUS_FAILED
                        )
                    }
                )
            }

            TextButton(
                onClick = onReceipt,
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ReceiptLong,
                    contentDescription =
                        null,
                    modifier =
                        Modifier.size(17.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(5.dp)
                )

                Text(
                    text = "View receipt"
                )
            }
        }
    }
}

@Composable
fun ControlStatusButton(
    text: String,
    color: Color,
    active: Boolean,
    onClick: () -> Unit
) {

    OutlinedButton(
        onClick = onClick,
        modifier =
            Modifier.width(100.dp),
        shape =
            RoundedCornerShape(9.dp),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(
                    horizontal = 2.dp
                ),
        colors =
            ButtonDefaults
                .outlinedButtonColors(
                    containerColor =
                        if (active) {
                            color.copy(
                                alpha = 0.18f
                            )
                        } else {
                            RenCard2
                        },
                    contentColor = color
                )
    ) {

        Text(
            text = text,
            fontSize = 8.sp,
            fontWeight =
                FontWeight.Bold
        )
    }
}

