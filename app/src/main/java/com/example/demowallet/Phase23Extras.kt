@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- Simulated name enquiry ----------

private val DEMO_NAMES = listOf(
    "CHINEDU OKAFOR",
    "ADEOLA BALOGUN",
    "FATIMA ABDULLAHI",
    "EMEKACHI NWOSU",
    "BLESSING ADEYEMI",
    "IBRAHIM MUSA",
    "NGOZI EZE",
    "TUNDE OLAWALE",
    "AMINA BELLO",
    "KELECHI UCHE"
)

/**
 * Demo name enquiry (NUBAN-style).
 * Returns empty string when the account cannot be resolved.
 * Rules for "not found" (so you can test failure):
 * - all zeros
 * - starts with 000
 * - ends with 0000
 */
fun simulateAccountName(accountNumber: String, bank: String): String {
    if (accountNumber.length != 10 || !accountNumber.all { it.isDigit() }) return ""
    if (bank.isBlank()) return ""
    if (accountNumber == "0000000000") return ""
    if (accountNumber.startsWith("000")) return ""
    if (accountNumber.endsWith("0000")) return ""
    val seed = (accountNumber + bank).hashCode().let { if (it < 0) -it else it }
    return DEMO_NAMES[seed % DEMO_NAMES.size]
}

fun isValidNubanFormat(accountNumber: String): Boolean =
    accountNumber.length == 10 && accountNumber.all { it.isDigit() }

// ---------- In-app notification centre ----------

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String, // success | failed | security | info
    val read: Boolean = false
)

object NotificationStore {
    private const val PREFS = "renmonie_notifications"
    private const val KEY = "items"

    fun add(context: Context, title: String, message: String, type: String = "info") {
        val list = load(context).toMutableList()
        val time = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        list.add(
            0,
            AppNotification(
                id = System.currentTimeMillis().toString(),
                title = title,
                message = message,
                time = time,
                type = type,
                read = false
            )
        )
        // keep last 50
        save(context, list.take(50))
    }

    fun load(context: Context): List<AppNotification> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        AppNotification(
                            id = o.optString("id"),
                            title = o.optString("title"),
                            message = o.optString("message"),
                            time = o.optString("time"),
                            type = o.optString("type", "info"),
                            read = o.optBoolean("read", false)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun markAllRead(context: Context) {
        save(context, load(context).map { it.copy(read = true) })
    }

    fun clear(context: Context) {
        save(context, emptyList())
    }

    private fun save(context: Context, list: List<AppNotification>) {
        val arr = JSONArray()
        list.forEach { n ->
            arr.put(
                JSONObject()
                    .put("id", n.id)
                    .put("title", n.title)
                    .put("message", n.message)
                    .put("time", n.time)
                    .put("type", n.type)
                    .put("read", n.read)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, arr.toString())
            .apply()
    }

    fun unreadCount(context: Context): Int = load(context).count { !it.read }
}

@Composable
fun NotificationCentreScreen(
    context: Context,
    onBack: () -> Unit
) {
    var items by remember { mutableStateOf(NotificationStore.load(context)) }

    LaunchedEffect(Unit) {
        NotificationStore.markAllRead(context)
        items = NotificationStore.load(context)
    }

    Scaffold(
        containerColor = RenDark,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = RenText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RenText)
                    }
                },
                actions = {
                    Text(
                        text = "Clear",
                        color = RenPurple,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable {
                                NotificationStore.clear(context)
                                items = emptyList()
                            }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RenDark,
                    titleContentColor = RenText
                )
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = RenMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No notifications yet", color = RenMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { n ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = RenCard)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (n.type) {
                                            "success" -> RenGreen.copy(alpha = 0.2f)
                                            "failed" -> RenRed.copy(alpha = 0.2f)
                                            "security" -> RenOrange.copy(alpha = 0.2f)
                                            else -> RenPurple.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when (n.type) {
                                        "success" -> Icons.Default.CheckCircle
                                        "security" -> Icons.Default.Security
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = null,
                                    tint = when (n.type) {
                                        "success" -> RenGreen
                                        "failed" -> RenRed
                                        "security" -> RenOrange
                                        else -> RenPurple
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(n.title, color = RenText, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(n.message, color = RenMuted, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(n.time, color = RenMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Processing overlay ----------

@Composable
fun ProcessingDialog(
    title: String = "Processing…",
    subtitle: String = "Please wait"
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RenCard)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = RenPurple)
                Spacer(modifier = Modifier.height(16.dp))
                Text(title, color = RenText, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(subtitle, color = RenMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun NameEnquiryBanner(
    verifying: Boolean,
    verifiedName: String?,
    bank: String = "",
    account: String = "",
    error: String?
) {
    when {
        verifying -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RenCard2)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = RenPurple
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Verifying account…", color = RenText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("Checking name with $bank", color = RenMuted, fontSize = 12.sp)
                }
            }
        }
        !verifiedName.isNullOrBlank() -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RenGreen.copy(alpha = 0.15f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RenGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Account verified", color = RenGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(verifiedName, color = RenText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (bank.isNotBlank() && account.isNotBlank()) {
                        Text("$bank · $account", color = RenMuted, fontSize = 12.sp)
                    }
                }
            }
        }
        !error.isNullOrBlank() -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RenRed.copy(alpha = 0.12f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = RenRed)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Verification failed", color = RenRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(error, color = RenMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

// ---------- About ----------

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = RenDark,
        topBar = {
            TopAppBar(
                title = { Text("About RenMonie", fontWeight = FontWeight.Bold, color = RenText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RenText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RenDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(RenPurple),
                contentAlignment = Alignment.Center
            ) {
                Text("RM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("RenMonie", color = RenText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Version 1.3.1", color = RenMuted)
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RenCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "RenMonie is a clearly marked banking-app simulator for UI/UX testing. It is not connected to a real bank and does not move real money.",
                        color = RenMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = RenCard2)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Demo features", color = RenText, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Transfers, airtime, data, bills\n• USSD simulator (*556#)\n• Cards, savings, insights\n• PIN security & themes", color = RenMuted, fontSize = 13.sp)
                }
            }
        }
    }
}

// ---------- Biometric prefs (mock) ----------

object SecurityPrefs {
    private const val PREFS = "renmonie_security_prefs"
    private const val BIOMETRIC = "biometric_enabled"

    fun isBiometricEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(BIOMETRIC, false)

    fun setBiometric(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(BIOMETRIC, enabled)
            .apply()
    }
}


@Composable
fun AddMoneyScreen(
    onBack: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val value = amount.toLongOrNull() ?: 0L
    val quick = listOf(500L, 1000L, 2000L, 5000L, 10000L, 20000L)

    Scaffold(
        containerColor = RenDark,
        topBar = {
            TopAppBar(
                title = { Text("Add Money", fontWeight = FontWeight.Bold, color = RenText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RenText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RenDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Fund your RenMonie wallet (demo). Amount is added to your balance in kobo.",
                color = RenMuted,
                fontSize = 13.sp
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { ch -> ch.isDigit() }.take(9) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount (₦)") },
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quick.take(3).forEach { q ->
                    Button(
                        onClick = { amount = q.toString() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RenCard2)
                    ) {
                        Text("₦$q", color = RenText, fontSize = 12.sp)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quick.drop(3).forEach { q ->
                    Button(
                        onClick = { amount = q.toString() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RenCard2)
                    ) {
                        Text("₦$q", color = RenText, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (value > 0L) onConfirm(value)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RenPurple),
                enabled = value > 0L
            ) {
                Text("Add ₦$value to wallet", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RenCard2)
            ) {
                Text("Back to Home", color = RenText)
            }
        }
    }
}

/**
 * Full-screen success celebration shown after transfer / payment,
 * before the detailed receipt dialog.
 */
@Composable
fun TransferSuccessAnimation(
    title: String = "Transfer successful",
    subtitle: String = "Your money is on its way",
    amountLabel: String = "",
    onFinished: () -> Unit
) {
    val scale = remember { androidx.compose.animation.core.Animatable(0.3f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(300))
        scale.animateTo(1.15f, animationSpec = tween(400))
        scale.animateTo(1f, animationSpec = tween(200))
        delay(1100)
        alpha.animateTo(0f, animationSpec = tween(250))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE0A0F0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(RenGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(RenGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = title,
                color = RenText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (amountLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = amountLabel,
                    color = RenGreen,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subtitle,
                color = RenMuted,
                fontSize = 14.sp
            )
        }
    }
}
