package com.example.demowallet

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

fun generateTransactionId(): String =
    "RN" + System.currentTimeMillis().toString().takeLast(10) + Random.nextInt(100, 999)

fun currentDateTime(): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
