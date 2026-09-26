package com.example.demowallet

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

fun bankCodeFor(bankName: String): String {
    return when (bankName.trim()) {
        "Access Bank" -> "044"
        "ALAT by Wema" -> "035A"
        "Carbon" -> "940"
        "Citibank Nigeria" -> "023"
        "Ecobank" -> "050"
        "FCMB" -> "214"
        "Fidelity Bank" -> "070"
        "First Bank" -> "011"
        "GTBank" -> "058"
        "Globus Bank" -> "00103"
        "Jaiz Bank" -> "301"
        "Keystone Bank" -> "082"
        "Kuda Bank" -> "50211"
        "LOTUS Bank" -> "303"
        "Moniepoint" -> "50515"
        "Opay" -> "999992"
        "PalmPay" -> "100033"
        "Parallex" -> "907"
        "Polaris Bank" -> "076"
        "Premium Trust Bank" -> "105"
        "Providus Bank" -> "101"
        "Renmoney" -> "090198"
        "Stanbic IBTC" -> "221"
        "Standard Chartered Bank" -> "068"
        "Sterling Bank" -> "232"
        "SunTrust Bank" -> "100"
        "Taj Bank" -> "302"
        "Tatum Bank" -> "109"
        "Titan Bank" -> "102"
        "UBA" -> "033"
        "Union Bank" -> "032"
        "Unity Bank" -> "215"
        "VFD" -> "566"
        "Wema Bank" -> "035"
        "Zenith Bank" -> "057"
        "FairMoney" -> "51318"
        "Eyowo" -> "50126"
        "Paga" -> "100002"
        "SmartCash" -> "00803"
        "Moneymaster PSB" -> "120005"
        "HopePSB" -> "120002"
        "LAPO Microfinance Bank" -> "090177"
        "Signature Bank" -> "000034"
        "Optimus Bank" -> "107"
        "Coronation Bank" -> "946"
        else -> ""
    }
}

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

        /*
         * Offline-first behaviour:
         * use a previously verified name immediately when there is
         * no usable network. We never create a name locally.
         */
        if (!isNetworkAvailable(context)) {
            return@withContext AccountNameCache.load(
                context = context,
                bankCode = bankCode,
                accountNumber = accountNumber
            ) ?: AccountVerificationResult(
                verified = false,
                message = "You're offline. Connect to the internet to verify this account."
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

                /*
                 * Only successful backend verification is cached.
                 */
                AccountNameCache.save(
                    context = context,
                    bankCode = bankCode,
                    accountNumber = accountNumber,
                    accountName = result.accountName,
                    bank = result.bank
                )

                result
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Account verification failed", exception)

            /*
             * A network can disappear after the connectivity check.
             * Fall back to a previously verified name rather than
             * breaking the rest of the app.
             */
            AccountNameCache.load(
                context = context,
                bankCode = bankCode,
                accountNumber = accountNumber
            ) ?: AccountVerificationResult(
                verified = false,
                message = "Verification service unavailable. Connect to the internet and try again."
            )
        } finally {
            connection?.disconnect()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as? ConnectivityManager ?: return false

        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) && capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }
}
