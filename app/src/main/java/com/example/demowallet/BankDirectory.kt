package com.example.demowallet

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class BankDirectoryResult(
    val banks: List<String>,
    val message: String = ""
)

object BankDirectoryClient {
    private const val PREFS = "renmonie_bank_directory"
    private const val KEY_BANKS = "banks"
    private const val KEY_CODES = "codes"

    suspend fun load(context: Context): BankDirectoryResult =
        withContext(Dispatchers.IO) {
            val cached = loadCachedBanks(context)

            if (!isNetworkAvailable(context)) {
                return@withContext BankDirectoryResult(
                    banks = cached.ifEmpty { transferBanks },
                    message = "Offline bank directory"
                )
            }

            var connection: HttpURLConnection? = null

            try {
                val baseUrl = BuildConfig.BACKEND_BASE_URL.trim().trimEnd('/')

                connection = (URL("$baseUrl/api/banks").openConnection()
                    as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    useCaches = false
                }

                val responseCode = connection.responseCode
                val body = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "{}"
                }

                if (responseCode !in 200..299) {
                    return@withContext BankDirectoryResult(
                        banks = cached.ifEmpty { transferBanks },
                        message = "Using saved bank directory"
                    )
                }

                val json = JSONObject(body)
                val array = json.optJSONArray("banks")
                    ?: return@withContext BankDirectoryResult(
                        banks = cached.ifEmpty { transferBanks },
                        message = "Using saved bank directory"
                    )

                val names = mutableListOf<String>()
                val codes = mutableMapOf<String, String>()

                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val name = item.optString("name").trim()
                    val code = item.optString("code").trim()
                    if (name.isNotBlank() && code.isNotBlank()) {
                        names.add(name)
                        codes[name] = code
                    }
                }

                val distinctNames = names.distinct().sortedBy { it.lowercase() }

                if (distinctNames.isNotEmpty()) {
                    saveCache(context, distinctNames, codes)
                    BankDirectoryResult(distinctNames, "Live bank directory")
                } else {
                    BankDirectoryResult(
                        banks = cached.ifEmpty { transferBanks },
                        message = "Using saved bank directory"
                    )
                }
            } catch (_: Exception) {
                BankDirectoryResult(
                    banks = cached.ifEmpty { transferBanks },
                    message = "Using saved bank directory"
                )
            } finally {
                connection?.disconnect()
            }
        }

    suspend fun codeFor(
        context: Context,
        bankName: String
    ): String = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val cachedCode = prefs.getString("$KEY_CODES:$bankName", "").orEmpty()
        if (cachedCode.isNotBlank()) return@withContext cachedCode
        bankCodeFor(bankName)
    }

    private fun saveCache(
        context: Context,
        banks: List<String>,
        codes: Map<String, String>
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit().putString(KEY_BANKS, banks.joinToString("\n"))
        codes.forEach { (name, code) ->
            editor.putString("$KEY_CODES:$name", code)
        }
        editor.apply()
    }

    private fun loadCachedBanks(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BANKS, "")
            .orEmpty()
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
