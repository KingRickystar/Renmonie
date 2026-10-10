package com.example.demowallet

import android.content.Context

data class RenDashboardStyle(
    val id: String,
    val name: String,
    val description: String,
    val layoutHint: String
)

val RenDashboardStyles = listOf(
    RenDashboardStyle(
        id = "modern",
        name = "Modern",
        description = "A balanced dashboard with insights, bills, savings and security cards.",
        layoutHint = "Full overview"
    ),
    RenDashboardStyle(
        id = "minimal",
        name = "Minimal",
        description = "A cleaner screen with your balance, quick actions and recent activity.",
        layoutHint = "Simple & compact"
    ),
    RenDashboardStyle(
        id = "premium",
        name = "Premium",
        description = "A richer financial overview with insights, savings and extra account cards.",
        layoutHint = "Insights & savings"
    ),
    RenDashboardStyle(
        id = "classic",
        name = "Classic Banking",
        description = "A familiar banking layout focused on balance, services and transactions.",
        layoutHint = "Traditional banking"
    )
)

object RenDashboardStyleStore {
    private const val PREFS = "renmonie_dashboard_preferences"
    private const val KEY = "dashboard_style"

    fun load(context: Context): RenDashboardStyle {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "modern") ?: "modern"
        return RenDashboardStyles.firstOrNull { it.id == id } ?: RenDashboardStyles.first()
    }

    fun save(context: Context, style: RenDashboardStyle) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, style.id)
            .apply()
    }
}
