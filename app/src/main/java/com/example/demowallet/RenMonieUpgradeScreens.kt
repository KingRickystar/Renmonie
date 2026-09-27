@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemeSettingsScreen(
    context: Context,
    onBack: () -> Unit
) {
    var selectedId by rememberSaveable {
        mutableStateOf(RenThemeStore.load(context).id)
    }

    Scaffold(
        containerColor = RenDark,
        topBar = {
            TopAppBar(
                title = {
                    Text("Theme & Appearance", fontWeight = FontWeight.Bold, color = RenText)
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
                Text(
                    "Choose your RenMonie colour", 
                    color = RenText, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp
                )
            }
            items(RenThemePresets) { preset ->
                val active = selectedId == preset.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedId = preset.id
                            RenThemeStore.save(context, preset)
                            Toast.makeText(context, "${preset.name} applied", Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) RenPurple.copy(alpha = 0.15f) else RenCard
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(preset.primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(preset.name, color = RenText, fontWeight = FontWeight.SemiBold)
                            Text(preset.description, color = RenMuted, fontSize = 12.sp)
                        }
                        if (active) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RenPurple)
                        }
                    }
                }
            }
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
                val profileResId = context.resources.getIdentifier(
                    "ic_profile",
                    "drawable",
                    context.packageName
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = RenCard)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(RenPurple.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileResId != 0) {
                                Image(
                                    painter = painterResource(id = profileResId),
                                    contentDescription = "Profile photo",
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(CircleShape)
                                        .background(RenPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(1).ifBlank { "R" },
                                        color = Color.White,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            name.ifBlank { "RenMonie User" },
                            color = RenText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("RenMonie MFB · Personal account", color = RenMuted, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "8094 •••• 4821",
                            color = RenPurple,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
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
        BillService("Airtime Top-up", "Mobile", Icons.Default.Phone)
    )

    var selected by rememberSaveable { mutableStateOf(services.first().name) }
    var meter by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var showConfirm by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = RenDark,
        topBar = {
            TopAppBar(
                title = { Text("Bills & Services", fontWeight = FontWeight.Bold, color = RenText) },
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
                Text("Choose a service", color = RenText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            items(services) { service ->
                val active = selected == service.name
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = service.name },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) RenPurple.copy(alpha = 0.15f) else RenCard
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(service.icon, contentDescription = null, tint = RenPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, color = RenText, fontWeight = FontWeight.SemiBold)
                            Text(service.category, color = RenMuted, fontSize = 12.sp)
                        }
                        if (active) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RenPurple)
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = meter,
                    onValueChange = { meter = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Meter / Account / Smartcard") },
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() }.take(8) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount (₦)") },
                    singleLine = true
                )
            }
            item {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = meter.isNotBlank() && (amount.toLongOrNull() ?: 0L) > 0L,
                    colors = ButtonDefaults.buttonColors(containerColor = RenPurple)
                ) {
                    Text("Continue")
                }
            }
        }

        if (showConfirm) {
            val amt = (amount.toLongOrNull() ?: 0L) * 100L
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("Confirm bill payment") },
                text = {
                    Text("Pay ₦$amount for $selected\nRef: $meter")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirm = false
                            onPay(selected, meter, amt)
                        }
                    ) { Text("Pay now") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun PinVerificationDialog(
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter PIN", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Confirm with your 4-digit wallet PIN", color = RenMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            pin = it
                            error = ""
                        }
                    },
                    label = { Text("PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(error, color = RenRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (pin.length != 4) {
                        error = "Enter 4 digits"
                    } else if (RenMonieStorage.verifyPin(context, pin)) {
                        onVerified()
                    } else {
                        error = "Incorrect PIN"
                        pin = ""
                    }
                }
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
