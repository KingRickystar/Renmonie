package com.example.demowallet

import android.content.Context
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
 */
data class AccountVerificationResult(
    val verified: Boolean,
    val accountName: String = "",
    val bank: String = "",
    val message: String = ""
)

object AccountVerificationClient {
    private const val TAG = "AccountVerificationClient"

    suspend fun verifyAccount(
        context: Context,
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
                val result = AccountVerificationResult(
                    verified = true,
                    accountName = accountName,
                    bank = bank,
                    message = message.ifBlank {
                        "Account verified"
                    }
                )

                result
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Live account verification failed", exception)

            AccountVerificationResult(
                verified = false,
                message = "Unable to verify this account right now. Please try again."
            )
        } finally {
            connection?.disconnect()
        }
    }

}
