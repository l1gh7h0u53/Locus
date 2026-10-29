package com.example

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * =========================================================================================
 * FEATURE: Country Tipping & Service Rule Domain Models (CountryTippingModel)
 * =========================================================================================
 *
 * Description:
 * Defines the core domain entities that encapsulate global tipping etiquette, cultural norms,
 * standard/generous percentage rates, and dynamic i18n string resource resolutions.
 *
 * Components:
 * - [ServiceRule]: Specific rule for a service category (Restaurants, Delivery, Bar, etc.)
 *   with standard, generous, legend, and MVP tip rates + cultural notes.
 * - [CountryTippingData]: Complete tipping profile for an individual sovereign nation or
 *   territory, including ISO code, base cultural tipping matrix (A through E), localized
 *   currency resolver, and per-service rules.
 *
 * Dependencies:
 * - [android.content.Context]: Used by [ServiceRule] to resolve dynamic string resources for notes and names.
 * - [com.example.CurrencyOption]: Currency mapping and resolution for each country.
 * =========================================================================================
 */

/**
 * Service rule representation for a specific country and service category.
 *
 * Feature:
 * - Encapsulates standard, generous, legend, and MVP tipping etiquette descriptions.
 * - Supports both static fallback strings and dynamic Android string resource keys for i18n.
 * - Parses textual percentage strings (e.g., "15%", "Round up / 5%") into mathematical decimals.
 *
 * Dependencies:
 * - [android.content.Context]: Required for resource identifier lookup via [Context.resources].
 */
data class ServiceRule(
    val serviceType: String,
    val standardTip: String,
    val generousTip: String,
    val legendTip: String,
    val mvpTip: String,
    val notes: String,
    val notesKey: String? = null,
    val serviceTypeKey: String? = null
) {
    val serviceName: String get() = serviceType

    /**
     * Resolves the localized cultural note string from Android resources if [notesKey] is present,
     * falling back to the hardcoded [notes] string if not found.
     *
     * Dependencies:
     * - [android.content.res.Resources.getIdentifier]: Resolves string resource by key name dynamically.
     *
     * @param context Android context for resource resolution.
     * @return Localized cultural etiquette note string.
     */
    fun getLocalizedNotes(context: Context): String {
        if (!notesKey.isNullOrEmpty()) {
            val resId = context.resources.getIdentifier(notesKey, "string", context.packageName)
            if (resId != 0) {
                return context.getString(resId)
            }
        }
        return notes
    }

    /** Alias for [getLocalizedNotes] for convenience and backward compatibility */
    fun getLocalizedNote(context: Context): String = getLocalizedNotes(context)

    /**
     * Resolves the localized service category name from Android resources if [serviceTypeKey] is present.
     *
     * Dependencies:
     * - [android.content.res.Resources.getIdentifier]: Resolves service type string resource.
     *
     * @param context Android context for resource resolution.
     * @return Localized service name (e.g., "Restaurants", "Delivery", "Bar").
     */
    fun getLocalizedServiceType(context: Context): String {
        if (!serviceTypeKey.isNullOrEmpty()) {
            val resId = context.resources.getIdentifier(serviceTypeKey, "string", context.packageName)
            if (resId != 0) {
                return context.getString(resId)
            }
        }
        return serviceType
    }

    /** Alias for [getLocalizedServiceType] for convenience and backward compatibility */
    fun getLocalizedServiceName(context: Context): String = getLocalizedServiceType(context)

    /**
     * Parses the standard tip string into a numeric percentage rate (e.g., "15%" -> 0.15, "0%" -> 0.0).
     *
     * Dependencies:
     * - [parsePercentageString]: Helper parsing regex over raw tip string.
     *
     * @return Standard percentage rate as a decimal (0.0 to 1.0).
     */
    fun parseStandardRate(): Double {
        return parsePercentageString(standardTip, defaultRate = 0.10)
    }

    /**
     * Parses the generous, legend, and MVP tip strings into a list of numeric decimal percentages.
     *
     * Dependencies:
     * - [parsePercentageString]: Helper parsing regex for each tier.
     *
     * @return List of 3 Double values representing generous, legend, and MVP rates.
     */
    fun parseGenerousRates(): List<Double> {
        val g = parsePercentageString(generousTip, defaultRate = 0.15)
        val l = parsePercentageString(legendTip, defaultRate = 0.20)
        val m = parsePercentageString(mvpTip, defaultRate = 0.25)
        return listOf(g, l, m)
    }

    /**
     * Extracts decimal percentage value from raw rule strings like "15%", "5% - 10%", "Round up / 5%".
     *
     * @param str Raw rule string to parse.
     * @param defaultRate Fallback rate if no percentages can be extracted.
     * @return Decimal percentage rate (e.g. 0.15).
     */
    private fun parsePercentageString(str: String, defaultRate: Double): Double {
        val trimmed = str.trim()
        if (trimmed == "0%" || trimmed.startsWith("0%")) return 0.0
        
        // Find percentage numbers in string like "15%", "5% - 10%", "Round up / 5% - 10%"
        val regex = Regex("""(\d+(?:\.\d+)?)\s*%""")
        val matches = regex.findAll(trimmed).toList()
        if (matches.isNotEmpty()) {
            val numbers = matches.mapNotNull { it.groupValues[1].toDoubleOrNull() }
            if (numbers.isNotEmpty()) {
                // Return the average or first found
                return numbers.average() / 100.0
            }
        }
        if (trimmed.contains("Round up", ignoreCase = true)) {
            return 0.05 // default nominal 5% for rounding up
        }
        return defaultRate
    }
}

/**
 * Country tipping data model representing complete tipping culture for a specific nation.
 *
 * Feature:
 * - Represents country name, ISO code, cultural tipping matrix archetype (A-E), currency, and rules.
 * - Resolves currency option intelligently using multiple fallback strategies.
 *
 * Dependencies:
 * - [CurrencyOption]: Maps ISO / country / currency code to available UI currency options.
 * - [ServiceRule]: Maps service category names to corresponding tipping rules.
 */
data class CountryTippingData(
    val country: String,
    val iso: String,
    val baseMatrix: String,
    val matrixName: String,
    val localCurrency: String?,
    val hasCustomRules: Boolean,
    val services: Map<String, ServiceRule>
) {
    /**
     * Resolves the proper [CurrencyOption] for this country.
     *
     * Feature:
     * - Attempts lookup by explicit localCurrency code.
     * - Falls back to ISO country code mapping.
     * - Falls back to country name mapping.
     * - Falls back to nearest regional currency by base matrix archetype.
     *
     * Dependencies:
     * - [CurrencyOption.fromCode], [CurrencyOption.forCountry], [CurrencyOption.getNearestCurrencyFor].
     *
     * @return Resolved [CurrencyOption] matching the nation's financial standard.
     */
    fun getResolvedCurrency(): CurrencyOption {
        return CurrencyOption.fromCode(localCurrency)
            ?: CurrencyOption.forCountry(iso)
            ?: CurrencyOption.forCountry(country)
            ?: CurrencyOption.getNearestCurrencyFor(iso, baseMatrix)
    }

    /**
     * Retrieves the [ServiceRule] for a given service category name (e.g. "Restaurants", "Delivery").
     *
     * Feature:
     * - Returns the category rule or provides a safe standard fallback rule if missing.
     *
     * @param category Service category string key.
     * @return Corresponding [ServiceRule].
     */
    fun getRuleFor(category: String): ServiceRule {
        return services[category] ?: ServiceRule(
            serviceType = category,
            standardTip = "10%",
            generousTip = "15%",
            legendTip = "20%",
            mvpTip = "25%",
            notes = "Standard guideline applies."
        )
    }
}
