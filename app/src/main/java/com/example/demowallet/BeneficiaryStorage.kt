package com.example.demowallet

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local-only beneficiary storage.
 *
 * Beneficiaries remain available when RenMonie is offline.
 */
object BeneficiaryStorage {

    private const val KEY = "renmonie_beneficiaries"

    fun load(context: Context): List<Triple<String, String, String>> {
        val raw = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString(KEY, null) ?: return emptyList()

        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val name = item.optString("name").trim()
                    val account = item.optString("account").trim()
                    val bank = item.optString("bank").trim()

                    if (
                        name.isNotBlank() &&
                        account.length == 10 &&
                        account.all { it.isDigit() } &&
                        bank.isNotBlank()
                    ) {
                        add(Triple(name, account, bank))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(
        context: Context,
        beneficiaries: List<Triple<String, String, String>>
    ) {
        val array = JSONArray()

        beneficiaries
            .distinctBy { item -> item.second + ":" + item.third }
            .take(100)
            .forEach { item ->
                array.put(
                    JSONObject().apply {
                        put("name", item.first)
                        put("account", item.second)
                        put("bank", item.third)
                    }
                )
            }

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }
}
