package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.history.SavePromptMode

/**
 * =========================================================================================
 * FEATURE: Coach Mark Step Data Specification (CoachMarkStep)
 * =========================================================================================
 *
 * Description:
 * Data model defining a single step within the interactive onboarding tour.
 *
 * @property stepIndex Zero-based sequence index.
 * @property titleRes String resource ID for the step title.
 * @property subtitleRes String resource ID for the step subtitle.
 * @property descRes String resource ID for the detailed explanation.
 * @property icon Material symbol representing the feature.
 * @property highlightTargetTag Compose testTag identifying the target UI element.
 * @property isHistoryStep Boolean indicating if this step contains interactive history toggle controls.
 * =========================================================================================
 */
data class CoachMarkStep(
    val stepIndex: Int,
    val titleRes: Int,
    val subtitleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val highlightTargetTag: String,
    val isHistoryStep: Boolean = false
)

/**
 * =========================================================================================
 * FEATURE: Expressive Onboarding Coach Mark Overlay (CoachMarkOverlay)
 * =========================================================================================
 *
 * Description:
 * Expressive Material 3 multi-step overlay introducing first-time users to primary features:
 * 1. Senior Mode: High-contrast surfaces, enlarged buttons, and Text-to-Speech vocal readout.
 * 2. Quick-Add Buttons: Instant bill amount increment chips (+1, +5, +10, etc.).
 * 3. History & Save Prompt Settings: DataStore transaction persistence with interactive
 *    toggles for "Prompt to save: Always vs. Never" and "Tutorial on launch: Always vs. Never".
 *
 * Motion & Physics:
 * - Employs spring-based physics (`Spring.StiffnessMediumLow`) for smooth scale-in and fade-out
 *   transitions between steps.
 *
 * @param visible Boolean state controlling overlay visibility.
 * @param savePromptMode User's current save prompt preference ([SavePromptMode]).
 * @param onSavePromptModeChange Callback when user updates the save prompt preference.
 * @param tutorialMode User's current tutorial on launch preference ("ALWAYS" or "NEVER").
 * @param onTutorialModeChange Callback when user updates the tutorial launch preference.
 * @param onDismiss Callback when user finishes or skips the tutorial tour.
 * =========================================================================================
 */
@Composable
fun CoachMarkOverlay(
    visible: Boolean,
    savePromptMode: SavePromptMode = SavePromptMode.ALWAYS,
    onSavePromptModeChange: (SavePromptMode) -> Unit = {},
    tutorialMode: String = "NEVER",
    onTutorialModeChange: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    if (!visible) return

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var currentSavePromptMode by remember(savePromptMode) { mutableStateOf(savePromptMode) }
    var currentTutorialMode by remember(tutorialMode) { mutableStateOf(tutorialMode) }

    val steps = remember {
        listOf(
            CoachMarkStep(
                stepIndex = 0,
                titleRes = R.string.senior_mode_title,
                subtitleRes = R.string.senior_mode_subtitle,
                descRes = R.string.senior_mode_explanation,
                icon = Icons.Default.Accessibility,
                highlightTargetTag = "senior_mode_toggle"
            ),
            CoachMarkStep(
                stepIndex = 1,
                titleRes = R.string.quick_add_title,
                subtitleRes = R.string.quick_add_subtitle,
                descRes = R.string.quick_add_description,
                icon = Icons.Default.AddCircle,
                highlightTargetTag = "quick_add_buttons"
            ),
            CoachMarkStep(
                stepIndex = 2,
                titleRes = R.string.coach_step_history_title,
                subtitleRes = R.string.coach_step_history_subtitle,
                descRes = R.string.coach_step_history_desc,
                icon = Icons.Default.Settings,
                highlightTargetTag = "settings_button",
                isHistoryStep = true
            )
        )
    }

    val currentStep = steps.getOrElse(currentStepIndex) { steps.first() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Intercept clicks to avoid underlying input */ }
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .testTag("coach_mark_overlay")
    ) {
        // Main Coach Mark Card
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                 scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                    .togetherWith(
                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                    )
            },
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            label = "CoachMarkStepTransition"
        ) { step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("coach_mark_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Header: Step pill & Skip button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = "Step ${step.stepIndex + 1} of ${steps.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("coach_mark_skip_button")
                        ) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Large Feature Icon Avatar
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feature Title
                    Text(
                        text = stringResource(step.titleRes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("coach_mark_title")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Feature Subtitle
                    Text(
                        text = stringResource(step.subtitleRes),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feature Detailed Description
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(step.descRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(14.dp)
                                .testTag("coach_mark_description")
                        )
                    }

                    // If on History step, provide interactive Always / Never toggle controls!
                    if (step.isHistoryStep) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Save Prompt Toggle: Always vs Never
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.save_prompt_toggle_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (currentSavePromptMode == SavePromptMode.ALWAYS) {
                                                stringResource(R.string.save_prompt_always_mode)
                                            } else {
                                                stringResource(R.string.save_prompt_never_mode)
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = currentSavePromptMode == SavePromptMode.ALWAYS,
                                            onClick = {
                                                currentSavePromptMode = SavePromptMode.ALWAYS
                                                onSavePromptModeChange(SavePromptMode.ALWAYS)
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.save_prompt_always_mode),
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("tutorial_save_always_chip")
                                        )

                                        FilterChip(
                                            selected = currentSavePromptMode == SavePromptMode.NEVER,
                                            onClick = {
                                                currentSavePromptMode = SavePromptMode.NEVER
                                                onSavePromptModeChange(SavePromptMode.NEVER)
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.save_prompt_never_mode),
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("tutorial_save_never_chip")
                                        )
                                    }
                                }

                                // Tutorial On Launch Toggle: Always vs Never
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.tutorial_toggle_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (currentTutorialMode == "ALWAYS") {
                                                stringResource(R.string.tutorial_toggle_always)
                                            } else {
                                                stringResource(R.string.tutorial_toggle_never)
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = currentTutorialMode == "ALWAYS",
                                            onClick = {
                                                currentTutorialMode = "ALWAYS"
                                                onTutorialModeChange("ALWAYS")
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.tutorial_toggle_always),
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("tutorial_show_always_chip")
                                        )

                                        FilterChip(
                                            selected = currentTutorialMode != "ALWAYS",
                                            onClick = {
                                                currentTutorialMode = "NEVER"
                                                onTutorialModeChange("NEVER")
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(R.string.tutorial_toggle_never),
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("tutorial_show_never_chip")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Indicators (Dots)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == step.stepIndex) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == step.stepIndex) MaterialTheme.colorScheme.primary
                                         else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (step.stepIndex > 0) {
                            OutlinedButton(
                                onClick = { currentStepIndex-- },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("coach_mark_back_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = "Back",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (step.stepIndex < steps.size - 1) {
                            Button(
                                onClick = { currentStepIndex++ },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("coach_mark_next_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Next",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("coach_mark_got_it_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Got It!",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
