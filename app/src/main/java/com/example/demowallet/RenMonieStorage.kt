package com.example.demowallet

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

object RenMonieStorage {

    private const val DEFAULT_BALANCE =
        150000L

    private const val DEFAULT_BALANCE_KOBO =
        DEFAULT_BALANCE * 100L

    // PIN security
    private const val PIN_KEY =
        "renmonie_pin"

    private const val PIN_HASH_KEY =
        "renmonie_pin_hash"

    private const val PIN_ENABLED_KEY =
        "renmonie_pin_enabled"

    private const val PIN_FAILED_ATTEMPTS_KEY =
        "renmonie_pin_failed_attempts"

    private const val PIN_LOCK_UNTIL_KEY =
        "renmonie_pin_lock_until"

    private const val MAX_PIN_ATTEMPTS = 3
    private const val PIN_LOCK_MILLIS = 30_000L

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun hasPin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(PIN_ENABLED_KEY, false)
    }

    fun savePin(context: Context, pin: String): Boolean {
        if (pin.length != 4 || !pin.all { it.isDigit() }) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(PIN_KEY)
            .putString(PIN_HASH_KEY, hashPin(pin))
            .putBoolean(PIN_ENABLED_KEY, true)
            .putInt(PIN_FAILED_ATTEMPTS_KEY, 0)
            .remove(PIN_LOCK_UNTIL_KEY)
            .apply()
        return true
    }

    fun isPinLocked(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val until = prefs.getLong(PIN_LOCK_UNTIL_KEY, 0L)
        if (until <= 0L) return false
        if (System.currentTimeMillis() >= until) {
            prefs.edit()
                .putInt(PIN_FAILED_ATTEMPTS_KEY, 0)
                .remove(PIN_LOCK_UNTIL_KEY)
                .apply()
            return false
        }
        return true
    }

    fun remainingPinAttempts(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return (MAX_PIN_ATTEMPTS - prefs.getInt(PIN_FAILED_ATTEMPTS_KEY, 0)).coerceAtLeast(0)
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(PIN_ENABLED_KEY, false) || isPinLocked(context)) return false

        val storedHash = prefs.getString(PIN_HASH_KEY, null)
        val valid = if (!storedHash.isNullOrBlank()) {
            storedHash == hashPin(pin)
        } else {
            // Migrate PINs saved by older RenMonie versions.
            prefs.getString(PIN_KEY, "") == pin
        }

        if (valid) {
            prefs.edit()
                .putInt(PIN_FAILED_ATTEMPTS_KEY, 0)
                .remove(PIN_LOCK_UNTIL_KEY)
                .apply()

            if (storedHash.isNullOrBlank()) {
                prefs.edit()
                    .remove(PIN_KEY)
                    .putString(PIN_HASH_KEY, hashPin(pin))
                    .apply()
            }
            return true
        }

        val failures = prefs.getInt(PIN_FAILED_ATTEMPTS_KEY, 0) + 1
        val editor = prefs.edit().putInt(PIN_FAILED_ATTEMPTS_KEY, failures)
        if (failures >= MAX_PIN_ATTEMPTS) {
            editor.putLong(
                PIN_LOCK_UNTIL_KEY,
                System.currentTimeMillis() + PIN_LOCK_MILLIS
            )
        }
        editor.apply()
        return false
    }

    fun clearPin(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(PIN_KEY)
            .remove(PIN_HASH_KEY)
            .putBoolean(PIN_ENABLED_KEY, false)
            .putInt(PIN_FAILED_ATTEMPTS_KEY, 0)
            .remove(PIN_LOCK_UNTIL_KEY)
            .apply()
    }


    // =========================================================
    // BALANCE
    // =========================================================

    fun loadBalanceKobo(
        context: Context
    ): Long {

        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        if (
            prefs.contains(
                BALANCE_KOBO_KEY
            )
        ) {

            return prefs.getLong(
                BALANCE_KOBO_KEY,
                DEFAULT_BALANCE_KOBO
            )
        }

        /*
         * Migrate the old whole-naira
         * balance.
         */
        val oldBalance =
            prefs.getLong(
                BALANCE_KEY,
                DEFAULT_BALANCE
            )

        val migrated =
            oldBalance * 100L

        prefs.edit()
            .putLong(
                BALANCE_KOBO_KEY,
                migrated
            )
            .apply()

        return migrated
    }


    fun saveBalanceKobo(
        context: Context,
        balanceKobo: Long
    ) {

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putLong(
                BALANCE_KOBO_KEY,
                balanceKobo
            )
            /*
             * Keep the old key updated too,
             * for compatibility with older
             * versions of RenMonie.
             */
            .putLong(
                BALANCE_KEY,
                balanceKobo / 100L
            )
            .apply()
    }


    // =========================================================
    // BALANCE COMPATIBILITY
    // =========================================================

    fun loadBalance(
        context: Context
    ): Long {

        return loadBalanceKobo(
            context
        ) / 100L
    }


    fun saveBalance(
        context: Context,
        balance: Long
    ) {

        saveBalanceKobo(
            context,
            balance * 100L
        )
    }


    // =========================================================
    // TRANSACTIONS
    // =========================================================

    fun loadTransactions(
        context: Context
    ): List<Transaction> {

        val json =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            ).getString(
                TRANSACTIONS_KEY,
                null
            ) ?: return emptyList()

        return try {

            val array =
                JSONArray(json)

            val result =
                mutableListOf<Transaction>()

            for (
                i in 0 until array.length()
            ) {

                val item =
                    array.getJSONObject(i)

                /*
                 * New transactions use amountKobo.
                 *
                 * Old transactions used amount
                 * in whole naira.
                 */
                val amountKobo =
                    if (
                        item.has(
                            "amountKobo"
                        )
                    ) {

                        item.optLong(
                            "amountKobo"
                        )

                    } else {

                        item.optLong(
                            "amount"
                        ) * 100L
                    }


                val savedStatus =
                    item.optString(
                        "status",
                        STATUS_SUCCESSFUL
                    )


                val validStatus =
                    when (savedStatus) {

                        STATUS_PENDING,
                        STATUS_SUCCESSFUL,
                        STATUS_FAILED,
                        STATUS_REVERSED ->
                            savedStatus

                        else ->
                            STATUS_SUCCESSFUL
                    }


                result.add(
                    Transaction(
                        id =
                            item.optString(
                                "id"
                            ),

                        recipient =
                            item.optString(
                                "recipient"
                            ),

                        account =
                            item.optString(
                                "account"
                            ),

                        bank =
                            item.optString(
                                "bank"
                            ),

                        amount =
                            amountKobo,

                        narration =
                            item.optString(
                                "narration"
                            ),

                        date =
                            item.optString(
                                "date"
                            ),

                        type =
                            item.optString(
                                "type",
                                "Transfer"
                            ),

                        isCredit =
                            item.optBoolean(
                                "isCredit",
                                false
                            ),

                        status =
                            validStatus,

                        reference =
                            item.optString("reference", "")
                    )
                )
            }

            result

        } catch (_: Exception) {

            emptyList()
        }
    }


    fun saveTransactions(
        context: Context,
        transactions: List<Transaction>
    ) {

        val array =
            JSONArray()

        transactions.forEach { transaction ->

            val item =
                JSONObject()

            item.put(
                "id",
                transaction.id
            )

            item.put(
                "recipient",
                transaction.recipient
            )

            item.put(
                "account",
                transaction.account
            )

            item.put(
                "bank",
                transaction.bank
            )

            /*
             * New canonical amount.
             */
            item.put(
                "amountKobo",
                transaction.amount
            )

            /*
             * Compatibility amount.
             */
            item.put(
                "amount",
                transaction.amount / 100L
            )

            item.put(
                "narration",
                transaction.narration
            )

            item.put(
                "date",
                transaction.date
            )

            item.put(
                "type",
                transaction.type
            )

            item.put(
                "isCredit",
                transaction.isCredit
            )

            item.put(
                "status",
                transaction.status
            )

            item.put(
                "reference",
                transaction.reference
            )

            array.put(
                item
            )
        }


        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(
                TRANSACTIONS_KEY,
                array.toString()
            )
            .apply()
    }
}