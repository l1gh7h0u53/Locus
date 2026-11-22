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

    /**
     * Hardware-like tactile vibration patterns tailored for calculator keypad switches,
     * decimal detents, backspace release steps, quick-add increments, confirmations, and boundary rejections.
     */
    enum class TactileKeypadPattern {
        /** Crisp mechanical switch click emitted when pressing digit keys (`0`-`9`). */
        DIGIT_PRESS,

        /** Subtle micro-tick detent emitted when entering the decimal separator. */
        DECIMAL_DETENT,

        /** Low-frequency release thud emitted when tapping backspace or clear. */
        BACKSPACE_THUD,

        /** Dual-step rising pulse emitted when tapping quick-add denomination keys. */
        QUICK_ADD_STEP,

        /** Heavy affirmative pulse emitted when toggling round-up/down or confirming actions. */
        ACTION_CONFIRM,

        /** Double-pulse rejection cue emitted when input hits length or numeric limits. */
        BOUNDARY_REJECT
    }

    /**
     * Dispatches a hardware-calibrated tactile vibration pattern for Tablet Mode's integrated
     * numeric keypad and related calculator keys.
     *
     * Uses View haptic constants combined with API 30+ [VibrationEffect.Composition] primitives
     * or microsecond amplitude waveforms when hardware actuation is needed.
     *
     * @param pattern Calibrated [TactileKeypadPattern] to actuate.
     * @param view Optional Android [View] attached to the Compose window for view-level haptics.
     */
    fun keypadPattern(pattern: TactileKeypadPattern, view: View? = null) {
        val viewHandled = if (view != null) {
            val constant = when (pattern) {
                TactileKeypadPattern.DIGIT_PRESS ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) HapticFeedbackConstants.KEYBOARD_PRESS
                    else HapticFeedbackConstants.KEYBOARD_TAP
                TactileKeypadPattern.DECIMAL_DETENT ->
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> HapticFeedbackConstants.SEGMENT_TICK
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CLOCK_TICK
                        else -> HapticFeedbackConstants.KEYBOARD_TAP
                    }
                TactileKeypadPattern.BACKSPACE_THUD ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) HapticFeedbackConstants.KEYBOARD_RELEASE
                    else HapticFeedbackConstants.KEYBOARD_TAP
                TactileKeypadPattern.QUICK_ADD_STEP ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) HapticFeedbackConstants.SEGMENT_TICK
                    else HapticFeedbackConstants.KEYBOARD_TAP
                TactileKeypadPattern.ACTION_CONFIRM ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                    else HapticFeedbackConstants.KEYBOARD_PRESS
                TactileKeypadPattern.BOUNDARY_REJECT ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
                    else HapticFeedbackConstants.LONG_PRESS
            }
            view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
        } else {
            false
        }

        if (!viewHandled) {
            triggerVibratorPattern(vibrator, pattern)
        }
    }

    /**
     * Executes a low-latency hardware [Vibrator] waveform / composition fallback matching the requested
     * [TactileKeypadPattern]. Safe on devices without a vibrator or missing permissions.
     */
    internal fun triggerVibratorPattern(targetVibrator: Vibrator?, pattern: TactileKeypadPattern) {
        if (targetVibrator == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val composition = VibrationEffect.startComposition()
                val primitivesSupported = when (pattern) {
                    TactileKeypadPattern.DIGIT_PRESS -> {
                        if (targetVibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.72f)
                            true
                        } else false
                    }
                    TactileKeypadPattern.DECIMAL_DETENT -> {
                        if (targetVibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)) {
                            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.58f)
                            true
                        } else false
                    }
                    TactileKeypadPattern.BACKSPACE_THUD -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            targetVibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)
                        ) {
                            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.78f)
                            true
                        } else if (targetVibrator.areAllPrimitivesSupported(
                                VibrationEffect.Composition.PRIMITIVE_CLICK,
                                VibrationEffect.Composition.PRIMITIVE_LOW_TICK
                            )
                        ) {
                            composition
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.75f)
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.45f, 16)
                            true
                        } else false
                    }
                    TactileKeypadPattern.QUICK_ADD_STEP -> {
                        if (targetVibrator.areAllPrimitivesSupported(
                                VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
                                VibrationEffect.Composition.PRIMITIVE_CLICK
                            )
                        ) {
                            composition
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.50f)
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.80f, 14)
                            true
                        } else false
                    }
                    TactileKeypadPattern.ACTION_CONFIRM -> {
                        if (targetVibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                            composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.92f)
                            true
                        } else false
                    }
                    TactileKeypadPattern.BOUNDARY_REJECT -> {
                        if (targetVibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK)) {
                            composition
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.65f)
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.65f, 32)
                            true
                        } else false
                    }
                }
                if (primitivesSupported) {
                    targetVibrator.vibrate(composition.compose())
                    return
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (pattern) {
                    TactileKeypadPattern.DIGIT_PRESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    TactileKeypadPattern.DECIMAL_DETENT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    TactileKeypadPattern.BACKSPACE_THUD -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    TactileKeypadPattern.QUICK_ADD_STEP -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    TactileKeypadPattern.ACTION_CONFIRM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    TactileKeypadPattern.BOUNDARY_REJECT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                }
                targetVibrator.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (pattern) {
                    TactileKeypadPattern.DIGIT_PRESS -> VibrationEffect.createOneShot(14L, 175)
                    TactileKeypadPattern.DECIMAL_DETENT -> VibrationEffect.createOneShot(9L, 120)
                    TactileKeypadPattern.BACKSPACE_THUD ->
                        VibrationEffect.createWaveform(longArrayOf(0L, 18L, 16L, 10L), intArrayOf(0, 200, 0, 110), -1)
                    TactileKeypadPattern.QUICK_ADD_STEP ->
                        VibrationEffect.createWaveform(longArrayOf(0L, 10L, 14L, 16L), intArrayOf(0, 130, 0, 190), -1)
                    TactileKeypadPattern.ACTION_CONFIRM -> VibrationEffect.createOneShot(24L, 220)
                    TactileKeypadPattern.BOUNDARY_REJECT ->
                        VibrationEffect.createWaveform(longArrayOf(0L, 14L, 30L, 14L), intArrayOf(0, 180, 0, 180), -1)
                }
                targetVibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                when (pattern) {
                    TactileKeypadPattern.DIGIT_PRESS -> targetVibrator.vibrate(14L)
                    TactileKeypadPattern.DECIMAL_DETENT -> targetVibrator.vibrate(9L)
                    TactileKeypadPattern.BACKSPACE_THUD -> targetVibrator.vibrate(longArrayOf(0L, 18L, 16L, 10L), -1)
                    TactileKeypadPattern.QUICK_ADD_STEP -> targetVibrator.vibrate(longArrayOf(0L, 10L, 14L, 16L), -1)
                    TactileKeypadPattern.ACTION_CONFIRM -> targetVibrator.vibrate(24L)
                    TactileKeypadPattern.BOUNDARY_REJECT -> targetVibrator.vibrate(longArrayOf(0L, 14L, 30L, 14L), -1)
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore when device vibrator is unavailable
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
