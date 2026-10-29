package com.example.i18n

import com.example.CountryTippingData
import com.example.CurrencyOption
import java.util.Currency
import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Dynamic Global Explorer & Currency Localization Engine
 * =========================================================================================
 *
 * Description:
 * Provides real-time dynamic internationalization (i18n) for:
 * 1. World Currencies (ISO-4217 labels across English, German, French, Spanish, Chinese,
 *    Japanese, Russian, Portuguese, Arabic, Hindi, and Bengali).
 * 2. Country Names (Official ISO-3166 sovereign translations in the active language).
 * 3. Cultural Tipping Matrix Archetypes (Matrix A through E descriptions).
 *
 * Dependencies:
 * - [com.example.CountryTippingData]: Tipping model and ISO codes.
 * - [com.example.CurrencyOption]: Currency options and symbols.
 * - [com.example.i18n.LocaleFormatter]: Target locale resolution.
 * =========================================================================================
 */
object GlobalExplorerLocalization {

    /**
     * Matrix Archetype descriptions localized in all 11 supported languages.
     */
    private val MATRIX_LOCALIZATIONS: Map<String, Map<String, String>> = mapOf(
        "A" to mapOf(
            "en" to "Matrix A: North America, Latin America & Africa",
            "de" to "Matrix A: Nordamerika, Lateinamerika & Afrika",
            "fr" to "Matrice A : Amérique du Nord, Amérique latine et Afrique",
            "es" to "Matriz A: Norteamérica, Latinoamérica y África",
            "pt" to "Matriz A: América do Norte, América Latina e África",
            "ru" to "Матрица А: Северная и Латинская Америка, Африка",
            "ja" to "マトリックスA：北米・中南米・アフリカ",
            "zh" to "矩阵 A：北美、拉丁美洲与非洲",
            "ar" to "المصفوفة أ: أمريكا الشمالية واللاتينية وإفريقيا",
            "hi" to "मैट्रिक्स A: उत्तर अमेरिका, लैटिन अमेरिका और अफ्रीका",
            "bn" to "ম্যাট্রিক্স A: উত্তর আমেরিকা, ল্যাটিন আমেরিকা এবং আফ্রিকা"
        ),
        "B" to mapOf(
            "en" to "Matrix B: Eastern Europe, Balkans & Central Asia",
            "de" to "Matrix B: Osteuropa, Balkan & Zentralasien",
            "fr" to "Matrice B : Europe de l'Est, Balkans et Asie centrale",
            "es" to "Matriz B: Europa del Este, Balcanes y Asia Central",
            "pt" to "Matriz B: Europa Oriental, Bálcãs e Ásia Central",
            "ru" to "Матрица Б: Восточная Европа, Балканы и Центральная Азия",
            "ja" to "マトリックスB：東欧・バルカン・中央アジア",
            "zh" to "矩阵 B：东欧、巴尔干与中亚",
            "ar" to "المصفوفة ب: أوروبا الشرقية والبلقان وآسيا الوسطى",
            "hi" to "मैट्रिक्स B: पूर्वी यूरोप, बाल्कन और मध्य एशिया",
            "bn" to "ম্যাট্রিক্স B: পূর্ব ইউরোপ, বলকান এবং মধ্য এশিয়া"
        ),
        "C" to mapOf(
            "en" to "Matrix C: Western & Northern Europe, Oceania",
            "de" to "Matrix C: West-/Nordeuropa & Ozeanien",
            "fr" to "Matrice C : Europe de l'Ouest / du Nord et Océanie",
            "es" to "Matriz C: Europa Occidental / del Norte y Oceanía",
            "pt" to "Matriz C: Europa Ocidental / Setentrional e Oceania",
            "ru" to "Матрица В: Западная и Северная Европа, Океания",
            "ja" to "マトリックスC：西欧・北欧・オセアニア",
            "zh" to "矩阵 C：西欧、北欧与大洋洲",
            "ar" to "المصفوفة ج: غرب وشمال أوروبا وأوقيانوسيا",
            "hi" to "मैट्रिक्स C: पश्चिमी और उत्तरी यूरोप, ओशिनिया",
            "bn" to "ম্যাট্রিক্স C: পশ্চিম ও উত্তর ইউরোপ এবং ওশেনিয়া"
        ),
        "D" to mapOf(
            "en" to "Matrix D: East & Southeast Asia (No Tip)",
            "de" to "Matrix D: Ost- & Südostasien (Kein Trinkgeld)",
            "fr" to "Matrice D : Asie de l'Est et du Sud-Est (Sans pourboire)",
            "es" to "Matriz D: Asia Oriental y Sudeste Asiático (Sin propina)",
            "pt" to "Matriz D: Leste e Sudeste Asiático (Sem gorjeta)",
            "ru" to "Матрица Г: Восточная и Юго-Восточная Азия (Без чаевых)",
            "ja" to "マトリックスD：東アジア・東南アジア（チップ不要）",
            "zh" to "矩阵 D：东亚与东南亚（无需小费）",
            "ar" to "المصفوفة د: شرق وجنوب شرق آسيا (بدون بقشيش)",
            "hi" to "मैट्रिक्स D: पूर्व और दक्षिण पूर्व एशिया (टिप नहीं)",
            "bn" to "ম্যাট্রিক্স D: পূর্ব ও দক্ষিণ-পূর্ব এশিয়া (টিপ নেই)"
        ),
        "E" to mapOf(
            "en" to "Matrix E: Middle East & North Africa (Baksheesh)",
            "de" to "Matrix E: Naher Osten & Nordafrika (Bakschisch)",
            "fr" to "Matrice E : Moyen-Orient et Afrique du Nord (Bakchich)",
            "es" to "Matriz E: Oriente Medio y Norte de África (Baksheesh)",
            "pt" to "Matriz E: Oriente Médio e Norte da África (Baksheesh)",
            "ru" to "Матрица Д: Ближний Восток и Северная Африка (Бакшиш)",
            "ja" to "マトリックスE：中東・北アフリカ（バクシーシ）",
            "zh" to "矩阵 E：中东与北非（小费习俗）",
            "ar" to "المصفوفة هـ: الشرق الأوسط وشمال إفريقيا (بقشيش)",
            "hi" to "मैट्रिक्स E: मध्य पूर्व और उत्तरी अफ्रीका (बख्शीश)",
            "bn" to "ম্যাট্রিক্স E: মধ্যপ্রাচ্য ও উত্তর আফ্রিকা (বখশিশ)"
        )
    )

    /**
     * Special currency overrides for custom/newer ISO-4217 currencies.
     */
    private val CURRENCY_FALLBACK_NAMES: Map<String, Map<String, String>> = mapOf(
        "XOF" to mapOf(
            "en" to "West African CFA Franc", "de" to "Westafrikanischer CFA-Franc",
            "fr" to "Franc CFA (BCEAO)", "es" to "Franco CFA de África Occidental",
            "pt" to "Franco CFA da África Ocidental", "ru" to "Западноафриканский франк",
            "ja" to "西アフリカCFAフラン", "zh" to "西非法郎",
            "ar" to "فرنك سيفا غرب إفريقيا", "hi" to "पश्चिम अफ़्रीकी सीएफ़ए फ़्रैंक", "bn" to "পশ্চিম আফ্রিকান সিএফএ ফ্রাঙ্ক"
        ),
        "XAF" to mapOf(
            "en" to "Central African CFA Franc", "de" to "Zentralafrikanischer CFA-Franc",
            "fr" to "Franc CFA (BEAC)", "es" to "Franco CFA de África Central",
            "pt" to "Franco CFA da África Central", "ru" to "Центральноафриканский франк",
            "ja" to "中央アフリカCFAフラン", "zh" to "中非法郎",
            "ar" to "فرنك سيفا وسط إفريقيا", "hi" to "मध्य अफ़्रीकी सीएफ़ए फ़्रैंक", "bn" to "মধ্য আফ্রিকান সিএফএ ফ্রাঙ্ক"
        ),
        "XCD" to mapOf(
            "en" to "East Caribbean Dollar", "de" to "Ostkaribischer Dollar",
            "fr" to "Dollar des Caraïbes orientales", "es" to "Dólar del Caribe Oriental",
            "pt" to "Dólar das Caraíbas Orientais", "ru" to "Восточнокарибский доллар",
            "ja" to "東カリブ・ドル", "zh" to "东加勒比元",
            "ar" to "دولار شرق الكاريبي", "hi" to "पूर्वी कैरेबियाई डॉलर", "bn" to "পূর্ব ক্যারিবীয় ডলার"
        ),
        "ZWG" to mapOf(
            "en" to "Zimbabwe Gold", "de" to "Simbabwe-Gold",
            "fr" to "Or du Zimbabwe", "es" to "Oro de Zimbabue",
            "pt" to "Ouro do Zimbábue", "ru" to "Зимбабвийское золото",
            "ja" to "ジンバブエ・ゴールド", "zh" to "津巴布韦黄金",
            "ar" to "ذهب زيمبابوي", "hi" to "ज़िम्बाब्वे गोल्ड", "bn" to "জিম্বাবুয়ে গোল্ড"
        )
    )

    /**
     * Normalizes ISO alpha-2/3 country codes for reliable Java Locale resolution.
     */
    fun normalizeCountryIso(iso: String): String {
        return when (iso.uppercase(Locale.US).trim()) {
            "USA" -> "US"
            "GBR", "UK" -> "GB"
            "DEU" -> "DE"
            "FRA" -> "FR"
            "ESP" -> "ES"
            "ITA" -> "IT"
            "JPN" -> "JP"
            "CHN" -> "CN"
            "RUS" -> "RU"
            "IND" -> "IN"
            "BRA" -> "BR"
            "ARE", "UAE" -> "AE"
            "KOR" -> "KR"
            "PRK" -> "KP"
            "CAN" -> "CA"
            "AUS" -> "AU"
            "MEX" -> "MX"
            "ZAF" -> "ZA"
            "EGY" -> "EG"
            "COD", "DRC" -> "CD"
            "COG" -> "CG"
            else -> if (iso.length == 2) iso.uppercase(Locale.US) else iso
        }
    }

    /**
     * Resolves the country name dynamically in the specified language.
     *
     * @param iso Country ISO alpha-2 or alpha-3 code.
     * @param langCode Active ISO language code (e.g., "de", "fr", "ja").
     * @param fallbackName Original English/default name to fallback to.
     * @return Localized country name.
     */
    fun getLocalizedCountry(iso: String, langCode: String, fallbackName: String): String {
        val cleanLang = langCode.lowercase(Locale.US).split("-", "_")[0]
        val targetLocale = LocaleFormatter.getLocale(cleanLang)
        val normalizedIso = normalizeCountryIso(iso)

        try {
            val countryLocale = Locale("", normalizedIso)
            val displayCountry = countryLocale.getDisplayCountry(targetLocale)
            if (displayCountry.isNotBlank() && !displayCountry.equals(normalizedIso, ignoreCase = true)) {
                return displayCountry
            }
        } catch (e: Throwable) {
            // Fallback
        }
        return fallbackName
    }

    /**
     * Resolves the cultural tipping matrix archetype description in the specified language.
     *
     * @param baseMatrix Archetype code ("A", "B", "C", "D", "E") or matrix name.
     * @param langCode Active ISO language code.
     * @param fallbackMatrixName Original matrix name.
     * @return Localized Matrix Archetype label.
     */
    fun getLocalizedMatrixName(baseMatrix: String, langCode: String, fallbackMatrixName: String = ""): String {
        val cleanLang = langCode.lowercase(Locale.US).split("-", "_")[0]
        val matrixKey = when {
            baseMatrix.contains("A", ignoreCase = true) -> "A"
            baseMatrix.contains("B", ignoreCase = true) -> "B"
            baseMatrix.contains("C", ignoreCase = true) -> "C"
            baseMatrix.contains("D", ignoreCase = true) -> "D"
            baseMatrix.contains("E", ignoreCase = true) -> "E"
            else -> "A"
        }

        val localized = MATRIX_LOCALIZATIONS[matrixKey]?.get(cleanLang)
            ?: MATRIX_LOCALIZATIONS[matrixKey]?.get("en")

        return localized ?: fallbackMatrixName.ifBlank { "Matrix $matrixKey" }
    }

    /**
     * Resolves the currency display name dynamically in the specified language.
     *
     * @param currency The [CurrencyOption] instance.
     * @param langCode Active ISO language code.
     * @return Localized currency name (e.g. "US-Dollar" in German, "euro" in French, "日本円" in Japanese).
     */
    fun getLocalizedCurrencyLabel(currency: CurrencyOption, langCode: String): String {
        val cleanLang = langCode.lowercase(Locale.US).split("-", "_")[0]
        val targetLocale = LocaleFormatter.getLocale(cleanLang)

        // 1. Check custom overrides (e.g. CFA franc, Gold)
        CURRENCY_FALLBACK_NAMES[currency.code]?.get(cleanLang)?.let {
            return it
        }

        // 2. Standard Java Currency localization
        try {
            val javaCurrency = Currency.getInstance(currency.code)
            val displayName = javaCurrency.getDisplayName(targetLocale)
            if (displayName.isNotBlank() && !displayName.equals(currency.code, ignoreCase = true)) {
                return displayName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(targetLocale) else it.toString()
                }
            }
        } catch (e: Throwable) {
            // Non-standard currency or older JVM fallback
        }

        return currency.label
    }

    /**
     * Resolves the localized countdown notification pill message for unlocking Global Explorer.
     *
     * @param remainingTaps Number of taps left (1 to 4).
     * @param langCode Active ISO language code.
     * @return Localized notification message.
     */
    fun getUnlockCountdownText(remainingTaps: Int, langCode: String): String {
        val cleanLang = langCode.lowercase(Locale.US).split("-", "_")[0]
        return when (cleanLang) {
            "de" -> if (remainingTaps == 1) "Noch 1 Tipp, um den Global Explorer freizuschalten" else "Noch $remainingTaps Tipps, um den Global Explorer freizuschalten"
            "es" -> if (remainingTaps == 1) "Falta 1 toque para desbloquear el Explorador Global" else "Faltan $remainingTaps toques para desbloquear el Explorador Global"
            "fr" -> if (remainingTaps == 1) "Encore 1 appui pour déverrouiller l'Explorateur Global" else "Encore $remainingTaps appuis pour déverrouiller l'Explorateur Global"
            "pt" -> if (remainingTaps == 1) "Mais 1 toque para desbloquear o Explorador Global" else "Mais $remainingTaps toques para desbloquear o Explorador Global"
            "ru" -> when {
                remainingTaps == 1 -> "Осталось 1 нажатие для разблокировки Global Explorer"
                remainingTaps in 2..4 -> "Осталось $remainingTaps нажатия для разблокировки Global Explorer"
                else -> "Осталось $remainingTaps нажатий для разблокировки Global Explorer"
            }
            "zh" -> "还需轻触 $remainingTaps 次以解锁全球探索器"
            "ja" -> "グローバルエクスプローラー解除まであと $remainingTaps 回タップ"
            "ar" -> if (remainingTaps == 1) "تبقّت نقرة واحدة لفتح المستكشف العالمي" else "تبقّت $remainingTaps نقرات لفتح المستكشف العالمي"
            "hi" -> if (remainingTaps == 1) "ग्लोबल एक्सप्लोरर अनलॉक करने के लिए 1 बार टैप करें" else "ग्लोबल एक्सप्लोरर अनलॉक करने के लिए $remainingTaps बार टैप करें"
            "bn" -> if (remainingTaps == 1) "গ্লোবাল এক্সপ্লোরার আনলক করতে আর ১ বার ট্যাপ করুন" else "গ্লোবাল এক্সপ্লোরার আনলক করতে আর $remainingTaps বার ট্যাপ করুন"
            else -> if (remainingTaps == 1) "1 tap to unlock Global Explorer" else "$remainingTaps taps to unlock Global Explorer"
        }
    }

    /**
     * Resolves the localized celebration notification pill message when Global Explorer is unlocked.
     *
     * @param langCode Active ISO language code.
     * @return Localized celebration message.
     */
    fun getUnlockSuccessText(langCode: String): String {
        val cleanLang = langCode.lowercase(Locale.US).split("-", "_")[0]
        return when (cleanLang) {
            "de" -> "🎉 Geheimnis entdeckt! Globaler Länderauswähler freigeschaltet"
            "es" -> "🎉 ¡Secreto descubierto! Selector global de países desbloqueado"
            "fr" -> "🎉 Secret découvert ! Sélecteur global de pays déverrouillé"
            "pt" -> "🎉 Segredo descoberto! Seletor global de países desbloqueado"
            "ru" -> "🎉 Секрет раскрыт! Глобальный выбор стран разблокирован"
            "zh" -> "🎉 发现隐藏功能！全球国家选择器已解锁"
            "ja" -> "🎉 シークレット発見！グローバル国セレクターが解除されました"
            "ar" -> "🎉 تم اكتشاف السر! تم فتح محدد البلدان العالمي"
            "hi" -> "🎉 गुप्त फीचर खुला! वैश्विक देश चयनकर्ता अनलॉक हो गया"
            "bn" -> "🎉 গোপন রহস্য উন্মোচিত! গ্লোবাল দেশ নির্বাচক আনলক হয়েছে"
            else -> "🎉 Secret Discovered! Global Country Selector Unlocked"
        }
    }
}

/**
 * Extension helper on [CountryTippingData] to retrieve the country name in the active language.
 *
 * @param languageCode Active 2-letter ISO language code (e.g. "en", "de", "es", "zh").
 * @return Localized country name string.
 */
fun CountryTippingData.getLocalizedCountry(languageCode: String): String {
    return GlobalExplorerLocalization.getLocalizedCountry(iso, languageCode, country)
}

/**
 * Extension helper on [CountryTippingData] to retrieve the cultural matrix description in the active language.
 *
 * @param languageCode Active 2-letter ISO language code (e.g. "en", "de", "es", "zh").
 * @return Localized cultural matrix description string.
 */
fun CountryTippingData.getLocalizedMatrixName(languageCode: String): String {
    return GlobalExplorerLocalization.getLocalizedMatrixName(baseMatrix, languageCode, matrixName)
}

/**
 * Extension helper on [CurrencyOption] to retrieve the currency label in the active language.
 *
 * @param languageCode Active 2-letter ISO language code (e.g. "en", "de", "es", "zh").
 * @return Localized currency display label.
 */
fun CurrencyOption.getLocalizedLabel(languageCode: String): String {
    return GlobalExplorerLocalization.getLocalizedCurrencyLabel(this, languageCode)
}
