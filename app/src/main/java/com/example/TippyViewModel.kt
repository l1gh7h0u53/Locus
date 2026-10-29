package com.example

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.i18n.LanguagePreferences
import com.example.i18n.LanguageResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Complete Tippy UI State Model (TippyUiState)
 * =========================================================================================
 *
 * Description:
 * Immutable state container holding all UI values, calculator inputs, active country
 * guidelines, tipping percentage selections, split party count, accessibility states,
 * currency choices, and active localization.
 *
 * Properties:
 * @property billAmountInput Raw text input string of the bill amount.
 * @property activeCountry Currently selected [CountryTippingData] rule set.
 * @property detectedIso 2-letter ISO code detected via SIM telephony or fallback.
 * @property allCountries Complete list of available world countries for selection.
 * @property selectedServiceType Active service category (Restaurant, Delivery, Takeout, Foodtruck, Groceries, Bar).
 * @property selectedPercent Active decimal tip percentage (e.g. 0.15 = 15%).
 * @property isCustomPercentActive True if user dragged slider or typed custom percentage.
 * @property customPercentValue Whole percentage value for custom slider display (e.g. 18.5%).
 * @property splitCount Number of individuals splitting the bill (1..99).
 * @property isSeniorMode Accessibility high-contrast & enlarged layout flag.
 * @property isDarkMode Explicit theme override (null = follow system).
 * @property tipAmount Calculated tip amount rounded to 2 decimal places.
 * @property totalBill Calculated total bill (bill + tip) rounded to 2 decimal places.
 * @property sharePerPerson Calculated share per diner (total / splitCount).
 * @property selectedCurrency Active ISO currency option and graphical symbol.
 * @property currentLanguage Active 2-letter language code ("en", "de", "es", "fr", etc.).
 * @property isManualLanguageOverride True if user explicitly selected language in UI.
 *
 * Dependencies:
 * - [CountryTippingData], [CountryDataRegistry], [ServiceRule], [ServiceType], [CurrencyOption].
 * =========================================================================================
 */
data class TippyUiState(
    val billAmountInput: String = "",
    val activeCountry: CountryTippingData = CountryDataRegistry.getDefaultCountry(),
    val detectedIso: String = "US",
    val allCountries: List<CountryTippingData> = CountryDataRegistry.getAllCountries(),
    val selectedServiceType: ServiceType = ServiceType.RESTAURANT,
    val selectedPercent: Double = 0.15,
    val isCustomPercentActive: Boolean = false,
    val customPercentValue: Double = 15.0,
    val splitCount: Int = 1,
    val isSeniorMode: Boolean = false,
    val isDarkMode: Boolean? = null, // null means follow system theme
    val tipAmount: Double = 0.00,
    val totalBill: Double = 0.00,
    val sharePerPerson: Double = 0.00,
    val selectedCurrency: CurrencyOption = CurrencyOption.USD,
    val currentLanguage: String = "en",
    val isManualLanguageOverride: Boolean = false
) {
    /** Active service category tipping rule from the selected country */
    val currentRule: ServiceRule
        get() = activeCountry.getRuleFor(selectedServiceType.datasetKey)

    /** Standard baseline percentage for the active service */
    val standardPercentage: Double
        get() = currentRule.parseStandardRate()

    /** List of generous/legendary percentage tiers for chip selectors */
    val generousPercentages: List<Double>
        get() = currentRule.parseGenerousRates()
}

/**
 * =========================================================================================
 * FEATURE: Main Tip Calculator ViewModel (TippyViewModel)
 * =========================================================================================
 *
 * Description:
 * Primary business logic and reactive state management controller for the Tippy app.
 * Handles bill math, rounding logic, multi-person bill splitting, country switching,
 * currency management, and offline language initialization.
 *
 * Dependencies:
 * - [BrainRepository]: Telephony-based country ISO detection without location permission.
 * - [CountryDataRegistry]: Local tipping database.
 * - [LanguagePreferences], [LanguageResolver]: SharedPreferences language management.
 * - [CurrencyOption]: Currency formatting and regional fallbacks.
 * =========================================================================================
 */
class TippyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TippyUiState())
    val uiState: StateFlow<TippyUiState> = _uiState.asStateFlow()

    init {
        // Initialize with default country rates
        val defaultCountry = CountryDataRegistry.getDefaultCountry()
        val defaultRule = defaultCountry.getRuleFor(ServiceType.RESTAURANT.datasetKey)
        _uiState.value = _uiState.value.copy(
            activeCountry = defaultCountry,
            selectedPercent = defaultRule.parseStandardRate(),
            selectedCurrency = CurrencyOption.getNearestCurrencyFor(defaultCountry.iso, defaultCountry.baseMatrix)
        )
    }

    /**
     * Initializes country detection and offline language resolution asynchronously on background thread.
     *
     * Feature:
     * - Uses [BrainRepository.getNetworkCountryIso] to query SIM / Network telephony metadata.
     * - Checks [LanguagePreferences] for persisted manual overrides.
     * - Dispatches state update back to Main dispatcher.
     *
     * Dependencies:
     * - [BrainRepository], [LanguagePreferences], [LanguageResolver], [CountryDataRegistry].
     *
     * @param context Application or Activity context.
     */
    fun initTelephonyCountry(context: Context) {
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            val brainRepo = BrainRepository(appContext)
            val iso = brainRepo.getNetworkCountryIso().ifEmpty {
                Locale.getDefault().country
            }
            if (iso.isNotEmpty()) {
                val country = CountryDataRegistry.getCountryByIso(iso)
                withContext(Dispatchers.Main) {
                    selectCountry(country, detectedIsoCode = iso)
                }
            }

            // Initialize language
            val prefs = LanguagePreferences(appContext)
            val override = prefs.getLanguageOverride()
            val (resolvedLang, isOverride) = if (override != null && LanguageResolver.SUPPORTED_LANGUAGES.contains(override)) {
                Pair(override, true)
            } else {
                val systemLang = Locale.getDefault().language
                Pair(LanguageResolver.resolveLanguage(systemLang, iso), false)
            }
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    currentLanguage = resolvedLang,
                    isManualLanguageOverride = isOverride
                )
            }
        }
    }

    /**
     * Resolves language offline checking:
     * 1. In-app manual override in SharedPreferences
     * 2. System Locale (if in supported 10 languages)
     * 3. Country ISO mapping lookup (Telephony SIM / Locale country)
     * 4. Hard fallback to "en"
     *
     * Dependencies:
     * - [LanguagePreferences], [LanguageResolver].
     *
     * @param context Application context.
     * @param countryIso Optional 2-letter ISO country code.
     */
    fun initLanguage(context: Context, countryIso: String?) {
        val prefs = LanguagePreferences(context.applicationContext)
        val override = prefs.getLanguageOverride()
        if (override != null && LanguageResolver.SUPPORTED_LANGUAGES.contains(override)) {
            _uiState.value = _uiState.value.copy(
                currentLanguage = override,
                isManualLanguageOverride = true
            )
        } else {
            val systemLang = Locale.getDefault().language
            val resolved = LanguageResolver.resolveLanguage(systemLang, countryIso)
            _uiState.value = _uiState.value.copy(
                currentLanguage = resolved,
                isManualLanguageOverride = false
            )
        }
    }

    /**
     * Allows the user to manually switch language and persist the choice in SharedPreferences.
     *
     * Dependencies:
     * - [LanguagePreferences.setLanguageOverride].
     * - [LanguageResolver.resolveLanguage].
     *
     * @param context Application context.
     * @param languageCode ISO language code (e.g. "en", "es", "de") or null to reset to automatic.
     */
    fun setLanguage(context: Context, languageCode: String?) {
        val appContext = context.applicationContext
        val prefs = LanguagePreferences(appContext)
        prefs.setLanguageOverride(languageCode)
        if (languageCode != null && LanguageResolver.SUPPORTED_LANGUAGES.contains(languageCode)) {
            _uiState.value = _uiState.value.copy(
                currentLanguage = languageCode,
                isManualLanguageOverride = true
            )
        } else {
            val brainRepo = BrainRepository(appContext)
            val iso = brainRepo.getNetworkCountryIso().ifEmpty {
                Locale.getDefault().country
            }
            val systemLang = Locale.getDefault().language
            val resolved = LanguageResolver.resolveLanguage(systemLang, iso)
            _uiState.value = _uiState.value.copy(
                currentLanguage = resolved,
                isManualLanguageOverride = false
            )
        }
    }

    /**
     * Selects a country (either auto-detected or picked via 6-tap menu).
     *
     * Feature:
     * - Updates active country tipping guidelines and cultural notes.
     * - Adapts standard tip percentage to match selected service type.
     * - Auto-switches currency to country's official currency or nearest regional fallback.
     *
     * Dependencies:
     * - [CurrencyOption.fromCode], [CurrencyOption.getNearestCurrencyFor].
     * - [recalculate].
     *
     * @param country Target [CountryTippingData].
     * @param detectedIsoCode Country ISO code.
     */
    fun selectCountry(country: CountryTippingData, detectedIsoCode: String = country.iso) {
        val rule = country.getRuleFor(_uiState.value.selectedServiceType.datasetKey)
        val nearestCurrency = CurrencyOption.fromCode(country.localCurrency)
            ?: CurrencyOption.getNearestCurrencyFor(country.iso, country.baseMatrix)

        _uiState.value = _uiState.value.copy(
            activeCountry = country,
            detectedIso = detectedIsoCode,
            selectedPercent = rule.parseStandardRate(),
            isCustomPercentActive = false,
            selectedCurrency = nearestCurrency
        )
        recalculate()
    }

    /**
     * Updates the bill amount text input and recalculates tips.
     *
     * Feature:
     * - Sanitizes numeric digits and single decimal dot.
     * - Enforces 12-character maximum input length.
     *
     * @param amount Raw input string.
     */
    fun updateBillAmount(amount: String) {
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        if (sanitized.length > 12) return

        val finalAmount = if (sanitized.count { it == '.' } > 1) {
            _uiState.value.billAmountInput
        } else {
            sanitized
        }

        _uiState.value = _uiState.value.copy(billAmountInput = finalAmount)
        recalculate()
    }

    /**
     * Selects a different service type (e.g. Restaurants, Delivery, Takeout, Bar),
     * adapting to the active country's specific guidelines.
     *
     * @param serviceType Target [ServiceType].
     */
    fun selectServiceType(serviceType: ServiceType) {
        val rule = _uiState.value.activeCountry.getRuleFor(serviceType.datasetKey)
        val standardRate = rule.parseStandardRate()

        _uiState.value = _uiState.value.copy(
            selectedServiceType = serviceType,
            selectedPercent = standardRate,
            isCustomPercentActive = false
        )
        recalculate()
    }

    /**
     * Selects a specific tip percentage from preset buttons.
     *
     * @param percent Decimal percentage value (e.g. 0.18 for 18%).
     */
    fun selectPercentage(percent: Double) {
        _uiState.value = _uiState.value.copy(
            selectedPercent = percent,
            isCustomPercentActive = false
        )
        recalculate()
    }

    /**
     * Toggles custom percentage mode and sets custom slider percentage.
     *
     * @param percent Whole percentage value (e.g. 18.5 for 18.5%).
     */
    fun updateCustomPercentage(percent: Double) {
        _uiState.value = _uiState.value.copy(
            selectedPercent = percent / 100.0,
            customPercentValue = percent,
            isCustomPercentActive = true
        )
        recalculate()
    }

    /**
     * Increments split count (number of people sharing bill) up to max 99.
     */
    fun incrementSplit() {
        if (_uiState.value.splitCount < 99) {
            _uiState.value = _uiState.value.copy(splitCount = _uiState.value.splitCount + 1)
            recalculate()
        }
    }

    /**
     * Decrements split count down to minimum 1 person.
     */
    fun decrementSplit() {
        if (_uiState.value.splitCount > 1) {
            _uiState.value = _uiState.value.copy(splitCount = _uiState.value.splitCount - 1)
            recalculate()
        }
    }

    /**
     * Increments bill amount by convenient quick-add chip values (e.g. +$1, +$5, +$10).
     *
     * @param dollars Amount to add to current bill.
     */
    fun addToBill(dollars: Double) {
        val current = _uiState.value.billAmountInput.toDoubleOrNull() ?: 0.0
        val next = current + dollars
        if (next > 1_000_000_000.0) return

        val formatted = if (next.isNaN() || next.isInfinite()) {
            "0.00"
        } else {
            BigDecimal.valueOf(next).setScale(2, RoundingMode.HALF_UP).toString()
        }
        _uiState.value = _uiState.value.copy(billAmountInput = formatted)
        recalculate()
    }

    /**
     * Toggles the Senior Mode accessibility view mode.
     */
    fun toggleSeniorMode() {
        _uiState.value = _uiState.value.copy(isSeniorMode = !_uiState.value.isSeniorMode)
    }

    /**
     * Sets explicit dark mode theme override (or null for system theme).
     *
     * @param dark True for dark, False for light, null for system.
     */
    fun setDarkMode(dark: Boolean?) {
        _uiState.value = _uiState.value.copy(isDarkMode = dark)
    }

    /**
     * Selects active currency option for monetary formatting and calculations.
     *
     * @param currency Selected [CurrencyOption].
     */
    fun selectCurrency(currency: CurrencyOption) {
        _uiState.value = _uiState.value.copy(selectedCurrency = currency)
    }

    /**
     * Rounds up the total bill to the nearest whole integer by adjusting the tip percentage.
     */
    fun roundUpTotal() {
        val bill = _uiState.value.billAmountInput.toDoubleOrNull() ?: 0.0
        if (bill <= 0.0) return
        val currentTotal = _uiState.value.totalBill
        val nextInt = kotlin.math.ceil(currentTotal).coerceAtLeast(kotlin.math.ceil(bill))
        val targetTotal = if (nextInt <= currentTotal) nextInt + 1.0 else nextInt
        val newTip = (targetTotal - bill).coerceAtLeast(0.0)
        val newPercent = (newTip / bill) * 100.0
        updateCustomPercentage(newPercent)
    }

    /**
     * Rounds down the total bill to the nearest whole integer (ensuring tip >= 0).
     */
    fun roundDownTotal() {
        val bill = _uiState.value.billAmountInput.toDoubleOrNull() ?: 0.0
        if (bill <= 0.0) return
        val currentTotal = _uiState.value.totalBill
        val targetTotal = kotlin.math.floor(currentTotal).coerceAtLeast(bill)
        val newTip = (targetTotal - bill).coerceAtLeast(0.0)
        val newPercent = (newTip / bill) * 100.0
        updateCustomPercentage(newPercent)
    }

    /**
     * Resets the entire calculator inputs to default state.
     */
    fun clearAll() {
        val rule = _uiState.value.activeCountry.getRuleFor(_uiState.value.selectedServiceType.datasetKey)
        _uiState.value = _uiState.value.copy(
            billAmountInput = "",
            selectedPercent = rule.parseStandardRate(),
            isCustomPercentActive = false,
            splitCount = 1,
            tipAmount = 0.0,
            totalBill = 0.0,
            sharePerPerson = 0.0
        )
    }

    /**
     * Core mathematical calculation engine computing tip, total bill, and split shares
     * using strict [BigDecimal] half-up 2-decimal precision.
     *
     * Dependencies:
     * - [roundToTwoDecimals].
     */
    private fun recalculate() {
        val bill = _uiState.value.billAmountInput.toDoubleOrNull() ?: 0.0
        val percentage = _uiState.value.selectedPercent
        val split = _uiState.value.splitCount

        val rawTip = bill * percentage
        val roundedTip = roundToTwoDecimals(rawTip)

        val rawTotal = bill + roundedTip
        val roundedTotal = roundToTwoDecimals(rawTotal)

        val rawShare = roundedTotal / split
        val roundedShare = roundToTwoDecimals(rawShare)

        _uiState.value = _uiState.value.copy(
            tipAmount = roundedTip,
            totalBill = roundedTotal,
            sharePerPerson = roundedShare
        )
    }

    /**
     * Safely rounds floating point values to 2 decimal places with HALF_UP strategy.
     */
    private fun roundToTwoDecimals(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    companion object {
        /** Calculates tip amount: bill * rate with 2-decimal precision */
        fun calculateTipAmount(bill: Double, rate: Double): Double {
            val res = bill * rate
            if (res.isNaN() || res.isInfinite()) return 0.0
            return BigDecimal.valueOf(res).setScale(2, RoundingMode.HALF_UP).toDouble()
        }

        /** Calculates total amount: bill + tip with 2-decimal precision */
        fun calculateTotalAmount(bill: Double, tip: Double): Double {
            val res = bill + tip
            if (res.isNaN() || res.isInfinite()) return 0.0
            return BigDecimal.valueOf(res).setScale(2, RoundingMode.HALF_UP).toDouble()
        }

        /** Calculates per-person split share: total / split with 2-decimal precision */
        fun calculateSharePerPerson(total: Double, split: Int): Double {
            if (split <= 0) return 0.0
            val res = total / split
            if (res.isNaN() || res.isInfinite()) return 0.0
            return BigDecimal.valueOf(res).setScale(2, RoundingMode.HALF_UP).toDouble()
        }
    }
}
