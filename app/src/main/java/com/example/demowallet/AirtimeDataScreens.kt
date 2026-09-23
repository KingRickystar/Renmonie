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
fun AirtimeScreen(
    balance: Long,
    onBack: () -> Unit,
    onPurchase: (
        network: String,
        phone: String,
        amount: Long
    ) -> Unit
) {

    var network by rememberSaveable {
        mutableStateOf("MTN")
    }

    var phone by rememberSaveable {
        mutableStateOf("")
    }

    var amountText by rememberSaveable {
        mutableStateOf("")
    }

    val amount =
        amountText.toLongOrNull() ?: 0L

    Scaffold(
        containerColor = RenDark,
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = "Airtime",
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
                Arrangement.spacedBy(16.dp)
        ) {

            item {
                ServiceBalanceCard(
                    balance
                )
            }

            item {
                ServiceTitle(
                    "Select network"
                )
            }

            item {

                NetworkSelector(
                    selected = network,
                    onSelected = {
                        network = it
                    }
                )
            }

            item {

                OutlinedTextField(
                    value = phone,
                    onValueChange = {

                        if (
                            it.length <= 11 &&
                            it.all {
                                c -> c.isDigit()
                            }
                        ) {
                            phone = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        ),
                    label = {
                        Text("Phone number")
                    },
                    leadingIcon = {

                        Icon(
                            imageVector =
                                Icons.Default.Phone,
                            contentDescription =
                                null
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Phone
                        ),
                    singleLine = true,
                    shape =
                        RoundedCornerShape(14.dp),
                    colors =
                        renTextFieldColors()
                )
            }

            item {
                ServiceTitle(
                    "Choose amount"
                )
            }

            item {

                QuickServiceAmounts(
                    amounts =
                        listOf(
                            500L,
                            1000L,
                            2000L,
                            5000L
                        ),
                    onSelected = {
                        amountText =
                            it.toString()
                    }
                )
            }

            item {

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {

                        if (
                            it.isEmpty() ||
                            it.all {
                                c -> c.isDigit()
                            }
                        ) {
                            amountText = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        ),
                    label = {
                        Text("Amount")
                    },
                    leadingIcon = {

                        Text(
                            text = "₦",
                            color = RenPurple,
                            fontWeight =
                                FontWeight.Bold
                        )
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),
                    singleLine = true,
                    shape =
                        RoundedCornerShape(14.dp),
                    colors =
                        renTextFieldColors()
                )
            }

            item {

                Button(
                    onClick = {

                        onPurchase(
                            network,
                            phone,
                            amount
                        )
                    },
                    enabled =
                        phone.length >= 10 &&
                                amount > 0L &&
                                amount <= balance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        )
                        .height(55.dp),
                    shape =
                        RoundedCornerShape(15.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                RenPurple
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Phone,
                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text = "Buy Airtime",
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DataScreen(
    balance: Long,
    onBack: () -> Unit,
    onPurchase: (
        network: String,
        bundle: String,
        amount: Long
    ) -> Unit
) {

    var network by rememberSaveable {
        mutableStateOf("MTN")
    }

    Scaffold(
        containerColor = RenDark,
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = "Data",
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
                Arrangement.spacedBy(15.dp)
        ) {

            item {
                ServiceBalanceCard(
                    balance
                )
            }

            item {
                ServiceTitle(
                    "Select network"
                )
            }

            item {

                NetworkSelector(
                    selected = network,
                    onSelected = {
                        network = it
                    }
                )
            }

            item {
                ServiceTitle(
                    "Data bundles"
                )
            }

            item {

                DataBundleCard(
                    network = network,
                    bundle = "1 GB",
                    amount = 500L,
                    onClick = {

                        onPurchase(
                            network,
                            "1 GB",
                            500L
                        )
                    },
                    enabled =
                        balance >= 500L
                )
            }

            item {

                DataBundleCard(
                    network = network,
                    bundle = "2 GB",
                    amount = 1000L,
                    onClick = {

                        onPurchase(
                            network,
                            "2 GB",
                            1000L
                        )
                    },
                    enabled =
                        balance >= 1000L
                )
            }

            item {

                DataBundleCard(
                    network = network,
                    bundle = "5 GB",
                    amount = 2000L,
                    onClick = {

                        onPurchase(
                            network,
                            "5 GB",
                            2000L
                        )
                    },
                    enabled =
                        balance >= 2000L
                )
            }

            item {

                DataBundleCard(
                    network = network,
                    bundle = "10 GB",
                    amount = 3500L,
                    onClick = {

                        onPurchase(
                            network,
                            "10 GB",
                            3500L
                        )
                    },
                    enabled =
                        balance >= 3500L
                )
            }

            item {

                DataBundleCard(
                    network = network,
                    bundle = "20 GB",
                    amount = 5000L,
                    onClick = {

                        onPurchase(
                            network,
                            "20 GB",
                            5000L
                        )
                    },
                    enabled =
                        balance >= 5000L
                )
            }
        }
    }
}

@Composable
fun ServiceBalanceCard(
    balance: Long
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape =
            RoundedCornerShape(19.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = RenCard
            )
    ) {

        Row(
            modifier =
                Modifier.padding(18.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    Icons.Default.Wallet,
                contentDescription = null,
                tint = RenPurple
            )

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Column {

                Text(
                    text =
                        "Available balance",
                    color = RenMuted,
                    fontSize = 12.sp
                )

                Text(
                    text = naira(balance),
                    color = RenText,
                    fontSize = 21.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ServiceTitle(
    title: String
) {

    Text(
        text = title,
        modifier =
            Modifier.padding(
                start = 18.dp,
                end = 18.dp,
                top = 4.dp
            ),
        color = RenText,
        fontSize = 17.sp,
        fontWeight =
            FontWeight.Bold
    )
}

@Composable
fun NetworkSelector(
    selected: String,
    onSelected: (String) -> Unit
) {

    val networks =
        listOf(
            "MTN",
            "Airtel",
            "Glo",
            "9mobile"
        )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            ),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        networks.forEach { network ->

            val active =
                network == selected

            OutlinedButton(
                onClick = {
                    onSelected(network)
                },
                modifier =
                    Modifier.width(82.dp),
                shape =
                    RoundedCornerShape(12.dp),
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
                    text = network,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun QuickServiceAmounts(
    amounts: List<Long>,
    onSelected: (Long) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            ),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        amounts.forEach { amount ->

            OutlinedButton(
                onClick = {
                    onSelected(amount)
                },
                modifier =
                    Modifier.width(78.dp),
                shape =
                    RoundedCornerShape(12.dp),
                contentPadding =
                    androidx.compose.foundation.layout
                        .PaddingValues(
                            horizontal = 3.dp
                        ),
                colors =
                    ButtonDefaults
                        .outlinedButtonColors(
                            containerColor =
                                RenCard,
                            contentColor =
                                RenText
                        )
            ) {

                Text(
                    text = naira(
                        amount * 100L
                    ),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun DataBundleCard(
    network: String,
    bundle: String,
    amount: Long,
    onClick: () -> Unit,
    enabled: Boolean
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape =
            RoundedCornerShape(17.dp),
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

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(
                        RenPurple.copy(
                            alpha = 0.14f
                        )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.DataUsage,
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
                    text =
                        "$network $bundle",
                    color = RenText,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Internet data bundle",
                    color = RenMuted,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onClick,
                enabled = enabled,
                shape =
                    RoundedCornerShape(10.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            RenPurple
                    )
            ) {

                Text(
                    text =
                        naira(
                            amount * 100L
                        ),
                    fontSize = 11.sp
                )
            }
        }
    }
}

