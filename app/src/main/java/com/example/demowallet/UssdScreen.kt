@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * In-app USSD simulator for RenMonie.
 * Code: *556#
 * Fully local — does not dial the real network.
 */
private enum class UssdStep {
    MAIN,
    BALANCE,
    TRANSFER_AMOUNT,
    TRANSFER_ACCOUNT,
    TRANSFER_CONFIRM,
    AIRTIME_AMOUNT,
    AIRTIME_PHONE,
    AIRTIME_CONFIRM,
    DATA_MENU,
    SUCCESS,
    ERROR
}

@Composable
fun UssdScreen(
    balanceNaira: Long,
    onBack: () -> Unit,
    onTransfer: (account: String, amount: Long, narration: String) -> Unit,
    onAirtime: (phone: String, amount: Long) -> Unit,
    onRequirePin: (action: () -> Unit) -> Unit
) {
    var step by remember { mutableStateOf(UssdStep.MAIN) }
    var message by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var accountText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }

    fun resetToMain() {
        step = UssdStep.MAIN
        message = ""
        amountText = ""
        accountText = ""
        phoneText = ""
        input = ""
    }

    fun formatNaira(v: Long): String {
        return "₦" + "%,d".format(v)
    }

    val screenText: String = when (step) {
        UssdStep.MAIN -> """
            RenMonie USSD
            *556#
            
            1. Balance
            2. Transfer
            3. Airtime
            4. Data
            0. Exit
            
            Reply with a number
        """.trimIndent()

        UssdStep.BALANCE -> """
            RenMonie Balance
            
            Available:
            ${formatNaira(balanceNaira)}
            
            Account: 8094****4821
            
            1. Main menu
            0. Exit
        """.trimIndent()

        UssdStep.TRANSFER_AMOUNT -> """
            Transfer
            
            Enter amount (₦):
            
            (numbers only)
        """.trimIndent()

        UssdStep.TRANSFER_ACCOUNT -> """
            Transfer ${formatNaira(amountText.toLongOrNull() ?: 0L)}
            
            Enter 10-digit
            account number:
        """.trimIndent()

        UssdStep.TRANSFER_CONFIRM -> """
            Confirm Transfer
            
            Amount: ${formatNaira(amountText.toLongOrNull() ?: 0L)}
            Account: $accountText
            Bank: Other Bank
            
            1. Confirm
            2. Cancel
        """.trimIndent()

        UssdStep.AIRTIME_AMOUNT -> """
            Buy Airtime
            
            Enter amount (₦):
        """.trimIndent()

        UssdStep.AIRTIME_PHONE -> """
            Airtime ${formatNaira(amountText.toLongOrNull() ?: 0L)}
            
            Enter phone number:
            (11 digits)
        """.trimIndent()

        UssdStep.AIRTIME_CONFIRM -> """
            Confirm Airtime
            
            Amount: ${formatNaira(amountText.toLongOrNull() ?: 0L)}
            Phone: $phoneText
            
            1. Confirm
            2. Cancel
        """.trimIndent()

        UssdStep.DATA_MENU -> """
            Buy Data
            
            1. ₦500 - 1.5GB
            2. ₦1000 - 3GB
            3. ₦2000 - 7GB
            0. Main menu
        """.trimIndent()

        UssdStep.SUCCESS -> """
            Successful
            
            $message
            
            1. Main menu
            0. Exit
        """.trimIndent()

        UssdStep.ERROR -> """
            Failed
            
            $message
            
            1. Main menu
            0. Exit
        """.trimIndent()
    }

    fun handleReply(reply: String) {
        val r = reply.trim()
        when (step) {
            UssdStep.MAIN -> when (r) {
                "1" -> step = UssdStep.BALANCE
                "2" -> {
                    amountText = ""
                    step = UssdStep.TRANSFER_AMOUNT
                }
                "3" -> {
                    amountText = ""
                    step = UssdStep.AIRTIME_AMOUNT
                }
                "4" -> step = UssdStep.DATA_MENU
                "0" -> onBack()
                else -> {
                    message = "Invalid option"
                    step = UssdStep.ERROR
                }
            }

            UssdStep.BALANCE -> when (r) {
                "1" -> resetToMain()
                "0" -> onBack()
                else -> Unit
            }

            UssdStep.TRANSFER_AMOUNT -> {
                val amt = r.toLongOrNull()
                if (amt == null || amt <= 0L) {
                    message = "Invalid amount"
                    step = UssdStep.ERROR
                } else if (amt > balanceNaira) {
                    message = "Insufficient balance"
                    step = UssdStep.ERROR
                } else {
                    amountText = amt.toString()
                    accountText = ""
                    step = UssdStep.TRANSFER_ACCOUNT
                }
            }

            UssdStep.TRANSFER_ACCOUNT -> {
                if (r.length != 10 || !r.all { it.isDigit() }) {
                    message = "Enter valid 10-digit account"
                    step = UssdStep.ERROR
                } else {
                    accountText = r
                    step = UssdStep.TRANSFER_CONFIRM
                }
            }

            UssdStep.TRANSFER_CONFIRM -> when (r) {
                "1" -> {
                    val amt = amountText.toLongOrNull() ?: 0L
                    onRequirePin {
                        onTransfer(accountText, amt, "USSD Transfer")
                        message = "Sent ${formatNaira(amt)}\nto $accountText"
                        step = UssdStep.SUCCESS
                    }
                }
                "2" -> resetToMain()
                else -> Unit
            }

            UssdStep.AIRTIME_AMOUNT -> {
                val amt = r.toLongOrNull()
                if (amt == null || amt <= 0L) {
                    message = "Invalid amount"
                    step = UssdStep.ERROR
                } else if (amt > balanceNaira) {
                    message = "Insufficient balance"
                    step = UssdStep.ERROR
                } else {
                    amountText = amt.toString()
                    phoneText = ""
                    step = UssdStep.AIRTIME_PHONE
                }
            }

            UssdStep.AIRTIME_PHONE -> {
                if (r.length != 11 || !r.all { it.isDigit() }) {
                    message = "Enter valid 11-digit phone"
                    step = UssdStep.ERROR
                } else {
                    phoneText = r
                    step = UssdStep.AIRTIME_CONFIRM
                }
            }

            UssdStep.AIRTIME_CONFIRM -> when (r) {
                "1" -> {
                    val amt = amountText.toLongOrNull() ?: 0L
                    onRequirePin {
                        onAirtime(phoneText, amt)
                        message = "Airtime ${formatNaira(amt)}\nto $phoneText"
                        step = UssdStep.SUCCESS
                    }
                }
                "2" -> resetToMain()
                else -> Unit
            }

            UssdStep.DATA_MENU -> when (r) {
                "1", "2", "3" -> {
                    val amt = when (r) {
                        "1" -> 500L
                        "2" -> 1000L
                        else -> 2000L
                    }
                    val bundle = when (r) {
                        "1" -> "1.5GB"
                        "2" -> "3GB"
                        else -> "7GB"
                    }
                    if (amt > balanceNaira) {
                        message = "Insufficient balance"
                        step = UssdStep.ERROR
                    } else {
                        onRequirePin {
                            onAirtime("08000000000", amt) // reuse path as data-like debit for demo
                            message = "Data $bundle\n${formatNaira(amt)} debited"
                            step = UssdStep.SUCCESS
                        }
                    }
                }
                "0" -> resetToMain()
                else -> Unit
            }

            UssdStep.SUCCESS, UssdStep.ERROR -> when (r) {
                "1" -> resetToMain()
                "0" -> onBack()
                else -> Unit
            }
        }
        input = ""
    }

    val needsFreeText = step == UssdStep.TRANSFER_AMOUNT ||
        step == UssdStep.TRANSFER_ACCOUNT ||
        step == UssdStep.AIRTIME_AMOUNT ||
        step == UssdStep.AIRTIME_PHONE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "USSD  *556#",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // USSD panel (classic look)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF111111))
                    .padding(16.dp)
            ) {
                Text(
                    text = screenText,
                    color = Color(0xFF00FF66),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (needsFreeText) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { ch -> ch.isDigit() }.take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type reply…", color = Color.Gray) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { if (input.isNotBlank()) handleReply(input) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RenPurple)
                ) {
                    Text("Send")
                }
            } else {
                // Number pad for menu choices
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "OK")
                )
                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (key.isEmpty()) Color.Transparent
                                        else Color(0xFF1A1A1A)
                                    )
                                    .clickable(enabled = key.isNotEmpty()) {
                                        when (key) {
                                            "OK" -> if (input.isNotBlank()) handleReply(input)
                                            else -> {
                                                input += key
                                                // auto-send single digit menus
                                                if (step != UssdStep.TRANSFER_AMOUNT &&
                                                    step != UssdStep.TRANSFER_ACCOUNT &&
                                                    step != UssdStep.AIRTIME_AMOUNT &&
                                                    step != UssdStep.AIRTIME_PHONE
                                                ) {
                                                    handleReply(input)
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key.isNotEmpty()) {
                                    Text(
                                        text = key,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            TextButton(
                onClick = { resetToMain() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Back to main menu", color = Color.Gray)
            }
        }
    }
}
