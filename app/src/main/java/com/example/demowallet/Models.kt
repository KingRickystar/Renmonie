package com.example.demowallet

data class Transaction(
    val id: String,
    val recipient: String,
    val account: String,
    val bank: String,
    /** Amount in kobo */
    val amount: Long,
    val narration: String,
    val date: String,
    val type: String = "Transfer",
    val isCredit: Boolean = false,
    val status: String = STATUS_SUCCESSFUL,
    val reference: String = ""
)

data class ReceiptData(
    val id: String,
    val recipient: String,
    val account: String,
    val bank: String,
    val amount: Long,
    val narration: String,
    val date: String,
    val type: String,
    val isCredit: Boolean,
    val status: String
)

fun Transaction.toReceiptData(): ReceiptData = ReceiptData(
    id = id,
    recipient = recipient,
    account = account,
    bank = bank,
    amount = amount,
    narration = narration,
    date = date,
    type = type,
    isCredit = isCredit,
    status = status,
    reference = reference
)
