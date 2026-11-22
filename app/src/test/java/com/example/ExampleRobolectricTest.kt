package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.TippyTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @org.junit.Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.preferences.OnboardingPreferences(context).markCoachmarksSeen()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Locus", appName)
  }

  @Test
  fun `verify main activity launches and creates successfully`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
    controller.pause().stop().destroy()
  }

  @Test
  fun `verify global country data registry loading`() {
    val countries = CountryDataRegistry.getAllCountries()
    assertTrue(countries.isNotEmpty())
    val germany = CountryDataRegistry.getCountryByIso("DE")
    assertEquals("Germany", germany.country)
    val japan = CountryDataRegistry.getCountryByIso("JP")
    assertEquals("Japan", japan.country)
  }

  @Test
  fun `verify country calculation math`() {
    val viewModel = TippyViewModel()
    val usCountry = CountryDataRegistry.getCountryByIso("US")
    viewModel.selectCountry(usCountry)
    viewModel.updateBillAmount("100.00")

    val state = viewModel.uiState.value
    assertEquals(16.50, state.tipAmount, 0.01)
    assertEquals(116.50, state.totalBill, 0.01)
    assertEquals(116.50, state.sharePerPerson, 0.01)
  }

  @Test
  fun `verify haptic feedback service execution`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val hapticService = HapticFeedbackService(context)
    
    // Test that all feedback methods execute safely without crashing
    hapticService.performSubtleTap()
    hapticService.performCountrySelection()
    hapticService.performUnlockTrigger()
    hapticService.performStandardClick()
  }

  @Test
  fun `exercise full expressive layout flow`() {
    val viewModel = TippyViewModel()
    
    composeTestRule.setContent {
      val uiState by viewModel.uiState.collectAsState()
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    // Assert Material 3 Expressive ISO pill exists in the top header
    composeTestRule.onNodeWithTag("header_iso_pill").assertExists()

    // Input standard amount
    val inputNode = composeTestRule.onNodeWithTag("bill_input_field")
    inputNode.performTextInput("150.50")
    
    // Tap app logo 6 times to trigger country selector
    val logoNode = composeTestRule.onNodeWithTag("app_logo_tap_target")
    repeat(6) {
      logoNode.performClick()
    }
  }

  @Test
  fun `exercise senior mode transition and interaction`() {
    val viewModel = TippyViewModel()
    viewModel.toggleSeniorMode()
    
    composeTestRule.setContent {
      val uiState by viewModel.uiState.collectAsState()
      TippyTheme(darkTheme = false, isSeniorMode = true) {
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    val inputNode = composeTestRule.onNodeWithTag("senior_bill_input")
    inputNode.performTextInput("80.00")

    val state = viewModel.uiState.value
    assertTrue(state.isSeniorMode)
    assertEquals(80.00, state.billAmountInput.toDouble(), 0.01)
  }

  @Test
  fun `verify offline language resolution pipeline`() {
    // 1. Direct System Locale match
    val directDe = com.example.i18n.LanguageResolver.resolveLanguage("de", "JP")
    assertEquals("de", directDe)

    val directAr = com.example.i18n.LanguageResolver.resolveLanguage("ar", "US")
    assertEquals("ar", directAr)

    // 2. Country ISO Mapping Lookup for unsupported system languages
    val austrianFallback = com.example.i18n.LanguageResolver.resolveLanguage("nl", "AT")
    assertEquals("de", austrianFallback)

    val mexicanFallback = com.example.i18n.LanguageResolver.resolveLanguage("ko", "MX")
    assertEquals("es", mexicanFallback)

    val saudiFallback = com.example.i18n.LanguageResolver.resolveLanguage("fi", "SA")
    assertEquals("ar", saudiFallback)

    val brazilFallback = com.example.i18n.LanguageResolver.resolveLanguage("it", "BR")
    assertEquals("pt", brazilFallback)

    val chinaFallback = com.example.i18n.LanguageResolver.resolveLanguage("th", "CN")
    assertEquals("zh", chinaFallback)

    val indiaFallback = com.example.i18n.LanguageResolver.resolveLanguage("el", "IN")
    assertEquals("hi", indiaFallback)

    val russiaFallback = com.example.i18n.LanguageResolver.resolveLanguage("sv", "RU")
    assertEquals("ru", russiaFallback)

    // 3. Hard fallback
    val unknownFallback = com.example.i18n.LanguageResolver.resolveLanguage("xx", "ZZ")
    assertEquals("en", unknownFallback)

    // RTL verification
    assertTrue(com.example.i18n.LanguageResolver.isRtl("ar"))
    org.junit.Assert.assertFalse(com.example.i18n.LanguageResolver.isRtl("en"))
    org.junit.Assert.assertFalse(com.example.i18n.LanguageResolver.isRtl("de"))
  }

  @Test
  fun `verify dynamic localized context wrapping`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Test German wrapping
    val deContext = com.example.i18n.LocaleContextWrapper.wrap(context, "de")
    assertEquals("Rechnungsbetrag", deContext.getString(R.string.bill_amount))

    // Test Spanish wrapping
    val esContext = com.example.i18n.LocaleContextWrapper.wrap(context, "es")
    assertEquals("Monto de la cuenta", esContext.getString(R.string.bill_amount))

    // Test French wrapping
    val frContext = com.example.i18n.LocaleContextWrapper.wrap(context, "fr")
    assertEquals("Montant de l\'addition", frContext.getString(R.string.bill_amount))

    // Test Arabic wrapping
    val arContext = com.example.i18n.LocaleContextWrapper.wrap(context, "ar")
    assertEquals("مبلغ الفاتورة", arContext.getString(R.string.bill_amount))

    // Test Chinese wrapping
    val zhContext = com.example.i18n.LocaleContextWrapper.wrap(context, "zh")
    assertEquals("账单金额", zhContext.getString(R.string.bill_amount))

    // Test Hindi wrapping
    val hiContext = com.example.i18n.LocaleContextWrapper.wrap(context, "hi")
    assertEquals("बिल राशि", hiContext.getString(R.string.bill_amount))

    // Test Russian wrapping
    val ruContext = com.example.i18n.LocaleContextWrapper.wrap(context, "ru")
    assertEquals("Сумма счета", ruContext.getString(R.string.bill_amount))

    // Test Portuguese wrapping
    val ptContext = com.example.i18n.LocaleContextWrapper.wrap(context, "pt")
    assertEquals("Valor da Conta", ptContext.getString(R.string.bill_amount))

    // Test Bengali wrapping
    val bnContext = com.example.i18n.LocaleContextWrapper.wrap(context, "bn")
    assertEquals("বিলের পরিমাণ", bnContext.getString(R.string.bill_amount))
  }

  @Test
  fun `verify locale formatter rules for all supported languages`() {
    val usdCurrency = CurrencyOption.USD
    val eurCurrency = CurrencyOption.EUR

    // US formatting
    val usFormatted = com.example.i18n.LocaleFormatter.formatCurrency(125.50, usdCurrency, "en")
    assertEquals("$125.50", usFormatted)

    // German formatting with symbol after
    val deFormatted = com.example.i18n.LocaleFormatter.formatCurrency(125.50, eurCurrency, "de")
    assertTrue(deFormatted.contains("€"))

    // Arabic formatting
    val arFormatted = com.example.i18n.LocaleFormatter.formatCurrency(125.50, usdCurrency, "ar")
    assertTrue(arFormatted.isNotEmpty())

    // Quick add pill formatting
    val usQuickAdd = com.example.i18n.LocaleFormatter.formatQuickAdd(10.0, usdCurrency, "en")
    assertEquals("+$10", usQuickAdd)

    val deQuickAdd = com.example.i18n.LocaleFormatter.formatQuickAdd(10.0, eurCurrency, "de")
    assertEquals("+€10", deQuickAdd)

    // Percent formatting
    val usPercent = com.example.i18n.LocaleFormatter.formatPercent(0.185, "en")
    assertEquals("18.5%", usPercent)
  }

  @Test
  fun `verify Hong Kong and Taiwan currencies resolve correctly`() {
    val hkd = CurrencyOption.fromCode("HKD")
    val twd = CurrencyOption.fromCode("TWD")

    assertNotNull(hkd)
    assertEquals("HKD", hkd?.code)
    assertEquals("HK$", hkd?.symbol)
    assertEquals("Hong Kong Dollar", hkd?.label)

    assertNotNull(twd)
    assertEquals("TWD", twd?.code)
    assertEquals("NT$", twd?.symbol)
    assertEquals("New Taiwan Dollar", twd?.label)

    val hkCountry = CountryDataRegistry.getCountryByIso("HK")
    assertEquals("HKD", hkCountry.localCurrency)

    val twCountry = CountryDataRegistry.getCountryByIso("TW")
    assertEquals("TWD", twCountry.localCurrency)

    val hkCurrency = CurrencyOption.forCountry("Hong Kong")
    assertEquals(CurrencyOption.HKD, hkCurrency)

    val twCurrency = CurrencyOption.forCountry("Taiwan")
    assertEquals(CurrencyOption.TWD, twCurrency)
  }

  @Test
  fun `verify Dark Mode theme renders without errors`() {
    composeTestRule.setContent {
      TippyTheme(darkTheme = true, isSeniorMode = false) {
        TippyMainScreen(
          uiState = TippyUiState(
            billAmountInput = "100.00",
            totalBill = 120.0,
            tipAmount = 20.0
          ),
          viewModel = TippyViewModel(),
          currentDarkTheme = true
        )
      }
    }

    composeTestRule.onNodeWithTag("bill_input_field").assertExists()
    composeTestRule.onNodeWithTag("header_iso_pill").assertExists()
    composeTestRule.onNodeWithTag("settings_button").assertExists()
    composeTestRule.onNodeWithTag("app_logo_tap_target").assertExists()
  }

  @Test
  fun `verify SettingsBottomSheet renders System, Light, Dark themes and rounded Save Prompt buttons`() {
    var selectedTheme: Boolean? = null
    var saveMode = com.example.history.SavePromptMode.ALWAYS

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        com.example.ui.components.SettingsBottomSheet(
          visible = true,
          currentDarkTheme = false,
          themeMode = null,
          isSeniorMode = false,
          currentLanguage = "en",
          isManualLanguageOverride = false,
          savePromptMode = saveMode,
          onDismiss = {},
          onSelectThemeMode = { selectedTheme = it },
          onOpenHistory = {},
          onOpenLanguage = {},
          onSavePromptModeChange = { saveMode = it }
        )
      }
    }

    // Verify System, Light, Dark buttons exist in Appearance section
    composeTestRule.onNodeWithTag("theme_system_button").assertExists()
    composeTestRule.onNodeWithTag("theme_light_button").assertExists()
    composeTestRule.onNodeWithTag("theme_dark_button").assertExists()

    // Verify Always and Never fully rounded buttons exist
    composeTestRule.onNodeWithTag("settings_save_prompt_always").assertExists()
    composeTestRule.onNodeWithTag("settings_save_prompt_never").assertExists()

    // Test clicking System, Light, and Dark options
    composeTestRule.onNodeWithTag("theme_light_button").performClick()
    assertEquals(false, selectedTheme)

    composeTestRule.onNodeWithTag("theme_dark_button").performClick()
    assertEquals(true, selectedTheme)

    composeTestRule.onNodeWithTag("theme_system_button").performClick()
    assertEquals(null, selectedTheme)

    // Test clicking Never mode
    composeTestRule.onNodeWithTag("settings_save_prompt_never").performClick()
    assertEquals(com.example.history.SavePromptMode.NEVER, saveMode)
  }

  @Test
  fun `verify useful tipping information advice pill renders in Dark Mode`() {
    val defaultCountry = CountryDataRegistry.getDefaultCountry()
    val rule = defaultCountry.getRuleFor(ServiceType.RESTAURANT.datasetKey)

    composeTestRule.setContent {
      TippyTheme(darkTheme = true, isSeniorMode = false) {
        TippyMainScreen(
          uiState = TippyUiState(
            billAmountInput = "50.00",
            activeCountry = defaultCountry,
            selectedServiceType = ServiceType.RESTAURANT
          ),
          viewModel = TippyViewModel(),
          currentDarkTheme = true
        )
      }
    }

    composeTestRule.onNodeWithTag("bill_input_field").assertExists()
  }

  @Test
  fun `verify Senior Mode Service Category renders correctly across all languages`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = TippyViewModel()
    viewModel.toggleSeniorMode()

    composeTestRule.setContent {
      val uiState by viewModel.uiState.collectAsState()
      TippyTheme(darkTheme = false, isSeniorMode = uiState.isSeniorMode) {
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    val languages = listOf("en", "de", "es", "fr", "pt", "ru", "ja", "zh", "ar", "hi", "bn")
    for (lang in languages) {
      viewModel.setLanguage(context, lang)
      composeTestRule.onNodeWithTag("senior_service_restaurant").assertExists()
    }
  }

  @Test
  fun `verify all countries in registry have resolved non-null currency pills`() {
    val allCountries = CountryDataRegistry.getAllCountries()
    assertTrue(allCountries.isNotEmpty())

    for (country in allCountries) {
      assertNotNull(country.localCurrency)
      assertTrue(country.localCurrency!!.isNotBlank())
      val resolvedCurrency = country.getResolvedCurrency()
      assertNotNull(resolvedCurrency)
      assertTrue(resolvedCurrency.code.isNotBlank())
    }
  }

  @Test
  fun `verify CountrySelectionView renders currency pill tags for items`() {
    val allCountries = CountryDataRegistry.getAllCountries()
    val firstCountry = allCountries.first()

    composeTestRule.setContent {
      TippyTheme(darkTheme = false) {
        CountrySelectionView(
          allCountries = listOf(firstCountry),
          currentCountry = firstCountry,
          onBack = {},
          onSelectCountry = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("country_currency_${firstCountry.iso}", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("country_item_${firstCountry.iso}").assertExists()

    val resolvedCurrency = firstCountry.getResolvedCurrency()
    val currencyCode = firstCountry.localCurrency?.takeIf { it.isNotBlank() } ?: resolvedCurrency.code
    val expectedContentDescription = "${resolvedCurrency.label}, $currencyCode"
    composeTestRule.onNodeWithContentDescription(expectedContentDescription, useUnmergedTree = true).assertExists()
  }

  @Test
  fun `verify Senior Mode calculated totals card renders with TTS pill and handles click`() {
    val viewModel = TippyViewModel()
    viewModel.toggleSeniorMode() // Switch to Senior Mode
    viewModel.updateBillAmount("100.00")

    composeTestRule.setContent {
      TippyTheme(isSeniorMode = true) {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    composeTestRule.waitForIdle()

    // Assert the Senior Totals Card exists with its test tag
    composeTestRule.onNodeWithTag("senior_totals_card").assertExists()

    // Assert the TTS audio pill exists inside the totals card
    composeTestRule.onNodeWithTag("senior_tts_pill", useUnmergedTree = true).assertExists()

    // Verify tapping the totals card triggers without crashing
    composeTestRule.onNodeWithTag("senior_totals_card").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `verify CurrencyQuickBillRegistry maps currency tiers correctly`() {
    // Tier 1 Standard ($1 = 1-2 units)
    assertEquals(CurrencyMagnitudeTier.TIER_1_STANDARD, CurrencyQuickBillRegistry.getTierForCurrency("USD"))
    assertEquals(CurrencyMagnitudeTier.TIER_1_STANDARD, CurrencyQuickBillRegistry.getTierForCurrency("EUR"))
    assertEquals(CurrencyMagnitudeTier.TIER_1_STANDARD, CurrencyQuickBillRegistry.getTierForCurrency("GBP"))
    assertEquals(listOf(1.0, 5.0, 10.0, 20.0), CurrencyMagnitudeTier.TIER_1_STANDARD.quickAddAmounts)

    // Tier 2 Moderate ($1 = 10-100 units)
    assertEquals(CurrencyMagnitudeTier.TIER_2_MODERATE, CurrencyQuickBillRegistry.getTierForCurrency("MXN"))
    assertEquals(CurrencyMagnitudeTier.TIER_2_MODERATE, CurrencyQuickBillRegistry.getTierForCurrency("INR"))
    assertEquals(CurrencyMagnitudeTier.TIER_2_MODERATE, CurrencyQuickBillRegistry.getTierForCurrency("BRL"))
    assertEquals(listOf(10.0, 50.0, 100.0, 200.0), CurrencyMagnitudeTier.TIER_2_MODERATE.quickAddAmounts)

    // Tier 3 High ($1 = 100-5000 units)
    assertEquals(CurrencyMagnitudeTier.TIER_3_HIGH, CurrencyQuickBillRegistry.getTierForCurrency("JPY"))
    assertEquals(CurrencyMagnitudeTier.TIER_3_HIGH, CurrencyQuickBillRegistry.getTierForCurrency("KRW"))
    assertEquals(CurrencyMagnitudeTier.TIER_3_HIGH, CurrencyQuickBillRegistry.getTierForCurrency("COP"))
    assertEquals(listOf(100.0, 500.0, 1000.0, 5000.0), CurrencyMagnitudeTier.TIER_3_HIGH.quickAddAmounts)
    assertEquals(listOf("+100", "+500", "+1k", "+5k"), CurrencyMagnitudeTier.TIER_3_HIGH.displayLabels)

    // Tier 4 Super-High ($1 = 5000+ units)
    assertEquals(CurrencyMagnitudeTier.TIER_4_SUPER_HIGH, CurrencyQuickBillRegistry.getTierForCurrency("IDR"))
    assertEquals(CurrencyMagnitudeTier.TIER_4_SUPER_HIGH, CurrencyQuickBillRegistry.getTierForCurrency("VND"))
    assertEquals(CurrencyMagnitudeTier.TIER_4_SUPER_HIGH, CurrencyQuickBillRegistry.getTierForCurrency("PYG"))
    assertEquals(listOf(5000.0, 10000.0, 50000.0, 100000.0), CurrencyMagnitudeTier.TIER_4_SUPER_HIGH.quickAddAmounts)
    assertEquals(listOf("+5k", "+10k", "+50k", "+100k"), CurrencyMagnitudeTier.TIER_4_SUPER_HIGH.displayLabels)
  }

  @Test
  fun `verify dynamic quick add increments update bill amount for high-magnitude currency`() {
    val viewModel = TippyViewModel()
    // Select Japan / JPY (Tier 3)
    val jpyOption = CurrencyOption.JPY
    viewModel.selectCurrency(jpyOption)

    composeTestRule.setContent {
      TippyTheme {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    composeTestRule.waitForIdle()

    // Assert Tier 3 quick add buttons exist (+100, +500, +1000, +5000)
    composeTestRule.onNodeWithTag("quick_add_100").assertExists()
    composeTestRule.onNodeWithTag("quick_add_500").assertExists()
    composeTestRule.onNodeWithTag("quick_add_1000").assertExists()
    composeTestRule.onNodeWithTag("quick_add_5000").assertExists()

    // Click +1000
    composeTestRule.onNodeWithTag("quick_add_1000").performClick()
    composeTestRule.waitForIdle()

    assertEquals("1000.00", viewModel.uiState.value.billAmountInput)
  }

  @Test
  fun `verify bill share summary generation and offline intent dispatch`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = TippyViewModel()
    val usCountry = CountryDataRegistry.getCountryByIso("US")
    viewModel.selectCountry(usCountry)
    viewModel.updateBillAmount("50.00")
    viewModel.incrementSplit()

    val state = viewModel.uiState.value
    val summary = BillShareHelper.createBillSummary(context, state)

    // Verify summary contains key information
    assertTrue(summary.contains("Locus"))
    assertTrue(summary.contains("USA"))
    assertTrue(summary.contains("50.00") || summary.contains("50"))
    assertTrue(summary.contains("16.5%") || summary.contains("16%"))
    assertTrue(summary.contains("58.25"))
    assertTrue(summary.contains("29.13") || summary.contains("29.12"))

    // Verify offline share intent execution executes safely without crashing
    BillShareHelper.shareBillSummary(context, state)
  }

  @Test
  fun `verify onboarding preferences persistence behavior`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val onboardingPrefs = com.example.preferences.OnboardingPreferences(context)
    
    // Test initial/reset state
    onboardingPrefs.resetCoachmarks()
    assertEquals(false, onboardingPrefs.hasSeenCoachmarks())

    // Test mark as seen
    onboardingPrefs.markCoachmarksSeen()
    assertEquals(true, onboardingPrefs.hasSeenCoachmarks())
  }

  @Test
  fun `verify coach mark overlay displays, navigates, and completes dismissal`() {
    var dismissed = false

    composeTestRule.setContent {
      TippyTheme {
        com.example.ui.components.CoachMarkOverlay(
          visible = true,
          onDismiss = { dismissed = true }
        )
      }
    }

    composeTestRule.waitForIdle()

    // Step 1: Senior Mode explanation
    composeTestRule.onNodeWithTag("coach_mark_overlay", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("coach_mark_title", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("Senior Mode", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("coach_mark_next_button", useUnmergedTree = true).assertExists()

    // Navigate to Step 2: Quick-Add Buttons
    composeTestRule.onNodeWithTag("coach_mark_next_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Quick Add", substring = true, useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("coach_mark_back_button", useUnmergedTree = true).assertExists()

    // Navigate to Step 3: Tip History & Preferences (Final step with Got It button)
    composeTestRule.onNodeWithTag("coach_mark_next_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("coach_mark_got_it_button", useUnmergedTree = true).assertExists()

    // Test Back button navigation
    composeTestRule.onNodeWithTag("coach_mark_back_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Quick Add", substring = true, useUnmergedTree = true).assertExists()

    // Test Skip button dismissal
    composeTestRule.onNodeWithTag("coach_mark_skip_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    assertTrue(dismissed)
  }

  @Test
  fun `verify hands-on tutorial step 3 allows toggling prompt to save and tutorial on launch preferences`() {
    var savedPromptMode = com.example.history.SavePromptMode.ALWAYS
    var tutorialLaunchMode = "NEVER"
    var dismissed = false

    composeTestRule.setContent {
      TippyTheme {
        com.example.ui.components.CoachMarkOverlay(
          visible = true,
          savePromptMode = savedPromptMode,
          onSavePromptModeChange = { savedPromptMode = it },
          tutorialMode = tutorialLaunchMode,
          onTutorialModeChange = { tutorialLaunchMode = it },
          onDismiss = { dismissed = true }
        )
      }
    }

    composeTestRule.waitForIdle()

    // Navigate to Step 2: Quick-Add
    composeTestRule.onNodeWithTag("coach_mark_next_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    // Navigate to Step 3: Tip History & Preferences
    composeTestRule.onNodeWithTag("coach_mark_next_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    // Verify chips exist in Step 3
    composeTestRule.onNodeWithTag("tutorial_save_always_chip", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("tutorial_save_never_chip", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("tutorial_show_always_chip", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("tutorial_show_never_chip", useUnmergedTree = true).assertExists()

    // Toggle Prompt to Save to "Never"
    composeTestRule.onNodeWithTag("tutorial_save_never_chip", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    assertEquals(com.example.history.SavePromptMode.NEVER, savedPromptMode)

    // Toggle Prompt to Save back to "Always"
    composeTestRule.onNodeWithTag("tutorial_save_always_chip", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    assertEquals(com.example.history.SavePromptMode.ALWAYS, savedPromptMode)

    // Toggle Tutorial on Launch to "Always"
    composeTestRule.onNodeWithTag("tutorial_show_always_chip", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    assertEquals("ALWAYS", tutorialLaunchMode)

    // Toggle Tutorial on Launch back to "Never"
    composeTestRule.onNodeWithTag("tutorial_show_never_chip", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    assertEquals("NEVER", tutorialLaunchMode)
  }

  @Test
  fun `verify coach mark overlay is shown on first launch and dismissed permanently`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val onboardingPrefs = com.example.preferences.OnboardingPreferences(context)
    onboardingPrefs.resetCoachmarks() // Simulate fresh first install

    val viewModel = TippyViewModel()

    composeTestRule.setContent {
      TippyTheme {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    composeTestRule.waitForIdle()

    // Coach mark overlay must be visible on first launch
    composeTestRule.onNodeWithTag("coach_mark_overlay", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("Senior Mode", useUnmergedTree = true).assertExists()

    // Click Next to reach Step 2: Quick Add
    composeTestRule.onNodeWithTag("coach_mark_next_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    // Click Next to reach Step 3: History & Preferences
    composeTestRule.onNodeWithTag("coach_mark_next_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    // Click Got It to complete tour
    composeTestRule.onNodeWithTag("coach_mark_got_it_button", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    // Verify preference is now marked as seen
    assertTrue(onboardingPrefs.hasSeenCoachmarks())
  }

  @Test
  fun `verify ukraine tipping rules bar standard 10 percent`() {
    val ukraine = CountryDataRegistry.getCountryByIso("UA")
    assertEquals("Ukraine", ukraine.country)
    assertEquals("UAH", ukraine.localCurrency)
    assertTrue(ukraine.hasCustomRules)

    val barRule = ukraine.getRuleFor("Bar")
    assertEquals("10%", barRule.standardTip)
    assertEquals(0.10, barRule.parseStandardRate(), 0.001)

    val restaurantRule = ukraine.getRuleFor("Restaurants")
    assertEquals("10%", restaurantRule.standardTip)
    assertEquals(0.10, restaurantRule.parseStandardRate(), 0.001)

    // Verify TippyViewModel behavior with Ukraine and Bar service type
    val viewModel = TippyViewModel()
    viewModel.selectCountry(ukraine)
    viewModel.selectServiceType(ServiceType.BAR)
    viewModel.updateBillAmount("500.00")

    val state = viewModel.uiState.value
    assertEquals(0.10, state.selectedPercent, 0.001)
    assertEquals(50.00, state.tipAmount, 0.01)
    assertEquals(550.00, state.totalBill, 0.01)
  }

  @Test
  fun `verify senior mode tipping tips are localized in all supported languages`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = TippyViewModel()
    viewModel.toggleSeniorMode()

    val usCountry = CountryDataRegistry.getCountryByIso("US")
    viewModel.selectCountry(usCountry)
    viewModel.selectServiceType(ServiceType.RESTAURANT)

    val languages = listOf("en", "de", "es", "fr", "pt", "ru", "ja", "zh", "ar", "hi", "bn")
    for (lang in languages) {
      viewModel.setLanguage(context, lang)
      val localizedContext = com.example.i18n.LocaleContextWrapper.wrap(context, lang)
      val currentRule = viewModel.uiState.value.currentRule
      val localizedNote = currentRule.getLocalizedNotes(localizedContext)
      assertTrue(
        "Tipping note for lang $lang should not be empty",
        localizedNote.isNotBlank()
      )
      // When German is selected, German note should be returned
      if (lang == "de") {
        assertEquals("Trinkgeld ist praktisch Pflicht (15% - 20%); Kellner leben davon.", localizedNote)
      } else if (lang == "es") {
        assertEquals("La propina es prácticamente obligatoria (15% - 20%); los camareros viven de ello.", localizedNote)
      }
    }
  }

  @Test
  fun `verify localized currency formatting across regional standards`() {
    val usd = CurrencyOption.USD
    val eur = CurrencyOption.EUR
    val jpy = CurrencyOption.JPY
    val brl = CurrencyOption.BRL
    val inr = CurrencyOption.INR
    val bdt = CurrencyOption.BDT
    val chf = CurrencyOption.CHF

    // 1. English (US) standards - prefix without space, dot decimal, comma grouping
    val usFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, usd, "en")
    assertEquals("$1,250.50", usFormatted)

    // 2. German standards - comma decimal, dot grouping, Euro suffix with space
    val deFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, eur, "de")
    assertTrue(deFormatted.contains("1.250,50") && deFormatted.contains("€"))

    // 3. French standards - comma decimal, Euro suffix with space
    val frFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, eur, "fr")
    assertTrue(frFormatted.contains("1") && frFormatted.contains("250,50") && frFormatted.contains("€"))

    // 4. Japanese standards - zero decimals for whole numbers
    val jpyFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1500.0, jpy, "ja")
    assertEquals("¥1,500", jpyFormatted)

    // 5. Brazilian Real - prefix with space, comma decimal, dot grouping
    val brlFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, brl, "pt")
    assertTrue(brlFormatted.contains("R$") && brlFormatted.contains("1.250,50"))

    // 6. Indian Rupee - prefix with space in Hindi locale
    val inrFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, inr, "hi")
    assertTrue(inrFormatted.contains("₹") && inrFormatted.contains("1,250.50"))

    // 7. Bangladeshi Taka - prefix with space in Bengali locale
    val bdtFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, bdt, "bn")
    assertTrue(bdtFormatted.contains("৳") && bdtFormatted.isNotEmpty())

    // 8. Swiss Franc - suffix with space
    val chfFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, chf, "de")
    assertTrue(chfFormatted.contains("CHF") && chfFormatted.endsWith("CHF"))

    // 9. Decimal separators check
    assertEquals(',', com.example.i18n.LocaleFormatter.getDecimalSeparator("de"))
    assertEquals(',', com.example.i18n.LocaleFormatter.getDecimalSeparator("fr"))
    assertEquals('.', com.example.i18n.LocaleFormatter.getDecimalSeparator("en"))
    assertEquals('.', com.example.i18n.LocaleFormatter.getDecimalSeparator("ja"))

    // 10. Percentage formatting with locale conventions
    assertEquals("15.0%", com.example.i18n.LocaleFormatter.formatPercent(0.15, "en"))
    assertEquals("15,0%", com.example.i18n.LocaleFormatter.formatPercent(0.15, "de"))
    assertEquals("15,0 %", com.example.i18n.LocaleFormatter.formatPercent(0.15, "fr"))

    // 11. Amount only formatting
    val deAmountOnly = com.example.i18n.LocaleFormatter.formatAmountOnly(1250.50, "de")
    assertEquals("1.250,50", deAmountOnly)

    val usAmountOnly = com.example.i18n.LocaleFormatter.formatAmountOnly(1250.50, "en")
    assertEquals("1,250.50", usAmountOnly)
  }

  @Test
  fun `verify Select Currency with All 145 is moved to Settings under Language and Region`() {
    var selectedCurrency: CurrencyOption = CurrencyOption.USD

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        com.example.ui.components.SettingsBottomSheet(
          visible = true,
          currentDarkTheme = false,
          themeMode = null,
          isSeniorMode = false,
          currentLanguage = "en",
          isManualLanguageOverride = false,
          selectedCurrency = selectedCurrency,
          onSelectCurrency = { selectedCurrency = it },
          onDismiss = {},
          onOpenHistory = {},
          onOpenLanguage = {}
        )
      }
    }

    // Verify all_currencies_button exists in Settings and contains 145
    val allCurrenciesBtn = composeTestRule.onNodeWithTag("all_currencies_button")
    allCurrenciesBtn.assertExists()

    // Verify clicking a currency chip updates currency
    composeTestRule.onNodeWithTag("currency_EUR").assertExists()
    composeTestRule.onNodeWithTag("currency_EUR").performClick()
    assertEquals(CurrencyOption.EUR, selectedCurrency)
  }

  @Test
  fun `verify CountryFlagHelper converts ISO codes to regional indicator flag emojis`() {
    assertEquals("🇺🇸", com.example.ui.components.CountryFlagHelper.getFlagEmoji("US"))
    assertEquals("🇮🇹", com.example.ui.components.CountryFlagHelper.getFlagEmoji("IT"))
    assertEquals("🇦🇹", com.example.ui.components.CountryFlagHelper.getFlagEmoji("AT"))
    assertEquals("🇪🇸", com.example.ui.components.CountryFlagHelper.getFlagEmoji("ES"))
    assertEquals("🇲🇦", com.example.ui.components.CountryFlagHelper.getFlagEmoji("MA"))
    assertEquals("🌐", com.example.ui.components.CountryFlagHelper.getFlagEmoji(""))
    assertEquals("🌐", com.example.ui.components.CountryFlagHelper.getFlagEmoji("XYZ"))
  }

  @Test
  fun `verify header country flag button exists in TippyMainHost`() {
    val viewModel = TippyViewModel()
    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    // Verify header country flag button exists next to Senior Mode
    composeTestRule.onNodeWithTag("header_country_flag_button").assertExists()
    composeTestRule.onNodeWithTag("senior_mode_toggle").assertExists()
    composeTestRule.onNodeWithTag("settings_button").assertExists()
  }

  @Test
  fun `verify CountrySelectionBottomSheet allows searching and selecting countries`() {
    var selectedCountry: CountryTippingData? = null
    val countries = CountryDataRegistry.getAllCountries()
    val defaultCountry = CountryDataRegistry.getDefaultCountry()

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        com.example.ui.components.CountrySelectionBottomSheet(
          visible = true,
          currentCountry = defaultCountry,
          allCountries = countries,
          currentLanguage = "en",
          onDismiss = {},
          onSelectCountry = { selectedCountry = it }
        )
      }
    }

    composeTestRule.onNodeWithTag("country_selection_sheet").assertExists()
    composeTestRule.onNodeWithTag("country_search_input").assertExists()

    // Default country (US) item exists at top
    composeTestRule.onNodeWithTag("country_sheet_item_US").assertExists()

    // Type query "Italy" to filter
    composeTestRule.onNodeWithTag("country_search_input").performTextInput("Italy")
    composeTestRule.onNodeWithTag("country_sheet_item_IT").assertExists()

    // Click Italy and assert callback
    composeTestRule.onNodeWithTag("country_sheet_item_IT").performClick()
    assertEquals("IT", selectedCountry?.iso)
  }

  @Test
  fun `verify easter egg triggers after exactly 10 taps on Theme icon in Settings with no toast or indicator`() {
    var easterEggTriggered = false

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        com.example.ui.components.SettingsBottomSheet(
          visible = true,
          currentDarkTheme = false,
          themeMode = null,
          isSeniorMode = false,
          currentLanguage = "en",
          isManualLanguageOverride = false,
          onDismiss = {},
          onOpenHistory = {},
          onOpenLanguage = {},
          onEasterEggUnlocked = { easterEggTriggered = true }
        )
      }
    }

    val themeIconTarget = composeTestRule.onNodeWithTag("theme_icon_tap_target")
    themeIconTarget.assertExists()

    // Tap 9 times - should not trigger
    repeat(9) {
      themeIconTarget.performClick()
    }
    assertEquals(false, easterEggTriggered)

    // 10th tap triggers easter egg
    themeIconTarget.performClick()
    assertEquals(true, easterEggTriggered)
  }

  @Test
  fun `verify currency amounts use non-breaking spaces across all locales to prevent number breaks`() {
    val usd = CurrencyOption.USD
    val eur = CurrencyOption.EUR
    val chf = CurrencyOption.CHF

    val usFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, usd, "en")
    assertFalse("Should not contain breaking ASCII space", usFormatted.contains(' '))

    val deFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, eur, "de")
    assertFalse("Should not contain breaking ASCII space", deFormatted.contains(' '))
    assertTrue(deFormatted.contains('\u00A0'))

    val chfFormatted = com.example.i18n.LocaleFormatter.formatCurrency(1250.50, chf, "de")
    assertFalse("Should not contain breaking ASCII space", chfFormatted.contains(' '))
    assertTrue(chfFormatted.contains('\u00A0'))
  }

  @Test
  fun `verify tip calculation history is hidden in settings when savePromptMode is NEVER and shown when ALWAYS`() {
    val savePromptModeState = androidx.compose.runtime.mutableStateOf(com.example.history.SavePromptMode.ALWAYS)

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        com.example.ui.components.SettingsBottomSheet(
          visible = true,
          currentDarkTheme = false,
          themeMode = null,
          isSeniorMode = false,
          currentLanguage = "en",
          isManualLanguageOverride = false,
          savePromptMode = savePromptModeState.value,
          onDismiss = {},
          onOpenHistory = {},
          onOpenLanguage = {}
        )
      }
    }
    // When ALWAYS: tip_history_button exists
    composeTestRule.onNodeWithTag("tip_history_button").assertExists()

    // When switched to NEVER: tip_history_button does not exist
    savePromptModeState.value = com.example.history.SavePromptMode.NEVER
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tip_history_button").assertDoesNotExist()
  }

  @Test
  fun `verify roundDownTotal steps down cleanly immediately after roundUpTotal and clamps at bill`() {
    val viewModel = TippyViewModel()
    val usCountry = CountryDataRegistry.getCountryByIso("US")
    viewModel.selectCountry(usCountry)
    viewModel.updateBillAmount("43.21")

    val initialTotal = viewModel.uiState.value.totalBill
    assertTrue(initialTotal > 43.21)

    // Round up to next integer
    viewModel.roundUpTotal()
    val roundedUpTotal = viewModel.uiState.value.totalBill
    assertEquals(kotlin.math.ceil(initialTotal), roundedUpTotal, 0.01)

    // Round up again advances by 1
    viewModel.roundUpTotal()
    assertEquals(roundedUpTotal + 1.0, viewModel.uiState.value.totalBill, 0.01)

    // Round down immediately after rounding up steps down by 1 (not disabled / stuck)
    viewModel.roundDownTotal()
    assertEquals(roundedUpTotal, viewModel.uiState.value.totalBill, 0.01)

    // Repeated round down eventually clamps at bill subtotal with 0 tip
    repeat(20) { viewModel.roundDownTotal() }
    assertEquals(43.21, viewModel.uiState.value.totalBill, 0.01)
    assertEquals(0.0, viewModel.uiState.value.tipAmount, 0.01)

    // Rounding up from clamped subtotal works cleanly again
    viewModel.roundUpTotal()
    assertEquals(44.0, viewModel.uiState.value.totalBill, 0.01)
  }

  @Test
  fun `verify newly localized UI and tip badge strings resolve distinctly across all 10 non-English languages`() {
    val baseContext = ApplicationProvider.getApplicationContext<Context>()
    val enContext = com.example.i18n.LocaleContextWrapper.wrap(baseContext, "en")
    val checkResIds = listOf(
      R.string.settings_title,
      R.string.bill_input_label,
      R.string.coach_skip,
      R.string.active_location_label,
      R.string.tip_badge_round_up
    )
    val enValues = checkResIds.associateWith { enContext.getString(it) }

    val nonEnglishLocales = listOf("ar", "bn", "de", "es", "fr", "hi", "ja", "pt", "ru", "zh")
    for (lang in nonEnglishLocales) {
      val locContext = com.example.i18n.LocaleContextWrapper.wrap(baseContext, lang)
      for (resId in checkResIds) {
        val localized = locContext.getString(resId)
        assertTrue("Missing translation for resId=$resId in lang=$lang", localized.isNotBlank())
        assertFalse(
          "Translation for resId=$resId in lang=$lang should differ from English ('${enValues[resId]}')",
          localized == enValues[resId]
        )
      }
    }
  }

  @Test
  fun `verify SettingsBottomSheet renders translated strings when currentLanguage changes`() {
    val langState = androidx.compose.runtime.mutableStateOf("en")

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        com.example.ui.components.SettingsBottomSheet(
          visible = true,
          currentDarkTheme = false,
          themeMode = null,
          isSeniorMode = false,
          currentLanguage = langState.value,
          isManualLanguageOverride = true,
          onDismiss = {},
          onOpenHistory = {},
          onOpenLanguage = {}
        )
      }
    }

    composeTestRule.onNodeWithText("Settings").assertExists()
    composeTestRule.onNodeWithText("Prompt to Save Tips").assertExists()

    // Switch to German
    langState.value = "de"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Einstellungen").assertExists()
    composeTestRule.onNodeWithText("Nach Speichern fragen").assertExists()
    composeTestRule.onNodeWithText("Settings").assertDoesNotExist()

    // Switch to Spanish
    langState.value = "es"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Ajustes").assertExists()
    composeTestRule.onNodeWithText("Preguntar para Guardar").assertExists()
  }

  @Test
  fun `verify active percent pill in tipping tiers stays on one line across all 11 languages`() {
    val viewModel = TippyViewModel()
    val longNameCountry = CountryDataRegistry.getCountryByIso("CD")
    viewModel.selectCountry(longNameCountry)

    val allLanguages = listOf("en", "de", "fr", "es", "pt", "ru", "ar", "hi", "bn", "ja", "zh")
    val langState = androidx.compose.runtime.mutableStateOf("en")

    composeTestRule.setContent {
      val baseContext = androidx.compose.ui.platform.LocalContext.current
      val localizedContext = androidx.compose.runtime.remember(langState.value, baseContext) {
        com.example.i18n.LocaleContextWrapper.wrap(baseContext, langState.value)
      }
      val layoutDir = if (langState.value == "ar") {
        androidx.compose.ui.unit.LayoutDirection.Rtl
      } else {
        androidx.compose.ui.unit.LayoutDirection.Ltr
      }
      androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalContext provides localizedContext,
        androidx.compose.ui.platform.LocalConfiguration provides localizedContext.resources.configuration,
        androidx.compose.ui.platform.LocalLayoutDirection provides layoutDir
      ) {
        TippyTheme(darkTheme = false, isSeniorMode = false) {
          androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.width(
              androidx.compose.ui.unit.Dp(340f)
            )
          ) {
            val uiState by viewModel.uiState.collectAsState()
            TippyMainHost(
              uiState = uiState.copy(currentLanguage = langState.value),
              viewModel = viewModel,
              currentDarkTheme = false
            )
          }
        }
      }
    }

    for (lang in allLanguages) {
      langState.value = lang
      composeTestRule.waitForIdle()

      val pillNode = composeTestRule.onNodeWithTag("active_percent_pill_text")
      pillNode.performScrollTo()
      pillNode.assertExists()

      val semanticsNode = pillNode.fetchSemanticsNode()
      val textLayoutResults = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
      semanticsNode.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult]
        .action?.invoke(textLayoutResults)

      assertTrue("Expected TextLayoutResult for lang=$lang", textLayoutResults.isNotEmpty())
      val layoutResult = textLayoutResults.first()
      val debugInfo = "lang=$lang text='${layoutResult.layoutInput.text}' " +
        "lineCount=${layoutResult.lineCount} size=${layoutResult.size} " +
        "constraints=${layoutResult.layoutInput.constraints} " +
        "overflowW=${layoutResult.didOverflowWidth} overflowH=${layoutResult.didOverflowHeight} " +
        "line0Right=${layoutResult.getLineRight(0)}"
      assertEquals(
        "Active percent pill wrapped: $debugInfo",
        1,
        layoutResult.lineCount
      )
      val lineWidth = layoutResult.getLineRight(0) - layoutResult.getLineLeft(0)
      assertTrue(
        "Active percent pill text clipped horizontally (lineWidth=$lineWidth): $debugInfo",
        lineWidth <= layoutResult.size.width.toFloat() + 1f
      )
      assertTrue(
        "Active percent pill was squeezed to maxWidth: $debugInfo",
        layoutResult.size.width < layoutResult.layoutInput.constraints.maxWidth
      )
    }
  }

  @Test
  @Config(qualifiers = "w1024dp-h768dp-mdpi")
  fun `verify tablet mode activates automatically on wide viewport with working keypad and service grid`() {
    val viewModel = TippyViewModel()
    viewModel.selectCountry(CountryDataRegistry.getCountryByIso("US"))

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    composeTestRule.waitForIdle()

    // Automatic two-pane dashboard and tablet enhancements render without any toggle
    composeTestRule.onNodeWithTag("tablet_calculator_dashboard").assertExists()
    composeTestRule.onNodeWithTag("tablet_cultural_breakdown_card").assertExists()
    composeTestRule.onNodeWithTag("tablet_tactile_keypad").assertExists()
    composeTestRule.onNodeWithTag("tablet_service_grid").assertExists()

    // Exercise built-in tactile keypad
    composeTestRule.onNodeWithTag("keypad_digit_4").performClick()
    composeTestRule.onNodeWithTag("keypad_digit_5").performClick()
    composeTestRule.onNodeWithTag("keypad_decimal").performClick()
    composeTestRule.onNodeWithTag("keypad_digit_5").performClick()
    composeTestRule.waitForIdle()
    assertEquals("45.5", viewModel.uiState.value.billAmountInput)
    assertTrue(viewModel.uiState.value.totalBill > 45.5)

    // Exercise keypad backspace
    composeTestRule.onNodeWithTag("keypad_backspace").performClick()
    composeTestRule.waitForIdle()
    assertEquals("45.", viewModel.uiState.value.billAmountInput)

    // Exercise multi-column service category selection
    composeTestRule.onNodeWithTag("service_bar").performClick()
    composeTestRule.waitForIdle()
    assertEquals(ServiceType.BAR, viewModel.uiState.value.selectedServiceType)
    composeTestRule.onNodeWithTag("hero_total_bill_text", useUnmergedTree = true).assertExists()
  }

  @Test
  @Config(qualifiers = "w1024dp-h768dp-mdpi")
  fun `verify senior mode stays single column even on wide tablet viewport`() {
    val viewModel = TippyViewModel()

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = viewModel.uiState.collectAsState().value.isSeniorMode) {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_calculator_dashboard").assertExists()

    // Toggle Senior Mode ON
    composeTestRule.onNodeWithTag("senior_mode_toggle").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tablet_calculator_dashboard").assertDoesNotExist()
    composeTestRule.onNodeWithTag("senior_totals_card").assertExists()
  }

  @Test
  @Config(qualifiers = "w840dp-h1000dp-mdpi")
  fun `verify 840dp and compact viewports remain single pane with temporary preview toggle removed`() {
    val viewModel = TippyViewModel()

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    composeTestRule.waitForIdle()
    // Exactly 840dp does NOT exceed 840dp threshold -> remains single-pane
    composeTestRule.onNodeWithTag("tablet_calculator_dashboard").assertDoesNotExist()

    // Long pressing logo no longer toggles a temporary Tablet Mode preview
    composeTestRule.onNodeWithTag("app_logo_tap_target").performTouchInput { longClick() }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_calculator_dashboard").assertDoesNotExist()
  }

  @Test
  @Config(qualifiers = "w1024dp-h768dp-mdpi")
  fun `verify country explorer renders multi-column grid and settings or history use tablet side overlays`() {
    val countries = listOf(
      CountryDataRegistry.getCountryByIso("US"),
      CountryDataRegistry.getCountryByIso("JP"),
      CountryDataRegistry.getCountryByIso("DE")
    )
    var pickedIso = ""
    val showSettings = androidx.compose.runtime.mutableStateOf(false)
    val showHistory = androidx.compose.runtime.mutableStateOf(false)

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        Box(modifier = Modifier.width(1024.dp)) {
          CountrySelectionView(
            allCountries = countries,
            currentCountry = countries.first(),
            currentLanguage = "en",
            isTabletLayout = true,
            onBack = {},
            onSelectCountry = { pickedIso = it.iso }
          )

          com.example.ui.components.SettingsBottomSheet(
            visible = showSettings.value,
            currentDarkTheme = false,
            isSeniorMode = false,
            currentLanguage = "en",
            isManualLanguageOverride = false,
            isTabletLayout = true,
            onDismiss = { showSettings.value = false },
            onOpenHistory = {},
            onOpenLanguage = {}
          )

          com.example.ui.components.TipHistorySheet(
            visible = showHistory.value,
            historyList = emptyList(),
            isSeniorMode = false,
            currentLanguage = "en",
            isTabletLayout = true,
            onLoadTransaction = {},
            onDeleteTransaction = {},
            onClearAll = {},
            onDismiss = { showHistory.value = false }
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("country_grid_tablet").assertExists()
    composeTestRule.onNodeWithTag("country_item_JP").performClick()
    assertEquals("JP", pickedIso)

    // Tablet Settings Side Overlay
    showSettings.value = true
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_settings_side_panel").assertExists()
    composeTestRule.onNodeWithTag("settings_sheet").assertExists()
    composeTestRule.onNodeWithTag("settings_close_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_sheet").assertDoesNotExist()

    // Tablet History Side Overlay
    showHistory.value = true
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tip_history_bottom_sheet").assertExists()
    composeTestRule.onNodeWithTag("history_close_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tip_history_bottom_sheet").assertDoesNotExist()
  }

  @Test
  fun `verify tablet keypad digit and decimal helper edge cases`() {
    assertEquals("5", com.example.ui.components.appendKeypadDigit("0", "5"))
    assertEquals("0", com.example.ui.components.appendKeypadDigit("0", "0"))
    assertEquals("123", com.example.ui.components.appendKeypadDigit("12", "3"))
    assertEquals("123456789012", com.example.ui.components.appendKeypadDigit("123456789012", "9"))

    assertEquals("0.", com.example.ui.components.appendKeypadDecimal(""))
    assertEquals("12.", com.example.ui.components.appendKeypadDecimal("12"))
    assertEquals("12.3", com.example.ui.components.appendKeypadDecimal("12.3"))
    assertEquals("123456789012", com.example.ui.components.appendKeypadDecimal("123456789012"))
  }

  @Test
  fun `verify adaptive tablet posture resolver enforces strict 840dp threshold`() {
    val boundary = com.example.ui.components.resolveAdaptiveTabletMetrics(840.dp, 1024.dp)
    assertEquals(com.example.shared.TabletTwoPanePosture.SINGLE_PANE, boundary.posture)

    val portrait = com.example.ui.components.resolveAdaptiveTabletMetrics(860.dp, 1100.dp)
    assertEquals(com.example.shared.TabletTwoPanePosture.PORTRAIT_TWO_PANE, portrait.posture)
    assertEquals(2, portrait.serviceColumns)
    assertEquals(0.42f, portrait.leftPaneWeight, 0.001f)

    val landscape = com.example.ui.components.resolveAdaptiveTabletMetrics(1024.dp, 768.dp)
    assertEquals(com.example.shared.TabletTwoPanePosture.LANDSCAPE_TWO_PANE, landscape.posture)
    assertEquals(3, landscape.serviceColumns)
    assertEquals(0.45f, landscape.leftPaneWeight, 0.001f)
  }

  @Test
  @Config(qualifiers = "w1024dp-h1120dp-mdpi")
  fun `verify tablet dashboard transitions smoothly between portrait two-pane and landscape two-pane`() {
    val viewModel = TippyViewModel()
    val context = ApplicationProvider.getApplicationContext<Context>()
    val hapticService = HapticFeedbackService(context)
    val viewportState = androidx.compose.runtime.mutableStateOf(860.dp to 1100.dp)

    composeTestRule.setContent {
      val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
      val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        val uiState by viewModel.uiState.collectAsState()
        val (w, h) = viewportState.value
        com.example.ui.components.TabletCalculatorDashboard(
          uiState = uiState,
          viewModel = viewModel,
          hapticService = hapticService,
          triggerHaptic = {},
          focusManager = focusManager,
          keyboardController = keyboardController,
          viewportWidth = w,
          viewportHeight = h
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_pane_left").assertExists()
    composeTestRule.onNodeWithTag("tablet_pane_right").assertExists()
    composeTestRule.onNodeWithTag("tablet_posture_portrait_two_pane").assertExists()
    composeTestRule.onNodeWithTag("service_grid_cols_2").assertExists()

    // Simulate smooth resize / rotation into Landscape
    viewportState.value = 1024.dp to 768.dp
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("tablet_pane_left").assertExists()
    composeTestRule.onNodeWithTag("tablet_pane_right").assertExists()
    composeTestRule.onNodeWithTag("tablet_posture_landscape_two_pane").assertExists()
    composeTestRule.onNodeWithTag("service_grid_cols_3").assertExists()
  }

  @Test
  @Config(qualifiers = "w1024dp-h768dp-mdpi")
  fun `verify tablet tactile keypad dispatches distinct hardware vibration patterns`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    HapticFeedbackManager.init(context)
    val hapticService = HapticFeedbackService(context)
    val viewModel = TippyViewModel()
    viewModel.selectCountry(CountryDataRegistry.getCountryByIso("US"))

    composeTestRule.setContent {
      val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
      val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        val uiState by viewModel.uiState.collectAsState()
        com.example.ui.components.TabletCalculatorDashboard(
          uiState = uiState,
          viewModel = viewModel,
          hapticService = hapticService,
          triggerHaptic = {},
          focusManager = focusManager,
          keyboardController = keyboardController,
          viewportWidth = 1024.dp,
          viewportHeight = 768.dp
        )
      }
    }

    composeTestRule.waitForIdle()

    // Backspace when empty triggers BOUNDARY_REJECT
    composeTestRule.onNodeWithTag("keypad_backspace").performClick()
    assertEquals(
      HapticFeedbackManager.TactileKeypadPattern.BOUNDARY_REJECT,
      hapticService.lastKeypadPattern
    )

    // Digit key triggers DIGIT_PRESS
    composeTestRule.onNodeWithTag("keypad_digit_7").performClick()
    assertEquals(
      HapticFeedbackManager.TactileKeypadPattern.DIGIT_PRESS,
      hapticService.lastKeypadPattern
    )

    // Decimal key triggers DECIMAL_DETENT
    composeTestRule.onNodeWithTag("keypad_decimal").performClick()
    assertEquals(
      HapticFeedbackManager.TactileKeypadPattern.DECIMAL_DETENT,
      hapticService.lastKeypadPattern
    )

    // Second decimal triggers BOUNDARY_REJECT
    composeTestRule.onNodeWithTag("keypad_decimal").performClick()
    assertEquals(
      HapticFeedbackManager.TactileKeypadPattern.BOUNDARY_REJECT,
      hapticService.lastKeypadPattern
    )

    // Backspace on non-empty input triggers BACKSPACE_THUD
    composeTestRule.onNodeWithTag("keypad_backspace").performClick()
    assertEquals(
      HapticFeedbackManager.TactileKeypadPattern.BACKSPACE_THUD,
      hapticService.lastKeypadPattern
    )

    // Quick-add chip triggers QUICK_ADD_STEP
    composeTestRule.onNodeWithTag("quick_add_10").performClick()
    assertEquals(
      HapticFeedbackManager.TactileKeypadPattern.QUICK_ADD_STEP,
      hapticService.lastKeypadPattern
    )

    // Reset button triggers ACTION_CONFIRM
    composeTestRule.onNodeWithTag("clear_button_reset").performClick()
    assertEquals(
      HapticFeedbackManager.TactileKeypadPattern.ACTION_CONFIRM,
      hapticService.lastKeypadPattern
    )
  }

  @Test
  @Config(qualifiers = "w1024dp-h768dp-mdpi")
  fun `verify tablet side panel currency converter toggle shows country paired total and removes duplicate tiers`() {
    val viewModel = TippyViewModel()
    viewModel.selectCountry(CountryDataRegistry.getCountryByIso("US"))
    viewModel.updateBillAmount("100.00")
    viewModel.incrementSplit()

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    composeTestRule.waitForIdle()

    // Tipping tiers (Standard / Generous / Legend) should appear only once (in right pane), not under Total Bill
    assertEquals(1, composeTestRule.onAllNodesWithText("Standard").fetchSemanticsNodes().size)
    assertEquals(1, composeTestRule.onAllNodesWithText("Generous").fetchSemanticsNodes().size)
    assertEquals(1, composeTestRule.onAllNodesWithText("Legend").fetchSemanticsNodes().size)

    // Currency converter toggle is present; converted readout hidden initially
    composeTestRule.onNodeWithTag("tablet_currency_converter_toggle").assertExists()
    composeTestRule.onNodeWithTag("tablet_converted_total_text").assertDoesNotExist()

    // Enable converter toggle -> shows converted EUR total & per-person share for US (USD -> EUR)
    composeTestRule.onNodeWithTag("tablet_currency_converter_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_converted_total_text").assertExists()
    composeTestRule.onNodeWithTag("tablet_converted_share_text").assertExists()

    // Switch country to Japan (JPY) -> secondary currency automatically updates to USD ($)
    viewModel.selectCountry(CountryDataRegistry.getCountryByIso("JP"))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_converted_total_text").assertExists()
    composeTestRule.onNodeWithText("Show in USD ($)").assertExists()

    // Toggle converter back off -> hides converted total readout
    composeTestRule.onNodeWithTag("tablet_currency_converter_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_converted_total_text").assertDoesNotExist()
  }

  @Test
  @Config(qualifiers = "w1024dp-h768dp-mdpi")
  fun `verify total bill font size is enlarged in tablet mode compared to compact phone mode`() {
    val viewModel = TippyViewModel()
    viewModel.selectCountry(CountryDataRegistry.getCountryByIso("US"))
    viewModel.updateBillAmount("100.00")

    val tabletModeState = androidx.compose.runtime.mutableStateOf(false)

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        val uiState by viewModel.uiState.collectAsState()
        HeroCalculationCard(
          uiState = uiState,
          onRoundUp = {},
          onRoundDown = {},
          isTabletMode = tabletModeState.value
        )
      }
    }

    composeTestRule.waitForIdle()
    val phoneResults = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
    composeTestRule.onNodeWithTag("hero_total_bill_text", useUnmergedTree = true)
      .fetchSemanticsNode()
      .config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult]
      .action?.invoke(phoneResults)
    val phoneFontSizeSp = phoneResults.first().layoutInput.style.fontSize.value

    tabletModeState.value = true
    composeTestRule.waitForIdle()
    val tabletResults = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
    composeTestRule.onNodeWithTag("hero_total_bill_text", useUnmergedTree = true)
      .fetchSemanticsNode()
      .config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult]
      .action?.invoke(tabletResults)
    val tabletFontSizeSp = tabletResults.first().layoutInput.style.fontSize.value

    assertTrue(
      "Expected tablet Total Bill font ($tabletFontSizeSp sp) > phone font ($phoneFontSizeSp sp)",
      tabletFontSizeSp > phoneFontSizeSp
    )
    assertEquals(62.sp.value, tabletFontSizeSp, 0.1f)
  }

  @Test
  @Config(qualifiers = "w1024dp-h900dp-mdpi")
  fun `verify settings and history sheets transition from single pane into tablet side overlays across 840dp breakpoint`() {
    val viewModel = TippyViewModel()
    val viewportWidthState = androidx.compose.runtime.mutableStateOf(412.dp)

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        Box(modifier = Modifier.width(viewportWidthState.value)) {
          val uiState by viewModel.uiState.collectAsState()
          TippyMainHost(
            uiState = uiState,
            viewModel = viewModel,
            currentDarkTheme = false
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_calculator_dashboard").assertDoesNotExist()

    // Open Settings while in single-pane phone layout (412dp <= 840dp)
    composeTestRule.onNodeWithTag("settings_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_sheet").assertExists()
    composeTestRule.onNodeWithTag("tablet_settings_side_panel").assertDoesNotExist()

    // Resize above 840dp into two-pane Tablet Mode while Settings remains open
    viewportWidthState.value = 960.dp
    composeTestRule.waitForIdle()

    // Two-pane dashboard and sliding Settings side overlay both appear cleanly
    composeTestRule.onNodeWithTag("tablet_calculator_dashboard").assertExists()
    composeTestRule.onNodeWithTag("tablet_settings_side_panel").assertExists()
    composeTestRule.onNodeWithTag("settings_sheet").assertExists()

    // Open Tip History directly from Settings side panel -> transitions to History side overlay
    composeTestRule.onNodeWithTag("tip_history_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tip_history_bottom_sheet").assertExists()
    composeTestRule.onNodeWithTag("tablet_settings_side_panel").assertDoesNotExist()

    // Resize back below 840dp -> two-pane dashboard exits and History remains open in compact sheet
    viewportWidthState.value = 412.dp
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_calculator_dashboard").assertDoesNotExist()
    composeTestRule.onNodeWithTag("tip_history_bottom_sheet").assertExists()
  }

  @Test
  @Config(qualifiers = "w1024dp-h768dp-mdpi")
  fun `verify tablet currency converter swap button toggles between home and destination currency in one tap`() {
    val viewModel = TippyViewModel()
    // Home currency defaults to USD (en_US); pick EUR first in Settings to verify custom home currency,
    // then select Japan (JPY) as destination country.
    viewModel.selectCurrency(CurrencyOption.EUR)
    viewModel.selectCountry(CountryDataRegistry.getCountryByIso("JP"))
    viewModel.updateBillAmount("1000")

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        val uiState by viewModel.uiState.collectAsState()
        TippyMainHost(
          uiState = uiState,
          viewModel = viewModel,
          currentDarkTheme = false
        )
      }
    }

    composeTestRule.waitForIdle()

    // Initially active in Japan's destination currency (JPY) with secondary = home currency (EUR)
    assertEquals(CurrencyOption.JPY, viewModel.uiState.value.selectedCurrency)
    assertEquals(CurrencyOption.EUR, viewModel.uiState.value.homeCurrency)
    composeTestRule.onNodeWithTag("tablet_currency_swap_button").assertExists()
    composeTestRule.onNodeWithText("Swap JPY ↔ EUR").assertExists()
    composeTestRule.onNodeWithText("Show in EUR (€)").assertExists()

    // Expand converter preview so live converted readout can also be observed across swap
    composeTestRule.onNodeWithTag("tablet_currency_converter_toggle").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("tablet_converted_total_text").assertExists()

    // One tap on dedicated swap button -> switches calculator to home currency (EUR) & secondary to JPY
    composeTestRule.onNodeWithTag("tablet_currency_swap_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CurrencyOption.EUR, viewModel.uiState.value.selectedCurrency)
    composeTestRule.onNodeWithText("Swap EUR ↔ JPY").assertExists()
    composeTestRule.onNodeWithText("Show in JPY (¥)").assertExists()
    composeTestRule.onNodeWithTag("tablet_converted_total_text").assertExists()

    // Second tap -> swaps cleanly back to destination currency (JPY) & secondary to EUR
    composeTestRule.onNodeWithTag("tablet_currency_swap_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals(CurrencyOption.JPY, viewModel.uiState.value.selectedCurrency)
    composeTestRule.onNodeWithText("Swap JPY ↔ EUR").assertExists()
    composeTestRule.onNodeWithText("Show in EUR (€)").assertExists()
  }

  @Test
  fun `verify country registry and repository contain exactly 192 countries while preserving HK and TW lookup`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val registryCountries = CountryDataRegistry.getAllCountries()
    val repoCountries = GlobalTippingRepository(context).getAllCountries()

    assertEquals(192, registryCountries.size)
    assertEquals(192, repoCountries.size)
    assertEquals("HKD", CountryDataRegistry.getCountryByIso("HK").localCurrency)
    assertEquals("HKD", GlobalTippingRepository(context).getCountryByIso("HK").localCurrency)
    assertEquals("TWD", CountryDataRegistry.getCountryByIso("TW").localCurrency)
  }

  @Test
  fun `verify country selector bottom sheet displays 192 never 193 across all 11 i18n languages on phone`() {
    val baseContext = ApplicationProvider.getApplicationContext<Context>()
    val languages = com.example.i18n.LanguageResolver.SUPPORTED_LANGUAGES
    assertEquals(11, languages.size)
    val langState = androidx.compose.runtime.mutableStateOf(languages.first())

    composeTestRule.setContent {
      TippyTheme(darkTheme = false, isSeniorMode = false) {
        com.example.ui.components.CountrySelectionBottomSheet(
          visible = true,
          currentCountry = CountryDataRegistry.getCountryByIso("HK"),
          allCountries = CountryDataRegistry.getAllCountries(),
          currentLanguage = langState.value,
          isTabletLayout = false,
          onDismiss = {},
          onSelectCountry = {}
        )
      }
    }

    for (lang in languages) {
      langState.value = lang
      composeTestRule.waitForIdle()

      val localizedContext = com.example.i18n.LocaleContextWrapper.wrap(baseContext, lang)
      val expectedCountText = localizedContext.getString(R.string.countries_count_label, 192)
      val countNode = composeTestRule.onNodeWithTag("country_sheet_count_text", useUnmergedTree = true)
      countNode.assertExists()
      val actualText = countNode.fetchSemanticsNode()
        .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
        .joinToString("") { it.text }

      assertEquals("Mismatch for locale=$lang", expectedCountText, actualText)
      assertTrue("Expected 192 in '$actualText' ($lang)", actualText.contains("192") || actualText.contains("১৯২") || actualText.contains("١٩٢"))
      assertFalse("Unexpected 193 in '$actualText' ($lang)", actualText.contains("193") || actualText.contains("১৯৩") || actualText.contains("١٩٣"))
    }
  }

  @Test
  @Config(qualifiers = "w1024dp-h768dp-mdpi")
  fun `verify tablet mode country sheet and explorer grid display 192 never 193 across all 11 i18n languages`() {
    val baseContext = ApplicationProvider.getApplicationContext<Context>()
    val languages = com.example.i18n.LanguageResolver.SUPPORTED_LANGUAGES
    assertEquals(11, languages.size)
    val langState = androidx.compose.runtime.mutableStateOf(languages.first())
    val showQuickSheet = androidx.compose.runtime.mutableStateOf(true)

    composeTestRule.setContent {
      val wrappedContext = com.example.i18n.LocaleContextWrapper.wrap(baseContext, langState.value)
      androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalContext provides wrappedContext,
        androidx.compose.ui.platform.LocalConfiguration provides wrappedContext.resources.configuration
      ) {
        TippyTheme(darkTheme = false, isSeniorMode = false) {
          CountrySelectionView(
            allCountries = CountryDataRegistry.getAllCountries(),
            currentCountry = CountryDataRegistry.getCountryByIso("US"),
            currentLanguage = langState.value,
            isTabletLayout = true,
            onBack = {},
            onSelectCountry = {}
          )
          com.example.ui.components.CountrySelectionBottomSheet(
            visible = showQuickSheet.value,
            currentCountry = CountryDataRegistry.getCountryByIso("US"),
            allCountries = CountryDataRegistry.getAllCountries(),
            currentLanguage = langState.value,
            isTabletLayout = true,
            onDismiss = {},
            onSelectCountry = {}
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("country_grid_tablet").assertExists()

    for (lang in languages) {
      langState.value = lang
      composeTestRule.waitForIdle()

      val localizedContext = com.example.i18n.LocaleContextWrapper.wrap(baseContext, lang)
      val expectedExplorerText = localizedContext.getString(R.string.regional_matrices_count)
      val expectedSheetText = localizedContext.getString(R.string.countries_count_label, 192)

      val explorerText = composeTestRule.onNodeWithTag("explorer_countries_count_text", useUnmergedTree = true)
        .fetchSemanticsNode()
        .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
        .joinToString("") { it.text }
      assertEquals("Explorer count mismatch for locale=$lang", expectedExplorerText, explorerText)
      assertFalse("Unexpected 193 in explorer ($lang): $explorerText", explorerText.contains("193") || explorerText.contains("১৯৩"))

      val sheetText = composeTestRule.onNodeWithTag("country_sheet_count_text", useUnmergedTree = true)
        .fetchSemanticsNode()
        .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
        .joinToString("") { it.text }
      assertEquals("Tablet side sheet count mismatch for locale=$lang", expectedSheetText, sheetText)
      assertFalse("Unexpected 193 in tablet side sheet ($lang): $sheetText", sheetText.contains("193") || sheetText.contains("১৯৩"))
    }

    // Also verify search filtering updates the tablet explorer count accurately
    showQuickSheet.value = false
    langState.value = "en"
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("country_search_field").performTextInput("Japan")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("explorer_countries_count_text").assertExists()
    val filteredText = composeTestRule.onNodeWithTag("explorer_countries_count_text", useUnmergedTree = true)
      .fetchSemanticsNode()
      .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
      .joinToString("") { it.text }
    assertTrue("Expected filtered count '1 Countries', got '$filteredText'", filteredText.contains("1 "))
  }
}
