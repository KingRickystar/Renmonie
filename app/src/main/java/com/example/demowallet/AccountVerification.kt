package com.example.demowallet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Result returned by the RenMonie backend after a server-side
 * Monnify account-name enquiry.
 *
 * Monnify credentials must never be placed in this Android project.
 */
data class AccountVerificationResult(
    val verified: Boolean,
    val accountName: String = "",
    val bank: String = "",
    val message: String = ""
)

/**
 * Bank-code mapping used by the backend contract.
 * The backend should validate this code again before calling Monnify.
 */
fun bankCodeFor(bankName: String): String {
    return when (bankName.trim()) {
        "Access Bank" -> "044"
        "ALAT by Wema" -> "035A"
        "Carbon" -> "565"
        "Ecobank" -> "050"
        "FCMB" -> "214"
        "Fidelity Bank" -> "070"
        "First Bank" -> "011"
        "GTBank" -> "058"
        "Kuda Bank" -> "50211"
        "Moniepoint" -> "50363"
        "Opay" -> "999992"
        "PalmPay" -> "999991"
        "Polaris Bank" -> "076"
        "Stanbic IBTC" -> "221"
        "Sterling Bank" -> "232"
        "UBA" -> "033"
        "Union Bank" -> "032"
        "Unity Bank" -> "215"
        "Wema Bank" -> "035"
        "Zenith Bank" -> "057"
        else -> ""
    }
}

/**
 * Calls the RenMonie backend. The backend, not this app, calls Monnify.
 *
 * Expected endpoint:
 * POST {BACKEND_BASE_URL}/api/verify-account
 */
object AccountVerificationClient {
    private const val TAG = "AccountVerificationClient"

    suspend fun verifyAccount(
        accountNumber: String,
        bankCode: String
    ): AccountVerificationResult = withContext(Dispatchers.IO) {
        if (accountNumber.length != 10 || accountNumber.any { !it.isDigit() }) {
            return@withContext AccountVerificationResult(
                verified = false,
                message = "Enter a valid 10-digit account number"
            )
        }

        if (bankCode.isBlank()) {
            return@withContext AccountVerificationResult(
                verified = false,
                message = "Select a supported bank before verifying"
            )
        }

        var connection: HttpURLConnection? = null

        try {
            val baseUrl = BuildConfig.BACKEND_BASE_URL
                .trim()
                .trimEnd('/')

            require(baseUrl.startsWith("https://")) {
                "BACKEND_BASE_URL must use HTTPS"
            }

            connection = (
                URL("$baseUrl/api/verify-account").openConnection()
                    as HttpURLConnection
                ).apply {
                requestMethod = "POST"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                useCaches = false
            }

            val requestBody = JSONObject().apply {
                put("accountNumber", accountNumber)
                put("bankCode", bankCode)
            }.toString()

            OutputStreamWriter(
                connection.outputStream,
                Charsets.UTF_8
            ).use { writer ->
                writer.write(requestBody)
            }

            val responseCode = connection.responseCode

            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: "{}"
            }

            val response = JSONObject(responseBody)
            val verified = response.optBoolean("verified", false)
            val accountName = response
                .optString("accountName", "")
                .trim()
            val bank = response
                .optString("bank", "")
                .trim()

            val message = response.optString(
                "message",
                if (responseCode in 200..299) {
                    "Unable to verify this account"
                } else {
                    "Verification service returned an error"
                }
            ).trim()

            if (
                responseCode !in 200..299 ||
                !verified ||
                accountName.isBlank()
            ) {
                AccountVerificationResult(
                    verified = false,
                    bank = bank,
                    message = message.ifBlank {
                        "Account name could not be verified"
                    }
                )
            } else {
                AccountVerificationResult(
                    verified = true,
                    accountName = accountName,
                    bank = bank,
                    message = message.ifBlank {
                        "Account verified"
                    }
                )
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Account verification failed", exception)

            AccountVerificationResult(
                verified = false,
                message = "Verification service unavailable. Please try again."
            )
        } finally {
            connection?.disconnect()
        }
    }
}
