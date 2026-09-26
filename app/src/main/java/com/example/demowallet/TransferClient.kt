package com.example.demowallet

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class TransferApiResult(val ok: Boolean, val reference: String = "", val status: String = "", val message: String = "")

object TransferClient {
    private const val TAG = "TransferClient"
    suspend fun submit(context: Context, amountNaira: Long, bankCode: String, accountNumber: String, accountName: String, narration: String): TransferApiResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val baseUrl = BuildConfig.BACKEND_BASE_URL.trim().trimEnd('/')
            require(baseUrl.startsWith("https://")) { "BACKEND_BASE_URL must use HTTPS" }
            connection = (URL("$baseUrl/api/transfers").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 20_000
                readTimeout = 30_000
                doOutput = true
                useCaches = false
            }
            val reference = "REN-" + System.currentTimeMillis()
            val body = JSONObject().apply {
                put("amount", amountNaira)
                put("reference", reference)
                put("destinationBankCode", bankCode)
                put("destinationAccountNumber", accountNumber)
                put("destinationAccountName", accountName)
                put("narration", narration.ifBlank { "RenMonie transfer" })
            }.toString()
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
            val code = connection.responseCode
            val responseBody = if (code in 200..299) connection.inputStream.bufferedReader().use { it.readText() } else connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "{}"
            val response = JSONObject(responseBody)
            TransferApiResult(response.optBoolean("ok", false) && code in 200..299, response.optString("reference", reference), response.optString("status", ""), response.optString("message", "Transfer could not be submitted"))
        } catch (e: Exception) {
            Log.e(TAG, "Transfer submission failed", e)
            TransferApiResult(false, message = "Transfer service is unavailable. Please try again.")
        } finally { connection?.disconnect() }
    }
}