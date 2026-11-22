package com.example.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveAndConverterTest {

    @Test
    fun `isTwoPaneWidth triggers strictly above 840dp`() {
        assertFalse(AdaptiveLayoutPolicy.isTwoPaneWidth(400f))
        assertFalse(AdaptiveLayoutPolicy.isTwoPaneWidth(600f))
        assertFalse(AdaptiveLayoutPolicy.isTwoPaneWidth(840f))
        assertFalse(AdaptiveLayoutPolicy.isTwoPaneWidth(Float.NaN))

        assertTrue(AdaptiveLayoutPolicy.isTwoPaneWidth(840.1f))
        assertTrue(AdaptiveLayoutPolicy.isTwoPaneWidth(841f))
        assertTrue(AdaptiveLayoutPolicy.isTwoPaneWidth(1024f))
    }

    @Test
    fun `resolveWindowMetrics distinguishes single pane, portrait two-pane, landscape two-pane, and senior override`() {
        val phone = AdaptiveLayoutPolicy.resolveWindowMetrics(412f, 915f)
        assertEquals(WindowPaneMode.SINGLE_PANE, phone.paneMode)
        assertEquals(TabletTwoPanePosture.SINGLE_PANE, phone.posture)
        assertFalse(phone.isTwoPaneTablet)

        val exactThreshold = AdaptiveLayoutPolicy.resolveWindowMetrics(840f, 1100f)
        assertEquals(WindowPaneMode.SINGLE_PANE, exactThreshold.paneMode)
        assertFalse(exactThreshold.isTwoPaneTablet)

        val portraitTwoPane = AdaptiveLayoutPolicy.resolveWindowMetrics(900f, 1200f)
        assertEquals(WindowPaneMode.TWO_PANE, portraitTwoPane.paneMode)
        assertEquals(TabletTwoPanePosture.PORTRAIT_TWO_PANE, portraitTwoPane.posture)
        assertTrue(portraitTwoPane.isTwoPaneTablet)
        assertEquals(2, portraitTwoPane.serviceColumns)
        assertEquals(0.42f, portraitTwoPane.leftPaneWeight)

        val landscapeTwoPane = AdaptiveLayoutPolicy.resolveWindowMetrics(1024f, 768f)
        assertEquals(WindowPaneMode.TWO_PANE, landscapeTwoPane.paneMode)
        assertEquals(TabletTwoPanePosture.LANDSCAPE_TWO_PANE, landscapeTwoPane.posture)
        assertTrue(landscapeTwoPane.isTwoPaneTablet)
        assertEquals(3, landscapeTwoPane.serviceColumns)
        assertEquals(0.45f, landscapeTwoPane.leftPaneWeight)

        val seniorOnTablet = AdaptiveLayoutPolicy.resolveWindowMetrics(1200f, 800f, isSeniorMode = true)
        assertEquals(WindowPaneMode.SINGLE_PANE, seniorOnTablet.paneMode)
        assertFalse(seniorOnTablet.isTwoPaneTablet)
    }

    @Test
    fun `CurrencyConverter selects secondary currency from selected country and converts amounts cleanly`() {
        // US with USD selected -> EUR
        assertEquals("EUR", CurrencyConverter.resolveSecondaryCurrencyCode("USD", "USD", "A"))
        // Germany with EUR selected -> USD
        assertEquals("USD", CurrencyConverter.resolveSecondaryCurrencyCode("EUR", "EUR", "C"))
        // Switzerland with CHF selected -> EUR (regional European reference)
        assertEquals("EUR", CurrencyConverter.resolveSecondaryCurrencyCode("CHF", "CHF", "C"))
        // Japan with JPY selected -> USD
        assertEquals("USD", CurrencyConverter.resolveSecondaryCurrencyCode("JPY", "JPY", "D"))
        // Japan while user manually selected USD -> JPY (country's local currency)
        assertEquals("JPY", CurrencyConverter.resolveSecondaryCurrencyCode("USD", "JPY", "D"))

        val usdToEur = CurrencyConverter.convertTotal(100.0, 50.0, "USD", "EUR")
        assertEquals("USD", usdToEur.sourceCode)
        assertEquals("EUR", usdToEur.targetCode)
        assertEquals(92.0, usdToEur.convertedTotal, 0.01)
        assertEquals(46.0, usdToEur.convertedSharePerPerson, 0.01)

        val zeroConversion = CurrencyConverter.convertTotal(0.0, 0.0, "USD", "JPY")
        assertEquals(0.0, zeroConversion.convertedTotal, 0.01)
    }

    @Test
    fun `CurrencyConverter pairs destination currency with distinct home currency when provided`() {
        // Traveler with EUR home currency visiting Japan (JPY) sees EUR as secondary when paying in JPY
        assertEquals(
            "EUR",
            CurrencyConverter.resolveSecondaryCurrencyCode("JPY", "JPY", "D", homeCurrencyCode = "EUR")
        )
        // Swapped to home EUR while destination is Japan (JPY) -> secondary switches to destination JPY
        assertEquals(
            "JPY",
            CurrencyConverter.resolveSecondaryCurrencyCode("EUR", "JPY", "D", homeCurrencyCode = "EUR")
        )
        // Traveler with GBP home currency visiting US (USD) sees GBP as secondary when paying in USD
        assertEquals(
            "GBP",
            CurrencyConverter.resolveSecondaryCurrencyCode("USD", "USD", "A", homeCurrencyCode = "GBP")
        )
    }
}
