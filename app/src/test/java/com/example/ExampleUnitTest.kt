package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying mathematically precise tip, total, and split share calculations.
 */
class ExampleUnitTest {

    @Test
    fun testStandardRestaurantPercentageTip() {
        val bill = 50.00
        val rate = 0.15 // 15% standard restaurant rate
        
        val calculatedTip = TippyViewModel.calculateTipAmount(bill, rate)
        val calculatedTotal = TippyViewModel.calculateTotalAmount(bill, calculatedTip)
        val sharePerPerson = TippyViewModel.calculateSharePerPerson(calculatedTotal, 2)

        assertEquals(7.50, calculatedTip, 0.001)
        assertEquals(57.50, calculatedTotal, 0.001)
        assertEquals(28.75, sharePerPerson, 0.001)
    }

    @Test
    fun testComplexRoundingTips() {
        val bill = 49.99
        val rate = 0.12 // 12% Foodtruck rate
        
        val calculatedTip = TippyViewModel.calculateTipAmount(bill, rate) // 49.99 * 0.12 = 5.9988 -> rounds to 6.00
        val calculatedTotal = TippyViewModel.calculateTotalAmount(bill, calculatedTip) // 49.99 + 6.00 = 55.99

        assertEquals(6.00, calculatedTip, 0.001)
        assertEquals(55.99, calculatedTotal, 0.001)
    }

    @Test
    fun testGroceriesVeryLowTip() {
        val bill = 100.0
        val rate = 0.02 // 2% Groceries rate
        
        val calculatedTip = TippyViewModel.calculateTipAmount(bill, rate)
        val calculatedTotal = TippyViewModel.calculateTotalAmount(bill, calculatedTip)

        assertEquals(2.00, calculatedTip, 0.001)
        assertEquals(102.00, calculatedTotal, 0.001)
    }

    @Test
    fun testZeroBillCalculations() {
        val bill = 0.0
        val rate = 0.25 // Generous rate
        
        val calculatedTip = TippyViewModel.calculateTipAmount(bill, rate)
        val calculatedTotal = TippyViewModel.calculateTotalAmount(bill, calculatedTip)
        val sharePerPerson = TippyViewModel.calculateSharePerPerson(calculatedTotal, 4)

        assertEquals(0.00, calculatedTip, 0.001)
        assertEquals(0.00, calculatedTotal, 0.001)
        assertEquals(0.00, sharePerPerson, 0.001)
    }

    @Test
    fun testHugeSplitCalculations() {
        val bill = 125.50
        val rate = 0.20 // 20%
        val split = 6 // 6 people splitting

        val calculatedTip = TippyViewModel.calculateTipAmount(bill, rate) // 25.10
        val calculatedTotal = TippyViewModel.calculateTotalAmount(bill, calculatedTip) // 150.60
        val sharePerPerson = TippyViewModel.calculateSharePerPerson(calculatedTotal, split) // 150.60 / 6 = 25.10

        assertEquals(25.10, calculatedTip, 0.001)
        assertEquals(150.60, calculatedTotal, 0.001)
        assertEquals(25.10, sharePerPerson, 0.001)
    }

    @Test
    fun testGlobalExplorerUnlockLocalizationAllLanguages() {
        val languages = listOf("en", "de", "es", "fr", "pt", "ru", "zh", "ja", "ar", "hi", "bn")
        for (lang in languages) {
            val countdownMsg4 = com.example.i18n.GlobalExplorerLocalization.getUnlockCountdownText(4, lang)
            val countdownMsg1 = com.example.i18n.GlobalExplorerLocalization.getUnlockCountdownText(1, lang)
            val successMsg = com.example.i18n.GlobalExplorerLocalization.getUnlockSuccessText(lang)

            org.junit.Assert.assertTrue("Countdown message for $lang must not be empty", countdownMsg4.isNotBlank())
            org.junit.Assert.assertTrue("Single tap countdown message for $lang must not be empty", countdownMsg1.isNotBlank())
            org.junit.Assert.assertTrue("Success message for $lang must not be empty", successMsg.isNotBlank())
            org.junit.Assert.assertTrue("Success message for $lang should contain celebratory emoji", successMsg.contains("🎉"))
        }

        // Verify distinct translations are present for non-English languages
        assertEquals("Noch 4 Tipps, um den Global Explorer freizuschalten", com.example.i18n.GlobalExplorerLocalization.getUnlockCountdownText(4, "de"))
        assertEquals("Faltan 4 toques para desbloquear el Explorador Global", com.example.i18n.GlobalExplorerLocalization.getUnlockCountdownText(4, "es"))
        assertEquals("Encore 4 appuis pour déverrouiller l'Explorateur Global", com.example.i18n.GlobalExplorerLocalization.getUnlockCountdownText(4, "fr"))
        assertEquals("还需轻触 4 次以解锁全球探索器", com.example.i18n.GlobalExplorerLocalization.getUnlockCountdownText(4, "zh"))
        assertEquals("グローバルエクスプローラー解除まであと 4 回タップ", com.example.i18n.GlobalExplorerLocalization.getUnlockCountdownText(4, "ja"))
    }
}
