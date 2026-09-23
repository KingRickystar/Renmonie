package com.example.demowallet

import java.util.Locale

fun formatNairaReal(kobo: Long): String {
    val naira = kobo / 100L
    val remainingKobo = (kobo % 100L)
        .toString()
        .padStart(2, '0')

    return "₦" +
            String.format(
                Locale.US,
                "%,d",
                naira
            ) +
            ".$remainingKobo"
}

fun formatNaira(amount: Long): String {
    return "₦" + String.format(Locale.US, "%,d", amount)
}


fun naira(amountKobo: Long): String {
    val nairaPart = amountKobo / 100L
    val koboPart = kotlin.math.abs(amountKobo % 100L)
    return "₦" + java.lang.String.format(java.util.Locale.US, "%,d.%02d", nairaPart, koboPart)
}
