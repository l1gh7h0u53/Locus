package com.example

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlin.math.roundToInt
import com.example.preferences.OnboardingPreferences
import com.example.ui.components.CoachMarkOverlay
import com.example.ui.components.TipHistorySheet
import com.example.ui.components.SettingsBottomSheet
import com.example.history.TipHistoryDataStore
import com.example.history.SavePromptMode
import com.example.history.TipTransaction
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.i18n.LanguagePreferences
import com.example.i18n.LanguageResolver
import com.example.i18n.LocaleContextWrapper
import com.example.i18n.LocaleFormatter
import com.example.i18n.GlobalExplorerLocalization
import com.example.i18n.getLocalizedCountry
import com.example.i18n.getLocalizedMatrixName
import com.example.i18n.getLocalizedLabel
import com.example.ui.theme.TippyTheme
import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Screen Navigation & Transition Modes (AppScreenMode)
 * =========================================================================================
 *
 * Description:
 * Defines top-level navigation destinations within the single-activity Jetpack Compose layout.
 * Supports smooth Material 3 animated transitions between the core calculator and country browser.
 * =========================================================================================
 */
enum class AppScreenMode {
    CALCULATOR,
    COUNTRY_SELECTOR
}

/**
 * =========================================================================================
 * FEATURE: Main Activity Entry Point (MainActivity)
 * =========================================================================================
 *
 * Description:
 * Top-level Android Activity hosting the Jetpack Compose environment. Responsible for:
 * 1. Uncaught exception crash logging.
 * 2. Hardware haptic feedback engine initialization via [HapticFeedbackManager].
 * 3. Edge-to-edge window insets configuration via [enableEdgeToEdge].
 * 4. Dynamic locale injection via [LocaleContextWrapper] and RTL layout direction.
 * 5. ViewModel lifecycle attachment and Compose hierarchy rendering.
 *
 * Dependencies:
 * - [TippyViewModel]: State management.
 * - [TippyTheme]: Material 3 dynamic styling & Senior Mode typography.
 * - [HapticFeedbackManager]: Physical tactile vibrations.
 * - [LocaleContextWrapper]: Multi-language configuration wrapping.
 * =========================================================================================
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "LocusMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install robust UncaughtExceptionHandler to catch and log any crash during startup
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val threadIdentifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                thread.threadId()
            } else {
                @Suppress("DEPRECATION")
                thread.id
            }
            Log.e(
                TAG,
                "FATAL UNCAUGHT EXCEPTION on thread [${thread.name} (id: $threadIdentifier)]: ${throwable.message}",
                throwable
            )
            defaultHandler?.uncaughtException(thread, throwable)
        }

        super.onCreate(savedInstanceState)
        Log.i(TAG, "Locus MainActivity onCreate started on Android SDK ${Build.VERSION.SDK_INT}")

        // Phase 1: Initialize Haptic Feedback
        try {
            Log.d(TAG, "Phase 1: Initializing HapticFeedbackManager...")
            HapticFeedbackManager.init(this)
            Log.d(TAG, "Phase 1: HapticFeedbackManager initialized.")
        } catch (e: Throwable) {
            Log.e(TAG, "Phase 1 Error: Failed to initialize HapticFeedbackManager: ${e.message}", e)
        }

        // Phase 2: Configure Window Insets & Edge-to-Edge
        try {
            Log.d(TAG, "Phase 2: Enabling Edge-to-Edge display...")
            enableEdgeToEdge()
            Log.d(TAG, "Phase 2: Edge-to-Edge configured.")
        } catch (e: Throwable) {
            Log.w(TAG, "Phase 2 Warning: Failed to enable edge to edge: ${e.message}", e)
        }

        // Phase 3: Mount Jetpack Compose UI Hierarchy
        try {
            Log.d(TAG, "Phase 3: Mounting Compose setContent...")
            setContent {
                val viewModel: TippyViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                // Auto-detect network country ISO and offline language on launch in background
                LaunchedEffect(Unit) {
                    try {
                        viewModel.initTelephonyCountry(context)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error detecting country/language on launch", e)
                    }
                }

                val isDarkTheme = when (uiState.isDarkMode) {
                    true -> true
                    false -> false
                    null -> isSystemInDarkTheme()
                }

                // Create localized context dynamically for the active language
                val localizedContext = remember(uiState.currentLanguage) {
                    try {
                        LocaleContextWrapper.wrap(context, uiState.currentLanguage)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Failed to wrap locale context for ${uiState.currentLanguage}", e)
                        context
                    }
                }

                val layoutDir = if (LanguageResolver.isRtl(uiState.currentLanguage)) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }

                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalLayoutDirection provides layoutDir
                ) {
                    TippyTheme(
                        darkTheme = isDarkTheme,
                        isSeniorMode = uiState.isSeniorMode,
                        dynamicColor = true
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            TippyMainHost(
                                uiState = uiState,
                                viewModel = viewModel,
                                currentDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
            Log.i(TAG, "Phase 3: Locus setContent completed successfully.")
        } catch (e: Throwable) {
            Log.e(TAG, "Phase 3 Fatal Error: Exception during setContent in onCreate: ${e.message}", e)
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: Root Screen Composable Entry Point (TippyMainScreen)
 * =========================================================================================
 *
 * Description:
 * Top-level Composable invoked by [MainActivity.setContent]. Delegates to [TippyMainHost]
 * while providing a decoupled architecture for previews and instrumented tests.
 *
 * @param uiState Reactive snapshot of the entire tip calculator state.
 * @param viewModel Business logic ViewModel driving state mutations.
 * @param currentDarkTheme Boolean indicating active theme mode (dark vs. light).
 * =========================================================================================
 */
@Composable
fun TippyMainScreen(
    uiState: TippyUiState,
    viewModel: TippyViewModel,
    currentDarkTheme: Boolean
) {
    TippyMainHost(
        uiState = uiState,
        viewModel = viewModel,
        currentDarkTheme = currentDarkTheme
    )
}

/**
 * =========================================================================================
 * FEATURE: Main Orchestration Host (TippyMainHost)
 * =========================================================================================
 *
 * Description:
 * Orchestrates screens, bottom sheets, modal overlays, and accessibility features:
 * - Screen Transitions: Calculator view vs. Global Country Selection Explorer.
 * - Modals: "Do you want to save it?" mask, Language selector dialog, Currency search picker.
 * - Bottom Sheets: Tip calculation history sheet with DataStore persistence.
 * - Onboarding: First-time coach marks and 6-tap discovery easter egg.
 * - Accessibility: Senior Mode layout switcher with Text-to-Speech vocal summaries.
 *
 * @param uiState Snapshot of application state.
 * @param viewModel State manager ViewModel.
 * @param currentDarkTheme Boolean indicating active dark theme.
 * =========================================================================================
 */
@Composable
fun TippyMainHost(
    uiState: TippyUiState,
    viewModel: TippyViewModel,
    currentDarkTheme: Boolean
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val view = LocalView.current

    val hapticService = remember(context) { HapticFeedbackService(context) }
    val onboardingPrefs = remember(context) { OnboardingPreferences(context) }
    val tipHistoryStore = remember(context) { TipHistoryDataStore(context) }
    val coroutineScope = rememberCoroutineScope()

    val savePromptMode by tipHistoryStore.savePromptModeFlow.collectAsState(initial = SavePromptMode.ALWAYS)
    val tutorialMode by tipHistoryStore.tutorialModeFlow.collectAsState(initial = "NEVER")
    val historyList by tipHistoryStore.historyFlow.collectAsState(initial = emptyList())

    var screenMode by remember { mutableStateOf(AppScreenMode.CALCULATOR) }
    var logoTapCount by remember { mutableIntStateOf(0) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showCoachMarks by remember { mutableStateOf(!onboardingPrefs.hasSeenCoachmarks()) }

    // If user configured tutorial to show on launch, ensure coach marks open
    LaunchedEffect(tutorialMode) {
        if (tutorialMode == "ALWAYS") {
            showCoachMarks = true
        }
    }

    val triggerHaptic = {
        HapticFeedbackManager.tick(view)
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.select_language_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    item {
                        // Reset to Auto (Telephony & System Locale Offline Lookup)
                        val isAutoSelected = !uiState.isManualLanguageOverride
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(
                                    role = Role.RadioButton,
                                    onClickLabel = "Set automatic system language"
                                ) {
                                    triggerHaptic()
                                    viewModel.setLanguage(context, null)
                                    showLanguageDialog = false
                                }
                                .semantics {
                                    selected = isAutoSelected
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAutoSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.auto_language_option),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.auto_language_sub),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (!uiState.isManualLanguageOverride) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active language mode",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    items(LanguageResolver.TARGET_LANGUAGES.values.toList()) { langInfo ->
                        val isSelected = uiState.isManualLanguageOverride && uiState.currentLanguage == langInfo.code
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(
                                    role = Role.RadioButton,
                                    onClickLabel = "Select language ${langInfo.name}"
                                ) {
                                    triggerHaptic()
                                    viewModel.setLanguage(context, langInfo.code)
                                    showLanguageDialog = false
                                }
                                .semantics {
                                    selected = isSelected
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = langInfo.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.iso_label, langInfo.code.uppercase(), langInfo.dir.uppercase()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected language",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close_button))
                }
            }
        )
    }

    // Intercept back button when country selection view is open
    BackHandler(enabled = screenMode == AppScreenMode.COUNTRY_SELECTOR) {
        HapticFeedbackManager.tick(view)
        screenMode = AppScreenMode.CALCULATOR
    }

    // Material 3 Expressive Container Transition Animation between Calculator and Country Menu
    AnimatedContent(
        targetState = screenMode,
        transitionSpec = {
            if (targetState == AppScreenMode.COUNTRY_SELECTOR) {
                (slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { fullHeight -> fullHeight / 4 }
                ) + fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                 scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                .togetherWith(
                    slideOutVertically(
                        animationSpec = tween(250, easing = FastOutSlowInEasing),
                        targetOffsetY = { fullHeight -> -fullHeight / 8 }
                    ) + fadeOut(animationSpec = tween(200)) +
                    scaleOut(targetScale = 0.95f, animationSpec = tween(250))
                )
            } else {
                (slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { fullHeight -> -fullHeight / 6 }
                ) + fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                 scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                .togetherWith(
                    slideOutVertically(
                        animationSpec = tween(250, easing = FastOutSlowInEasing),
                        targetOffsetY = { fullHeight -> fullHeight / 4 }
                    ) + fadeOut(animationSpec = tween(200)) +
                    scaleOut(targetScale = 0.92f, animationSpec = tween(250))
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        label = "SharedElementScreenTransition"
    ) { currentMode ->
        when (currentMode) {
            AppScreenMode.CALCULATOR -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(top = 48.dp, bottom = 90.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Expressive App Header Row with Centralized Settings
                        ExpressiveHeader(
                            uiState = uiState,
                            logoTapCount = logoTapCount,
                            onLogoClick = {
                                logoTapCount++
                                if (logoTapCount >= 6) {
                                    logoTapCount = 0
                                    HapticFeedbackManager.onEasterEggUnlocked(view)
                                    screenMode = AppScreenMode.COUNTRY_SELECTOR
                                    Toast.makeText(context, "🎉 Secret Discovered! Global Country Selector Unlocked", Toast.LENGTH_SHORT).show()
                                } else {
                                    HapticFeedbackManager.onLogoTap(view)
                                    if (logoTapCount >= 2) {
                                        Toast.makeText(context, "${6 - logoTapCount} taps to unlock Global Explorer", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onOpenSettings = {
                                triggerHaptic()
                                showSettingsSheet = true
                            },
                            onToggleSeniorMode = {
                                triggerHaptic()
                                viewModel.toggleSeniorMode()
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch between Expressive standard mode and Senior High-Contrast Mode with smooth animated expansion
                        AnimatedContent(
                            targetState = uiState.isSeniorMode,
                            transitionSpec = {
                                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + 
                                 scaleIn(initialScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                    .togetherWith(
                                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + 
                                        scaleOut(targetScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                    )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                            label = "SeniorModeContentTransition"
                        ) { isSenior ->
                            if (isSenior) {
                                SeniorTippyLayout(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    hapticService = hapticService,
                                    triggerHaptic = triggerHaptic,
                                    focusManager = focusManager,
                                    keyboardController = keyboardController
                                )
                            } else {
                                ExpressiveTippyLayout(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    hapticService = hapticService,
                                    triggerHaptic = triggerHaptic,
                                    focusManager = focusManager,
                                    keyboardController = keyboardController
                                )
                            }
                        }
                    }
                }
            }

            AppScreenMode.COUNTRY_SELECTOR -> {
                CountrySelectionView(
                    allCountries = uiState.allCountries,
                    currentCountry = uiState.activeCountry,
                    currentLanguage = uiState.currentLanguage,
                    onBack = {
                        HapticFeedbackManager.tick(view)
                        screenMode = AppScreenMode.CALCULATOR
                    },
                    onSelectCountry = { selected ->
                        viewModel.selectCountry(selected)
                        HapticFeedbackManager.confirm(view)
                        screenMode = AppScreenMode.CALCULATOR
                        val localizedName = selected.getLocalizedCountry(uiState.currentLanguage)
                        Toast.makeText(context, context.getString(R.string.selected_toast, localizedName, selected.iso), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Tip Calculation History Bottom Sheet
    TipHistorySheet(
        visible = showHistorySheet,
        historyList = historyList,
        isSeniorMode = uiState.isSeniorMode,
        currentLanguage = uiState.currentLanguage,
        onLoadTransaction = { tx ->
            viewModel.updateBillAmount(tx.billAmount.toString())
            showHistorySheet = false
            Toast.makeText(context, "Loaded calculation for ${tx.countryIso}", Toast.LENGTH_SHORT).show()
        },
        onDeleteTransaction = { id ->
            coroutineScope.launch {
                tipHistoryStore.deleteTransaction(id)
            }
        },
        onClearAll = {
            coroutineScope.launch {
                tipHistoryStore.clearAllHistory()
            }
        },
        onDismiss = { showHistorySheet = false }
    )

    // Centralized Settings Bottom Sheet (Theme, Tip History, Language & Preferences)
    SettingsBottomSheet(
        visible = showSettingsSheet,
        currentDarkTheme = currentDarkTheme,
        themeMode = uiState.isDarkMode,
        isSeniorMode = uiState.isSeniorMode,
        currentLanguage = uiState.currentLanguage,
        isManualLanguageOverride = uiState.isManualLanguageOverride,
        savePromptMode = savePromptMode,
        onDismiss = { showSettingsSheet = false },
        onSelectThemeMode = { mode ->
            triggerHaptic()
            viewModel.setDarkMode(mode)
        },
        onToggleTheme = {
            triggerHaptic()
            viewModel.setDarkMode(!currentDarkTheme)
        },
        onOpenHistory = {
            showSettingsSheet = false
            triggerHaptic()
            showHistorySheet = true
        },
        onOpenLanguage = {
            showSettingsSheet = false
            triggerHaptic()
            showLanguageDialog = true
        },
        onToggleSeniorMode = {
            triggerHaptic()
            viewModel.toggleSeniorMode()
        },
        onRestartTutorial = {
            showSettingsSheet = false
            triggerHaptic()
            showCoachMarks = true
        },
        onSavePromptModeChange = { newMode ->
            coroutineScope.launch {
                tipHistoryStore.setSavePromptMode(newMode)
            }
        }
    )

    // First-Time User Coach Mark Overlay (explains Senior Mode, Quick-Add Buttons, and History/Preferences)
    CoachMarkOverlay(
        visible = showCoachMarks,
        savePromptMode = savePromptMode,
        onSavePromptModeChange = { newMode ->
            coroutineScope.launch {
                tipHistoryStore.setSavePromptMode(newMode)
            }
        },
        tutorialMode = tutorialMode,
        onTutorialModeChange = { newMode ->
            coroutineScope.launch {
                tipHistoryStore.setTutorialMode(newMode)
            }
        },
        onDismiss = {
            showCoachMarks = false
            if (tutorialMode != "ALWAYS") {
                onboardingPrefs.markCoachmarksSeen()
            }
        }
    )
}

/**
 * =========================================================================================
 * FEATURE: Expressive Application Header & Quick Mode Controls (ExpressiveHeader)
 * =========================================================================================
 *
 * Description:
 * Top app bar displaying:
 * - Branded passport logo and title with 6-tap discovery easter egg detector.
 * - Active Country ISO Code pill with live indicator dot.
 * - Senior Mode accessibility quick toggle button.
 * - Centralized Settings toggle button opening dedicated settings sheet (Theme, History, Language).
 *
 * @param uiState Reactive application state snapshot.
 * @param logoTapCount Current count of taps toward unlocking Global Explorer (0..6).
 * @param onLogoClick Callback invoked on app logo tap.
 * @param onOpenSettings Callback invoked to open the Settings bottom sheet.
 * @param onToggleSeniorMode Callback invoked to toggle senior accessibility mode.
 * @param currentDarkTheme Optional boolean indicating active theme mode for backward compatibility.
 * @param onOpenLanguage Optional callback invoked to open language selector modal.
 * @param onOpenHistory Optional callback invoked to open history sheet.
 * @param onToggleTheme Optional callback invoked to switch between light and dark themes.
 * =========================================================================================
 */
@Composable
private fun ExpressiveHeader(
    uiState: TippyUiState,
    logoTapCount: Int,
    onLogoClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSeniorMode: () -> Unit,
    currentDarkTheme: Boolean = false,
    onOpenLanguage: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Brand Title with Clickable App Logo (6-tap secret trigger)
        val isDark = when (uiState.isDarkMode) {
            true -> true
            false -> false
            null -> isSystemInDarkTheme()
        }
        val appLogoTint = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "Locus"
                }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onLogoClick() }
                    .testTag("app_logo_tap_target"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.passport_24),
                    contentDescription = stringResource(R.string.passport_logo_desc),
                    tint = appLogoTint,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black
                    )
                    // Material 3 Expressive Pill with active country ISO Code
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.testTag("header_iso_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = uiState.activeCountry.iso.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.header_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Clean Quick Controls: Senior Mode Accessibility & Consolidated Settings Toggle
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Senior Mode Accessibility Toggle Button
            IconButton(
                onClick = onToggleSeniorMode,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (uiState.isSeniorMode) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .testTag("senior_mode_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Accessibility,
                    contentDescription = stringResource(R.string.toggle_senior_mode),
                    tint = if (uiState.isSeniorMode) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Centralized Settings Toggle Button (Theme, Tip History, Language, Preferences)
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: Standard Expressive Material 3 Layout (ExpressiveTippyLayout)
 * =========================================================================================
 *
 * Description:
 * Primary calculator screen composition in standard mode:
 * - Hero Calculation Card: Large live total readout with round up/down actions.
 * - Cultural Advice Pill & Country Indicator: Regional tipping norms and advice modal.
 * - Currency Quick-Select Strip: Instant currency switching.
 * - Bill Subtotal Input Card: Numeric keyboard input with currency-specific quick add chips.
 * - Service Category Carousel: Horizontal selector for Restaurants, Delivery, Bar, etc.
 * - Tip Percentage Grid & Slider: Tactile notch slider and preset rate buttons.
 * - Party Split Card: Increment and decrement diner count controls.
 * - Bottom Bar: Reset and Save to History action triggers.
 *
 * @param uiState Reactive application state snapshot.
 * @param viewModel State manager ViewModel driving calculations.
 * @param hapticService Calibrated haptic vibration provider.
 * @param triggerHaptic Lambda to execute tactile feedback.
 * @param focusManager Focus manager for keyboard management.
 * @param keyboardController Software keyboard controller.
 * =========================================================================================
 */
@Composable
private fun ExpressiveTippyLayout(
    uiState: TippyUiState,
    viewModel: TippyViewModel,
    hapticService: HapticFeedbackService,
    triggerHaptic: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
    ) {
        // 1. HERO CALCULATION CARD (High Polish Visuals + animateContentSize)
        HeroCalculationCard(
            uiState = uiState,
            onRoundUp = {
                triggerHaptic()
                viewModel.roundUpTotal()
            },
            onRoundDown = {
                triggerHaptic()
                viewModel.roundDownTotal()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. CURRENCY SELECTOR STRIP
        CurrencyPillsStrip(
            uiState = uiState,
            onSelectCurrency = { currency ->
                triggerHaptic()
                viewModel.selectCurrency(currency)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. BILL INPUT CARD WITH NUMPAD SHORTCUTS
        BillAmountSection(
            uiState = uiState,
            onValueChange = {
                if (it != uiState.billAmountInput) {
                    HapticFeedbackManager.tick(null)
                }
                viewModel.updateBillAmount(it)
            },
            onClear = {
                triggerHaptic()
                viewModel.updateBillAmount("")
            },
            onQuickAdd = { amount ->
                triggerHaptic()
                viewModel.addToBill(amount)
            },
            focusManager = focusManager,
            keyboardController = keyboardController
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 4. SERVICE CATEGORY SELECTOR (Tactile Cards with Country Standard Rates)
        ServiceSelectorSection(
            uiState = uiState,
            onSelectService = { service ->
                triggerHaptic()
                viewModel.selectServiceType(service)
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 5. TIP PERCENTAGE & SLIDER (With System Volume Slider Ticks)
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

        Spacer(modifier = Modifier.height(18.dp))

        // 6. SPLIT BILL SECTION
        SplitBillSection(
            uiState = uiState,
            onIncrement = {
                triggerHaptic()
                viewModel.incrementSplit()
            },
            onDecrement = {
                triggerHaptic()
                viewModel.decrementSplit()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 7. RESET ACTION BUTTON
        val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)
        Button(
            onClick = {
                triggerHaptic()
                viewModel.clearAll()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isLongText) 64.dp else 58.dp)
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
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
}

/**
 * =========================================================================================
 * FEATURE: Hero Totals & Split Readout Display (HeroCalculationCard)
 * =========================================================================================
 *
 * Description:
 * Primary visual focal point displaying real-time financial calculations:
 * - Total Bill Amount formatted with active currency symbol and locale separators.
 * - Tip Breakdown: Decimal amount and active percentage.
 * - Split Bill Share: Per-person amount when splitting across 2+ diners.
 * - Quick Action Buttons: "Round Up" and "Round Down" whole-integer buttons.
 *
 * @param uiState Reactive application state snapshot.
 * @param onRoundUp Callback to round total bill up to the next integer.
 * @param onRoundDown Callback to round total bill down to the nearest integer.
 * =========================================================================================
 */
@Composable
private fun HeroCalculationCard(
    uiState: TippyUiState,
    onRoundUp: () -> Unit,
    onRoundDown: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
            .semantics(mergeDescendants = true) {
                contentDescription = "Total calculation"
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // Top Metric Row: Total Bill Display
                Text(
                    text = stringResource(R.string.total_bill_label).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = uiState.totalBill,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.95f)).togetherWith(fadeOut() + scaleOut(targetScale = 1.05f))
                    },
                    label = "TotalBillAnimation"
                ) { total ->
                    val formatted = LocaleFormatter.formatCurrency(total, uiState.selectedCurrency, uiState.currentLanguage)
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("hero_total_bill_text")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown Sub-Card: Tip Amount & Split Share (50/50 Balanced Layout)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tip Amount Column (Balanced 50%)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.tip_amount_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedTip = LocaleFormatter.formatCurrency(uiState.tipAmount, uiState.selectedCurrency, uiState.currentLanguage)
                        Text(
                            text = formattedTip,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("hero_tip_amount_text")
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Share Per Person Column (Balanced 50%)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.each_pays_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedShare = LocaleFormatter.formatCurrency(uiState.sharePerPerson, uiState.selectedCurrency, uiState.currentLanguage)
                        Text(
                            text = formattedShare,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("hero_share_amount_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Expressive Quick Rounding Controls
                val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRoundUp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLongText) 58.dp else 46.dp)
                            .testTag("round_up_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.round_up),
                                style = if (isLongText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = onRoundDown,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLongText) 58.dp else 46.dp)
                            .testTag("round_down_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.round_down),
                                style = if (isLongText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Expressive Share Bill Summary Button
                val context = LocalContext.current
                OutlinedButton(
                    onClick = {
                        BillShareHelper.shareBillSummary(context, uiState)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isLongText) 54.dp else 46.dp)
                        .testTag("share_summary_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.share_bill_summary),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Center,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

/**
 * =========================================================================================
 * FEATURE: Searchable Currency Picker Modal Dialog (CurrencyPickerDialog)
 * =========================================================================================
 *
 * Description:
 * Modal selection dialog allowing users to search and select from 140+ global currencies.
 * Supports filtering by three-letter ISO code, graphical symbol, English name, or localized name.
 *
 * @param currentCurrency The currently active [CurrencyOption].
 * @param currentLanguage Active 2-letter ISO language code.
 * @param onSelectCurrency Callback invoked when the user selects a currency.
 * @param onDismissRequest Callback invoked to dismiss the dialog without changes.
 * =========================================================================================
 */
@Composable
private fun CurrencyPickerDialog(
    currentCurrency: CurrencyOption,
    currentLanguage: String = "en",
    onSelectCurrency: (CurrencyOption) -> Unit,
    onDismissRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val allCurrencies = remember { CurrencyOption.values().toList() }
    val mappingsByCode = remember {
        CurrencyOption.ALL_MAPPINGS.groupBy { it.currencyCode }
    }

    val filteredCurrencies = remember(searchQuery, currentLanguage) {
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) {
            listOf(currentCurrency) + (allCurrencies - currentCurrency).sortedBy { it.code }
        } else {
            allCurrencies.filter { option ->
                val localizedLabel = option.getLocalizedLabel(currentLanguage)
                val codeMatch = option.code.lowercase(Locale.US).contains(q)
                val labelMatch = option.label.lowercase(Locale.US).contains(q) || localizedLabel.lowercase(Locale.US).contains(q)
                val symbolMatch = option.symbol.lowercase(Locale.US).contains(q)
                val countries = mappingsByCode[option.code]?.joinToString(" ") {
                    val locCountry = GlobalExplorerLocalization.getLocalizedCountry(it.alpha2, currentLanguage, it.country)
                    "${it.country} $locCountry ${it.alpha2} ${it.alpha3}"
                }?.lowercase(Locale.US) ?: ""
                val countryMatch = countries.contains(q)
                codeMatch || labelMatch || symbolMatch || countryMatch
            }.sortedBy { it.code }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.global_currencies_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.currencies_count, filteredCurrencies.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("currency_search_input"),
                    placeholder = { Text(stringResource(R.string.search_currency_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        },
        text = {
            if (filteredCurrencies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_currency_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCurrencies, key = { it.code }) { currency ->
                        val isSelected = currency == currentCurrency
                        val localizedLabel = currency.getLocalizedLabel(currentLanguage)
                        val mappings = mappingsByCode[currency.code] ?: emptyList()
                        val countriesPreview = mappings.joinToString(", ") {
                            GlobalExplorerLocalization.getLocalizedCountry(it.alpha2, currentLanguage, it.country)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    role = Role.RadioButton,
                                    onClickLabel = "Select currency ${currency.code} $localizedLabel"
                                ) {
                                    onSelectCurrency(currency)
                                }
                                .semantics {
                                    selected = isSelected
                                }
                                .testTag("currency_item_${currency.code}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surface
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currency.symbol,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = currency.code,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "• $localizedLabel",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (countriesPreview.isNotEmpty()) {
                                            Text(
                                                text = countriesPreview,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.close_button))
            }
        }
    )
}

/**
 * =========================================================================================
 * FEATURE: Quick Currency Selector Pill Strip (CurrencyPillsStrip)
 * =========================================================================================
 *
 * Description:
 * Compact horizontal pill strip showcasing the nation's primary currency alongside major
 * global currencies (USD, EUR, GBP, JPY) with a "+ More" trigger opening the complete picker dialog.
 *
 * @param uiState Reactive application state snapshot.
 * @param onSelectCurrency Callback invoked when a currency pill is clicked.
 * =========================================================================================
 */
@Composable
private fun CurrencyPillsStrip(
    uiState: TippyUiState,
    onSelectCurrency: (CurrencyOption) -> Unit
) {
    var showAllCurrenciesDialog by remember { mutableStateOf(false) }

    if (showAllCurrenciesDialog) {
        CurrencyPickerDialog(
            currentCurrency = uiState.selectedCurrency,
            currentLanguage = uiState.currentLanguage,
            onSelectCurrency = { currency ->
                onSelectCurrency(currency)
                showAllCurrenciesDialog = false
            },
            onDismissRequest = { showAllCurrenciesDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.select_currency),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            TextButton(
                onClick = { showAllCurrenciesDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.testTag("all_currencies_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.all_currencies, CurrencyOption.values().size),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Popular Currency Carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val featuredCurrencies = listOf(
                uiState.selectedCurrency,
                CurrencyOption.USD,
                CurrencyOption.EUR,
                CurrencyOption.GBP,
                CurrencyOption.JPY,
                CurrencyOption.CAD,
                CurrencyOption.AUD,
                CurrencyOption.CHF,
                CurrencyOption.BRL,
                CurrencyOption.MXN,
                CurrencyOption.INR
            ).distinct()

            items(featuredCurrencies) { currency ->
                val isSelected = uiState.selectedCurrency == currency
                val localizedLabel = currency.getLocalizedLabel(uiState.currentLanguage)
                val currencyDesc = "${currency.code}, $localizedLabel (${currency.symbol})${if (isSelected) ", selected" else ""}"
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCurrency(currency) },
                    label = {
                        Text(
                            text = "${currency.code} (${currency.symbol})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            softWrap = true,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .testTag("currency_${currency.code}")
                        .semantics {
                            contentDescription = currencyDesc
                        }
                )
            }

            item {
                val moreCurrenciesDesc = stringResource(R.string.all_currencies, CurrencyOption.values().size)
                FilterChip(
                    selected = false,
                    onClick = { showAllCurrenciesDialog = true },
                    label = {
                        Text(
                            text = stringResource(R.string.more_currencies),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            softWrap = true,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .testTag("more_currencies_chip")
                        .semantics {
                            contentDescription = moreCurrenciesDesc
                        }
                )
            }
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: Bill Subtotal Input & Quick-Add Controller (BillAmountSection)
 * =========================================================================================
 *
 * Description:
 * Primary financial input card allowing users to type bill amounts with decimal precision,
 * clear inputs, or tap denomination chips (+1, +5, +10, +20 scaled to local currency tier).
 *
 * @param uiState Reactive application state snapshot.
 * @param onValueChange Callback invoked when the user modifies the raw bill text.
 * @param onClear Callback invoked when the user clears the bill input.
 * @param onQuickAdd Callback invoked when the user taps an additive currency chip.
 * @param focusManager Focus manager for keyboard management.
 * @param keyboardController Software keyboard controller.
 * =========================================================================================
 */
@Composable
private fun BillAmountSection(
    uiState: TippyUiState,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onQuickAdd: (Double) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    val currencyTier = remember(uiState.selectedCurrency.code) {
        CurrencyQuickBillRegistry.getTierForCurrency(uiState.selectedCurrency.code)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.bill_amount).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Tactile Outlined Text Field
            OutlinedTextField(
                value = uiState.billAmountInput,
                onValueChange = onValueChange,
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
                                modifier = Modifier.testTag("clear_button_tag")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_button),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Quick-Add Chips based on currency magnitude tier
            val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_add_buttons"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currencyTier.quickAddAmounts.forEachIndexed { index, amount ->
                    val label = currencyTier.displayLabels.getOrElse(index) { "+${amount.toInt()}" }
                    Button(
                        onClick = { onQuickAdd(amount) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLongText) 50.dp else 44.dp)
                            .testTag("quick_add_${amount.toInt()}")
                            .semantics {
                                contentDescription = "Add $label to bill"
                            },
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
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: Service Category Horizontal Carousel (ServiceSelectorSection)
 * =========================================================================================
 *
 * Description:
 * Horizontally scrollable row of service category cards (Restaurant, Delivery, Takeout,
 * Foodtruck, Groceries, Bar) displaying icons, category titles, and localized standard tip rates.
 *
 * @param uiState Reactive application state snapshot.
 * @param onSelectService Callback invoked when the user selects a service category.
 * =========================================================================================
 */
@Composable
private fun ServiceSelectorSection(
    uiState: TippyUiState,
    onSelectService: (ServiceType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
    ) {
        Text(
            text = stringResource(R.string.service_category).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            maxLines = 1,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )

        val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(ServiceType.values()) { service ->
                val isSelected = uiState.selectedServiceType == service
                val rule = uiState.activeCountry.getRuleFor(service.datasetKey)

                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )

                val serviceLabel = stringResource(service.nameRes)
                Card(
                    modifier = Modifier
                        .width(if (isLongText) 126.dp else 120.dp)
                        .height(if (isLongText) 154.dp else 140.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(
                            role = Role.RadioButton,
                            onClickLabel = "Select $serviceLabel"
                        ) { onSelectService(service) }
                        .semantics {
                            selected = isSelected
                        }
                        .testTag("service_${service.id}"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
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
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(service.nameRes),
                                style = MaterialTheme.typography.labelMedium,
                                color = contentColor,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = true
                            )
                        }

                        Text(
                            text = rule.standardTip,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) contentColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: Tip Percentage Selection Grid & Custom Slider (TipPercentageSection)
 * =========================================================================================
 *
 * Description:
 * Interactive tip rate controller offering both quick preset rate buttons (Standard, Generous,
 * Legend, Angel Tip) and a continuous custom slider (0% to 50%) calibrated with tactile notch
 * haptic feedback matching system volume sliders.
 *
 * @param uiState Reactive application state snapshot.
 * @param hapticService Calibrated haptic feedback engine.
 * @param onSelectRate Callback when preset percentage is tapped.
 * @param onSliderChange Callback when custom slider value changes.
 * =========================================================================================
 */
@Composable
private fun TipPercentageSection(
    uiState: TippyUiState,
    hapticService: HapticFeedbackService,
    onSelectRate: (Double) -> Unit,
    onSliderChange: (Double) -> Unit
) {
    val currentRule = uiState.currentRule
    val standardRate = uiState.standardPercentage
    val generousRates = uiState.generousPercentages
    val view = LocalView.current

    var lastSliderTick by remember { mutableIntStateOf((uiState.selectedPercent * 100).toInt()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.tipping_tiers_header, uiState.activeCountry.getLocalizedCountry(uiState.currentLanguage).uppercase()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                // Active Percentage Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = LocaleFormatter.formatPercent(uiState.selectedPercent, uiState.currentLanguage),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cultural Advice Callout Box
            val context = LocalContext.current
            val localizedNotes = currentRule.getLocalizedNotes(context)
            if (localizedNotes.isNotEmpty()) {
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
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Presets Row (Standard + Generous Tiers matching active country - Equal 1/3 symmetric distribution)
            val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tier 1: Standard
                val isStandardSelected = !uiState.isCustomPercentActive &&
                        (Math.abs(uiState.selectedPercent - standardRate) < 0.001)

                RateOptionButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.standard_tip),
                    subLabel = currentRule.standardTip,
                    isSelected = isStandardSelected,
                    isLongText = isLongText,
                    onClick = { onSelectRate(standardRate) }
                )

                // Tier 2: Generous
                val genRate = generousRates.getOrNull(0) ?: (standardRate + 0.05)
                val isGenSelected = !uiState.isCustomPercentActive &&
                        (Math.abs(uiState.selectedPercent - genRate) < 0.001)

                RateOptionButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.generous_tip),
                    subLabel = currentRule.generousTip,
                    isSelected = isGenSelected,
                    isLongText = isLongText,
                    onClick = { onSelectRate(genRate) }
                )

                // Tier 3: Legend / MVP
                val legRate = generousRates.getOrNull(1) ?: (standardRate + 0.10)
                val isLegSelected = !uiState.isCustomPercentActive &&
                        (Math.abs(uiState.selectedPercent - legRate) < 0.001)

                RateOptionButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.legend_tip),
                    subLabel = currentRule.legendTip,
                    isSelected = isLegSelected,
                    isLongText = isLongText,
                    onClick = { onSelectRate(legRate) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Smooth Material 3 Slider with System Volume Slider Vibrations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.custom_slider),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = LocaleFormatter.formatIntegerPercent(uiState.selectedPercent, uiState.currentLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Slider(
                value = (uiState.selectedPercent * 100).toFloat().coerceIn(0f, 50f),
                onValueChange = { newValue ->
                    val currentTick = newValue.toInt()
                    if (currentTick != lastSliderTick) {
                        lastSliderTick = currentTick
                        HapticFeedbackManager.tick(view)
                    }
                    onSliderChange(newValue.toDouble())
                },
                valueRange = 0f..50f,
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tip_slider_control")
            )
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: Tip Percentage Preset Chip Button (RateOptionButton)
 * =========================================================================================
 *
 * Description:
 * Animated chip button displaying a percentage rate tier (e.g. Standard 15%, Generous 20%)
 * with smooth container color animation and haptic response.
 *
 * @param modifier Optional layout modifier.
 * @param label Primary percentage text (e.g. "18%").
 * @param subLabel Descriptive tier label (e.g. "Standard", "Generous").
 * @param isSelected Whether this rate is currently active.
 * @param isLongText Whether active language requires multi-line wrapping and expanded height.
 * @param onClick Click event handler.
 * =========================================================================================
 */
@Composable
private fun RateOptionButton(
    modifier: Modifier = Modifier,
    label: String,
    subLabel: String,
    isSelected: Boolean,
    isLongText: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Column(
        modifier = modifier
            .heightIn(min = if (isLongText) 76.dp else 64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                role = Role.RadioButton,
                onClickLabel = "Select $label $subLabel tip"
            ) { onClick() }
            .semantics {
                selected = isSelected
            }
            .padding(vertical = if (isLongText) 8.dp else 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subLabel,
            style = if (subLabel.length > 6) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            color = contentColor,
            fontWeight = FontWeight.Black,
            maxLines = 2,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * =========================================================================================
 * FEATURE: Standard Bill Split Controller (SplitBillSection)
 * =========================================================================================
 *
 * Description:
 * Card container allowing users to split the total bill across 1 to 99 diners with real-time
 * share recalculation and haptic decrement/increment step buttons.
 *
 * @param uiState Reactive application state snapshot.
 * @param onIncrement Callback to add another person to the bill split.
 * @param onDecrement Callback to reduce party count (minimum 1).
 * =========================================================================================
 */
@Composable
private fun SplitBillSection(
    uiState: TippyUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.split_bill).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (uiState.splitCount == 1) stringResource(R.string.solo_person)
                    else stringResource(R.string.people_sharing, uiState.splitCount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Tactile Increment/Decrement Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    enabled = uiState.splitCount > 1,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.splitCount > 1) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("split_decrement_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(R.string.decrease_people),
                        tint = if (uiState.splitCount > 1) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                Text(
                    text = "${uiState.splitCount}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .testTag("split_count_display")
                )

                IconButton(
                    onClick = onIncrement,
                    enabled = uiState.splitCount < 99,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("split_increment_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.increase_people),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: Senior Accessibility High-Contrast Layout (SeniorTippyLayout)
 * =========================================================================================
 *
 * Description:
 * Complete accessible UI layout replacing standard controls when Senior Mode is activated:
 * - High Contrast: Stark black and white color scheme meeting AAA accessibility standards.
 * - Text-to-Speech: Vocal button reading calculated totals aloud with localized TTS voice.
 * - Massive Touch Targets: Enlarged buttons, sliders, and input chips (minimum 56dp).
 * - Extra Large Typography: Bold, high-visibility headline and body text scales.
 *
 * @param uiState Reactive application state snapshot.
 * @param viewModel State manager ViewModel driving mutations.
 * @param hapticService Calibrated haptic vibration provider.
 * @param triggerHaptic Lambda to actuate haptic feedback on touch.
 * @param focusManager Focus manager for hiding keyboard.
 * @param keyboardController Software keyboard controller.
 * =========================================================================================
 */
@Composable
private fun SeniorTippyLayout(
    uiState: TippyUiState,
    viewModel: TippyViewModel,
    hapticService: HapticFeedbackService,
    triggerHaptic: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    val context = LocalContext.current
    val ttsManager = rememberSeniorTtsManager()
    val isSpeaking by ttsManager.isSpeaking.collectAsState()
    val isLongText = LanguageResolver.isLongTextLanguage(uiState.currentLanguage)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. SENIOR HERO CALCULATION CARD (High prominence, high contrast with TTS Audio readout)
        val tipFormatted = LocaleFormatter.formatCurrency(uiState.tipAmount, uiState.selectedCurrency, uiState.currentLanguage)
        val totalFormatted = LocaleFormatter.formatCurrency(uiState.totalBill, uiState.selectedCurrency, uiState.currentLanguage)
        val percentFormatted = LocaleFormatter.formatIntegerPercent(uiState.selectedPercent, uiState.currentLanguage)
        val ttsHint = stringResource(R.string.tts_tap_to_read)

        val spokenReadout = buildString {
            append(context.getString(R.string.calculated_totals))
            append(". ")
            append(context.getString(R.string.calculated_tip))
            append(": ")
            append(tipFormatted)
            append(" (")
            append(percentFormatted)
            append("). ")
            append(context.getString(R.string.calculated_total))
            append(": ")
            append(totalFormatted)
            append(".")
            if (uiState.splitCount > 1) {
                val shareFormatted = LocaleFormatter.formatCurrency(uiState.sharePerPerson, uiState.selectedCurrency, uiState.currentLanguage)
                append(" ")
                append(context.getString(R.string.split_share))
                append(": ")
                append(shareFormatted)
                append(".")
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                .testTag("senior_totals_card")
                .clickable(
                    onClickLabel = ttsHint,
                    role = Role.Button
                ) {
                    triggerHaptic()
                    ttsManager.speak(spokenReadout, uiState.currentLanguage)
                }
                .semantics(mergeDescendants = true) {
                    contentDescription = "$spokenReadout $ttsHint"
                },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row with Total Bill label and high-contrast TTS audio pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.total_bill_label).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )

                    // Accessible TTS Audio Indicator Pill
                    Surface(
                        color = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("senior_tts_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = ttsHint,
                                tint = if (isSpeaking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isSpeaking) stringResource(R.string.tts_speaking_now) else stringResource(R.string.tts_tap_to_speak_label),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isSpeaking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                AnimatedContent(
                    targetState = uiState.totalBill,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.95f)).togetherWith(fadeOut() + scaleOut(targetScale = 1.05f))
                    },
                    label = "SeniorTotalBillAnimation"
                ) { total ->
                    val formatted = LocaleFormatter.formatCurrency(total, uiState.selectedCurrency, uiState.currentLanguage)
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("hero_total_bill_text")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown Sub-Card: Tip Amount & Split Share (High Contrast 50/50 Balanced Layout)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tip Amount Column (50%)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.tip_amount_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tipFormatted,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("hero_tip_amount_text")
                        )
                    }

                    // High Contrast Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(2.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )

                    // Share Per Person Column (50%)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.each_pays_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val formattedShare = LocaleFormatter.formatCurrency(uiState.sharePerPerson, uiState.selectedCurrency, uiState.currentLanguage)
                        Text(
                            text = formattedShare,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("hero_share_amount_text")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tactile Rounding Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            triggerHaptic()
                            viewModel.roundUpTotal()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLongText) 62.dp else 50.dp)
                            .testTag("round_up_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.round_up),
                                style = if (isLongText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = {
                            triggerHaptic()
                            viewModel.roundDownTotal()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isLongText) 62.dp else 50.dp)
                            .testTag("round_down_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.round_down),
                                style = if (isLongText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Senior Share Summary Button
                OutlinedButton(
                    onClick = {
                        triggerHaptic()
                        BillShareHelper.shareBillSummary(context, uiState)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isLongText) 56.dp else 48.dp)
                        .testTag("senior_share_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.share_bill_summary),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Center,
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. BILL INPUT CARD WITH NUMPAD SHORTCUTS (Senior Mode)
        val currencyTier = remember(uiState.selectedCurrency.code) {
            CurrencyQuickBillRegistry.getTierForCurrency(uiState.selectedCurrency.code)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.bill_amount).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // High-Contrast Senior Outlined Text Field
                OutlinedTextField(
                    value = uiState.billAmountInput,
                    onValueChange = {
                        if (it != uiState.billAmountInput) {
                            HapticFeedbackManager.tick(null)
                        }
                        viewModel.updateBillAmount(it)
                    },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
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
                                    onClick = {
                                        triggerHaptic()
                                        viewModel.updateBillAmount("")
                                    },
                                    modifier = Modifier.testTag("clear_button_tag")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_button),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("senior_bill_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Quick-Add Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_add_buttons"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencyTier.quickAddAmounts.forEachIndexed { index, amount ->
                        val label = currencyTier.displayLabels.getOrElse(index) { "+${amount.toInt()}" }
                        Button(
                            onClick = {
                                triggerHaptic()
                                viewModel.addToBill(amount)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isLongText) 52.dp else 46.dp)
                                .testTag("quick_add_${amount.toInt()}")
                                .semantics {
                                    contentDescription = "Add $label to bill"
                                },
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
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. SERVICE CATEGORY SELECTOR (Senior Mode)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
        ) {
            Text(
                text = stringResource(R.string.service_category).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(ServiceType.values()) { service ->
                    val isSelected = uiState.selectedServiceType == service
                    val rule = uiState.activeCountry.getRuleFor(service.datasetKey)
                    val standardRate = rule?.parseStandardRate() ?: service.defaultStandardRate
                    val rateText = if (standardRate > 0) "${(standardRate * 100).toInt()}%" else (rule?.standardTip?.takeIf { it.isNotBlank() } ?: "${(service.defaultStandardRate * 100).toInt()}%")

                    val serviceLabel = stringResource(service.nameRes)
                    Card(
                        modifier = Modifier
                            .width(if (isLongText) 130.dp else 120.dp)
                            .height(if (isLongText) 160.dp else 140.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(
                                role = Role.RadioButton,
                                onClickLabel = "Select $serviceLabel"
                            ) {
                                triggerHaptic()
                                viewModel.selectServiceType(service)
                            }
                            .semantics {
                                selected = isSelected
                            }
                            .testTag("senior_service_${service.id}"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 3.dp else 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = service.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = serviceLabel,
                                style = if (isLongText) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                lineHeight = 15.sp,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = rateText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Cultural Advice Note for Senior Mode (Fully localized across all supported languages)
            val currentRule = uiState.currentRule
            val localizedNotes = currentRule.getLocalizedNotes(context)
            val culturalNote = localizedNotes.takeIf { it.isNotBlank() } ?: stringResource(R.string.note_default_guideline)
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("senior_cultural_note_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.culture_advice),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = culturalNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("senior_cultural_note_text")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. SPLIT BILL SECTION (Simplified Senior Mode with Big Stepper)
        SeniorSplitBillSection(
            uiState = uiState,
            onIncrement = {
                triggerHaptic()
                viewModel.incrementSplit()
            },
            onDecrement = {
                triggerHaptic()
                viewModel.decrementSplit()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. RESET ACTION BUTTON (Senior Mode)
        Button(
            onClick = {
                triggerHaptic()
                viewModel.clearAll()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isLongText) 66.dp else 58.dp)
                .testTag("clear_button_reset"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
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
}

/**
 * =========================================================================================
 * FEATURE: Accessible Senior Mode Bill Split Section (SeniorSplitBillSection)
 * =========================================================================================
 *
 * Description:
 * High-contrast, large-button party size controller designed specifically for Senior Mode.
 * Features oversized decrement and increment buttons (>=56dp) with high-contrast borders
 * and clear visual accessibility states.
 *
 * @param uiState Reactive snapshot of calculation state including party split count.
 * @param onIncrement Callback to increase party size count.
 * @param onDecrement Callback to decrease party size count.
 * =========================================================================================
 */
@Composable
private fun SeniorSplitBillSection(
    uiState: TippyUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.split_bill).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        enabled = uiState.splitCount > 1,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(R.string.decrease_people),
                            tint = if (uiState.splitCount > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val countText = if (uiState.splitCount == 1) "1 Person" else "${uiState.splitCount} People"
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    if (uiState.splitCount > 1) {
                        val shareFormatted = LocaleFormatter.formatCurrency(uiState.sharePerPerson, uiState.selectedCurrency, uiState.currentLanguage)
                        Text(
                            text = "$shareFormatted each",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    IconButton(
                        onClick = onIncrement,
                        enabled = uiState.splitCount < 50,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.increase_people),
                            tint = if (uiState.splitCount < 50) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * =========================================================================================
 * FEATURE: Global Country Selection Explorer (CountrySelectionView)
 * =========================================================================================
 *
 * Description:
 * Full-screen modal view providing real-time search, browsing, and selection across 190+
 * sovereign nations and regional cultural tipping matrices.
 *
 * Capabilities:
 * - Dynamic Search: Filters by country name, ISO code (e.g. "US", "DE"), or Matrix archetype.
 * - Cultural Insight Card: Displays tipping etiquette advice and cultural background for selected nation.
 * - Matrix Tagging: Visually badges countries with their Matrix tier (A, B, C, D, E).
 *
 * @param allCountries Complete list of [CountryTippingData] entries.
 * @param currentCountry Currently active [CountryTippingData].
 * @param currentLanguage Active 2-letter ISO language code.
 * @param onBack Callback to navigate back to the main calculator screen.
 * @param onSelectCountry Callback invoked when the user taps a country card to switch nations.
 * =========================================================================================
 */
@Composable
fun CountrySelectionView(
    allCountries: List<CountryTippingData>,
    currentCountry: CountryTippingData,
    currentLanguage: String = "en",
    onBack: () -> Unit,
    onSelectCountry: (CountryTippingData) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery, allCountries, currentLanguage) {
        if (searchQuery.isBlank()) allCountries
        else allCountries.filter { country ->
            val localizedCountry = country.getLocalizedCountry(currentLanguage)
            val localizedMatrix = country.getLocalizedMatrixName(currentLanguage)
            val currencyOption = country.getResolvedCurrency()
            val localizedCurrency = currencyOption.getLocalizedLabel(currentLanguage)

            country.country.contains(searchQuery, ignoreCase = true) ||
            country.iso.contains(searchQuery, ignoreCase = true) ||
            country.matrixName.contains(searchQuery, ignoreCase = true) ||
            localizedCountry.contains(searchQuery, ignoreCase = true) ||
            localizedMatrix.contains(searchQuery, ignoreCase = true) ||
            localizedCurrency.contains(searchQuery, ignoreCase = true) ||
            (country.localCurrency ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        // Expressive Top Navigation Row with Back Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_to_calculator),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.select_country_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.regional_matrices_count),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Search Bar Card
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                if (it != searchQuery) {
                    HapticFeedbackManager.tick(null)
                }
                searchQuery = it
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.search_country_placeholder),
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("country_search_field"),
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Countries List with Expressive Cards
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredCountries, key = { it.iso }) { country ->
                val isSelected = country.iso == currentCountry.iso
                val restaurantRule = country.getRuleFor("Restaurants")
                val localizedCountry = country.getLocalizedCountry(currentLanguage)
                val localizedMatrix = country.getLocalizedMatrixName(currentLanguage)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Select $localizedCountry"
                        ) { onSelectCountry(country) }
                        .semantics {
                            selected = isSelected
                        }
                        .testTag("country_item_${country.iso}"),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    shadowElevation = if (isSelected) 4.dp else 1.dp,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // ISO Badge
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = country.iso,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localizedCountry,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$localizedMatrix • Rest. ${restaurantRule.standardTip}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        val currencyOption = country.getResolvedCurrency()
                        val currencyCode = country.localCurrency?.takeIf { it.isNotBlank() } ?: currencyOption.code
                        val currencySymbol = currencyOption.symbol
                        val localizedCurrencyLabel = currencyOption.getLocalizedLabel(currentLanguage)
                        val currencyDescription = "$localizedCurrencyLabel, $currencyCode"

                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("country_currency_${country.iso}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = currencyDescription,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (currencySymbol.isNotBlank() && currencySymbol != currencyCode) "$currencyCode ($currencySymbol)" else currencyCode,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
