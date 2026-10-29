package com.example.i18n

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Dynamic Runtime Locale Context Wrapper (LocaleContextWrapper)
 * =========================================================================================
 *
 * Description:
 * Injects a customized [Configuration] into the Android [Context] hierarchy at runtime.
 * Allows instant, flicker-free language switching (e.g., between English, German, Spanish,
 * French, Arabic, Chinese, Japanese, Russian) without requiring a full process restart.
 *
 * Capabilities:
 * - API 24+ LocaleList support: Updates Android N+ locale lists and layout direction (LTR/RTL).
 * - Legacy fallback: Sets configuration locale for older Android API levels.
 * - RTL awareness: Automatically flips layout directions for Right-to-Left languages (Arabic).
 * =========================================================================================
 */
class LocaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        /**
         * Wraps an existing [Context] with a new [Configuration] tuned to the requested [languageCode].
         *
         * @param context Source Android context.
         * @param languageCode 2-letter ISO 639-1 language code (e.g. "en", "de", "es", "zh", "ar").
         * @return Wrapped [ContextWrapper] referencing the localized configuration.
         */
        fun wrap(context: Context, languageCode: String): ContextWrapper {
            val locale = when (languageCode) {
                "zh" -> Locale.SIMPLIFIED_CHINESE
                else -> Locale.forLanguageTag(languageCode)
            }
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = Configuration(resources.configuration)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocales(LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                configuration.locale = locale
            }

            configuration.setLayoutDirection(locale)
            val newContext = context.createConfigurationContext(configuration)
            return ContextWrapper(newContext)
        }
    }
}
