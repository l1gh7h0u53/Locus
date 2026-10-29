package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.history.SavePromptMode
import com.example.i18n.LanguageResolver

/**
 * =========================================================================================
 * FEATURE: Material 3 Expressive Settings Sheet (SettingsBottomSheet)
 * =========================================================================================
 *
 * Description:
 * Dedicated central settings modal bottom sheet consolidating controls that were previously
 * crowding the top header bar:
 * - Theme Mode: Light vs. Dark theme toggle switch.
 * - Tip Calculation History: Navigation tile launching the persistent DataStore history viewer.
 * - Select Language: Offline target language switcher and auto-telephony locale selector.
 * - Senior Accessibility Mode: High-contrast surfaces, massive buttons, and voice readout toggle.
 * - Onboarding Tutorial: Quick-launch button to re-run the hands-on feature tour anytime.
 * - Save Prompt Preference: Preference chips configuring whether to prompt to save tips.
 *
 * Architecture & Design:
 * - Adheres to Material Design 3 Expressive guidelines with high-contrast surfaces, 48dp+ touch targets,
 *   and clear categorization cards.
 * - All actions and toggles are fully offline-first with zero external cloud dependencies.
 *
 * @param visible Boolean state controlling bottom sheet presentation.
 * @param currentDarkTheme Current active theme mode (true for dark, false for light).
 * @param isSeniorMode Boolean flag indicating whether Senior accessibility mode is active.
 * @param currentLanguage Active 2-letter language code ("en", "de", "es", "fr", etc.).
 * @param isManualLanguageOverride Boolean indicating whether user manually chose a language.
 * @param savePromptMode Active save prompt setting ([SavePromptMode.ALWAYS] or [SavePromptMode.NEVER]).
 * @param onDismiss Callback invoked when the user dismisses the bottom sheet.
 * @param onToggleTheme Callback invoked when the user flips the Dark/Light theme toggle.
 * @param onOpenHistory Callback invoked when the user taps to open the Tip History sheet.
 * @param onOpenLanguage Callback invoked when the user taps to open the Language selection dialog.
 * @param onToggleSeniorMode Callback invoked to toggle senior accessibility mode.
 * @param onRestartTutorial Callback invoked to re-launch the onboarding coach marks overlay.
 * @param onSavePromptModeChange Callback invoked to update the persistent save prompt preference.
 * =========================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    visible: Boolean,
    currentDarkTheme: Boolean,
    isSeniorMode: Boolean,
    currentLanguage: String,
    isManualLanguageOverride: Boolean,
    savePromptMode: SavePromptMode = SavePromptMode.ALWAYS,
    onDismiss: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenLanguage: () -> Unit,
    onToggleSeniorMode: () -> Unit = {},
    onRestartTutorial: () -> Unit = {},
    onSavePromptModeChange: (SavePromptMode) -> Unit = {}
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("settings_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Indicator Handle
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sheet Title Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.header_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("settings_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_button),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: APPEARANCE (Light / Dark Theme Mode)
            SettingsSectionHeader(title = stringResource(R.string.settings_section_appearance))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsTileCard(
                icon = if (currentDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_dark_mode_title),
                subtitle = stringResource(R.string.settings_dark_mode_desc),
                trailingContent = {
                    Switch(
                        checked = currentDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("dark_mode_toggle")
                    )
                },
                onClick = onToggleTheme
            )

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 2: RECORDS & TIP HISTORY
            SettingsSectionHeader(title = stringResource(R.string.settings_section_records))
            Spacer(modifier = Modifier.height(8.dp))

            SettingsTileCard(
                icon = Icons.Default.History,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.settings_history_title),
                subtitle = stringResource(R.string.settings_history_desc),
                trailingContent = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                testTag = "tip_history_button",
                onClick = onOpenHistory
            )

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 3: LANGUAGE & REGION
            SettingsSectionHeader(title = stringResource(R.string.settings_section_language))
            Spacer(modifier = Modifier.height(8.dp))

            val currentLangName = if (isManualLanguageOverride) {
                LanguageResolver.TARGET_LANGUAGES[currentLanguage]?.name ?: currentLanguage.uppercase()
            } else {
                stringResource(R.string.auto_language_option)
            }

            SettingsTileCard(
                icon = Icons.Default.Language,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = stringResource(R.string.settings_language_title),
                subtitle = currentLangName,
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = currentLanguage.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                },
                testTag = "language_dialog_button",
                onClick = onOpenLanguage
            )

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 4: ACCESSIBILITY & PREFERENCES
            SettingsSectionHeader(title = stringResource(R.string.settings_section_accessibility))
            Spacer(modifier = Modifier.height(8.dp))

            // Senior Mode Toggle Tile
            SettingsTileCard(
                icon = Icons.Default.Accessibility,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_senior_mode_title),
                subtitle = stringResource(R.string.settings_senior_mode_desc),
                trailingContent = {
                    Switch(
                        checked = isSeniorMode,
                        onCheckedChange = { onToggleSeniorMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("settings_senior_mode_toggle")
                    )
                },
                onClick = onToggleSeniorMode
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Prompt to Save Setting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_save_prompt_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_save_prompt_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = savePromptMode == SavePromptMode.ALWAYS,
                            onClick = { onSavePromptModeChange(SavePromptMode.ALWAYS) },
                            label = { Text(stringResource(R.string.save_prompt_always_mode)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_save_prompt_always")
                        )

                        FilterChip(
                            selected = savePromptMode == SavePromptMode.NEVER,
                            onClick = { onSavePromptModeChange(SavePromptMode.NEVER) },
                            label = { Text(stringResource(R.string.save_prompt_never_mode)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_save_prompt_never")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feature Tour Re-launch Tile
            SettingsTileCard(
                icon = Icons.Default.HelpOutline,
                iconTint = MaterialTheme.colorScheme.outline,
                title = stringResource(R.string.settings_tutorial_title),
                subtitle = stringResource(R.string.settings_tutorial_desc),
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_restart_tutorial),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                },
                testTag = "settings_restart_tutorial_button",
                onClick = onRestartTutorial
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // Footer Information
            Text(
                text = stringResource(R.string.settings_footer_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

/**
 * Reusable Section Header for grouping settings categories.
 */
@Composable
private fun SettingsSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Reusable interactive Material 3 Card tile representing a setting row.
 */
@Composable
private fun SettingsTileCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailingContent: @Composable () -> Unit,
    testTag: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            trailingContent()
        }
    }
}
