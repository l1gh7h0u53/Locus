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
 * FEATURE: Global Haptic Feedback Engine (HapticFeedbackManager)
 * =========================================================================================
 *
 * Description:
 * Singleton haptic vibration manager delivering low-latency, tactile physical feedback
 * across Android UI components, sliders, number inputs, chip selectors, and celebratory easter eggs.
 *
 * Dual-Layer Execution Architecture:
 * 1. View-based haptics: Uses [android.view.View.performHapticFeedback] with Android 14 (API 34)
 *    SEGMENT_TICK, Android 11 (API 30) CLOCK_TICK / CONFIRM, or standard KEYBOARD_TAP.
 * 2. Hardware vibrator fallback: Uses [android.os.Vibrator] or [android.os.VibratorManager]
 *    with [android.os.VibrationEffect.EFFECT_TICK] or microsecond pulse waveforms.
 *
 * Dependencies:
 * - [android.os.VibratorManager]: API 31+ Vibrator system service manager.
 * - [android.os.Vibrator]: System vibrator hardware service.
 * - [android.view.View]: Target UI view receiving performHapticFeedback requests.
 * - [android.view.HapticFeedbackConstants]: Standard Android haptic feedback constants.
 * =========================================================================================
 */
object HapticFeedbackManager {

    /** Cached Vibrator hardware instance */
    @Volatile
    private var vibrator: Vibrator? = null

    /**
     * Initializes the global vibrator service instance using application or activity context.
     *
     * Dependencies:
     * - [android.content.Context.getSystemService]: Resolves VIBRATOR_MANAGER_SERVICE (API 31+) or VIBRATOR_SERVICE.
     *
     * @param context Application or Activity context.
     */
    fun init(context: Context) {
        if (vibrator == null) {
            synchronized(this) {
                if (vibrator == null) {
                    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        vibratorManager?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    }
                }
            }
        }
    }

    /**
     * Produces a distinct, tactile 'tick' vibration for keystrokes, bill changes, and slider steps.
     *
     * Feature:
     * - Uses [HapticFeedbackConstants.SEGMENT_TICK] on Android 14+ (UPSIDE_DOWN_CAKE).
     * - Uses [HapticFeedbackConstants.CLOCK_TICK] on Android 11+ (API 30).
     * - Uses [HapticFeedbackConstants.KEYBOARD_TAP] on older APIs.
     * - Falls back to [fallbackTick] if view haptics are disabled or unhandled.
     *
     * @param view Optional view attached to current window hierarchy.
     */
    fun tick(view: View? = null) {
        if (view != null) {
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> HapticFeedbackConstants.SEGMENT_TICK
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CLOCK_TICK
                else -> HapticFeedbackConstants.KEYBOARD_TAP
            }
            val performed = view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            if (!performed) {
                fallbackTick()
            }
        } else {
            fallbackTick()
        }
    }

    /**
     * Tactile 'tick' for Easter Egg logo taps.
     *
     * Dependencies:
     * - Delegates to [tick].
     */
    fun onLogoTap(view: View? = null) {
        tick(view)
    }

    /**
     * Distinct celebratory vibration pattern when an easter egg is unlocked.
     *
     * Dependencies:
     * - [HapticFeedbackConstants.CONFIRM] on API 30+.
     * - [fallbackUnlock] multi-stage waveform pulse.
     */
    fun onEasterEggUnlocked(view: View? = null) {
        if (view != null) {
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
                else -> HapticFeedbackConstants.LONG_PRESS
            }
            val performed = view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            if (!performed) {
                fallbackUnlock()
            }
        } else {
            fallbackUnlock()
        }
    }

    /**
     * Stronger confirmation haptic pulse on selecting a country or saving options.
     *
     * Dependencies:
     * - [HapticFeedbackConstants.CONFIRM] / [HapticFeedbackConstants.KEYBOARD_PRESS].
     * - [fallbackClick].
     */
    fun confirm(view: View? = null) {
        if (view != null) {
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
                else -> HapticFeedbackConstants.KEYBOARD_PRESS
            }
            val performed = view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            if (!performed) {
                fallbackClick()
            }
        } else {
            fallbackClick()
        }
    }

    /** Hardware vibrator fallback for subtle tick effect */
    private fun fallbackTick() {
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

    /** Hardware vibrator fallback for click effect */
    private fun fallbackClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {
            // Gracefully ignore
        }
    }

    /** Hardware vibrator fallback for celebration waveform */
    private fun fallbackUnlock() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 50, 75), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(75)
            }
        } catch (_: Exception) {
            // Gracefully ignore
        }
    }
}
