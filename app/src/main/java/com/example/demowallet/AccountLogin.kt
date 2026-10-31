package com.example.demowallet

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest

/** Local account login (simulator — credentials stay on device). */
object AccountAuth {
    private const val USER_KEY = "renmonie_account_username"
    private const val EMAIL_KEY = "renmonie_account_email"
    private const val PASS_HASH_KEY = "renmonie_account_pass_hash"
    private const val HAS_ACCOUNT_KEY = "renmonie_has_account"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun hasAccount(context: Context): Boolean =
        prefs(context).getBoolean(HAS_ACCOUNT_KEY, false)

    fun savedUsername(context: Context): String =
        prefs(context).getString(USER_KEY, "") ?: ""

    fun savedEmail(context: Context): String =
        prefs(context).getString(EMAIL_KEY, "") ?: ""

    fun register(
        context: Context,
        username: String,
        email: String,
        password: String
    ): String? {
        val u = username.trim()
        val e = email.trim()
        val p = password
        if (u.length < 3) return "Username must be at least 3 characters"
        if (!e.contains("@") || e.length < 5) return "Enter a valid email"
        if (p.length < 6) return "Password must be at least 6 characters"
        prefs(context).edit()
            .putString(USER_KEY, u)
            .putString(EMAIL_KEY, e.lowercase())
            .putString(PASS_HASH_KEY, hashPassword(p))
            .putBoolean(HAS_ACCOUNT_KEY, true)
            .apply()
        return null
    }

    fun login(context: Context, userOrEmail: String, password: String): Boolean {
        val p = prefs(context)
        if (!p.getBoolean(HAS_ACCOUNT_KEY, false)) return false
        val id = userOrEmail.trim().lowercase()
        val user = (p.getString(USER_KEY, "") ?: "").lowercase()
        val email = (p.getString(EMAIL_KEY, "") ?: "").lowercase()
        if (id != user && id != email) return false
        val stored = p.getString(PASS_HASH_KEY, "") ?: return false
        return stored == hashPassword(password)
    }
}

@Composable
fun AccountLoginScreen(
    context: Context,
    onSuccess: () -> Unit,
    onNeedPinSetup: () -> Unit = {}
) {
    var isRegister by rememberSaveable {
        mutableStateOf(!AccountAuth.hasAccount(context))
    }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var userOrEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegister) "Create RenMonie account" else "Sign in to RenMonie",
            color = RenText,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Text(
            text = if (isRegister)
                "Username, email and password (saved on this phone only)"
            else
                "Use your username or email and password",
            color = RenMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        if (isRegister) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; error = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        } else {
            OutlinedTextField(
                value = userOrEmail,
                onValueChange = { userOrEmail = it; error = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username or email") },
                singleLine = true
            )
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = "" },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (isRegister) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it; error = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }

        if (error.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(error, color = RenRed, fontSize = 13.sp)
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                if (isRegister) {
                    if (password != confirm) {
                        error = "Passwords do not match"
                        return@Button
                    }
                    val err = AccountAuth.register(context, username, email, password)
                    if (err != null) {
                        error = err
                        return@Button
                    }
                    if (!RenMonieStorage.hasPin(context)) {
                        onNeedPinSetup()
                    } else {
                        onSuccess()
                    }
                } else {
                    if (AccountAuth.login(context, userOrEmail, password)) {
                        onSuccess()
                    } else {
                        error = "Incorrect username/email or password"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RenPurple)
        ) {
            Text(if (isRegister) "Create account" else "Sign in")
        }

        TextButton(
            onClick = {
                isRegister = !isRegister
                error = ""
            }
        ) {
            Text(
                if (isRegister) "Already have an account? Sign in"
                else "New here? Create account",
                color = RenPurple
            )
        }

        Text(
            "Simulator only — not a real bank login",
            color = RenMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
