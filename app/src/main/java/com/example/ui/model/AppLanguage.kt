package com.example.ui.model

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    PT("pt", "Português (Brasil)", "🇧🇷"),
    ES("es", "Español", "🇪🇸"),
    EN("en", "English", "🇺🇸");

    companion object {
        fun fromCode(code: String): AppLanguage {
            val lower = code.lowercase()
            return when {
                lower.startsWith("es") -> ES
                lower.startsWith("en") -> EN
                else -> PT // Português do Brasil por padrão
            }
        }
    }
}
