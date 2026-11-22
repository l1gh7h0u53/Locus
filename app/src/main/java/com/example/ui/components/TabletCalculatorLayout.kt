package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CurrencyOption
import com.example.CurrencyQuickBillRegistry
import com.example.HapticFeedbackManager.TactileKeypadPattern
import com.example.HapticFeedbackService
import com.example.HeroCalculationCard
import com.example.R
import com.example.ServiceType
import com.example.SplitBillSection
import com.example.TipPercentageSection
import com.example.TippyUiState
import com.example.TippyViewModel
import com.example.i18n.LanguageResolver
import com.example.i18n.LocaleFormatter
import com.example.i18n.getLocalizedCountry
import com.example.i18n.getLocalizedMatrixName
import com.example.shared.AdaptiveLayoutPolicy
import com.example.shared.CurrencyConverter
import com.example.shared.TabletTwoPanePosture

/**
 * Resolved Android Dp layout geometry parameters mapped from shared [AdaptiveLayoutPolicy].
 *
 * @property posture Portrait or landscape two-pane tablet posture.
 * @property leftPaneWeight Fractional row weight allocated to the left summary/advice pane.
 * @property paneGap Horizontal spacing between left and right dashboard panes.
 * @property cardPadding Internal padding applied inside tablet cards.
 * @property serviceColumns Number of grid columns (`2` or `3`) in [TabletServiceGridSection].
 * @property keypadButtonHeight Target height for keys on the 3x4 tactile numeric keypad.
 * @property maxDashboardWidth Maximum centered width constraint for ultra-wide tablet/desktop displays.
 */
data class AdaptiveTabletMetrics(
    val posture: TabletTwoPanePosture,
    val leftPaneWeight: Float,
    val paneGap: Dp,
    val cardPadding: Dp,
    val serviceColumns: Int,
    val keypadButtonHeight: Dp,
    val maxDashboardWidth: Dp
)

/**
 * Pure adaptive posture resolver delegating to [AdaptiveLayoutPolicy.resolveWindowMetrics] in the
 * shared KMP module so threshold (`> 840dp`) and pane geometry remain unified across platforms.
 *
 * @param viewportWidth Available container width in [Dp].
 * @param viewportHeight Available container height in [Dp].
 * @return [AdaptiveTabletMetrics] converted from shared KMP dp metrics into Compose [Dp] tokens.
 */
fun resolveAdaptiveTabletMetrics(
    viewportWidth: Dp,
    viewportHeight: Dp
): AdaptiveTabletMetrics {
    val shared = AdaptiveLayoutPolicy.resolveWindowMetrics(
        windowWidthDp = viewportWidth.value,
        windowHeightDp = viewportHeight.value,
        isSeniorMode = false
    )
    return AdaptiveTabletMetrics(
        posture = shared.posture,
        leftPaneWeight = if (shared.isTwoPaneTablet) shared.leftPaneWeight else 0.42f,
        paneGap = shared.paneGapDp.dp,
        cardPadding = shared.cardPaddingDp.dp,
        serviceColumns = shared.serviceColumns.coerceAtLeast(2),
        keypadButtonHeight = shared.keypadButtonHeightDp.dp,
        maxDashboardWidth = shared.maxDashboardWidthDp?.dp ?: Dp.Unspecified
    )
}

/**
 * =========================================================================================
 * FEATURE: Adaptive Two-Pane Tablet Calculator Dashboard (TabletCalculatorDashboard)
 * =========================================================================================
 *
 * Description:
 * Silent large-screen two-pane layout for Standard Calculator mode on tablets, foldables,
 * and wide desktop windows:
 * - Left Summary & Etiquette Pane: Hero Total Card (Round Up/Down & Share), always-visible
 *   localized Cultural Advice & Secondary Currency Converter Card, and Reset action.
 * - Right Input & Controls Pane: Bill input with hardware-like tactile 3x4 numeric keypad
 *   and differentiated vibration patterns, adaptive multi-column Service Category grid,
 *   Tipping Tiers & Slider, and Split Bill stepper.
 *
 * Smoothly animates pane weights, gaps, card padding, key height, and service grid columns
 * via Material 3 Expressive springs whenever the device rotates or resizes between portrait
 * and landscape states.
 *
 * @param uiState Reactive snapshot of calculator and country state.
 * @param viewModel [TippyViewModel] handling calculation, rounding, split, and currency mutations.
 * @param hapticService Tactile vibration engine dispatching differentiated keypad patterns.
 * @param triggerHaptic Lightweight haptic callback for standard chip/service selections.
 * @param focusManager Focus controller used to dismiss soft keyboard focus on IME Done.
 * @param keyboardController Optional software keyboard controller.
 * @param viewportWidth Available container width in [Dp] driving responsive tablet posture.
 * @param viewportHeight Available container height in [Dp] distinguishing portrait vs landscape.
 * =========================================================================================
 */
@Composable
fun TabletCalculatorDashboard(
    uiState: TippyUiState,
    viewModel: TippyViewModel,
    hapticService: HapticFeedbackService,
    triggerHaptic: () -> Unit,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    viewportWidth: Dp = 1024.dp,
    viewportHeight: Dp = 768.dp
) {
    val view = LocalView.current
    val metrics = remember(viewportWidth, viewportHeight) {
        resolveAdaptiveTabletMetrics(viewportWidth, viewportHeight)
    }

    // Spatial & layout springs for silky portrait <-> landscape resizing
    val spatialSpringFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val spatialSpringDp = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val animatedLeftWeight by animateFloatAsState(
        targetValue = metrics.leftPaneWeight,
        animationSpec = spatialSpringFloat,
        label = "TabletLeftPaneWeight"
    )
    val animatedPaneGap by animateDpAsState(
        targetValue = metrics.paneGap,
        animationSpec = spatialSpringDp,
        label = "TabletPaneGap"
    )
    val animatedCardPadding by animateDpAsState(
        targetValue = metrics.cardPadding,
        animationSpec = spatialSpringDp,
        label = "TabletCardPadding"
    )
    val animatedKeypadHeight by animateDpAsState(
        targetValue = metrics.keypadButtonHeight,
        animationSpec = spatialSpringDp,
        label = "TabletKeypadHeight"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (metrics.maxDashboardWidth != Dp.Unspecified) {
                    Modifier.widthIn(max = metrics.maxDashboardWidth)
                } else {
                    Modifier
                }
            )
            .testTag("tablet_calculator_dashboard"),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tablet_posture_${metrics.posture.name.lowercase()}"),
            horizontalArrangement = Arrangement.spacedBy(animatedPaneGap),
            verticalAlignment = Alignment.Top
        ) {
            // LEFT PANE: Live Totals, Rounding Actions, Always-Visible Etiquette & Reset
            Column(
                modifier = Modifier
                    .weight(animatedLeftWeight)
                    .testTag("tablet_pane_left"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeroCalculationCard(
                    uiState = uiState,
                    isTabletMode = true,
                    onRoundUp = {
                        hapticService.performKeypadPattern(TactileKeypadPattern.ACTION_CONFIRM, view)
                        viewModel.roundUpTotal()
                    },
                    onRoundDown = {
                        hapticService.performKeypadPattern(TactileKeypadPattern.ACTION_CONFIRM, view)
                        viewModel.roundDownTotal()
                    }
                )

                TabletCulturalBreakdownCard(
                    uiState = uiState,
                    hapticService = hapticService,
                    onSwapCurrency = { secondaryCurrency ->
                        viewModel.swapHomeAndDestinationCurrency(secondaryCurrency)
                    },
                    cardPadding = animatedCardPadding
                )

                val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)
                Button(
                    onClick = {
                        hapticService.performKeypadPattern(TactileKeypadPattern.ACTION_CONFIRM, view)
                        viewModel.clearAll()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isLongText) 60.dp else 54.dp)
                        .testTag("clear_button_reset"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.clear_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        softWrap = true,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // RIGHT PANE: Subtotal Input + Hardware Tactile Keypad, Service Grid, Tiers & Split
            Column(
                modifier = Modifier
                    .weight((1f - animatedLeftWeight).coerceAtLeast(0.45f))
                    .testTag("tablet_pane_right"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TabletBillInputAndKeypadCard(
                    uiState = uiState,
                    hapticService = hapticService,
                    cardPadding = animatedCardPadding,
                    keypadButtonHeight = animatedKeypadHeight,
                    onBillTextTyped = { newValue ->
                        val previous = uiState.billAmountInput
                        if (newValue.length > previous.length) {
                            hapticService.performKeypadPattern(TactileKeypadPattern.DIGIT_PRESS, view)
                        } else if (newValue.length < previous.length) {
                            hapticService.performKeypadPattern(TactileKeypadPattern.BACKSPACE_THUD, view)
                        }
                        viewModel.updateBillAmount(newValue)
                    },
                    onKeypadUpdate = { nextValue ->
                        viewModel.updateBillAmount(nextValue)
                    },
                    onClear = {
                        hapticService.performKeypadPattern(TactileKeypadPattern.ACTION_CONFIRM, view)
                        viewModel.updateBillAmount("")
                    },
                    onQuickAdd = { amount ->
                        val currentNumeric = uiState.billAmountInput.toDoubleOrNull() ?: 0.0
                        val pattern = if (currentNumeric + amount > 1_000_000_000.0) {
                            TactileKeypadPattern.BOUNDARY_REJECT
                        } else {
                            TactileKeypadPattern.QUICK_ADD_STEP
                        }
                        hapticService.performKeypadPattern(pattern, view)
                        viewModel.addToBill(amount)
                    },
                    focusManager = focusManager,
                    keyboardController = keyboardController
                )

                TabletServiceGridSection(
                    uiState = uiState,
                    targetColumns = metrics.serviceColumns,
                    onSelectService = { service ->
                        triggerHaptic()
                        viewModel.selectServiceType(service)
                    }
                )

                TipPercentageSection(
                    uiState = uiState,
                    hapticService = hapticService,
                    onSelectRate = { rate ->
                        triggerHaptic()
                        viewModel.selectPercentage(rate)
                    },
                    onSliderChange = { percent ->
                        viewModel.updateCustomPercentage(percent)
                    }
                )

                SplitBillSection(
                    uiState = uiState,
                    onIncrement = {
                        hapticService.performKeypadPattern(TactileKeypadPattern.QUICK_ADD_STEP, view)
                        viewModel.incrementSplit()
                    },
                    onDecrement = {
                        hapticService.performKeypadPattern(TactileKeypadPattern.DECIMAL_DETENT, view)
                        viewModel.decrementSplit()
                    }
                )
            }
        }
    }
}

/**
 * Always-visible Cultural Etiquette & Secondary Currency Converter Card pinned beneath the Hero
 * Totals Card in the left tablet summary pane. Tipping tier pills are intentionally omitted
 * here because interactive tiers already live in the right-hand pane.
 */
@Composable
private fun TabletCulturalBreakdownCard(
    uiState: TippyUiState,
    hapticService: HapticFeedbackService,
    onSwapCurrency: (CurrencyOption) -> Unit,
    cardPadding: Dp = 18.dp
) {
    val context = LocalContext.current
    val view = LocalView.current
    val currentRule = uiState.currentRule
    val localizedNotes = currentRule.getLocalizedNotes(context)
        .takeIf { it.isNotBlank() } ?: stringResource(R.string.note_default_guideline)
    val flagEmoji = remember(uiState.activeCountry.iso) {
        CountryFlagHelper.getFlagEmoji(uiState.activeCountry.iso)
    }

    var showConvertedCurrency by rememberSaveable { mutableStateOf(false) }
    var swapTapCount by rememberSaveable { mutableIntStateOf(0) }
    val swapIconRotation by animateFloatAsState(
        targetValue = swapTapCount * 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "CurrencySwapIconRotation"
    )

    val secondaryCurrencyCode = remember(
        uiState.selectedCurrency.code,
        uiState.activeCountry.localCurrency,
        uiState.activeCountry.baseMatrix,
        uiState.homeCurrency.code
    ) {
        CurrencyConverter.resolveSecondaryCurrencyCode(
            selectedCurrencyCode = uiState.selectedCurrency.code,
            countryLocalCurrencyCode = uiState.activeCountry.localCurrency,
            countryMatrixCode = uiState.activeCountry.baseMatrix,
            homeCurrencyCode = uiState.homeCurrency.code
        )
    }
    val secondaryCurrency = remember(secondaryCurrencyCode) {
        CurrencyOption.fromCode(secondaryCurrencyCode)
            ?: if (uiState.selectedCurrency == CurrencyOption.EUR) CurrencyOption.USD else CurrencyOption.EUR
    }
    val destinationCurrency = uiState.destinationCurrency
    val homeCurrency = uiState.homeCurrency
    // Determine which currency tapping "Swap" will activate next (Home <-> Destination, or fallback pair)
    val swapTargetCurrency = remember(
        uiState.selectedCurrency.code,
        destinationCurrency.code,
        homeCurrency.code,
        secondaryCurrency.code
    ) {
        when {
            homeCurrency.code != destinationCurrency.code -> {
                if (uiState.selectedCurrency.code == destinationCurrency.code) homeCurrency
                else destinationCurrency
            }
            uiState.selectedCurrency.code != destinationCurrency.code -> destinationCurrency
            else -> secondaryCurrency
        }
    }
    val conversion = remember(
        uiState.totalBill,
        uiState.sharePerPerson,
        uiState.selectedCurrency.code,
        secondaryCurrency.code
    ) {
        CurrencyConverter.convertTotal(
            totalAmount = uiState.totalBill,
            sharePerPerson = uiState.sharePerPerson,
            fromCode = uiState.selectedCurrency.code,
            toCode = secondaryCurrency.code
        )
    }
    val formattedRate = remember(conversion.exchangeRate, uiState.currentLanguage) {
        LocaleFormatter.formatAmountOnly(
            amount = conversion.exchangeRate,
            langCode = uiState.currentLanguage,
            forceTwoDecimals = conversion.exchangeRate < 100.0
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("tablet_cultural_breakdown_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Country & Matrix Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = flagEmoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = uiState.activeCountry.getLocalizedCountry(uiState.currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = uiState.activeCountry.getLocalizedMatrixName(uiState.currentLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Cultural Advice Callout Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.culture_advice),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = localizedNotes,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Country-Aware Secondary Currency Converter Toggle
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = if (showConvertedCurrency) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (showConvertedCurrency) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .toggleable(
                                value = showConvertedCurrency,
                                role = Role.Switch,
                                onValueChange = { enabled ->
                                    hapticService.performKeypadPattern(
                                        if (enabled) TactileKeypadPattern.QUICK_ADD_STEP else TactileKeypadPattern.DECIMAL_DETENT,
                                        view
                                    )
                                    showConvertedCurrency = enabled
                                }
                            )
                            .heightIn(min = 48.dp)
                            .testTag("tablet_currency_converter_toggle"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (showConvertedCurrency) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                tint = if (showConvertedCurrency) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(
                                    R.string.tablet_currency_converter_label,
                                    "${secondaryCurrency.code} (${secondaryCurrency.symbol})"
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(
                                    R.string.tablet_currency_converter_rate,
                                    uiState.selectedCurrency.code,
                                    formattedRate,
                                    secondaryCurrency.code
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Switch(
                            checked = showConvertedCurrency,
                            onCheckedChange = null,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dedicated One-Tap Home <-> Destination Currency Swap Button
                    val swapLabel = stringResource(
                        R.string.tablet_currency_swap_action,
                        uiState.selectedCurrency.code,
                        swapTargetCurrency.code
                    )
                    Surface(
                        onClick = {
                            hapticService.performKeypadPattern(TactileKeypadPattern.ACTION_CONFIRM, view)
                            swapTapCount++
                            onSwapCurrency(secondaryCurrency)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("tablet_currency_swap_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = swapLabel,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer { rotationZ = swapIconRotation }
                                )
                                Text(
                                    text = swapLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = "${uiState.selectedCurrency.symbol} → ${swapTargetCurrency.symbol}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showConvertedCurrency,
                        enter = expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                        exit = shrinkVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val convertedTotalFormatted = LocaleFormatter.formatCurrency(
                                amount = conversion.convertedTotal,
                                currency = secondaryCurrency,
                                langCode = uiState.currentLanguage
                            )
                            Text(
                                text = convertedTotalFormatted,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("tablet_converted_total_text")
                            )

                            if (uiState.splitCount > 1) {
                                val convertedShareFormatted = LocaleFormatter.formatCurrency(
                                    amount = conversion.convertedSharePerPerson,
                                    currency = secondaryCurrency,
                                    langCode = uiState.currentLanguage
                                )
                                Text(
                                    text = stringResource(R.string.per_person_amount_format, convertedShareFormatted),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("tablet_converted_share_text")
                                )
                            }

                            Text(
                                text = stringResource(R.string.tablet_currency_converter_offline),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bill Subtotal Input Card equipped with quick-add currency chips and a built-in
 * hardware-inspired 3x4 tactile numeric keypad with physical press morphing and
 * differentiated haptic patterns per key category.
 */
@Composable
private fun TabletBillInputAndKeypadCard(
    uiState: TippyUiState,
    hapticService: HapticFeedbackService,
    cardPadding: Dp = 18.dp,
    keypadButtonHeight: Dp = 52.dp,
    onBillTextTyped: (String) -> Unit,
    onKeypadUpdate: (String) -> Unit,
    onClear: () -> Unit,
    onQuickAdd: (Double) -> Unit,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?
) {
    val view = LocalView.current
    val currencyTier = remember(uiState.selectedCurrency.code) {
        CurrencyQuickBillRegistry.getTierForCurrency(uiState.selectedCurrency.code)
    }
    val decimalLabel = remember(uiState.currentLanguage) {
        LocaleFormatter.getDecimalSeparator(uiState.currentLanguage).toString()
    }
    val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Text(
                text = stringResource(R.string.bill_amount).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = uiState.billAmountInput,
                onValueChange = onBillTextTyped,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                ),
                placeholder = {
                    Text(
                        text = "0.00",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        fontWeight = FontWeight.Bold
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                leadingIcon = {
                    if (!uiState.selectedCurrency.isSymbolAfter) {
                        Text(
                            text = uiState.selectedCurrency.symbol,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        if (uiState.selectedCurrency.isSymbolAfter) {
                            Text(
                                text = uiState.selectedCurrency.symbol,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        if (uiState.billAmountInput.isNotEmpty()) {
                            IconButton(
                                onClick = onClear,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .testTag("clear_button_tag")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_button),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bill_input_field"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick-Add Denomination Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_add_buttons"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currencyTier.quickAddAmounts.forEachIndexed { index, amount ->
                    val label = currencyTier.displayLabels.getOrElse(index) { "+${amount.toInt()}" }
                    val addToBillDesc = stringResource(R.string.quick_add_content_desc, label)
                    Button(
                        onClick = { onQuickAdd(amount) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLongText) 48.dp else 44.dp)
                            .testTag("quick_add_${amount.toInt()}")
                            .semantics { contentDescription = addToBillDesc },
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tactile 3x4 Hardware-Inspired Tablet Number Pad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tablet_tactile_keypad"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val digitRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )
                digitRows.forEach { rowDigits ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowDigits.forEach { digit ->
                            TactileKeypadKey(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                                pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                keyHeight = keypadButtonHeight,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("keypad_digit_$digit"),
                                onClick = {
                                    val next = appendKeypadDigit(uiState.billAmountInput, digit)
                                    val pattern = if (next == uiState.billAmountInput) {
                                        TactileKeypadPattern.BOUNDARY_REJECT
                                    } else {
                                        TactileKeypadPattern.DIGIT_PRESS
                                    }
                                    hapticService.performKeypadPattern(pattern, view)
                                    onKeypadUpdate(next)
                                }
                            ) {
                                Text(
                                    text = digit,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Bottom Row: Decimal Separator | 0 | Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TactileKeypadKey(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                        keyHeight = keypadButtonHeight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keypad_decimal"),
                        onClick = {
                            val next = appendKeypadDecimal(uiState.billAmountInput)
                            val pattern = if (next == uiState.billAmountInput) {
                                TactileKeypadPattern.BOUNDARY_REJECT
                            } else {
                                TactileKeypadPattern.DECIMAL_DETENT
                            }
                            hapticService.performKeypadPattern(pattern, view)
                            onKeypadUpdate(next)
                        }
                    ) {
                        Text(
                            text = decimalLabel,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                    }

                    TactileKeypadKey(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                        pressedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                        keyHeight = keypadButtonHeight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keypad_digit_0"),
                        onClick = {
                            val next = appendKeypadDigit(uiState.billAmountInput, "0")
                            val pattern = if (next == uiState.billAmountInput) {
                                TactileKeypadPattern.BOUNDARY_REJECT
                            } else {
                                TactileKeypadPattern.DIGIT_PRESS
                            }
                            hapticService.performKeypadPattern(pattern, view)
                            onKeypadUpdate(next)
                        }
                    ) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val backspaceDesc = stringResource(R.string.history_delete_item)
                    TactileKeypadKey(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                        pressedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.22f),
                        contentColor = MaterialTheme.colorScheme.error,
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                        keyHeight = keypadButtonHeight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keypad_backspace")
                            .semantics { contentDescription = backspaceDesc },
                        onClick = {
                            val pattern = if (uiState.billAmountInput.isEmpty()) {
                                TactileKeypadPattern.BOUNDARY_REJECT
                            } else {
                                TactileKeypadPattern.BACKSPACE_THUD
                            }
                            hapticService.performKeypadPattern(pattern, view)
                            onKeypadUpdate(uiState.billAmountInput.dropLast(1))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = backspaceDesc,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hardware-inspired tactile key surface featuring Material 3 Expressive spring scale depression,
 * corner-radius morphing, and tonal color shift while pressing.
 */
@Composable
private fun TactileKeypadKey(
    containerColor: Color,
    pressedContainerColor: Color,
    contentColor: Color,
    borderColor: Color,
    keyHeight: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "KeypadKeyScale"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 11.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "KeypadKeyCorner"
    )
    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed) pressedContainerColor else containerColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "KeypadKeyColor"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius),
        color = animatedBgColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isPressed) 0.dp else 2.dp,
        modifier = modifier
            .height(keyHeight.coerceAtLeast(48.dp))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * Multi-column Service Category grid displaying all 6 service types without horizontal scrolling
 * in Tablet Mode, smoothly animating between 3x2 (landscape) and 2x3 (portrait) configurations.
 */
@Composable
private fun TabletServiceGridSection(
    uiState: TippyUiState,
    targetColumns: Int,
    onSelectService: (ServiceType) -> Unit
) {
    val context = LocalContext.current
    val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tablet_service_grid")
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        Text(
            text = stringResource(R.string.service_category).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        AnimatedContent(
            targetState = targetColumns.coerceIn(2, 3),
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                 scaleIn(initialScale = 0.97f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                    .togetherWith(
                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                        scaleOut(targetScale = 0.97f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                    )
                    .using(
                        SizeTransform(
                            clip = false,
                            sizeAnimationSpec = { _, _ ->
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            }
                        )
                    )
            },
            label = "TabletServiceGridColumnsTransition"
        ) { columnCount ->
            val rows = ServiceType.values().toList().chunked(columnCount)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("service_grid_cols_$columnCount"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { service ->
                            val isSelected = uiState.selectedServiceType == service
                            val rule = uiState.activeCountry.getRuleFor(service.datasetKey)
                            val containerColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "ServiceCardBg"
                            )
                            val contentColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "ServiceCardContent"
                            )
                            val serviceLabel = stringResource(service.nameRes)

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = if (isLongText) 114.dp else 104.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(
                                        role = Role.RadioButton,
                                        onClickLabel = serviceLabel
                                    ) { onSelectService(service) }
                                    .semantics { selected = isSelected }
                                    .testTag("service_${service.id}"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = service.icon,
                                            contentDescription = null,
                                            tint = contentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = serviceLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = contentColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = rule.getLocalizedStandardTip(context),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) contentColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Balance incomplete final row if needed
                        repeat(columnCount - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Appends a numeric digit ("0".."9") from the tablet tactile keypad to [current] bill input,
 * preventing redundant leading zeros ("0" + "5" -> "5") and enforcing the 12-character input limit.
 *
 * @param current Current raw bill string shown in the input field.
 * @param digit Single numeric digit character ("0".."9") to append.
 * @return Updated bill string, or [current] unchanged if the 12-character cap would be exceeded.
 */
internal fun appendKeypadDigit(current: String, digit: String): String {
    if (current == "0") {
        return if (digit == "0") "0" else digit
    }
    val candidate = current + digit
    return if (candidate.length <= 12) candidate else current
}

/**
 * Appends a single decimal point to [current] bill input (prefixing "0." when empty), ignoring
 * duplicate dots and respecting the 12-character cap.
 *
 * @param current Current raw bill string shown in the input field.
 * @return Updated bill string ending with ".", or [current] unchanged if a decimal already exists or cap is reached.
 */
internal fun appendKeypadDecimal(current: String): String {
    if (current.contains('.')) return current
    val candidate = if (current.isEmpty()) "0." else "$current."
    return if (candidate.length <= 12) candidate else current
}
