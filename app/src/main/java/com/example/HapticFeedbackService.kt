package com.example

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * =========================================================================================
 * FEATURE: Context-Bound Haptic Feedback Service (HapticFeedbackService)
 * =========================================================================================
 *
 * Description:
 * Injectable service instance providing calibrated haptic pulses tailored specifically
 * for Jetpack Compose UI components (sliders, country selection cards, easter egg counters).
 *
 * Feedback Types:
 * - Slider Tick: High-frequency tactile notch feedback matching system volume sliders.
 * - Subtle Tap: Soft vibration for incremental counters and keystrokes.
 * - Country Selection: Affirmative confirmation pulse upon choosing a new nation.
 * - Unlock Trigger: Multi-stage celebratory vibration upon completing Easter Egg unlocking.
 * - Standard Click: Default virtual key feedback for general button presses.
 *
 * Dependencies:
 * - [android.content.Context]: Resolves VibratorManager / Vibrator system service.
 * - [android.view.View]: Target view hierarchy for performHapticFeedback.
 * - [android.os.Vibrator] / [android.os.VibratorManager]: Fallback hardware actuation.
 * =========================================================================================
 */
class HapticFeedbackService(private val context: Context) {

    /** Lazy evaluation of hardware Vibrator */
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Slider tick vibration calibrated to match the feel of the Android system volume slider.
     *
     * Feature:
     * - Uses [HapticFeedbackConstants.SEGMENT_TICK] on Android 14+ or [HapticFeedbackConstants.CLOCK_TICK] on API 30+.
     * - Falls back to [fallbackSliderVibration].
     *
     * @param view Optional View attached to the window.
     */
    fun performSliderTick(view: View? = null) {
        if (view != null) {
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> HapticFeedbackConstants.SEGMENT_TICK
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CLOCK_TICK
                else -> HapticFeedbackConstants.KEYBOARD_TAP
            }
            val success = view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            if (!success) {
                fallbackSliderVibration()
            }
        } else {
            fallbackSliderVibration()
        }
    }

    /**
     * Subtle tick vibration for incremental taps (e.g. logo tap sequence).
     *
     * @param view Optional View attached to the window.
     */
    fun performSubtleTap(view: View? = null) {
        if (view != null) {
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> HapticFeedbackConstants.SEGMENT_TICK
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CLOCK_TICK
                else -> HapticFeedbackConstants.KEYBOARD_TAP
            }
            val success = view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            if (!success) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        } else {
            fallbackSubtleVibration()
        }
    }

    /**
     * Confirmation haptic feedback when a country is selected from the menu.
     *
     * @param view Optional View attached to the window.
     */
    fun performCountrySelection(view: View? = null) {
        if (view != null) {
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
                else -> HapticFeedbackConstants.KEYBOARD_PRESS
            }
            val success = view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            if (!success) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        } else {
            fallbackSelectionVibration()
        }
    }

    /**
     * Distinct unlock vibration triggered on the final tap that reveals the country selector.
     *
     * @param view Optional View attached to the window.
     */
    fun performUnlockTrigger(view: View? = null) {
        if (view != null) {
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
                else -> HapticFeedbackConstants.LONG_PRESS
            }
            val success = view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            if (!success) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } else {
            fallbackUnlockVibration()
        }
    }

    /**
     * Standard UI click haptic for simple buttons.
     *
     * @param view Optional View attached to the window.
     */
    fun performStandardClick(view: View? = null) {
        if (view != null) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
        } else {
            fallbackSubtleVibration()
        }
    }

    /** Hardware vibrator fallback for slider tick */
    private fun fallbackSliderVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(10, 80))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (_: Exception) {
            // Gracefully ignore
        }
    }

    /** Hardware vibrator fallback for subtle tap */
    private fun fallbackSubtleVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {
            // Gracefully ignore if vibration permission is not granted
        }
    }

    /** Hardware vibrator fallback for selection confirmation */
    private fun fallbackSelectionVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {
            // Gracefully ignore
        }
    }

    /** Hardware vibrator fallback for multi-pulse unlock */
    private fun fallbackUnlockVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {
            // Gracefully ignore
        }
    }
}
