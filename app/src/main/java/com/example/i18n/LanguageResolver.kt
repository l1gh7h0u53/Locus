package com.example.i18n

import java.util.Locale

/**
 * Standard configuration schema for target languages.
 */
data class TargetLanguageInfo(
    val code: String,
    val name: String,
    val dir: String,
    val script: String,
    val plurals: Int
)

/**
 * Complete Offline Country-to-Language Resolver.
 * Resolves language completely offline using:
 * 1. Direct System Locale Check (if in target 10 languages)
 * 2. Country ISO Mapping Lookup (Telephony SIM / Locale Country)
 * 3. Hard Fallback to English ("en")
 */
object LanguageResolver {

    const val DEFAULT_LANGUAGE = "en"

    val TARGET_LANGUAGES = mapOf(
        "en" to TargetLanguageInfo("en", "English", "ltr", "Latn", 2),
        "zh" to TargetLanguageInfo("zh", "Mandarin Chinese (中文)", "ltr", "Hans", 1),
        "hi" to TargetLanguageInfo("hi", "Hindi (हिन्दी)", "ltr", "Deva", 2),
        "es" to TargetLanguageInfo("es", "Spanish (Español)", "ltr", "Latn", 2),
        "fr" to TargetLanguageInfo("fr", "French (Français)", "ltr", "Latn", 2),
        "ar" to TargetLanguageInfo("ar", "Arabic (العربية)", "rtl", "Arab", 6),
        "bn" to TargetLanguageInfo("bn", "Bengali (বাংলা)", "ltr", "Beng", 2),
        "pt" to TargetLanguageInfo("pt", "Portuguese (Português)", "ltr", "Latn", 2),
        "ru" to TargetLanguageInfo("ru", "Russian (Русский)", "ltr", "Cyrl", 3),
        "de" to TargetLanguageInfo("de", "German (Deutsch)", "ltr", "Latn", 2),
        "ja" to TargetLanguageInfo("ja", "Japanese (日本語)", "ltr", "Jpan", 1)
    )

    val SUPPORTED_LANGUAGES = TARGET_LANGUAGES.keys

    // Static offline country-to-language routing table
    private val COUNTRY_TO_LANG_MAP: Map<String, String> = mapOf(
        // German (de)
        "DE" to "de", "AT" to "de", "CH" to "de", "LI" to "de", "LU" to "de",

        // Spanish (es)
        "ES" to "es", "MX" to "es", "AR" to "es", "CO" to "es", "PE" to "es",
        "CL" to "es", "EC" to "es", "GT" to "es", "CU" to "es", "BO" to "es",
        "DO" to "es", "HN" to "es", "PY" to "es", "SV" to "es", "NI" to "es",
        "CR" to "es", "PA" to "es", "UY" to "es", "VE" to "es", "GQ" to "es",

        // French (fr)
        "FR" to "fr", "BE" to "fr", "MC" to "fr", "SN" to "fr", "CI" to "fr",
        "CD" to "fr", "CG" to "fr", "CM" to "fr", "MG" to "fr", "ML" to "fr",
        "BF" to "fr", "NE" to "fr", "GN" to "fr", "TD" to "fr", "HT" to "fr",
        "BI" to "fr", "BJ" to "fr", "TG" to "fr", "GA" to "fr", "DJ" to "fr",

        // Arabic (ar)
        "SA" to "ar", "AE" to "ar", "EG" to "ar", "DZ" to "ar", "MA" to "ar",
        "IQ" to "ar", "SD" to "ar", "YE" to "ar", "SY" to "ar", "TN" to "ar",
        "JO" to "ar", "LY" to "ar", "OM" to "ar", "KW" to "ar", "QA" to "ar",
        "BH" to "ar", "MR" to "ar", "SO" to "ar", "KM" to "ar",

        // Russian (ru)
        "RU" to "ru", "BY" to "ru", "KZ" to "ru", "KG" to "ru", "TJ" to "ru",
        "UZ" to "ru", "TM" to "ru", "AM" to "ru", "AZ" to "ru", "GE" to "ru",
        "MD" to "ru", "UA" to "ru",

        // Portuguese (pt)
        "PT" to "pt", "BR" to "pt", "AO" to "pt", "MZ" to "pt", "CV" to "pt",
        "GW" to "pt", "ST" to "pt", "TL" to "pt",

        // Mandarin Chinese (zh)
        "CN" to "zh", "TW" to "zh", "HK" to "zh", "MO" to "zh", "SG" to "zh",

        // Hindi (hi)
        "IN" to "hi", "NP" to "hi", "BT" to "hi",

        // Bengali (bn)
        "BD" to "bn",

        // Japanese (ja)
        "JP" to "ja",

        // English (en)
        "US" to "en", "GB" to "en", "CA" to "en", "AU" to "en", "NZ" to "en",
        "IE" to "en", "ZA" to "en", "NG" to "en", "PH" to "en", "PK" to "en",
        "KE" to "en", "GH" to "en"
    )

    /**
     * Resolves the nearest language code ("en", "de", "ar", etc.)
     * 1. Direct System Locale Check (if in target 10 languages)
     * 2. Offline Country ISO Mapping (SIM / Network / Region)
     * 3. Hard Fallback to English ("en")
     */
    fun resolveLanguage(systemLang: String, countryIso: String?): String {
        val normalizedLang = systemLang.lowercase(Locale.US).split("-", "_")[0]

        // 1. Direct language match
        if (SUPPORTED_LANGUAGES.contains(normalizedLang)) {
            return normalizedLang
        }

        // 2. Offline Country ISO lookup
        val normalizedCountry = countryIso?.uppercase(Locale.US)?.trim().orEmpty()
        if (normalizedCountry.isNotEmpty() && COUNTRY_TO_LANG_MAP.containsKey(normalizedCountry)) {
            return COUNTRY_TO_LANG_MAP[normalizedCountry]!!
        }

        // 3. Fallback
        return DEFAULT_LANGUAGE
    }

    /**
     * Checks if language code is RTL.
     */
    fun isRtl(languageCode: String): Boolean {
        return TARGET_LANGUAGES[languageCode]?.dir == "rtl" || languageCode == "ar"
    }

    /**
     * Checks if language code is one of the romance languages with longer button text strings
     * (Spanish, French, Portuguese) requiring taller buttons and adaptive line-wrapping.
     */
    fun isLongTextLanguage(languageCode: String?): Boolean {
        if (languageCode == null) return false
        val lang = languageCode.lowercase(Locale.US).split("-", "_")[0]
        return lang == "es" || lang == "fr" || lang == "pt"
    }
}
