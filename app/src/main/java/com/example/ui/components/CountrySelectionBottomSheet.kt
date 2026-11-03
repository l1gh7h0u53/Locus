package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CountryTippingData
import com.example.R
import com.example.i18n.getLocalizedCountry
import com.example.i18n.getLocalizedLabel
import com.example.i18n.getLocalizedMatrixName
import java.util.Locale

/**
 * Utility helper to convert two-letter ISO country codes into Unicode regional indicator flag emojis.
 */
object CountryFlagHelper {
    /**
     * Converts a 2-letter ISO country code (e.g. "US", "IT", "AT", "ES", "MA") into its corresponding national flag emoji.
     * Falls back to the globe symbol 🌐 if the ISO code is invalid or missing.
     */
    fun getFlagEmoji(iso: String): String {
        if (iso.length != 2) return "🌐"
        val upper = iso.uppercase(Locale.US)
        val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
        if (firstChar !in 0x1F1E6..0x1F1FF || secondChar !in 0x1F1E6..0x1F1FF) return "🌐"
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}

/**
 * =========================================================================================
 * FEATURE: Material 3 Expressive Quick Country Selector (CountrySelectionBottomSheet)
 * =========================================================================================
 *
 * Description:
 * Modal bottom sheet allowing users to rapidly override the active country during border crossings,
 * cellular roaming delays, or offline travel (e.g. Tyrol to Italy, Spain to Morocco).
 * Supports pulling up to expand or pulling down to dismiss smoothly.
 *
 * @param visible Boolean controlling whether the sheet is displayed.
 * @param currentCountry Currently active [CountryTippingData].
 * @param allCountries Complete list of available world countries.
 * @param currentLanguage Active 2-letter ISO language code.
 * @param onDismiss Callback invoked when the user dismisses the bottom sheet.
 * @param onSelectCountry Callback invoked when the user picks a new active nation.
 * =========================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountrySelectionBottomSheet(
    visible: Boolean,
    currentCountry: CountryTippingData,
    allCountries: List<CountryTippingData>,
    currentLanguage: String = "en",
    onDismiss: () -> Unit,
    onSelectCountry: (CountryTippingData) -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }

    val filteredCountries = remember(searchQuery, allCountries, currentLanguage) {
        val q = searchQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) {
            listOf(currentCountry) + (allCountries.filter { !it.iso.equals(currentCountry.iso, ignoreCase = true) }.sortedBy { it.country })
        } else {
            allCountries.filter { country ->
                val localizedCountry = country.getLocalizedCountry(currentLanguage).lowercase(Locale.US)
                val localizedMatrix = country.getLocalizedMatrixName(currentLanguage).lowercase(Locale.US)
                val currency = country.getResolvedCurrency()
                val localizedCurrency = currency.getLocalizedLabel(currentLanguage).lowercase(Locale.US)

                country.country.lowercase(Locale.US).contains(q) ||
                country.iso.lowercase(Locale.US).contains(q) ||
                localizedCountry.contains(q) ||
                localizedMatrix.contains(q) ||
                currency.code.lowercase(Locale.US).contains(q) ||
                localizedCurrency.contains(q)
            }.sortedBy { it.country }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("country_selection_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.select_country_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "192 Countries" else "${filteredCountries.size} Countries",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .testTag("country_sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_button),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Currently Active Country Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = CountryFlagHelper.getFlagEmoji(currentCountry.iso),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE LOCATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${currentCountry.country} (${currentCountry.getLocalizedCountry(currentLanguage)})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(
                            text = currentCountry.iso.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("country_search_input"),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_country_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            // Country List
            if (filteredCountries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_currency_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredCountries, key = { it.iso }) { country ->
                        val isSelected = country.iso.equals(currentCountry.iso, ignoreCase = true)
                        val localizedName = country.getLocalizedCountry(currentLanguage)
                        val flag = CountryFlagHelper.getFlagEmoji(country.iso)
                        val currency = country.getResolvedCurrency()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    role = Role.RadioButton,
                                    onClickLabel = "Select country ${country.country}"
                                ) {
                                    onSelectCountry(country)
                                }
                                .semantics {
                                    selected = isSelected
                                }
                                .testTag("country_sheet_item_${country.iso}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                                            text = flag,
                                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 24.sp),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = country.country,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            ) {
                                                Text(
                                                    text = country.iso.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        if (localizedName != country.country) {
                                            Text(
                                                text = localizedName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Text(
                                            text = country.getLocalizedMatrixName(currentLanguage),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "${currency.code} (${currency.symbol})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
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
            }
        }
    }
}
