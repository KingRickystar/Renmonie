package com.example.demowallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ReceiptPurple = Color(0xFF5B2EFF)
private val ReceiptGreen = Color(0xFF159447)
private val ReceiptBackground = Color(0xFF171222)
private val ReceiptGreenBackground = Color(0xFFEAF8EF)
private val ReceiptGrey = Color(0xFF858585)
private val ReceiptDark = Color(0xFF171222)

@Composable
fun ReceiptScreen(
    bankName: String,
    accountNumber: String,
    accountName: String,
    amount: Long,
    narration: String,
    onBack: () -> Unit
) {

    val transactionReference = remember {
        "RM${System.currentTimeMillis().toString().takeLast(10)}"
    }

    val transactionDate = remember {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date())
    }

    val maskedAccount = remember(accountNumber) {
        if (accountNumber.length >= 4) {
            "******" + accountNumber.takeLast(4)
        } else {
            accountNumber
        }
    }

    val formattedAmount = String.format(
        Locale.getDefault(),
        "%,.2f",
        amount.toDouble()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReceiptBackground)
    ) {

        // TOP BAR

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Transaction Receipt",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // RECEIPT

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 25.dp
                )
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(22.dp)
                ) {

                    // RENMONIE HEADER

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Card(
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ReceiptPurple
                            )
                        ) {

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {

                                Text(
                                    text = "R",
                                    color = Color.White,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.width(14.dp)
                        )

                        Column {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "Ren",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ReceiptDark
                                )

                                Text(
                                    text = "Monie",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ReceiptPurple
                                )
                            }

                            Text(
                                text = "Smart. Secure. Simulated.",
                                fontSize = 13.sp,
                                color = ReceiptGrey
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    HorizontalDivider(
                        thickness = 3.dp,
                        color = ReceiptPurple
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    // RECEIPT TITLE

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "TRANSACTION RECEIPT",
                            color = ReceiptPurple,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = transactionDate,
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    Text(
                        text = "Thank you for using RenMonie",
                        color = ReceiptGrey,
                        fontSize = 15.sp
                    )

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    // SUCCESS CARD

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ReceiptGreenBackground
                        )
                    ) {

                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Successful",
                                tint = ReceiptGreen,
                                modifier = Modifier.size(42.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(14.dp)
                            )

                            Column {

                                Text(
                                    text = "Transaction Successful",
                                    color = ReceiptGreen,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(
                                    modifier = Modifier.height(3.dp)
                                )

                                Text(
                                    text = "Your transaction was completed successfully.",
                                    color = ReceiptGrey,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(25.dp)
                    )

                    // AMOUNT

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "AMOUNT",
                            color = ReceiptGrey,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "₦$formattedAmount",
                            color = ReceiptDark,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Card(
                            shape = RoundedCornerShape(25.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF0E9FF)
                            )
                        ) {

                            Text(
                                text = "Transfer",
                                color = ReceiptPurple,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 20.dp,
                                    vertical = 10.dp
                                )
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(25.dp)
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    // TRANSACTION DETAILS

                    Text(
                        text = "TRANSACTION DETAILS",
                        color = ReceiptPurple,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    ReceiptDetail(
                        label = "Transaction Type",
                        value = "Transfer"
                    )

                    ReceiptDetail(
                        label = "Recipient",
                        value = accountName
                    )

                    ReceiptDetail(
                        label = "Bank",
                        value = bankName
                    )

                    ReceiptDetail(
                        label = "Account Number",
                        value = maskedAccount
                    )

                    ReceiptDetail(
                        label = "Amount",
                        value = "₦$formattedAmount"
                    )

                    ReceiptDetail(
                        label = "Transaction Fee",
                        value = "₦0.00"
                    )

                    ReceiptDetail(
                        label = "Total Debited",
                        value = "₦$formattedAmount"
                    )

                    ReceiptDetail(
                        label = "Narration",
                        value = if (narration.isBlank()) {
                            "Transfer"
                        } else {
                            narration
                        }
                    )

                    ReceiptDetail(
                        label = "Status",
                        value = "Successful",
                        valueColor = ReceiptGreen
                    )

                    ReceiptDetail(
                        label = "Reference",
                        value = transactionReference
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    // TRANSACTION REFERENCE

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF6F3FF)
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(15.dp)
                        ) {

                            Text(
                                text = "Transaction Reference",
                                color = ReceiptGrey,
                                fontSize = 13.sp
                            )

                            Spacer(
                                modifier = Modifier.height(5.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = transactionReference,
                                    color = ReceiptDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                TextButton(
                                    onClick = {}
                                ) {
                                    Text(
                                        text = "Copy",
                                        color = ReceiptPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(22.dp)
                    )

                    // ACTION BUTTONS

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text = "Share"
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ReceiptPurple
                            )
                        ) {

                            Text(
                                text = "Done",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "This is a simulated RenMonie transaction receipt.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = ReceiptGrey,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptDetail(
    label: String,
    value: String,
    valueColor: Color = ReceiptDark
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            color = ReceiptGrey,
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        HorizontalDivider(
            color = Color(0xFFE1E1E1)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )
    }
}