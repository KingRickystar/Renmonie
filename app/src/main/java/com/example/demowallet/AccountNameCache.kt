package com.example.demowallet

import android.content.Context
import org.json.JSONObject

/**
 * Small offline cache for successful account-name lookups.
 *
 * The cache lets RenMonie continue displaying a previously verified
 * recipient when the device temporarily has no internet connection.
 * It never invents a name: only names previously returned as verified
 * by the backend are stored.
 */
object AccountNameCache {

    private const val PREFS = PREFS_NAME
    private const val CACHE_KEY = "verified_account_names"

    fun save(
        context: Context,
        bankCode: String,
        accountNumber: String,
        accountName: String,
        bank: String
    ) {
        if (
            bankCode.isBlank() ||
            accountNumber.length != 10 ||
            accountName.isBlank()
        ) return

        val prefs = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

        val root = try {
            JSONObject(
                prefs.getString(CACHE_KEY, "{}") ?: "{}"
            )
        } catch (_: Exception) {
            JSONObject()
        }

        val key = "$bankCode:$accountNumber"

        root.put(
            key,
            JSONObject().apply {
                put("accountName", accountName.trim())
                put("bank", bank.trim())
                put("savedAt", System.currentTimeMillis())
            }
        )

        prefs.edit()
            .putString(CACHE_KEY, root.toString())
            .apply()
    }

    fun load(
        context: Context,
        bankCode: String,
        accountNumber: String
    ): AccountVerificationResult? {
        if (
            bankCode.isBlank() ||
            accountNumber.length != 10
        ) return null

        val prefs = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

        val root = try {
            JSONObject(
                prefs.getString(CACHE_KEY, "{}") ?: "{}"
            )
        } catch (_: Exception) {
            return null
        }

        val item = root.optJSONObject(
            "$bankCode:$accountNumber"
        ) ?: return null

        val accountName = item
            .optString("accountName")
            .trim()

        if (accountName.isBlank()) return null

        return AccountVerificationResult(
            verified = true,
            accountName = accountName,
            bank = item.optString("bank").trim(),
            message = "Previously verified recipient • offline"
        )
    }
}
