package com.example.shared

/**
 * Top-level pane arrangement resolved from current window dimensions.
 */
enum class WindowPaneMode {
    /** Compact vertical single-column layout (`<= 840dp` or Senior Accessibility Mode). */
    SINGLE_PANE,

    /** Side-by-side two-pane tablet/desktop dashboard (`> 840dp`). */
    TWO_PANE
}

/**
 * Granular responsive posture inside [AdaptiveLayoutPolicy] for single-pane and two-pane Tablet Mode.
 */
enum class TabletTwoPanePosture {
    /** Phone or Senior-mode single-column layout. */
    SINGLE_PANE,

    /** Narrower two-pane split tuned for portrait tablets (`840dp < width <= height`). */
    PORTRAIT_TWO_PANE,

    /** Wide two-pane split with 3-column service grid tuned for landscape tablets and desktop windows. */
    LANDSCAPE_TWO_PANE
}

/**
 * Pure multiplatform layout metrics computed from container width and height in density-independent
 * pixels (dp). Consumed by the Android Compose UI layer to drive two-pane transitions cleanly.
 *
 * @property paneMode Single-pane vs two-pane layout classification.
 * @property posture Portrait or landscape tablet arrangement inside [AdaptiveLayoutPolicy].
 * @property isTwoPaneTablet True when the two-pane calculator dashboard should be active.
 * @property leftPaneWeight Fractional width assigned to the summary/advice pane in two-pane mode.
 * @property paneGapDp Horizontal spacing in dp between left and right panes.
 * @property cardPaddingDp Inner card padding in dp tuned for the active window size.
 * @property serviceColumns Column count for the tablet service category grid (2 portrait, 3 landscape).
 * @property keypadButtonHeightDp Target key height in dp for the tablet tactile numeric keypad.
 * @property maxDashboardWidthDp Optional maximum dashboard width cap in dp (null on compact screens).
 */
data class AdaptiveWindowMetrics(
    val paneMode: WindowPaneMode,
    val posture: TabletTwoPanePosture,
    val isTwoPaneTablet: Boolean,
    val leftPaneWeight: Float,
    val paneGapDp: Float,
    val cardPaddingDp: Float,
    val serviceColumns: Int,
    val keypadButtonHeightDp: Float,
    val maxDashboardWidthDp: Float?
)

/**
 * Shared screen-width detection and adaptive two-pane geometry policy.
 *
 * Triggers the two-pane Tablet Mode transition strictly when the available window width exceeds
 * [TABLET_TWO_PANE_WIDTH_THRESHOLD_DP] (840dp), keeping Senior Mode focused in a single-column layout.
 */
object AdaptiveLayoutPolicy {

    /** Width breakpoint in dp above which the two-pane Tablet Mode layout activates (`840dp`). */
    const val TABLET_TWO_PANE_WIDTH_THRESHOLD_DP: Float = 840f

    /**
     * Checks whether the current container width qualifies for two-pane Tablet Mode.
     *
     * @param windowWidthDp Available window or container width in density-independent pixels (dp).
     * @return `true` when [windowWidthDp] is finite and strictly greater than `840dp`; `false` otherwise.
     */
    fun isTwoPaneWidth(windowWidthDp: Float): Boolean {
        return windowWidthDp.isFinite() && windowWidthDp > TABLET_TWO_PANE_WIDTH_THRESHOLD_DP
    }

    /**
     * Computes the full adaptive layout configuration for the supplied window bounds.
     *
     * @param windowWidthDp Available window/container width in dp.
     * @param windowHeightDp Available window/container height in dp.
     * @param isSeniorMode Whether Senior accessibility mode is currently active (keeps single-pane layout).
     * @return [AdaptiveWindowMetrics] specifying pane mode, posture, spacing, keypad height, and grid columns.
     */
    fun resolveWindowMetrics(
        windowWidthDp: Float,
        windowHeightDp: Float,
        isSeniorMode: Boolean = false
    ): AdaptiveWindowMetrics {
        if (isSeniorMode || !isTwoPaneWidth(windowWidthDp)) {
            return AdaptiveWindowMetrics(
                paneMode = WindowPaneMode.SINGLE_PANE,
                posture = TabletTwoPanePosture.SINGLE_PANE,
                isTwoPaneTablet = false,
                leftPaneWeight = 1f,
                paneGapDp = 16f,
                cardPaddingDp = 20f,
                serviceColumns = 1,
                keypadButtonHeightDp = 48f,
                maxDashboardWidthDp = null
            )
        }

        val isLandscape = !windowHeightDp.isFinite() || windowWidthDp > windowHeightDp
        return if (isLandscape) {
            AdaptiveWindowMetrics(
                paneMode = WindowPaneMode.TWO_PANE,
                posture = TabletTwoPanePosture.LANDSCAPE_TWO_PANE,
                isTwoPaneTablet = true,
                leftPaneWeight = 0.45f,
                paneGapDp = 24f,
                cardPaddingDp = 20f,
                serviceColumns = 3,
                keypadButtonHeightDp = 54f,
                maxDashboardWidthDp = 1280f
            )
        } else {
            AdaptiveWindowMetrics(
                paneMode = WindowPaneMode.TWO_PANE,
                posture = TabletTwoPanePosture.PORTRAIT_TWO_PANE,
                isTwoPaneTablet = true,
                leftPaneWeight = 0.42f,
                paneGapDp = 16f,
                cardPaddingDp = 16f,
                serviceColumns = 2,
                keypadButtonHeightDp = 52f,
                maxDashboardWidthDp = 960f
            )
        }
    }
}
