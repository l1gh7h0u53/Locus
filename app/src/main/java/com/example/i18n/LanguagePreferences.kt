package com.example.i18n

import android.content.Context
import android.content.SharedPreferences

/**
 * =========================================================================================
 * FEATURE: Persistent Language Override Storage (LanguagePreferences)
 * =========================================================================================
 *
 * Description:
 * Manages SharedPreferences persistence for user-selected manual language overrides
 * (e.g. English, Spanish, French, German, Italian, Japanese, Simplified Chinese, Arabic, Russian).
 * When null, the app relies on automatic offline device locale detection.
 *
 * Dependencies:
 * - [android.content.SharedPreferences]: Key-value storage on Android.
 * - Used by [TippyViewModel] during startup and language switching.
 * =========================================================================================
 */
class LanguagePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Retrieves the manually overridden language code, or null if relying on offline auto-detection.
     *
     * @return 2-letter ISO language code (e.g. "en", "es", "de") or null.
     */
    fun getLanguageOverride(): String? {
        return prefs.getString(KEY_LANGUAGE_OVERRIDE, null)
    }

    /**
     * Persists manual language override (e.g. "en", "de", "ar", "zh"), or resets to auto if null.
     *
     * Dependencies:
     * - [SharedPreferences.Editor.putString] / [SharedPreferences.Editor.remove].
     *
     * @param languageCode ISO 639-1 language code, or null to clear override.
     */
    fun setLanguageOverride(languageCode: String?) {
        if (languageCode == null) {
            prefs.edit().remove(KEY_LANGUAGE_OVERRIDE).apply()
        } else {
            prefs.edit().putString(KEY_LANGUAGE_OVERRIDE, languageCode).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "tippy_language_prefs"
        private const val KEY_LANGUAGE_OVERRIDE = "pref_language_override"
    }
}
