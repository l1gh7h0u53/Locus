package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.CurrencyOption
import com.example.R
import com.example.i18n.GlobalExplorerLocalization
import com.example.i18n.getLocalizedLabel
import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Searchable Currency Picker Modal Dialog (CurrencyPickerDialog)
 * =========================================================================================
 *
 * Description:
 * Modal selection dialog allowing users to search and select from 145 global currencies.
 * Supports filtering by three-letter ISO code, graphical symbol, English name, or localized name.
 *
 * @param currentCurrency The currently active [CurrencyOption].
 * @param currentLanguage Active 2-letter ISO language code.
 * @param onSelectCurrency Callback invoked when the user selects a currency.
 * @param onDismissRequest Callback invoked to dismiss the dialog without changes.
 * =========================================================================================
 */
@Composable
fun CurrencyPickerDialog(
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
 * FEATURE: Select Currency with All (145) Section (CurrencySelectorSection)
 * =========================================================================================
 *
 * Description:
 * Material 3 Expressive currency selection card containing:
 * - "SELECT CURRENCY" header label
 * - "All (145)" TextButton opening [CurrencyPickerDialog]
 * - Horizontal carousel of featured and active currency chips
 *
 * @param currentCurrency Active [CurrencyOption] selected.
 * @param currentLanguage Active 2-letter language code.
 * @param onSelectCurrency Callback invoked when a currency is selected.
 * @param modifier Optional [Modifier].
 * =========================================================================================
 */
@Composable
fun CurrencySelectorSection(
    currentCurrency: CurrencyOption,
    currentLanguage: String = "en",
    onSelectCurrency: (CurrencyOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAllCurrenciesDialog by remember { mutableStateOf(false) }

    if (showAllCurrenciesDialog) {
        CurrencyPickerDialog(
            currentCurrency = currentCurrency,
            currentLanguage = currentLanguage,
            onSelectCurrency = { currency ->
                onSelectCurrency(currency)
                showAllCurrenciesDialog = false
            },
            onDismissRequest = { showAllCurrenciesDialog = false }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Popular Currency Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val featuredCurrencies = listOf(
                    currentCurrency,
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
                    val isSelected = currentCurrency == currency
                    val localizedLabel = currency.getLocalizedLabel(currentLanguage)
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
}
