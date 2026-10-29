package com.example

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import java.io.IOException
import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Telephony Network Country & Brain Asset Repository (BrainRepository)
 * =========================================================================================
 *
 * Description:
 * Safely inspects device cellular network metadata (TelephonyManager) to detect the user's
 * current ISO-3166-1 alpha-2 country code without requesting sensitive runtime location
 * permissions. Also provides offline asset loading functionality for country-specific
 * tipping rules and configuration files located in the Android assets directory.
 *
 * Characteristics:
 * - Zero-Permission Country Detection: Uses telephony network country ISO provided by cell towers.
 * - Multi-SIM / Dual-SIM Aware: Supports slotIndex parameter on Android R (API 30)+.
 * - Resilient Fallback: Gracefully catches security exceptions or hardware absence.
 *
 * Dependencies:
 * - [android.telephony.TelephonyManager]: Android system service providing network telemetry.
 * - [android.content.pm.PackageManager]: Checks hardware features (FEATURE_TELEPHONY_RADIO_ACCESS).
 * - [android.content.Context]: Provides access to system services and asset manager.
 * =========================================================================================
 */
class BrainRepository(private val context: Context) {

    // TelephonyManager system service dependency
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    /**
     * Safely retrieves the 2-character ISO-3166-1 alpha-2 country code from the cellular carrier network.
     *
     * Feature:
     * - Detects the country code based on connected cellular towers.
     * - Uses modern TelephonyManager.getNetworkCountryIso(slotIndex) on Android R+ with radio access.
     * - Falls back to legacy TelephonyManager.networkCountryIso on earlier Android versions.
     *
     * Dependencies:
     * - [android.telephony.TelephonyManager.getNetworkCountryIso]: API 30+ method for multi-SIM country queries.
     * - [android.content.pm.PackageManager.hasSystemFeature]: Verifies FEATURE_TELEPHONY_RADIO_ACCESS.
     * - Returns a lowercase trimmed 2-character ISO code (e.g., "us", "de", "jp"), or empty string on failure.
     *
     * @param slotIndex SIM card slot index (defaults to 0 for primary SIM).
     * @return 2-letter ISO country code or empty string if unavailable (e.g. airplane mode, Wi-Fi only tablet).
     */
    fun getNetworkCountryIso(slotIndex: Int = 0): String {
        if (telephonyManager == null) return ""

        val hasRadioAccess = context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_TELEPHONY_RADIO_ACCESS
        )

        return try {
            val iso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasRadioAccess) {
                telephonyManager.getNetworkCountryIso(slotIndex)
            } else {
                // Fallback for API < 30 or devices without FEATURE_TELEPHONY_RADIO_ACCESS
                telephonyManager.networkCountryIso
            }
            iso?.lowercase(Locale.US)?.trim() ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Loads the JSON brain configuration file matching the detected country ISO from the app's assets.
     *
     * Feature:
     * - Checks for country-specific asset file (e.g., "brains/us.json", "brains/de.json").
     * - Automatically falls back to [defaultFile] (e.g., "brains/default.json") if missing or country undetected.
     *
     * Dependencies:
     * - [getNetworkCountryIso]: Detects country code to find matching JSON file.
     * - [readAsset]: Reads asset file stream into string.
     *
     * @param slotIndex SIM slot to query.
     * @param defaultFile Fallback JSON asset file name.
     * @return Raw JSON configuration string.
     * @throws IllegalStateException if both country asset and fallback default asset are not found.
     */
    fun loadBrainConfig(slotIndex: Int = 0, defaultFile: String = "default.json"): String {
        val iso = getNetworkCountryIso(slotIndex)

        // Attempt loading country-specific brain (e.g., "brains/us.json")
        if (iso.isNotEmpty()) {
            val countryBrain = readAsset("brains/$iso.json")
            if (countryBrain != null) return countryBrain
        }

        // Fallback to default brain configuration
        return readAsset("brains/$defaultFile")
            ?: throw IllegalStateException("Default brain file 'brains/$defaultFile' not found in assets.")
    }

    /**
     * Reads a text file from the Android application assets directory.
     *
     * Dependencies:
     * - [android.content.Context.assets]: Accesses APK asset storage.
     *
     * @param path Relative path within the assets folder (e.g., "brains/us.json").
     * @return File contents as a String, or null if an IOException occurs.
     */
    private fun readAsset(path: String): String? {
        return try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            null
        }
    }
}
