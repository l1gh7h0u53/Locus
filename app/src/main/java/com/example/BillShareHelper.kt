package com.example

import android.content.Context
import android.content.Intent
import com.example.i18n.LocaleFormatter
import com.example.i18n.getLocalizedCountry

/**
 * =========================================================================================
 * FEATURE: Bill Calculation Sharing Utility (BillShareHelper)
 * =========================================================================================
 *
 * Description:
 * Generates formatted, localized text breakdowns of the bill calculation (bill amount,
 * tip percentage & value, total amount, and per-person split breakdown) and shares them
 * using the native Android system share sheet (Intent.ACTION_SEND).
 *
 * Characteristics:
 * - 100% Offline: Operates entirely on-device with zero network requirements.
 * - Localized: Uses LocaleFormatter to respect decimal/grouping separators and currency symbols.
 * - Intent Chooser: Integrates directly with Android's system share sheet for compatibility
 *   with WhatsApp, SMS, Email, Notes, Slack, etc.
 *
 * Dependencies:
 * - [com.example.TippyUiState]: Source of truth for calculation state (amounts, currency, language, split).
 * - [com.example.i18n.LocaleFormatter]: Handles locale-aware currency & percentage string formatting.
 * - [android.content.Context]: Required to resolve string resources (R.string.*) and start activities.
 * - [android.content.Intent]: Framework API used to construct ACTION_SEND intent and system chooser.
 * - [com.example.R.string]: String resource IDs for labels (bill_amount, tip_amount_label, etc.).
 * =========================================================================================
 */
object BillShareHelper {

    /**
     * Constructs a clean, human-readable, and localized text receipt of the current bill calculation.
     *
     * Feature:
     * - Formats bill total, tip percentage, tip value, total bill, and split shares.
     * - Integrates country name and currency code into the receipt header.
     *
     * Dependencies:
     * - [context]: Resolves localized labels from strings.xml (e.g., R.string.bill_amount, R.string.split_bill).
     * - [uiState]: Provides bill input, active currency [CurrencyOption], active language, split count, etc.
     * - [LocaleFormatter.formatCurrency]: Formats numerical amounts into localized strings (e.g., "$15.00" or "15,00 €").
     * - [LocaleFormatter.formatPercent]: Formats the tip percentage according to the active locale.
     *
     * @param context Android context for string resource lookup.
     * @param uiState Snapshot of current calculation state.
     * @return Formatted multi-line text receipt ready for sharing or copying.
     */
    fun createBillSummary(
        context: Context,
        uiState: TippyUiState
    ): String {
        // Resolve raw bill from user input or derive from total minus tip
        val rawBill = uiState.billAmountInput.toDoubleOrNull() ?: (uiState.totalBill - uiState.tipAmount).coerceAtLeast(0.0)
        
        // Localized string representations of all values
        val billFormatted = LocaleFormatter.formatCurrency(rawBill, uiState.selectedCurrency, uiState.currentLanguage)
        val tipPercentFormatted = LocaleFormatter.formatPercent(uiState.selectedPercent, uiState.currentLanguage)
        val tipFormatted = LocaleFormatter.formatCurrency(uiState.tipAmount, uiState.selectedCurrency, uiState.currentLanguage)
        val totalFormatted = LocaleFormatter.formatCurrency(uiState.totalBill, uiState.selectedCurrency, uiState.currentLanguage)
        val rawCountry = uiState.activeCountry.country
        val localizedCountry = uiState.activeCountry.getLocalizedCountry(uiState.currentLanguage)
        val countryName = if (rawCountry.isNotBlank() && !localizedCountry.equals(rawCountry, ignoreCase = true)) {
            "$localizedCountry ($rawCountry)"
        } else {
            localizedCountry
        }
        val currencyCode = uiState.selectedCurrency.code

        // Localized labels from string resources
        val billLabel = context.getString(R.string.bill_amount)
        val tipLabel = context.getString(R.string.tip_amount_label)
        val totalLabel = context.getString(R.string.total_bill_label)
        val eachPaysLabel = context.getString(R.string.each_pays_label)
        val splitLabel = context.getString(R.string.split_bill)
        val appName = context.getString(R.string.app_name)

        // Build textual receipt representation
        return buildString {
            append("🧾 $appName - $countryName ($currencyCode)\n")
            append("--------------------------------\n")
            append("$billLabel: $billFormatted\n")
            append("$tipLabel ($tipPercentFormatted): $tipFormatted\n")
            append("--------------------------------\n")
            append("$totalLabel: $totalFormatted\n")
            if (uiState.splitCount > 1) {
                val perPerson = LocaleFormatter.formatCurrency(uiState.sharePerPerson, uiState.selectedCurrency, uiState.currentLanguage)
                append("$splitLabel: ${uiState.splitCount}\n")
                append("$eachPaysLabel: $perPerson\n")
            }
        }
    }

    /**
     * Dispatches the generated bill calculation text to other applications via the Android system share sheet.
     *
     * Feature:
     * - Launches native ACTION_SEND Intent with text/plain payload.
     * - Wraps the intent in Intent.createChooser so users can pick their preferred app.
     *
     * Dependencies:
     * - [createBillSummary]: Helper function to generate the text body.
     * - [android.content.Intent]: Constructs ACTION_SEND intent and chooser dialog.
     * - [android.content.Context.startActivity]: Launches the Android system sheet.
     *
     * @param context Android context used to trigger startActivity.
     * @param uiState Current application state containing bill and tipping details.
     */
    fun shareBillSummary(
        context: Context,
        uiState: TippyUiState
    ) {
        val summary = createBillSummary(context, uiState)
        val shareTitle = context.getString(R.string.share_bill_summary_title)

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$shareTitle - ${uiState.activeCountry.getLocalizedCountry(uiState.currentLanguage)}")
            putExtra(Intent.EXTRA_TEXT, summary)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, shareTitle).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }
}

