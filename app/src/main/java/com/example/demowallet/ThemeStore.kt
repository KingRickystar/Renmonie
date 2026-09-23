package com.example.demowallet

import android.content.Context
import androidx.compose.ui.graphics.Color

data class RenThemePreset(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val preview: Color,
    val description: String
)

val RenThemePresets = listOf(
    RenThemePreset(
        "opay_green",
        "OPay Green",
        Color(0xFF00B14F),
        Color(0xFF00C853),
        Color(0xFF00B14F),
        "Vibrant emerald — inspired by OPay"
    ),
    RenThemePreset(
        "royal_purple",
        "Royal Purple",
        Color(0xFF6C3BFF),
        Color(0xFF8B5CF6),
        Color(0xFF6C3BFF),
        "Premium purple fintech look"
    ),
    RenThemePreset(
        "ocean_blue",
        "Ocean Blue",
        Color(0xFF1769FF),
        Color(0xFF42A5F5),
        Color(0xFF1769FF),
        "Classic banking blue"
    ),
    RenThemePreset(
        "teal",
        "Deep Teal",
        Color(0xFF008F95),
        Color(0xFF20C7D9),
        Color(0xFF008F95),
        "Calm and modern"
    ),
    RenThemePreset(
        "sunset",
        "Sunset Orange",
        Color(0xFFF57C00),
        Color(0xFFFFA726),
        Color(0xFFF57C00),
        "Warm and energetic"
    ),
    RenThemePreset(
        "rose",
        "Rose Pink",
        Color(0xFFE83E8C),
        Color(0xFFFF6B9D),
        Color(0xFFE83E8C),
        "Soft and distinctive"
    ),
    RenThemePreset(
        "indigo",
        "Indigo Night",
        Color(0xFF4F46E5),
        Color(0xFF818CF8),
        Color(0xFF4F46E5),
        "Deep indigo premium"
    ),
    RenThemePreset(
        "graphite",
        "Graphite",
        Color(0xFF546E7A),
        Color(0xFF78909C),
        Color(0xFF546E7A),
        "Minimalist charcoal"
    )
)

// Brand colors live ONLY in MainActivity.kt (RenPurple, RenViolet, RenBlue)

object RenThemeStore {
    private const val PREFS = "renmonie_theme_v13"
    private const val KEY = "theme_preset"

    fun apply(preset: RenThemePreset) {
        RenPurple = preset.primary
        RenViolet = preset.secondary
        RenBlue = preset.primary
    }

    fun save(context: Context, preset: RenThemePreset) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, preset.id)
            .apply()
        apply(preset)
    }

    fun load(context: Context): RenThemePreset {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "opay_green") ?: "opay_green"
        val preset = RenThemePresets.find { it.id == id } ?: RenThemePresets.first()
        apply(preset)
        return preset
    }
}
