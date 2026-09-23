package com.example.demowallet

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Result of account name enquiry (NUBAN resolve).
 */
data class AccountVerifyResult(
    val success: Boolean,
    val accountName: String = "",
    val accountNumber: String = "",
    val bankCode: String = "",
    val message: String = "",
    val source: String = "demo" // demo | monnify
)

/**
 * Live name enquiry via **Monnify** (not Paystack / Flutterwave).
 *
 * Dashboard: https://app.monnify.com  (sandbox keys for testing)
 *
 * Leave keys empty → offline **demo** names.
 */
object VerificationConfig {
    /** Sandbox or live API key from Monnify dashboard */
    const val MONNIFY_API_KEY: String = "MK_TEST_JGKQDUZKE3"

    /** Matching secret key */
    const val MONNIFY_SECRET_KEY: String = "JX7JM30L20T7ZDW5G6KVZLRYWNPJ4UKJ"

    /**
     * true  → https://sandbox.monnify.com
     * false → https://api.monnify.com
     */
    const val USE_SANDBOX: Boolean = true

    private const val PREFS = "renmonie_verify"
    private const val KEY_API = "monnify_api_key"
    private const val KEY_SECRET = "monnify_secret"

    fun effectiveApiKey(context: Context): String {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_API, null)?.trim().orEmpty()
        if (stored.isNotBlank()) return stored
        return MONNIFY_API_KEY.trim()
    }

    fun effectiveSecret(context: Context): String {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SECRET, null)?.trim().orEmpty()
        if (stored.isNotBlank()) return stored
        return MONNIFY_SECRET_KEY.trim()
    }

    fun saveKeys(context: Context, apiKey: String, secret: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_API, apiKey.trim())
            .putString(KEY_SECRET, secret.trim())
            .apply()
    }

    fun clearKeys(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .remove(KEY_API)
            .remove(KEY_SECRET)
            .apply()
    }

    fun isLive(context: Context): Boolean =
        effectiveApiKey(context).isNotBlank() && effectiveSecret(context).isNotBlank()

    fun baseUrl(): String =
        if (USE_SANDBOX) "https://sandbox.monnify.com" else "https://api.monnify.com"
}

/** Nigerian bank display name → Monnify / CBN-style bank code */
object NigerianBankCodes {
    private val map = mapOf(
        "Access Bank" to "044",
        "Citibank Nigeria" to "023",
        "Ecobank Nigeria" to "050",
        "Fidelity Bank" to "070",
        "First Bank" to "011",
        "First City Monument Bank" to "214",
        "FCMB" to "214",
        "Globus Bank" to "103",
        "Guaranty Trust Bank" to "058",
        "GTBank" to "058",
        "Heritage Bank" to "030",
        "Jaiz Bank" to "301",
        "Keystone Bank" to "082",
        "Kuda" to "090267",
        "Kuda Bank" to "090267",
        "Moniepoint" to "50515",
        "Opay" to "100004",
        "OPay" to "100004",
        "PalmPay" to "100033",
        "Polaris Bank" to "076",
        "Providus Bank" to "101",
        "Stanbic IBTC Bank" to "221",
        "Stanbic" to "221",
        "Standard Chartered Bank" to "068",
        "Sterling Bank" to "232",
        "Suntrust Bank" to "100",
        "Union Bank" to "032",
        "United Bank For Africa" to "033",
        "UBA" to "033",
        "Unity Bank" to "215",
        "VFD Microfinance Bank" to "566",
        "Wema Bank" to "035",
        "Zenith Bank" to "057"
    )

    fun codeFor(bankName: String): String? {
        map[bankName]?.let { return it }
        return map.entries.firstOrNull { it.key.equals(bankName, ignoreCase = true) }?.value
    }
}

object AccountVerification {

    suspend fun resolve(
        context: Context,
        accountNumber: String,
        bankName: String
    ): AccountVerifyResult = withContext(Dispatchers.IO) {
        if (!isValidNubanFormat(accountNumber)) {
            return@withContext AccountVerifyResult(
                success = false,
                message = "Enter a valid 10-digit account number"
            )
        }
        if (bankName.isBlank()) {
            return@withContext AccountVerifyResult(
                success = false,
                message = "Select a bank"
            )
        }

        if (VerificationConfig.isLive(context)) {
            return@withContext resolveMonnify(
                context,
                accountNumber,
                bankName
            )
        }
        resolveDemo(accountNumber, bankName)
    }

    private fun resolveDemo(accountNumber: String, bankName: String): AccountVerifyResult {
        val name = simulateAccountName(accountNumber, bankName)
        return if (name.isNotBlank()) {
            AccountVerifyResult(
                success = true,
                accountName = name,
                accountNumber = accountNumber,
                bankCode = NigerianBankCodes.codeFor(bankName).orEmpty(),
                message = "Account verified (demo)",
                source = "demo"
            )
        } else {
            AccountVerifyResult(
                success = false,
                accountNumber = accountNumber,
                message = "Could not resolve account name. Check the number or bank.",
                source = "demo"
            )
        }
    }

    private fun resolveMonnify(
        context: Context,
        accountNumber: String,
        bankName: String
    ): AccountVerifyResult {
        val bankCode = NigerianBankCodes.codeFor(bankName)
            ?: return AccountVerifyResult(
                success = false,
                message = "Bank not supported for live verification: $bankName",
                source = "monnify"
            )

        val token = loginMonnify(context)
            ?: return AccountVerifyResult(
                success = false,
                message = "Monnify login failed. Check API key & secret.",
                source = "monnify"
            )

        return try {
            val base = VerificationConfig.baseUrl()
            val q = "accountNumber=" + URLEncoder.encode(accountNumber, "UTF-8") +
                "&bankCode=" + URLEncoder.encode(bankCode, "UTF-8")
            val url = URL("$base/api/v1/disbursements/account/validate?$q")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15_000
                readTimeout = 15_000
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()

            val json = JSONObject(if (body.isBlank()) "{}" else body)
            val ok = json.optBoolean("requestSuccessful", false) ||
                json.optString("responseCode") == "0"

            if (ok) {
                val data = json.optJSONObject("responseBody")
                val accountName = data?.optString("accountName").orEmpty()
                if (accountName.isNotBlank()) {
                    AccountVerifyResult(
                        success = true,
                        accountName = accountName.trim(),
                        accountNumber = data?.optString("accountNumber") ?: accountNumber,
                        bankCode = data?.optString("bankCode") ?: bankCode,
                        message = "Account verified",
                        source = "monnify"
                    )
                } else {
                    AccountVerifyResult(
                        success = false,
                        message = "No account name returned",
                        source = "monnify"
                    )
                }
            } else {
                val msg = json.optString(
                    "responseMessage",
                    json.optString("message", "Could not verify account")
                )
                AccountVerifyResult(
                    success = false,
                    message = msg,
                    source = "monnify"
                )
            }
        } catch (e: Exception) {
            AccountVerifyResult(
                success = false,
                message = "Network error: ${e.message ?: "unable to reach Monnify"}",
                source = "monnify"
            )
        }
    }

    /** OAuth-style login: Basic(apiKey:secret) → accessToken */
    private fun loginMonnify(context: Context): String? {
        return try {
            val apiKey = VerificationConfig.effectiveApiKey(context)
            val secret = VerificationConfig.effectiveSecret(context)
            val basic = Base64.encodeToString(
                "$apiKey:$secret".toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            val url = URL(VerificationConfig.baseUrl() + "/api/v1/auth/login")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Basic $basic")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 15_000
            }
            // empty body is fine for Monnify login
            conn.outputStream.use { /* no body required */ }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()

            val json = JSONObject(if (body.isBlank()) "{}" else body)
            val responseBody = json.optJSONObject("responseBody")
            responseBody?.optString("accessToken")?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
