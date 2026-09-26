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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReceiptDialog(
    receipt: ReceiptData,
    onDismiss: () -> Unit
) {

    var successPlayed by rememberSaveable { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue = if (successPlayed && receipt.status == STATUS_SUCCESSFUL) 1f else 0.2f,
        animationSpec = tween(durationMillis = 450),
        label = "receiptCheckScale"
    )

    LaunchedEffect(receipt.id) {
        successPlayed = false
        kotlinx.coroutines.delay(80)
        successPlayed = true
    }

    val context =
        LocalContext.current

    val clipboard =
        LocalClipboardManager.current

    val receiptStatusColor =
        statusColor(
            receipt.status
        )

    val receiptText =
        buildString {

            append("RENMONIE\n")
            append("Transaction Receipt\n\n")
            append("Transaction ID: ${receipt.id}\n")
            append("Reference: ${receipt.reference.ifBlank { receipt.id }}\n")
            append(
                "Type: ${receipt.type}\n"
            )
            append(
                "Recipient: ${receipt.recipient}\n"
            )
            append(
                "Account: ${maskAccount(receipt.account)}\n"
            )
            append("Account verification: ${receipt.recipient} verified\n")
            )
            append(
                "Bank: ${receipt.bank}\n"
            )
            append(
                "Amount: ${naira(receipt.amount)}\n"
            )
            append(
                "Narration: ${receipt.narration}\n"
            )
            append(
                "Date: ${receipt.date}\n"
            )
            append(
                "Status: ${receipt.status}"
            )
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RenCard,

        icon = {

            Icon(
                imageVector =
                    when (receipt.status) {

                        STATUS_PENDING ->
                            Icons.Default.Pending

                        STATUS_FAILED ->
                            Icons.Default.Error

                        else ->
                            Icons.Default.CheckCircle
                    },
                contentDescription =
                    null,
                tint =
                    receiptStatusColor,
                modifier =
                    Modifier
                        .size(52.dp)
                        .graphicsLayer {
                            scaleX = if (receipt.status == STATUS_SUCCESSFUL) checkScale else 1f
                            scaleY = if (receipt.status == STATUS_SUCCESSFUL) checkScale else 1f
                        }
            )
        },

        title = {

            Text(
                text =
                    "Transaction Receipt",
                color = RenText,
                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            Column {

                ReceiptAmount(
                    amount =
                        receipt.amount,
                    isCredit =
                        receipt.isCredit
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                ReceiptDetail(
                    label =
                        "Transaction ID",
                    value =
                        receipt.id
                )

                ReceiptDetail(
                    label = "Type",
                    value =
                        receipt.type
                )

                ReceiptDetail(
                    label = "Recipient",
                    value =
                        receipt.recipient
                )

                ReceiptDetail(
                    label = "Account",
                    value =
                        receipt.account
                )

                ReceiptDetail(
                    label =
                        "Bank / Network",
                    value =
                        receipt.bank
                )

                ReceiptDetail(
                    label = "Narration",
                    value =
                        receipt.narration
                )

                ReceiptDetail(
                    label = "Date",
                    value =
                        receipt.date
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            when (
                                receipt.status
                            ) {

                                STATUS_PENDING ->
                                    Icons.Default.Pending

                                STATUS_FAILED ->
                                    Icons.Default.Error

                                else ->
                                    Icons.Default.CheckCircle
                            },
                        contentDescription =
                            null,
                        tint =
                            receiptStatusColor,
                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(7.dp)
                    )

                    Text(
                        text =
                            receipt.status,
                        color =
                            receiptStatusColor,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        },

        confirmButton = {

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {

                TextButton(
                    onClick = {

                        clipboard.setText(
                            AnnotatedString(
                                receiptText
                            )
                        )

                        Toast.makeText(
                            context,
                            "Receipt copied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.ContentCopy,
                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(4.dp)
                    )

                    Text("Copy")
                }

                TextButton(
                    onClick = {

                        val shareIntent =
                            Intent(
                                Intent.ACTION_SEND
                            ).apply {

                                type =
                                    "text/plain"

                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    receiptText
                                )
                            }

                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share receipt"
                            )
                        )
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Share,
                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(4.dp)
                    )

                    Text("Share")
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RenPurple)
                ) {
                    Text(
                        text = "Back to Home",
                        color = RenText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

@Composable
fun ReceiptAmount(
    amount: Long,
    isCredit: Boolean
) {

    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                if (isCredit) {
                    "+${naira(amount)}"
                } else {
                    naira(amount)
                },
            color =
                if (isCredit) {
                    RenGreen
                } else {
                    RenText
                },
            fontSize = 29.sp,
            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                if (isCredit) {
                    "Money received"
                } else {
                    "Money sent"
                },
            color = RenMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ReceiptDetail(
    label: String,
    value: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 4.dp
            )
    ) {

        Text(
            text = label,
            color = RenMuted,
            fontSize = 10.sp
        )

        Text(
            text = value,
            color = RenText,
            fontSize = 13.sp,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}

fun statusColor(
    status: String
): Color {

    return when (status) {

        STATUS_PENDING ->
            RenOrange

        STATUS_FAILED ->
            RenRed

        else ->
            RenGreen
    }
}

@Composable
fun renTextFieldColors() =
    androidx.compose.material3
        .OutlinedTextFieldDefaults
        .colors(
            focusedTextColor =
                RenText,
            unfocusedTextColor =
                RenText,
            focusedBorderColor =
                RenPurple,
            unfocusedBorderColor =
                RenCard2,
            focusedLabelColor =
                RenPurple,
            unfocusedLabelColor =
                RenMuted,
            cursorColor =
                RenPurple,
            focusedLeadingIconColor =
                RenPurple,
            unfocusedLeadingIconColor =
                RenMuted
        )
\n\nprivate fun maskAccount(account: String): String {\n    if (account.length < 4) return account\n    return "******" + account.takeLast(4)\n}\n