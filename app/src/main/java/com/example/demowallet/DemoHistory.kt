package com.example.demowallet

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

/**
 * Seeds ~80 realistic Nigerian-style demo transactions once.
 */
object DemoHistory {

    private const val PREFS = "renmonie_demo"
    private const val KEY_SEEDED = "history_seeded_v3"

    private val firstNames = listOf(
        "Chinedu", "Adeola", "Fatima", "Emeka", "Blessing", "Ibrahim", "Ngozi",
        "Tunde", "Amina", "Kelechi", "Ubong", "Esther", "Patrick", "Onyekwere",
        "Joseph", "Grace", "Daniel", "Ruth", "Samuel", "Chioma", "Yusuf", "Halima"
    )
    private val lastNames = listOf(
        "Okafor", "Balogun", "Abdullahi", "Nwosu", "Adeyemi", "Musa", "Eze",
        "Olawale", "Bello", "Uche", "Effiong", "Ihinrere", "Okonkwo", "Adebayo"
    )
    private val banks = listOf(
        "GTBank", "Access Bank", "Zenith Bank", "UBA", "First Bank", "Opay",
        "PalmPay", "Kuda", "Moniepoint", "Stanbic", "Fidelity", "Union Bank"
    )
    private val typesDebit = listOf(
        "Transfer", "Airtime", "Data", "Bills", "Stamp Duty", "Value Added Tax", "Bill Payments"
    )

    fun ensureSeeded(context: Context): List<Transaction> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = RenMonieStorage.loadTransactions(context)

        // Fast path: already seeded — do not rebuild or re-save
        if (prefs.getBoolean(KEY_SEEDED, false)) {
            return existing
        }
        if (existing.size >= 80) {
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
            return existing
        }

        val seeded = buildList {
            addAll(existing)
            addAll(generate(80 - existing.size))
        }
        // Keep newest first without expensive date-string sort
        RenMonieStorage.saveTransactions(context, seeded)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        return seeded
    }

    private fun generate(count: Int): List<Transaction> {
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val rnd = Random(42)
        val list = mutableListOf<Transaction>()

        for (i in 0 until count) {
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.HOUR_OF_DAY, -i * 5 - rnd.nextInt(8))
            cal.add(Calendar.MINUTE, -rnd.nextInt(50))

            val isCredit = i % 5 == 0
            val type = when {
                isCredit -> "Transfer"
                i % 11 == 0 -> "Stamp Duty"
                i % 13 == 0 -> "Value Added Tax"
                i % 7 == 0 -> "Airtime"
                i % 9 == 0 -> "Data"
                i % 10 == 0 -> "Bills"
                else -> "Transfer"
            }

            val name = when (type) {
                "Stamp Duty" -> "Stamp Duty"
                "Value Added Tax" -> "Value Added Tax"
                "Airtime" -> listOf("MTN", "Airtel", "Glo", "9mobile").random(rnd)
                "Data" -> listOf("MTN Data", "Airtel Data", "Glo Data").random(rnd)
                "Bills" -> listOf("DSTV", "IKEDC", "Gotv", "ILotBet").random(rnd)
                else -> "${firstNames.random(rnd)} ${lastNames.random(rnd)}"
            }

            val amountNaira = when (type) {
                "Stamp Duty" -> 50L
                "Value Added Tax" -> listOf(1L, 0L).let { if (rnd.nextBoolean()) 1L else 0L }
                "Airtime" -> listOf(100L, 200L, 500L, 1000L).random(rnd)
                "Data" -> listOf(500L, 1000L, 2000L).random(rnd)
                else -> listOf(200L, 500L, 1000L, 2000L, 5000L, 10000L, 15000L, 19900L, 20000L).random(rnd)
            }.coerceAtLeast(1L)

            // VAT as 0.75 sometimes - store as kobo 75
            val amountKobo = if (type == "Value Added Tax") 75L else amountNaira * 100L

            val account = (1000000000L + rnd.nextLong(8999999999L - 1000000000L)).toString().take(10)

            list.add(
                Transaction(
                    id = "TXN${100000 + i}",
                    recipient = name,
                    account = account,
                    bank = if (type in listOf("Stamp Duty", "Value Added Tax")) "RenMonie" else banks.random(rnd),
                    amount = amountKobo,
                    narration = when {
                        isCredit -> "Credit from $name"
                        type == "Transfer" -> "Transfer to $name"
                        else -> type
                    },
                    date = fmt.format(cal.time),
                    type = type,
                    isCredit = isCredit,
                    status = "Successful"
                )
            )
        }
        return list
    }
}
