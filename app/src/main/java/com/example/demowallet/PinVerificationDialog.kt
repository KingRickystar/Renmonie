@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.demowallet

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Transfer / secure-action PIN dialog.
 * Soft keyboard is numeric-only (NumberPassword).
 */
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
                        val digits = value.filter { ch -> ch.isDigit() }.take(4)
                        pin = digits
                        error = ""
                    },
                    label = { Text("4-digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    )
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
