package com.example.shared

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

/**
 * Result of a complete tip calculation in the shared KMP domain layer.
 *
 * @property tipAmount Tip monetary amount rounded to 2 decimal places (HALF_UP).
 * @property totalBill Total bill (`bill + tipAmount`) rounded to 2 decimal places.
 * @property sharePerPerson Monetary share due per diner when splitting across multiple people.
 */
data class TipCalculationResult(
    val tipAmount: Double,
    val totalBill: Double,
    val sharePerPerson: Double
)

/**
 * Result of a total-bill rounding adjustment (Round Up / Round Down).
 *
 * @property targetTotal Whole-integer target total (or clamped bill subtotal for Round Down).
 * @property tipAmount Resulting non-negative tip monetary amount rounded to 2 decimal places.
 * @property customPercentValue Adjusted custom percentage on the 0..100+ UI scale (e.g., `18.5` for `18.5%`).
 */
data class RoundingAdjustment(
    val targetTotal: Double,
    val tipAmount: Double,
    val customPercentValue: Double
)

/**
 * Pure Kotlin Multiplatform tip calculation and bill rounding engine shared across platforms.
 *
 * All monetary rounding uses deterministic decimal `HALF_UP` to 2 fractional digits without
 * JVM-only or Android-specific dependencies so logic executes identically in `commonMain`
 * and `commonTest`.
 */
object TipCalculator {

    /**
     * Rounds a floating-point monetary value to 2 decimal places using `HALF_UP` semantics.
     * Non-finite inputs (`NaN`, `Infinity`) and `-0.0` normalize safely to `0.0`.
     *
     * @param value Raw floating-point monetary value.
     * @return Rounded value with at most 2 decimal places, or `0.0` if non-finite or zero.
     */
    fun roundToTwoDecimals(value: Double): Double {
        if (!value.isFinite() || value == 0.0) return 0.0

        val sign = if (value < 0.0) -1.0 else 1.0
        val magnitude = abs(value)
        val raw = magnitude.toString()

        val eIndex = raw.indexOfAny(charArrayOf('E', 'e'))
        val mantissa = if (eIndex >= 0) raw.substring(0, eIndex) else raw
        val exp = if (eIndex >= 0) raw.substring(eIndex + 1).toIntOrNull() ?: 0 else 0

        val dotIndex = mantissa.indexOf('.')
        val digits = if (dotIndex >= 0) {
            mantissa.substring(0, dotIndex) + mantissa.substring(dotIndex + 1)
        } else {
            mantissa
        }
        val fractionalDigitsInMantissa = if (dotIndex >= 0) mantissa.length - 1 - dotIndex else 0
        val scale = fractionalDigitsInMantissa - exp

        if (scale <= 2) {
            // Already has 2 or fewer decimal places; normalize -0.0
            return if (magnitude == 0.0) 0.0 else sign * magnitude
        }

        val keepCount = digits.length - (scale - 2)
        if (keepCount < 0) return 0.0

        if (keepCount == 0) {
            val roundedCents = if (digits.isNotEmpty() && digits[0] >= '5') 1L else 0L
            return if (roundedCents == 0L) 0.0 else sign * (roundedCents / 100.0)
        }

        val keptPrefix = digits.substring(0, keepCount)
        val baseCents = keptPrefix.toLongOrNull()
            ?: return round(value * 100.0) / 100.0

        val roundDigit = digits.getOrElse(keepCount) { '0' }
        val roundedCents = if (roundDigit >= '5') baseCents + 1L else baseCents
        return if (roundedCents == 0L) 0.0 else sign * (roundedCents / 100.0)
    }

    /**
     * Calculates the monetary tip amount (`bill * rate`) rounded to 2 decimal places.
     *
     * @param bill Pre-tip bill subtotal. Non-finite or non-positive values yield `0.0`.
     * @param rate Decimal tip fraction (e.g., `0.15` for 15%). Non-finite or non-positive values yield `0.0`.
     * @return Calculated tip amount rounded with `HALF_UP` precision.
     */
    fun calculateTipAmount(bill: Double, rate: Double): Double {
        if (!bill.isFinite() || !rate.isFinite() || bill <= 0.0 || rate <= 0.0) return 0.0
        return roundToTwoDecimals(bill * rate)
    }

    /**
     * Calculates the total bill (`bill + tip`) rounded to 2 decimal places, clamping negative inputs to `0.0`.
     *
     * @param bill Pre-tip bill subtotal.
     * @param tip Calculated monetary tip amount.
     * @return Sum of [bill] and [tip] rounded to 2 decimal places, or `0.0` if either input is non-finite.
     */
    fun calculateTotalAmount(bill: Double, tip: Double): Double {
        if (!bill.isFinite() || !tip.isFinite()) return 0.0
        val safeBill = if (bill < 0.0) 0.0 else bill
        val safeTip = if (tip < 0.0) 0.0 else tip
        return roundToTwoDecimals(safeBill + safeTip)
    }

    /**
     * Calculates the per-person split share (`total / split`) rounded to 2 decimal places.
     *
     * @param total Combined bill + tip total.
     * @param split Number of diners sharing the bill (must be `>= 1`).
     * @return Per-person share rounded to 2 decimal places, or `0.0` if inputs are invalid.
     */
    fun calculateSharePerPerson(total: Double, split: Int): Double {
        if (split <= 0 || !total.isFinite() || total <= 0.0) return 0.0
        return roundToTwoDecimals(total / split)
    }

    /**
     * Executes the complete calculation pipeline (`tip`, `total`, and `sharePerPerson`)
     * from a bill subtotal, decimal tip rate, and party split count.
     *
     * @param bill Pre-tip bill subtotal.
     * @param rate Decimal tip fraction (e.g., `0.18` for 18%).
     * @param split Number of people splitting the total (defaults to `1`).
     * @return [TipCalculationResult] containing rounded tip, total, and per-person share.
     */
    fun calculate(bill: Double, rate: Double, split: Int = 1): TipCalculationResult {
        val safeBill = if (bill.isFinite() && bill > 0.0) bill else 0.0
        val safeRate = if (rate.isFinite() && rate > 0.0) rate else 0.0
        val tipAmount = calculateTipAmount(safeBill, safeRate)
        val totalBill = calculateTotalAmount(safeBill, tipAmount)
        val sharePerPerson = calculateSharePerPerson(totalBill, split)
        return TipCalculationResult(
            tipAmount = tipAmount,
            totalBill = totalBill,
            sharePerPerson = sharePerPerson
        )
    }

    /**
     * Returns whether Round Down is currently enabled and actionable for the given bill and total.
     *
     * Round Down remains enabled whenever a positive bill has a total strictly above the bill
     * subtotal (even when the current total is an exact whole integer immediately after Round Up).
     *
     * @param bill Pre-tip bill subtotal.
     * @param currentTotal Current bill + tip total displayed in the calculator.
     * @return `true` if rounding down can decrease the total without dropping below [bill]; `false` otherwise.
     */
    fun isRoundDownEnabled(bill: Double, currentTotal: Double): Boolean {
        if (!bill.isFinite() || !currentTotal.isFinite() || bill <= 0.0) return false
        return roundToTwoDecimals(currentTotal) > roundToTwoDecimals(bill)
    }

    /**
     * Computes the Round Up adjustment to the next whole integer above [currentTotal]
     * (never lower than `ceil(bill)`). If [currentTotal] is already a whole integer,
     * advances cleanly to the next integer (`+1`).
     *
     * @param bill Pre-tip bill subtotal (must be positive and finite).
     * @param currentTotal Current bill + tip total before rounding up.
     * @return [RoundingAdjustment] targeting the next whole integer, or `null` if [bill] is invalid.
     */
    fun calculateRoundUp(bill: Double, currentTotal: Double): RoundingAdjustment? {
        if (!bill.isFinite() || bill <= 0.0) return null
        val safeTotal = if (currentTotal.isFinite()) currentTotal.coerceAtLeast(bill) else bill
        val nextInt = ceil(safeTotal).coerceAtLeast(ceil(bill))
        val targetTotal = if (nextInt <= safeTotal) nextInt + 1.0 else nextInt
        val newTip = roundToTwoDecimals((targetTotal - bill).coerceAtLeast(0.0))
        val newPercentValue = (newTip / bill) * 100.0
        return RoundingAdjustment(
            targetTotal = roundToTwoDecimals(bill + newTip),
            tipAmount = newTip,
            customPercentValue = newPercentValue
        )
    }

    /**
     * Computes the Round Down adjustment to the nearest lower whole integer below [currentTotal],
     * clamped to at least [bill] so the resulting tip is never negative.
     *
     * When [currentTotal] is already a whole integer (such as immediately after Round Up),
     * steps down to the previous integer (`-1`), clamped at [bill].
     *
     * @param bill Pre-tip bill subtotal (must be positive and finite).
     * @param currentTotal Current bill + tip total before rounding down.
     * @return [RoundingAdjustment] targeting the lower whole integer or [bill], or `null` if [bill] is invalid.
     */
    fun calculateRoundDown(bill: Double, currentTotal: Double): RoundingAdjustment? {
        if (!bill.isFinite() || bill <= 0.0) return null
        val safeTotal = if (currentTotal.isFinite()) currentTotal.coerceAtLeast(bill) else bill
        val prevInt = floor(safeTotal)
        val candidate = if (prevInt >= safeTotal) prevInt - 1.0 else prevInt
        val targetTotal = candidate.coerceAtLeast(bill)
        val newTip = roundToTwoDecimals((targetTotal - bill).coerceAtLeast(0.0))
        val newPercentValue = (newTip / bill) * 100.0
        return RoundingAdjustment(
            targetTotal = roundToTwoDecimals(bill + newTip),
            tipAmount = newTip,
            customPercentValue = newPercentValue
        )
    }
}
