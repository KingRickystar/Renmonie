package com.example.demowallet

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class BankOption(
    val name: String,
    val code: String
)

data class BankDirectoryResult(
    val banks: List<BankOption>,
    val message: String = ""
)

object BankDirectoryClient {
    suspend fun load(context: Context): BankDirectoryResult =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val baseUrl = BuildConfig.BACKEND_BASE_URL.trim().trimEnd('/')
                require(baseUrl.startsWith("https://")) {
                    "BACKEND_BASE_URL must use HTTPS"
                }

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
                        emptyList(),
                        "Unable to load live supported banks"
                    )
                }

                val json = JSONObject(body)
                val array = json.optJSONArray("banks")
                    ?: return@withContext BankDirectoryResult(
                        emptyList(),
                        "Live bank directory is unavailable"
                    )

                val result = mutableListOf<BankOption>()
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val name = item.optString("name").trim()
                    val code = item.optString("code").trim()
                    if (name.isNotBlank() && code.isNotBlank()) {
                        result.add(BankOption(name, code))
                    }
                }

                val distinct = result
                    .distinctBy { it.code }
                    .sortedBy { it.name.lowercase() }

                BankDirectoryResult(
                    distinct,
                    if (distinct.isEmpty()) {
                        "Live bank directory returned no supported banks"
                    } else {
                        "Live bank directory"
                    }
                )
            } catch (_: Exception) {
                BankDirectoryResult(
                    emptyList(),
                    "Unable to reach live bank directory"
                )
            } finally {
                connection?.disconnect()
            }
        }
}
