package com.example.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * =========================================================================================
 * FEATURE: First-Time User Onboarding Preferences (OnboardingPreferences)
 * =========================================================================================
 *
 * Description:
 * Manages persistent flags for first-time user discovery coach marks using Android [SharedPreferences].
 * Guarantees that interactive feature tours (explaining Senior Mode, Quick-Add currency chips,
 * and DataStore history) are shown on initial app launch and suppressed on subsequent launches,
 * while allowing intentional user-triggered restarts.
 *
 * Dependencies:
 * - [android.content.SharedPreferences]: Private key-value storage.
 * =========================================================================================
 */
class OnboardingPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Checks whether the user has previously viewed and completed the onboarding coach mark tour.
     *
     * @return True if coach marks have been completed; false if first launch or reset.
     */
    fun hasSeenCoachmarks(): Boolean {
        return prefs.getBoolean(KEY_COACHMARKS_SEEN, false)
    }

    /**
     * Records that the user has completed the onboarding tour, preventing automatic re-display.
     */
    fun markCoachmarksSeen() {
        prefs.edit().putBoolean(KEY_COACHMARKS_SEEN, true).apply()
    }

    /**
     * Clears the coach mark completion flag, allowing the onboarding tour to be re-run on demand.
     */
    fun resetCoachmarks() {
        prefs.edit().remove(KEY_COACHMARKS_SEEN).apply()
    }

    companion object {
        private const val PREFS_NAME = "locus_onboarding_prefs"
        private const val KEY_COACHMARKS_SEEN = "has_seen_first_time_coachmarks_v1"
    }
}
