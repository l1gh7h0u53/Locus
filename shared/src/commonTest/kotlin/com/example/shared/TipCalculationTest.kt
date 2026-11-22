package com.example.shared

import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Cross-platform Kotlin Multiplatform unit tests verifying [TipCalculator] math,
 * round-up/round-down state transitions, round-down enablement, and boundary edge cases.
 */
class TipCalculationTest {

    @Test
    fun `roundDown is enabled whenever total exceeds positive bill including exact integers`() {
        // Standard fractional total above bill
        assertTrue(TipCalculator.isRoundDownEnabled(bill = 43.21, currentTotal = 49.69))

        // Exact integer total immediately after rounding up -> roundDown MUST still be enabled
        assertTrue(TipCalculator.isRoundDownEnabled(bill = 43.21, currentTotal = 50.00))

        // Exact integer bill with whole-integer tip added -> roundDown MUST be enabled
        assertTrue(TipCalculator.isRoundDownEnabled(bill = 50.00, currentTotal = 58.00))

        // Tiny 1-cent tip above bill -> roundDown MUST be enabled to step back to subtotal
        assertTrue(TipCalculator.isRoundDownEnabled(bill = 43.21, currentTotal = 43.22))
    }

    @Test
    fun `roundDown is disabled when at subtotal or with invalid or zero bills`() {
        // Total equals bill subtotal (0 tip) -> disabled
        assertFalse(TipCalculator.isRoundDownEnabled(bill = 43.21, currentTotal = 43.21))
        assertFalse(TipCalculator.isRoundDownEnabled(bill = 50.00, currentTotal = 50.00))

        // Zero or negative bill -> disabled
        assertFalse(TipCalculator.isRoundDownEnabled(bill = 0.0, currentTotal = 0.0))
        assertFalse(TipCalculator.isRoundDownEnabled(bill = -15.0, currentTotal = 0.0))

        // Non-finite inputs -> disabled
        assertFalse(TipCalculator.isRoundDownEnabled(bill = Double.NaN, currentTotal = 10.0))
        assertFalse(TipCalculator.isRoundDownEnabled(bill = 10.0, currentTotal = Double.NaN))
        assertFalse(TipCalculator.isRoundDownEnabled(bill = Double.POSITIVE_INFINITY, currentTotal = 10.0))
        assertFalse(TipCalculator.isRoundDownEnabled(bill = 10.0, currentTotal = Double.NEGATIVE_INFINITY))

        // Sub-cent floating point noise should not falsely enable roundDown
        assertFalse(TipCalculator.isRoundDownEnabled(bill = 100.00, currentTotal = 100.001))
    }

    @Test
    fun `roundDown steps down cleanly immediately after roundUp without getting stuck`() {
        val bill = 43.21
        val initial = TipCalculator.calculate(bill = bill, rate = 0.15) // tip 6.48 -> total 49.69
        assertEquals(49.69, initial.totalBill, 0.001)

        // Step 1: Round up 49.69 -> 50.00
        val up1 = TipCalculator.calculateRoundUp(bill, initial.totalBill)
        assertNotNull(up1)
        assertEquals(50.00, up1.targetTotal, 0.001)
        assertTrue(TipCalculator.isRoundDownEnabled(bill, up1.targetTotal))

        // Step 2: Round up again 50.00 -> 51.00
        val up2 = TipCalculator.calculateRoundUp(bill, up1.targetTotal)
        assertNotNull(up2)
        assertEquals(51.00, up2.targetTotal, 0.001)
        assertTrue(TipCalculator.isRoundDownEnabled(bill, up2.targetTotal))

        // Step 3: Round down immediately after rounding up -> steps cleanly 51.00 -> 50.00
        val down1 = TipCalculator.calculateRoundDown(bill, up2.targetTotal)
        assertNotNull(down1)
        assertEquals(50.00, down1.targetTotal, 0.001)
        assertEquals(6.79, down1.tipAmount, 0.001)

        // Recompute via rate to ensure round-trip calculation matches exact target total
        val recalculatedAfterDown1 = TipCalculator.calculate(bill, down1.customPercentValue / 100.0)
        assertEquals(50.00, recalculatedAfterDown1.totalBill, 0.001)

        // Step 4: Round down again -> 50.00 -> 49.00
        val down2 = TipCalculator.calculateRoundDown(bill, recalculatedAfterDown1.totalBill)
        assertNotNull(down2)
        assertEquals(49.00, down2.targetTotal, 0.001)
        assertEquals(5.79, down2.tipAmount, 0.001)
    }

    @Test
    fun `roundDown floors fractional total and clamps safely at fractional and integer subtotals`() {
        // Fractional bill: 43.21 + 6.48 = 49.69 -> floors to 49.00
        val fromFraction = TipCalculator.calculateRoundDown(bill = 43.21, currentTotal = 49.69)
        assertNotNull(fromFraction)
        assertEquals(49.00, fromFraction.targetTotal, 0.001)
        assertEquals(5.79, fromFraction.tipAmount, 0.001)

        // Near fractional subtotal: 43.21 with total 44.00 -> candidate 43.00 clamps at 43.21 (tip = 0.00)
        val clampFractional = TipCalculator.calculateRoundDown(bill = 43.21, currentTotal = 44.00)
        assertNotNull(clampFractional)
        assertEquals(43.21, clampFractional.targetTotal, 0.001)
        assertEquals(0.00, clampFractional.tipAmount, 0.001)
        assertEquals(0.0, clampFractional.customPercentValue, 0.001)
        assertFalse(TipCalculator.isRoundDownEnabled(43.21, clampFractional.targetTotal))

        // Exact integer bill: 50.00 with total 51.00 -> steps down to 50.00 (tip = 0.00)
        val clampInteger = TipCalculator.calculateRoundDown(bill = 50.00, currentTotal = 51.00)
        assertNotNull(clampInteger)
        assertEquals(50.00, clampInteger.targetTotal, 0.001)
        assertEquals(0.00, clampInteger.tipAmount, 0.001)

        // Calling roundDown when already at subtotal stays clamped at 50.00 (never negative tip)
        val stayClamped = TipCalculator.calculateRoundDown(bill = 50.00, currentTotal = 50.00)
        assertNotNull(stayClamped)
        assertEquals(50.00, stayClamped.targetTotal, 0.001)
        assertEquals(0.00, stayClamped.tipAmount, 0.001)
    }

    @Test
    fun `roundDown handles sub-dollar bills without dropping below subtotal to zero`() {
        val smallBill = 0.75
        val initial = TipCalculator.calculate(bill = smallBill, rate = 0.20) // tip 0.15 -> total 0.90
        assertEquals(0.90, initial.totalBill, 0.001)
        assertTrue(TipCalculator.isRoundDownEnabled(smallBill, initial.totalBill))

        // floor(0.90) is 0.00, which MUST clamp to smallBill (0.75) rather than 0.00
        val roundedDown = TipCalculator.calculateRoundDown(smallBill, initial.totalBill)
        assertNotNull(roundedDown)
        assertEquals(0.75, roundedDown.targetTotal, 0.001)
        assertEquals(0.00, roundedDown.tipAmount, 0.001)
        assertEquals(0.0, roundedDown.customPercentValue, 0.001)
    }

    @Test
    fun `roundDown and roundUp return null for zero negative and non-finite bills`() {
        val invalidBills = listOf(0.0, -0.01, -100.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        for (badBill in invalidBills) {
            assertNull(TipCalculator.calculateRoundDown(badBill, 20.0), "Expected null roundDown for bill=$badBill")
            assertNull(TipCalculator.calculateRoundUp(badBill, 20.0), "Expected null roundUp for bill=$badBill")
        }

        // Valid bill with non-finite currentTotal falls back safely to bill subtotal
        val recovered = TipCalculator.calculateRoundDown(bill = 25.50, currentTotal = Double.NaN)
        assertNotNull(recovered)
        assertEquals(25.50, recovered.targetTotal, 0.001)
        assertEquals(0.00, recovered.tipAmount, 0.001)
    }

    @Test
    fun `roundDown supports high-value zero-decimal currencies cleanly`() {
        val jpyBill = 4850.0
        val initial = TipCalculator.calculate(bill = jpyBill, rate = 0.10) // 5335.0
        assertEquals(5335.0, initial.totalBill, 0.001)

        // Whole integer above bill steps down by 1 unit cleanly
        val stepped = TipCalculator.calculateRoundDown(jpyBill, initial.totalBill)
        assertNotNull(stepped)
        assertEquals(5334.0, stepped.targetTotal, 0.001)
        assertEquals(484.0, stepped.tipAmount, 0.001)

        // Round up from fractional 4850.40 -> 4851.0 -> round down -> 4850.40
        val upFromFrac = TipCalculator.calculateRoundUp(4850.40, 4850.40)
        assertNotNull(upFromFrac)
        assertEquals(ceil(4850.40), upFromFrac.targetTotal, 0.001)

        val downFromUp = TipCalculator.calculateRoundDown(4850.40, upFromFrac.targetTotal)
        assertNotNull(downFromUp)
        assertEquals(4850.40, downFromUp.targetTotal, 0.001)
        assertEquals(0.00, downFromUp.tipAmount, 0.001)
    }

    @Test
    fun `core tip total split and half-up rounding handle precision and edge cases`() {
        // Standard 15% tip & 2-way split
        val standard = TipCalculator.calculate(bill = 50.00, rate = 0.15, split = 2)
        assertEquals(7.50, standard.tipAmount, 0.001)
        assertEquals(57.50, standard.totalBill, 0.001)
        assertEquals(28.75, standard.sharePerPerson, 0.001)

        // HALF_UP decimal rounding: 49.99 * 0.12 = 5.9988 -> 6.00
        assertEquals(6.00, TipCalculator.calculateTipAmount(49.99, 0.12), 0.001)
        assertEquals(55.99, TipCalculator.calculateTotalAmount(49.99, 6.00), 0.001)

        // IEEE-754 half-up boundary: 10.005 -> 10.01 (not 10.00)
        assertEquals(10.01, TipCalculator.roundToTwoDecimals(10.005), 0.0001)
        assertEquals(0.01, TipCalculator.roundToTwoDecimals(0.005), 0.0001)
        assertEquals(0.00, TipCalculator.roundToTwoDecimals(0.004), 0.0001)

        // Invalid split counts & non-finite values
        assertEquals(0.00, TipCalculator.calculateSharePerPerson(100.0, 0), 0.001)
        assertEquals(0.00, TipCalculator.calculateSharePerPerson(100.0, -3), 0.001)
        assertEquals(0.00, TipCalculator.calculateTipAmount(Double.NaN, 0.15), 0.001)
        assertEquals(0.00, TipCalculator.calculateTotalAmount(Double.POSITIVE_INFINITY, 5.0), 0.001)
    }
}
