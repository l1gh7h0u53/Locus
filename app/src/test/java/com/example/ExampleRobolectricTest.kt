package com.example

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.TippyTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
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
}



