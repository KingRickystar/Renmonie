@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemeSettingsScreen(
    context: Context,
    onBack: () -> Unit
) {
    var selected by remember {
        mutableStateOf(
            context.getSharedPreferences("renmonie_theme_v13", Context.MODE_PRIVATE)
                .getString("theme_preset", "opay_green") ?: "opay_green"
        )
    }

    Scaffold(
        containerColor = RenDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Theme & Appearance",
                        fontWeight = FontWeight.Bold,
                        color = RenText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RenText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RenDark,
                    titleContentColor = RenText,
                    navigationIconContentColor = RenText
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = RenCard)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Dashboard theme colours",
                            color = RenText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Choose a colour for your home screen and app accents.",
                            color = RenMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            items(RenThemePresets) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selected = preset.id
                            RenThemeStore.save(context, preset)
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected == preset.id) RenCard2 else RenCard
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(preset.preview)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(preset.name, color = RenText, fontWeight = FontWeight.SemiBold)
                            Text(preset.description, color = RenMuted, fontSize = 12.sp)
                        }
                        if (selected == preset.id) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = preset.primary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun AccountScreen(
    context: Context,
    onBack: () -> Unit
) {
    val prefs = context.getSharedPreferences("renmonie_account", Context.MODE_PRIVATE)

    var name by rememberSaveable {
        mutableStateOf(prefs.getString("name", "Patrick") ?: "Patrick")
    }
    var phone by rememberSaveable {
        mutableStateOf(prefs.getString("phone", "08000000000") ?: "08000000000")
    }
    var email by rememberSaveable {
        mutableStateOf(prefs.getString("email", "user@renmonie.app") ?: "user@renmonie.app")
    }
    var saved by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = RenDark,
        topBar = {
            TopAppBar(
                title = {
                    Text("Account & Login", fontWeight = FontWeight.Bold, color = RenText)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RenText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RenDark,
                    titleContentColor = RenText,
                    navigationIconContentColor = RenText
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = RenCard)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(RenPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).ifBlank { "R" },
                                color = RenText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            name.ifBlank { "RenMonie User" },
                            color = RenText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Personal Wallet Account", color = RenMuted, fontSize = 13.sp)
                    }
                }
            }

            item {
                Text("Personal details", color = RenText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full name") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true
                )
            }

            item {
                Button(
                    onClick = {
                        prefs.edit()
                            .putString("name", name)
                            .putString("phone", phone)
                            .putString("email", email)
                            .apply()
                        saved = true
                        Toast.makeText(context, "Account saved", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RenPurple)
                ) {
                    Text(if (saved) "Saved" else "Save changes")
                }
            }
        }
    }
}

data class BillService(
    val name: String,
    val category: String,
    val icon: ImageVector
)

@Composable
fun BillsScreen(
    balance: Long,
    onBack: () -> Unit,
    onPay: (String, String, Long) -> Unit
) {
    val services = listOf(
        BillService("Electricity (Prepaid)", "Utilities", Icons.Default.Bolt),
        BillService("DSTV / Cable", "TV", Icons.Default.Tv),
        BillService("Internet", "Data", Icons.Default.Wifi),
        BillService("Airtime (Other)", "Mobile", Icons.Default.Phone)
    )

    var selected by remember { mutableStateOf<BillService?>(null) }
    var customer by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = RenDark,
        topBar = {
            TopAppBar(
                title = {
                    Text("Bills & Services", fontWeight = FontWeight.Bold, color = RenText)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RenText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RenDark,
                    titleContentColor = RenText,
                    navigationIconContentColor = RenText
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = RenCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Wallet balance", color = RenMuted, fontSize = 12.sp)
                        Text("₦$balance", color = RenText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(services) { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = service },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RenCard)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(service.icon, contentDescription = null, tint = RenPurple)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, color = RenText, fontWeight = FontWeight.SemiBold)
                            Text(service.category, color = RenMuted, fontSize = 12.sp)
                        }
                        Text("Pay", color = RenPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val current = selected
    if (current != null) {
        AlertDialog(
            onDismissRequest = { selected = null },
            containerColor = RenCard,
            title = {
                Text("${current.name} payment", color = RenText, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter the customer / meter / account reference and amount.",
                        color = RenMuted,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = customer,
                        onValueChange = { customer = it },
                        label = { Text("Customer / meter number") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { value ->
                            if (value.length <= 8 && value.all { ch -> ch.isDigit() }) {
                                amount = value
                            }
                        },
                        label = { Text("Amount (₦)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = amount.toLongOrNull() ?: 0L
                        if (customer.isBlank() || value <= 0L) {
                            return@Button
                        }
                        onPay(current.name, customer, value)
                        selected = null
                        customer = ""
                        amount = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RenPurple)
                ) {
                    Text("Pay")
                }
            },
            dismissButton = {
                TextButton(onClick = { selected = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PinVerificationDialog(
    onDismiss: () -> Unit,
    onVerified: () -> Unit,
    context: Context
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RenCard,
        title = {
            Text("Enter transaction PIN", color = RenText, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { value ->
                        if (value.length <= 4 && value.all { ch -> ch.isDigit() }) {
                            pin = value
                            error = ""
                        }
                    },
                    label = { Text("4-digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (error.isNotBlank()) {
                    Text(error, color = RenRed, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length != 4) {
                        error = "Enter 4 digits"
                        return@Button
                    }
                    if (RenMonieStorage.verifyPin(context, pin)) {
                        onVerified()
                    } else {
                        error = "Incorrect PIN"
                        pin = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RenPurple)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
