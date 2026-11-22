package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive side-mounted overlay sheet for Tablet Mode (`> 840dp`).
 *
 * Designed to stay mounted around an [AnimatedVisibility] so transitioning from a single-pane
 * bottom sheet (`<= 840dp`) to a two-pane tablet dashboard (`> 840dp`) smoothly slides the sheet
 * in from the trailing side edge in coordination with the two-pane dashboard expansion.
 *
 * @param visible Controls whether the backdrop scrim and trailing side panel are shown.
 * @param onDismiss Callback invoked when the user taps outside the panel or presses system Back.
 * @param modifier Optional [Modifier] applied to the outer overlay container.
 * @param panelTestTag Optional Compose `testTag` attached to the sliding panel surface for UI testing.
 * @param content Composable content rendered inside the side-sheet container.
 */
@Composable
fun TabletSideSheetOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    panelTestTag: String? = null,
    content: @Composable () -> Unit
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val spatialSlideSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val scrimEnterSpec = spring<Float>(stiffness = Spring.StiffnessMediumLow)
    val scrimExitSpec = spring<Float>(stiffness = Spring.StiffnessMedium)

    BackHandler(enabled = visible) {
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = scrimEnterSpec),
        exit = fadeOut(animationSpec = scrimExitSpec),
        modifier = modifier
            .fillMaxSize()
            .testTag("tablet_side_sheet_overlay")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dimmed backdrop scrim (outside tap dismisses the side sheet)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.36f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .testTag("tablet_side_sheet_scrim")
            )

            // Trailing side panel surface with coordinated horizontal slide animation
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .animateEnterExit(
                        enter = slideInHorizontally(
                            animationSpec = spatialSlideSpec,
                            initialOffsetX = { fullWidth -> if (isRtl) -fullWidth else fullWidth }
                        ) + fadeIn(animationSpec = scrimEnterSpec),
                        exit = slideOutHorizontally(
                            animationSpec = spatialSlideSpec,
                            targetOffsetX = { fullWidth -> if (isRtl) -fullWidth else fullWidth }
                        ) + fadeOut(animationSpec = scrimExitSpec)
                    )
                    .fillMaxHeight()
                    .fillMaxWidth(0.46f)
                    .widthIn(min = 340.dp, max = 440.dp)
                    .pointerInput(Unit) {
                        // Consume unconsumed taps inside the sheet so the underlying scrim does not dismiss
                        detectTapGestures {}
                    }
                    .then(if (panelTestTag != null) Modifier.testTag(panelTestTag) else Modifier),
                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    content()
                }
            }
        }
    }
}
