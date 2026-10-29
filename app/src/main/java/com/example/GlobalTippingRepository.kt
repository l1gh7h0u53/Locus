package com.example

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Global Tipping Repository (GlobalTippingRepository)
 * =========================================================================================
 *
 * Description:
 * Manages loading and caching of tipping guidelines, percentage matrices, cultural notes,
 * and currency metadata across 192 countries worldwide.
 *
 * Loading Strategy:
 * 1. Primary: Tries parsing the offline JSON asset "global_tipping_guide_master_v4.json".
 * 2. Fallback: Loads the built-in static Kotlin registry [CountryDataRegistry].
 *
 * Dependencies:
 * - [android.content.Context]: Accesses Android APK assets (AssetManager).
 * - [CountryDataRegistry]: Hardcoded fallback data source for all 192 countries.
 * - [CountryTippingData]: Output entity model for individual countries.
 * - [ServiceRule]: Output entity model for per-category service rules.
 * - [CurrencyOption]: Currency resolution and mapping utilities.
 * =========================================================================================
 */
class GlobalTippingRepository(private val context: Context) {

    /** Map for fast O(1) lookups by uppercase ISO country code */
    private val countriesByIso = mutableMapOf<String, CountryTippingData>()
    
    /** Complete list of loaded countries */
    private val allCountriesList = mutableListOf<CountryTippingData>()

    init {
        loadData()
    }

    /**
     * Initializes repository data by loading JSON asset or falling back to [CountryDataRegistry].
     *
     * Dependencies:
     * - [context.assets]: Opens "global_tipping_guide_master_v4.json".
     * - [parseMasterJson]: Parses raw JSON payload into structured objects.
     * - [CountryDataRegistry.getAllCountries]: Fallback provider if asset reading fails.
     */
    private fun loadData() {
        val jsonString = try {
            context.assets.open("global_tipping_guide_master_v4.json").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            null
        }

        if (!jsonString.isNullOrEmpty()) {
            parseMasterJson(jsonString)
        }

        if (allCountriesList.isEmpty()) {
            // Load all 192 countries from built-in CountryDataRegistry
            val builtInCountries = CountryDataRegistry.getAllCountries()
            allCountriesList.addAll(builtInCountries)
            for (c in builtInCountries) {
                countriesByIso[c.iso.uppercase(Locale.US)] = c
            }
        }
    }

    /**
     * Parses the master JSON guide into [CountryTippingData] and [ServiceRule] structures.
     *
     * Feature:
     * - Extracts country details, ISO codes, matrix archetypes, currencies, and service arrays.
     * - Resolves localized note keys (e.g., notes_key) for dynamic i18n support.
     * - Populates [countriesByIso] and [allCountriesList].
     *
     * @param jsonStr Raw string content of the master JSON asset.
     */
    private fun parseMasterJson(jsonStr: String) {
        try {
            val rootObj = JSONObject(jsonStr)
            val countriesArray = rootObj.optJSONArray("countries") ?: JSONArray()
            for (i in 0 until countriesArray.length()) {
                val cObj = countriesArray.getJSONObject(i)
                val countryName = cObj.optString("country", "Unknown")
                val iso = cObj.optString("iso", "").uppercase(Locale.US)
                val baseMatrix = cObj.optString("base_matrix", "Universal")
                val matrixName = cObj.optString("matrix_name", "Global Standard")
                val localCurrency = if (cObj.isNull("local_currency")) null else cObj.optString("local_currency")
                val hasCustomRules = cObj.optBoolean("has_custom_rules", false)

                val servicesMap = mutableMapOf<String, ServiceRule>()
                val servicesArray = cObj.optJSONArray("services") ?: JSONArray()
                for (j in 0 until servicesArray.length()) {
                    val sObj = servicesArray.getJSONObject(j)
                    val sType = sObj.optString("service_type")
                    val stdTip = sObj.optString("standard_tip", "10%")
                    val genTip = sObj.optString("generous_tip", "15%")
                    val legTip = sObj.optString("legend_tip", "20%")
                    val mvpTip = sObj.optString("mvp_tip", "25%")
                    val notes = sObj.optString("notes", "")
                    val notesKey = if (sObj.has("notes_key")) sObj.optString("notes_key") else if (sObj.has("notesKey")) sObj.optString("notesKey") else null
                    val serviceTypeKey = if (sObj.has("service_type_key")) sObj.optString("service_type_key") else if (sObj.has("serviceTypeKey")) sObj.optString("serviceTypeKey") else null

                    servicesMap[sType] = ServiceRule(
                        serviceType = sType,
                        standardTip = stdTip,
                        generousTip = genTip,
                        legendTip = legTip,
                        mvpTip = mvpTip,
                        notes = notes,
                        notesKey = notesKey,
                        serviceTypeKey = serviceTypeKey
                    )
                }

                val resolvedCurrency = localCurrency?.takeIf { it.isNotBlank() }
                    ?: CurrencyOption.forCountry(iso)?.code
                    ?: CurrencyOption.forCountry(countryName)?.code
                    ?: CurrencyOption.getNearestCurrencyFor(iso, baseMatrix).code

                val countryData = CountryTippingData(
                    country = countryName,
                    iso = iso,
                    baseMatrix = baseMatrix,
                    matrixName = matrixName,
                    localCurrency = resolvedCurrency,
                    hasCustomRules = hasCustomRules,
                    services = servicesMap
                )

                countriesByIso[iso] = countryData
                allCountriesList.add(countryData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves alphabetically sorted list of all supported countries.
     *
     * @return List of [CountryTippingData] sorted by country name.
     */
    fun getAllCountries(): List<CountryTippingData> {
        return allCountriesList.sortedBy { it.country }
    }

    /**
     * Resolves country by 2-character ISO code (case-insensitive).
     *
     * Feature:
     * - Looks up in loaded ISO cache map.
     * - Falls back to device system Locale country if available.
     * - Falls back to USA ("US") or first list entry.
     *
     * @param iso 2-character ISO country code.
     * @return Resolved [CountryTippingData].
     */
    fun getCountryByIso(iso: String): CountryTippingData {
        val cleanIso = iso.trim().uppercase(Locale.US)
        if (cleanIso.isNotEmpty() && countriesByIso.containsKey(cleanIso)) {
            return countriesByIso[cleanIso]!!
        }

        // Fallback to system locale country
        val localeIso = Locale.getDefault().country.uppercase(Locale.US)
        if (localeIso.isNotEmpty() && countriesByIso.containsKey(localeIso)) {
            return countriesByIso[localeIso]!!
        }

        return countriesByIso["US"] ?: allCountriesList.firstOrNull() ?: createDefaultCountry("USA", "US", "A", "Matrix A", "USD")
    }

    /**
     * Resolves the primary or nearest regional fallback [CurrencyOption] for a given country.
     *
     * Dependencies:
     * - [CurrencyOption.fromCode]: Direct 3-letter currency code match.
     * - [CurrencyOption.getNearestCurrencyFor]: Regional macro-currency fallback.
     *
     * @param country Target [CountryTippingData].
     * @return Resolved [CurrencyOption].
     */
    fun resolveCurrencyForCountry(country: CountryTippingData): CurrencyOption {
        // 1. Direct local_currency match
        country.localCurrency?.let { code ->
            CurrencyOption.fromCode(code)?.let { return it }
        }

        // 2. Nearest geographical/regional currency fallback
        return CurrencyOption.getNearestCurrencyFor(country.iso, country.baseMatrix)
    }

    /**
     * Generates a safe fallback US configuration if assets and registries fail completely.
     */
    private fun createDefaultCountry(name: String, iso: String, matrix: String, matrixName: String, currency: String): CountryTippingData {
        val services = listOf("Restaurants", "Delivery", "Takeout", "Foodtrucks", "Groceries", "Bar").associateWith { type ->
            ServiceRule(
                serviceType = type,
                standardTip = "15%",
                generousTip = "18%",
                legendTip = "20%",
                mvpTip = "25%",
                notes = "Standard tipping practices."
            )
        }
        return CountryTippingData(
            country = name,
            iso = iso,
            baseMatrix = matrix,
            matrixName = matrixName,
            localCurrency = currency,
            hasCustomRules = true,
            services = services
        )
    }
}
