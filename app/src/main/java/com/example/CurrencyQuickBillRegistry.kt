package com.example

/**
 * =========================================================================================
 * FEATURE: Dynamic Currency Quick-Add Magnitude Registry (CurrencyQuickBillRegistry)
 * =========================================================================================
 *
 * Description:
 * Scales the "+1, +5, +10, +20" quick-bill additive chip buttons dynamically to sensible
 * magnitudes based on the purchasing power and unit denominations of the active currency.
 * For example:
 * - USD/EUR (Standard): +1, +5, +10, +20
 * - MXN/BRL/SEK (Moderate): +10, +50, +100, +200
 * - JPY/KRW/ARS (High): +100, +500, +1k, +5k
 * - IDR/VND/PYG (Super High): +5k, +10k, +50k, +100k
 *
 * Dependencies:
 * - Used by [BillAmountSection] in MainActivity to render the quick increment chips.
 * - Used by [TippyViewModel.addToBill] to handle button click additions.
 * =========================================================================================
 */

/**
 * Enumeration of currency magnitude tiers and their associated additive values & display chips.
 *
 * @property quickAddAmounts Numerical values added to the bill when chips are pressed.
 * @property displayLabels Localized short label formatted on each button chip.
 */
enum class CurrencyMagnitudeTier(
    val quickAddAmounts: List<Double>,
    val displayLabels: List<String>
) {
    TIER_1_STANDARD(
        quickAddAmounts = listOf(1.0, 5.0, 10.0, 20.0),
        displayLabels = listOf("+1", "+5", "+10", "+20")
    ),
    TIER_2_MODERATE(
        quickAddAmounts = listOf(10.0, 50.0, 100.0, 200.0),
        displayLabels = listOf("+10", "+50", "+100", "+200")
    ),
    TIER_3_HIGH(
        quickAddAmounts = listOf(100.0, 500.0, 1000.0, 5000.0),
        displayLabels = listOf("+100", "+500", "+1k", "+5k")
    ),
    TIER_4_SUPER_HIGH(
        quickAddAmounts = listOf(5000.0, 10000.0, 50000.0, 100000.0),
        displayLabels = listOf("+5k", "+10k", "+50k", "+100k")
    )
}

/**
 * Registry mapping ISO currency codes to appropriate [CurrencyMagnitudeTier] denominations.
 */
object CurrencyQuickBillRegistry {

    /** Tier 2 currencies (moderate denomination, 10x multiplier) */
    private val tier2Currencies = setOf(
        "MXN", "BRL", "SEK", "NOK", "DKK", "PLN", "CZK", "THB", "INR",
        "EGP", "TRY", "ZAR", "AED", "SAR", "QAR", "ILS", "MYR", "PHP",
        "HKD", "TWD", "MAD", "DZD", "TND", "GHS", "KES", "MUR", "UAH"
    )

    /** Tier 3 currencies (hundreds/thousands denomination, 100x multiplier) */
    private val tier3Currencies = setOf(
        "JPY", "KRW", "HUF", "ALL", "ARS", "COP", "CLP", "CRC", "DOP",
        "NIO", "HNL", "GTQ", "JMD", "LKR", "NPR", "PKR", "BDT", "KZT",
        "MNT", "KHR", "MMK", "AMD", "XOF", "XAF", "CVE", "ETB", "MZN"
    )

    /** Tier 4 currencies (thousands/tens of thousands denomination, 5000x multiplier) */
    private val tier4Currencies = setOf(
        "IDR", "VND", "PYG", "UZS", "LBP", "UGX", "TZS", "RWF", "IQD",
        "SLL", "GNF", "IRR", "VES", "MGA", "KMF", "BIF", "CDF", "SSP"
    )

    /**
     * Resolves the [CurrencyMagnitudeTier] corresponding to a given 3-letter currency code.
     *
     * @param currencyCode ISO-4217 code (e.g. "USD", "JPY", "VND").
     * @return Resolved [CurrencyMagnitudeTier], defaulting to [CurrencyMagnitudeTier.TIER_1_STANDARD].
     */
    fun getTierForCurrency(currencyCode: String?): CurrencyMagnitudeTier {
        if (currencyCode.isNullOrEmpty()) return CurrencyMagnitudeTier.TIER_1_STANDARD
        val code = currencyCode.uppercase().trim()
        return when {
            tier4Currencies.contains(code) -> CurrencyMagnitudeTier.TIER_4_SUPER_HIGH
            tier3Currencies.contains(code) -> CurrencyMagnitudeTier.TIER_3_HIGH
            tier2Currencies.contains(code) -> CurrencyMagnitudeTier.TIER_2_MODERATE
            else -> CurrencyMagnitudeTier.TIER_1_STANDARD
        }
    }
}
