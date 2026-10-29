package com.example.i18n

import com.example.CurrencyOption
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

/**
 * =========================================================================================
 * FEATURE: Localized Currency & Numeric Formatter (LocaleFormatter)
 * =========================================================================================
 *
 * Description:
 * Production-grade typographic and numerical formatting engine ensuring tip values,
 * totals, percentages, and bill shares adhere strictly to regional conventions across
 * all supported international languages.
 *
 * Formatting Rules Enforced:
 * - Decimal separators: Comma in German/French/Spanish (e.g. 12,50 €), Dot in English/Japanese ($12.50).
 * - Grouping thousands separators: Periods, spaces, or commas.
 * - Currency placement: Prefix ($10.00, ¥1,500) vs Suffix (10,00 €, 150.00 zł).
 * - Zero-decimal currencies: Japanese Yen, Korean Won, Vietnamese Dong (e.g. ¥1500 instead of ¥1500.00).
 * - Percentage typographic rules: French space before percent sign (15 % vs 15%).
 *
 * Dependencies:
 * - [com.example.CurrencyOption]: Currency symbols, suffix definitions, and codes.
 * - [java.text.DecimalFormat], [java.text.DecimalFormatSymbols]: Standard Java i18n formatters.
 * - Used by [TippyViewModel], [BillShareHelper], [SeniorTtsManager], and Jetpack Compose UI cards.
 * =========================================================================================
 */
object LocaleFormatter {

    /**
     * Currencies that conventionally operate without decimal fraction subdivisions (e.g. JPY, KRW).
     */
    private val ZERO_DECIMAL_CURRENCIES = setOf(
        "JPY", "KRW", "VND", "CLP", "PYG", "HUF", "UGX", "RWF",
        "BIF", "GNF", "KMF", "DJF", "XAF", "XOF", "XPF", "ISK"
    )

    /**
     * Maps an ISO language code to its standard primary Java [Locale].
     *
     * @param langCode 2-letter ISO 639-1 language code (e.g. "de", "fr", "ar").
     * @return Resolved [Locale].
     */
    fun getLocaleForLanguage(langCode: String): Locale {
        return getLocale(langCode, null)
    }

    /**
     * Resolves precise Java [Locale] using both language code and optional country ISO.
     *
     * Feature:
     * - Configures RTL Arabic (ar-SA), Traditional vs Simplified Chinese (TW/HK vs CN),
     *   German (de-DE), French (fr-FR), Spanish (es-ES), Portuguese (pt-BR), Russian, Hindi, Bengali, Japanese.
     *
     * @param langCode ISO language code.
     * @param countryIso Optional 2-letter ISO country code.
     * @return Configured [Locale].
     */
    fun getLocale(langCode: String, countryIso: String? = null): Locale {
        val cleanLang = langCode.lowercase(Locale.US).trim()
        val cleanCountry = countryIso?.uppercase(Locale.US)?.trim()

        return try {
            when (cleanLang) {
                "ar" -> Locale.Builder().setLanguage("ar").setRegion(cleanCountry ?: "SA").build()
                "zh" -> if (cleanCountry == "TW" || cleanCountry == "HK") Locale.TRADITIONAL_CHINESE else Locale.SIMPLIFIED_CHINESE
                "de" -> Locale.Builder().setLanguage("de").setRegion(cleanCountry ?: "DE").build()
                "fr" -> Locale.Builder().setLanguage("fr").setRegion(cleanCountry ?: "FR").build()
                "es" -> Locale.Builder().setLanguage("es").setRegion(cleanCountry ?: "ES").build()
                "pt" -> Locale.Builder().setLanguage("pt").setRegion(cleanCountry ?: "BR").build()
                "ru" -> Locale.Builder().setLanguage("ru").setRegion(cleanCountry ?: "RU").build()
                "hi" -> Locale.Builder().setLanguage("hi").setRegion(cleanCountry ?: "IN").build()
                "bn" -> Locale.Builder().setLanguage("bn").setRegion(cleanCountry ?: "BD").build()
                "ja" -> Locale.JAPAN
                else -> Locale.Builder().setLanguage("en").setRegion(cleanCountry ?: "US").build()
            }
        } catch (e: Throwable) {
            Locale.US
        }
    }

    /**
     * Retrieves the localized decimal separator character for the given language.
     *
     * @param langCode ISO language code.
     * @return Character (e.g. '.' or ',').
     */
    fun getDecimalSeparator(langCode: String): Char {
        val locale = getLocaleForLanguage(langCode)
        return DecimalFormatSymbols.getInstance(locale).decimalSeparator
    }

    /**
     * Retrieves the localized grouping (thousands) separator character for the given language.
     *
     * @param langCode ISO language code.
     * @return Character (e.g. ',' or '.' or ' ').
     */
    fun getGroupingSeparator(langCode: String): Char {
        val locale = getLocaleForLanguage(langCode)
        return DecimalFormatSymbols.getInstance(locale).groupingSeparator
    }

    /**
     * Formats decimal currency values according to currency symbol placement and regional locale rules.
     *
     * Feature:
     * - Respects zero-decimal currency status (JPY, KRW integer formatting).
     * - Handles prefix/suffix positioning and spacing.
     *
     * Dependencies:
     * - [ZERO_DECIMAL_CURRENCIES]: Identifies non-fractional world currencies.
     * - [isLocaleSuffixStandard], [isPrefixWithSpace].
     *
     * @param amount Numerical monetary amount.
     * @param currency Active [CurrencyOption].
     * @param langCode Target ISO language code.
     * @param countryIso Optional ISO country code.
     * @return Localized formatted string (e.g. "$25.00", "25,00 €", "¥2,500").
     */
    fun formatCurrency(
        amount: Double,
        currency: CurrencyOption,
        langCode: String,
        countryIso: String? = null
    ): String {
        if (amount.isNaN() || amount.isInfinite()) {
            return if (currency.isSymbolAfter) "0.00 ${currency.symbol}" else "${currency.symbol}0.00"
        }

        val locale = getLocale(langCode, countryIso)
        val symbols = DecimalFormatSymbols.getInstance(locale)

        val isZeroDecimal = ZERO_DECIMAL_CURRENCIES.contains(currency.code.uppercase(Locale.US))
        val pattern = if (isZeroDecimal && amount % 1.0 == 0.0) {
            "#,##0"
        } else {
            "#,##0.00"
        }

        val df = DecimalFormat(pattern, symbols)
        val formattedNumber = df.format(amount)

        val isSuffix = currency.isSymbolAfter || isLocaleSuffixStandard(langCode, currency)
        val spaceSeparator = if (isSuffix) " " else if (isPrefixWithSpace(langCode, currency)) " " else ""

        return if (isSuffix) {
            "$formattedNumber$spaceSeparator${currency.symbol}"
        } else {
            "${currency.symbol}$spaceSeparator$formattedNumber"
        }
    }

    /**
     * Formats pure numerical amount with locale grouping and decimal separators without currency symbol.
     *
     * @param amount Decimal amount.
     * @param langCode Target ISO language code.
     * @param forceTwoDecimals True to always output 2 fractional digits.
     * @return Formatted numerical string.
     */
    fun formatAmountOnly(
        amount: Double,
        langCode: String,
        forceTwoDecimals: Boolean = true
    ): String {
        if (amount.isNaN() || amount.isInfinite()) return "0.00"
        val locale = getLocaleForLanguage(langCode)
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val pattern = if (forceTwoDecimals) "#,##0.00" else "#,##0.##"
        val df = DecimalFormat(pattern, symbols)
        return df.format(amount)
    }

    /**
     * Formats quick add pill values (+1, +5, +10, etc.) with correct symbol placement.
     *
     * @param amount Value to increment.
     * @param currency Target currency.
     * @param langCode Language code.
     * @return Formatted chip text (e.g. "+$5", "+5 €").
     */
    fun formatQuickAdd(amount: Double, currency: CurrencyOption, langCode: String): String {
        val whole = amount.toInt()
        return if (currency.isSymbolAfter) {
            "+$whole ${currency.symbol}"
        } else {
            "+${currency.symbol}$whole"
        }
    }

    /**
     * Formats percentage values with locale-aware decimal formatting.
     *
     * @param percent Percentage as decimal fraction (e.g. 0.15 for 15%).
     * @param langCode Target language code.
     * @return Formatted percentage string (e.g. "15.0%", "15,0 %").
     */
    fun formatPercent(percent: Double, langCode: String): String {
        val locale = getLocaleForLanguage(langCode)
        val symbols = DecimalFormatSymbols.getInstance(locale)
        val percentValue = percent * 100
        val pattern = if (percentValue % 1.0 == 0.0) "#0.0" else "#0.0"
        val df = DecimalFormat(pattern, symbols)
        val valueStr = df.format(percentValue)
        return when (langCode.lowercase(Locale.US).trim()) {
            "fr" -> "$valueStr %"
            else -> "$valueStr%"
        }
    }

    /**
     * Formats integer percentage values (e.g. 15%).
     *
     * @param percent Percentage as decimal fraction (e.g. 0.15).
     * @param langCode Target language code.
     * @return Formatted string (e.g. "15%", "15 %").
     */
    fun formatIntegerPercent(percent: Double, langCode: String): String {
        val intVal = (percent * 100).roundToInt()
        return when (langCode.lowercase(Locale.US).trim()) {
            "fr" -> "$intVal %"
            else -> "$intVal%"
        }
    }

    /**
     * Determines whether regional conventions for this locale place the currency symbol as a suffix.
     */
    private fun isLocaleSuffixStandard(langCode: String, currency: CurrencyOption): Boolean {
        val cleanLang = langCode.lowercase(Locale.US).trim()
        return when (cleanLang) {
            "de" -> currency == CurrencyOption.EUR || currency.code == "EUR"
            "fr" -> currency == CurrencyOption.EUR || currency.code == "EUR" || currency.code.endsWith("F")
            "es" -> currency == CurrencyOption.EUR || currency.code == "EUR"
            "ru" -> true
            else -> false
        }
    }

    /**
     * Determines whether regional conventions for this locale insert a space between symbol and number for prefixes.
     */
    private fun isPrefixWithSpace(langCode: String, currency: CurrencyOption): Boolean {
        val cleanLang = langCode.lowercase(Locale.US).trim()
        return when {
            currency.code == "BRL" -> true
            cleanLang == "hi" && currency.code == "INR" -> true
            cleanLang == "bn" && currency.code == "BDT" -> true
            else -> false
        }
    }
}
