package com.example.shared

/**
 * Result of an offline secondary-currency conversion for the Tablet Mode side panel.
 *
 * @property sourceCode Uppercase 3-letter ISO-4217 currency code converted from.
 * @property targetCode Uppercase 3-letter ISO-4217 currency code converted into.
 * @property convertedTotal Converted total bill rounded to 2 decimal places (HALF_UP).
 * @property convertedSharePerPerson Converted per-person split share rounded to 2 decimal places.
 * @property exchangeRate Indicative multiplicative conversion rate (`1 [sourceCode] = rate [targetCode]`).
 */
data class CurrencyConversionResult(
    val sourceCode: String,
    val targetCode: String,
    val convertedTotal: Double,
    val convertedSharePerPerson: Double,
    val exchangeRate: Double
)

/**
 * Pure Kotlin Multiplatform offline currency converter & country-paired secondary currency selector.
 *
 * Uses indicative USD-anchored reference rates so travelers can inspect an approximate converted
 * total completely offline without requiring network permissions.
 */
object CurrencyConverter {

    private val EUROPEAN_SECONDARY_CURRENCIES = setOf(
        "GBP", "CHF", "SEK", "NOK", "DKK", "PLN", "CZK", "HUF",
        "RON", "BGN", "RSD", "ISK", "UAH", "TRY", "MAD", "TND"
    )

    /**
     * Indicative offline units per 1 USD across supported ISO-4217 currencies.
     */
    private val UNITS_PER_USD: Map<String, Double> = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "CHF" to 0.88,
        "JPY" to 152.0,
        "CAD" to 1.39,
        "AUD" to 1.53,
        "NZD" to 1.66,
        "CNY" to 7.24,
        "HKD" to 7.78,
        "TWD" to 32.4,
        "KRW" to 1390.0,
        "SGD" to 1.34,
        "INR" to 84.4,
        "MXN" to 20.1,
        "BRL" to 5.75,
        "ARS" to 995.0,
        "CLP" to 965.0,
        "COP" to 4380.0,
        "PEN" to 3.76,
        "UYU" to 42.0,
        "SEK" to 10.75,
        "NOK" to 11.0,
        "DKK" to 6.86,
        "ISK" to 138.0,
        "PLN" to 4.02,
        "CZK" to 23.4,
        "HUF" to 378.0,
        "RON" to 4.58,
        "BGN" to 1.80,
        "RSD" to 108.0,
        "UAH" to 41.3,
        "TRY" to 34.4,
        "RUB" to 97.5,
        "AED" to 3.6725,
        "SAR" to 3.75,
        "QAR" to 3.64,
        "KWD" to 0.307,
        "BHD" to 0.376,
        "OMR" to 0.385,
        "JOD" to 0.709,
        "ILS" to 3.72,
        "EGP" to 49.3,
        "MAD" to 9.95,
        "TND" to 3.12,
        "DZD" to 133.5,
        "ZAR" to 17.9,
        "KES" to 129.0,
        "NGN" to 1660.0,
        "GHS" to 16.0,
        "THB" to 34.2,
        "VND" to 25300.0,
        "IDR" to 15800.0,
        "MYR" to 4.42,
        "PHP" to 58.6,
        "PKR" to 278.0,
        "BDT" to 119.5,
        "LKR" to 293.0,
        "NPR" to 135.0,
        "KZT" to 492.0,
        "GEL" to 2.73,
        "AMD" to 388.0,
        "AZN" to 1.70,
        "AFN" to 68.0,
        "ALL" to 91.5,
        "AOA" to 912.0,
        "XCD" to 2.70,
        "BSD" to 1.0,
        "BBD" to 2.0,
        "BYN" to 3.27,
        "BZD" to 2.0,
        "XOF" to 603.5,
        "BTN" to 84.4,
        "BOB" to 6.91,
        "BAM" to 1.80,
        "BWP" to 13.5,
        "BND" to 1.34,
        "BIF" to 2900.0,
        "CVE" to 101.4,
        "KHR" to 4060.0,
        "XAF" to 603.5,
        "KMF" to 452.6,
        "CDF" to 2850.0,
        "CRC" to 512.0,
        "CUP" to 24.0,
        "DJF" to 177.7,
        "DOP" to 60.2,
        "ERN" to 15.0,
        "SZL" to 17.9,
        "ETB" to 121.0,
        "FJD" to 2.26,
        "GMD" to 70.5,
        "GTQ" to 7.72,
        "GNF" to 8600.0,
        "GYD" to 209.0,
        "HTG" to 131.5,
        "HNL" to 25.1,
        "IRR" to 42000.0,
        "IQD" to 1310.0,
        "JMD" to 157.5,
        "KGS" to 86.2,
        "LAK" to 21900.0,
        "LBP" to 89500.0,
        "LSL" to 17.9,
        "LRD" to 192.0,
        "LYD" to 4.82,
        "MGA" to 4630.0,
        "MWK" to 1735.0,
        "MVR" to 15.4,
        "MRU" to 39.7,
        "MUR" to 46.2,
        "MDL" to 17.9,
        "MNT" to 3398.0,
        "MZN" to 63.9,
        "MMK" to 2100.0,
        "NAD" to 17.9,
        "NIO" to 36.8,
        "KPW" to 900.0,
        "MKD" to 56.6,
        "PAB" to 1.0,
        "PGK" to 3.96,
        "PYG" to 7780.0,
        "RWF" to 1365.0,
        "WST" to 2.75,
        "STN" to 22.5,
        "SCR" to 13.7,
        "SLE" to 22.6,
        "SBD" to 8.45,
        "SOS" to 571.0,
        "SSP" to 3200.0,
        "SDG" to 601.0,
        "SRD" to 35.1,
        "SYP" to 13000.0,
        "TJS" to 10.65,
        "TZS" to 2680.0,
        "TOP" to 2.35,
        "TTD" to 6.78,
        "TMT" to 3.50,
        "UGX" to 3670.0,
        "UZS" to 12800.0,
        "VUV" to 119.0,
        "VED" to 44.5,
        "YER" to 250.0,
        "ZMW" to 26.8,
        "ZWG" to 25.4
    )

    /**
     * Resolves an appropriate secondary comparison currency code based on the selected country,
     * active primary currency, and optional user home currency.
     *
     * - If the user is viewing a currency different from the country's local currency (e.g., their
     *   home currency), returns the destination country's local currency.
     * - Else if a distinct [homeCurrencyCode] is known, returns the user's home currency.
     * - Otherwise selects a natural travel reference pair (`USD` for Eurozone/Americas/Asia/global,
     *   `EUR` for USD and European/Mediterranean regional currencies).
     *
     * @param selectedCurrencyCode Currently active calculator ISO-4217 currency code.
     * @param countryLocalCurrencyCode Official local ISO-4217 currency code of the active destination country.
     * @param countryMatrixCode Tipping culture matrix archetype ("A" through "E") used for regional fallback.
     * @param homeCurrencyCode Optional remembered traveler home ISO-4217 currency code.
     * @return Uppercase 3-letter ISO-4217 code for the secondary display/swap currency.
     */
    fun resolveSecondaryCurrencyCode(
        selectedCurrencyCode: String,
        countryLocalCurrencyCode: String?,
        countryMatrixCode: String,
        homeCurrencyCode: String? = null
    ): String {
        val selected = selectedCurrencyCode.trim().uppercase()
        val local = countryLocalCurrencyCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
        val home = homeCurrencyCode?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }

        if (local != null && local != selected && UNITS_PER_USD.containsKey(local)) {
            return local
        }

        if (home != null && home != selected && UNITS_PER_USD.containsKey(home)) {
            return home
        }

        return when {
            selected == "EUR" -> "USD"
            selected == "USD" -> "EUR"
            countryMatrixCode == "C" || countryMatrixCode == "B" || selected in EUROPEAN_SECONDARY_CURRENCIES -> "EUR"
            else -> "USD"
        }
    }

    /**
     * Computes the indicative offline exchange rate from [fromCode] to [toCode].
     *
     * @param fromCode Source ISO-4217 currency code.
     * @param toCode Target ISO-4217 currency code.
     * @return Multiplicative conversion ratio (`1.0` if codes match or a rate is unavailable).
     */
    fun getExchangeRate(fromCode: String, toCode: String): Double {
        val normalizedFrom = fromCode.trim().uppercase()
        val normalizedTo = toCode.trim().uppercase()
        if (normalizedFrom == normalizedTo) return 1.0

        val fromPerUsd = UNITS_PER_USD[normalizedFrom] ?: 1.0
        val toPerUsd = UNITS_PER_USD[normalizedTo] ?: 1.0
        if (fromPerUsd <= 0.0 || toPerUsd <= 0.0) return 1.0
        return toPerUsd / fromPerUsd
    }

    /**
     * Converts [totalAmount] and [sharePerPerson] from [fromCode] to [toCode] using
     * deterministic HALF_UP rounding.
     *
     * @param totalAmount Total bill amount expressed in [fromCode].
     * @param sharePerPerson Per-person split amount expressed in [fromCode].
     * @param fromCode Source ISO-4217 currency code.
     * @param toCode Target ISO-4217 currency code.
     * @return [CurrencyConversionResult] containing converted totals and applied exchange rate.
     */
    fun convertTotal(
        totalAmount: Double,
        sharePerPerson: Double,
        fromCode: String,
        toCode: String
    ): CurrencyConversionResult {
        val rate = getExchangeRate(fromCode, toCode)
        val safeTotal = if (totalAmount.isFinite() && totalAmount > 0.0) totalAmount else 0.0
        val safeShare = if (sharePerPerson.isFinite() && sharePerPerson > 0.0) sharePerPerson else 0.0
        return CurrencyConversionResult(
            sourceCode = fromCode.trim().uppercase(),
            targetCode = toCode.trim().uppercase(),
            convertedTotal = TipCalculator.roundToTwoDecimals(safeTotal * rate),
            convertedSharePerPerson = TipCalculator.roundToTwoDecimals(safeShare * rate),
            exchangeRate = rate
        )
    }
}
