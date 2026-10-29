package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.history.SavePromptMode
import com.example.i18n.LanguageResolver

/**
 * =========================================================================================
 * FEATURE: Save Tip Confirmation Modal Mask (SaveTipDialog)
 * =========================================================================================
 *
 * Description:
 * Expressive Material 3 modal dialog prompting the user to save completed tip calculations
 * into local DataStore history. Provides dual-mode rendering:
 * - Standard Mode: Clean, elevated card layout with smooth typography and subtle outlines.
 * - Senior Accessibility Mode: High-contrast surfaces, enlarged touch targets (>=48dp),
 *   and bold typographic hierarchy.
 *
 * Capabilities:
 * - Summary Breakdown: Displays bill amount, calculated tip, total bill, and split count.
 * - Interactive Setting: In-dialog switch to toggle "Prompt to save: Always vs. Never".
 * - Dismiss & Save: Direct confirmation or dismissal with accessible action buttons.
 *
 * @param visible Boolean state controlling dialog visibility.
 * @param billAmount Raw subtotal entered by the user.
 * @param tipAmount Calculated tip amount.
 * @param totalBill Total amount (bill + tip).
 * @param splitCount Number of individuals splitting the bill.
 * @param currencySymbol Formatted currency sign (e.g. "$", "€").
 * @param isSeniorMode Accessibility flag enabling high-contrast and enlarged components.
 * @param currentLanguage Active 2-letter language code ("en", "de", "es", "fr", etc.).
 * @param currentPromptMode Current user preference ([SavePromptMode.ALWAYS] or [SavePromptMode.NEVER]).
 * @param onPromptModeChanged Callback invoked when the user toggles the save prompt mode switch.
 * @param onConfirmSave Callback invoked when the user confirms saving the calculation.
 * @param onDismiss Callback invoked when the dialog is dismissed without saving.
 * =========================================================================================
 */
@Composable
fun SaveTipDialog(
    visible: Boolean,
    billAmount: Double,
    tipAmount: Double,
    totalBill: Double,
    splitCount: Int,
    currencySymbol: String,
    isSeniorMode: Boolean,
    currentLanguage: String,
    currentPromptMode: SavePromptMode,
    onPromptModeChanged: (SavePromptMode) -> Unit,
    onConfirmSave: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val isLongText = LanguageResolver.isLongTextLanguage(currentLanguage)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isSeniorMode) 0.94f else 0.90f)
                .clip(RoundedCornerShape(if (isSeniorMode) 32.dp else 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(if (isSeniorMode) 24.dp else 20.dp)
                .testTag("save_tip_mask_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Avatar Icon
                Surface(
                    modifier = Modifier.size(if (isSeniorMode) 68.dp else 56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(if (isSeniorMode) 36.dp else 28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title: "Do you want to save it?"
                Text(
                    text = stringResource(R.string.save_tip_title),
                    style = if (isSeniorMode) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("save_tip_dialog_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Description
                Text(
                    text = stringResource(R.string.save_tip_message),
                    style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.bill_input_label),
                                style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", billAmount)}",
                                style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.tip_amount_label),
                                style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", tipAmount)}",
                                style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.total_bill_label),
                                style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", totalBill)}",
                                style = if (isSeniorMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (splitCount > 1) {
                            val perPerson = totalBill / splitCount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${stringResource(R.string.split_bill_label)} ($splitCount)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currencySymbol${String.format(java.util.Locale.US, "%.2f", perPerson)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "Prompt to Save: Always / Never" interactive toggle in mask
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.save_prompt_toggle_label),
                            style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentPromptMode == SavePromptMode.ALWAYS) {
                                stringResource(R.string.save_prompt_always_mode)
                            } else {
                                stringResource(R.string.save_prompt_never_mode)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Switch(
                        checked = currentPromptMode == SavePromptMode.ALWAYS,
                        onCheckedChange = { checked ->
                            onPromptModeChanged(if (checked) SavePromptMode.ALWAYS else SavePromptMode.NEVER)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("save_prompt_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons (Save & Don't Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isSeniorMode || isLongText) 58.dp else 48.dp)
                            .testTag("save_tip_dismiss_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.save_button_dismiss),
                                style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                softWrap = true,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = onConfirmSave,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(if (isSeniorMode || isLongText) 58.dp else 48.dp)
                            .testTag("save_tip_confirm_button"),
                        shape = RoundedCornerShape(16.dp),
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
                                text = stringResource(R.string.save_button_confirm),
                                style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                softWrap = true,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
