package com.example.history

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * =========================================================================================
 * FEATURE: Tip Calculation Transaction Record (TipTransaction)
 * =========================================================================================
 *
 * Description:
 * Immutable domain model representing a single saved tip calculation. Contains full audit
 * parameters including timestamp, bill amount, tip percentage and value, total bill, split count,
 * individual share, currency metadata, sovereign country tag, and service category key.
 *
 * Characteristics:
 * - Deterministic serialization: Converts to and from native [JSONObject] for DataStore persistence.
 * - Human-readable formatting: Provides formatted date representation for history display.
 * - Resilient deserialization: Handles missing JSON fields gracefully with safe fallbacks.
 *
 * @property id Unique transaction identifier (UUID v4 string).
 * @property timestamp Epoch timestamp in milliseconds when transaction was recorded.
 * @property billAmount Subtotal before tip calculation.
 * @property tipPercentage Decimal tip fraction (e.g., 0.18 for 18%).
 * @property tipAmount Calculated tip amount.
 * @property totalBill Calculated total bill (bill + tip).
 * @property splitCount Number of individuals splitting the bill (>= 1).
 * @property sharePerPerson Calculated portion due per person.
 * @property currencyCode Three-letter ISO 4217 currency code (e.g. "USD", "EUR").
 * @property currencySymbol Visual currency sign (e.g. "$", "€", "¥").
 * @property countryIso Two-letter ISO 3166-1 alpha-2 country code (e.g. "US", "DE").
 * @property countryName English name of the sovereign nation.
 * @property serviceKey Service category identifier (e.g. "restaurant", "delivery").
 * =========================================================================================
 */
data class TipTransaction(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val billAmount: Double,
    val tipPercentage: Double,
    val tipAmount: Double,
    val totalBill: Double,
    val splitCount: Int,
    val sharePerPerson: Double,
    val currencyCode: String,
    val currencySymbol: String,
    val countryIso: String,
    val countryName: String,
    val serviceKey: String
) {
    /**
     * Formats the transaction epoch timestamp into a human-readable date and time string.
     *
     * Example output: "Oct 28, 2026 • 2:30 PM".
     *
     * @return Formatted date string, or "Recent" if timestamp parsing encounters an unexpected error.
     */
    fun formattedDate(): String {
        return try {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            "Recent"
        }
    }

    /**
     * Serializes this transaction model into a [JSONObject] representation for persistent DataStore storage.
     *
     * @return [JSONObject] containing all transaction fields.
     */
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("timestamp", timestamp)
            put("billAmount", billAmount)
            put("tipPercentage", tipPercentage)
            put("tipAmount", tipAmount)
            put("totalBill", totalBill)
            put("splitCount", splitCount)
            put("sharePerPerson", sharePerPerson)
            put("currencyCode", currencyCode)
            put("currencySymbol", currencySymbol)
            put("countryIso", countryIso)
            put("countryName", countryName)
            put("serviceKey", serviceKey)
        }
    }

    companion object {
        /**
         * Reconstructs a [TipTransaction] instance from a persisted [JSONObject].
         *
         * @param json [JSONObject] containing transaction properties.
         * @return Valid [TipTransaction] instance, or null if parsing fails.
         */
        fun fromJson(json: JSONObject): TipTransaction? {
            return try {
                TipTransaction(
                    id = json.optString("id", UUID.randomUUID().toString()),
                    timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                    billAmount = json.optDouble("billAmount", 0.0),
                    tipPercentage = json.optDouble("tipPercentage", 0.15),
                    tipAmount = json.optDouble("tipAmount", 0.0),
                    totalBill = json.optDouble("totalBill", 0.0),
                    splitCount = json.optInt("splitCount", 1),
                    sharePerPerson = json.optDouble("sharePerPerson", 0.0),
                    currencyCode = json.optString("currencyCode", "USD"),
                    currencySymbol = json.optString("currencySymbol", "$"),
                    countryIso = json.optString("countryIso", "US"),
                    countryName = json.optString("countryName", "United States"),
                    serviceKey = json.optString("serviceKey", "restaurant")
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
