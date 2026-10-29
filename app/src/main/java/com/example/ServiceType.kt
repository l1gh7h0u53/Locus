package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * =========================================================================================
 * FEATURE: Service Category Enumeration & Tipping Archetypes (ServiceType)
 * =========================================================================================
 *
 * Description:
 * Defines supported service categories within the hospitality, delivery, and commerce sectors.
 * Each entry provides an internal identifier, matching dataset key for regional matrix lookup,
 * localized string resource identifier, default fallback percentage rate, and Material icon.
 *
 * Supported Categories:
 * - [RESTAURANT]: Sit-down dining with dedicated waitstaff.
 * - [DELIVERY]: Courier door-to-door food delivery.
 * - [TAKEOUT]: Counter pickup service.
 * - [FOODTRUCK]: Mobile catering and street vendor food.
 * - [GROCERIES]: Supermarket checkout and grocery packing helpers.
 * - [BAR]: Pub, lounge, and counter drink service.
 *
 * @property id Internal lowercase key used in analytics and transaction storage.
 * @property datasetKey Matching service key used in country JSON tipping matrices (e.g. "Restaurants").
 * @property nameRes Android string resource ID providing localized category name.
 * @property defaultStandardRate Fallback decimal standard tip rate (e.g. 0.15 for 15%).
 * @property icon Material symbol vector representing the service.
 * =========================================================================================
 */
enum class ServiceType(
    val id: String,
    val datasetKey: String,
    val nameRes: Int,
    val defaultStandardRate: Double,
    val icon: ImageVector
) {
    RESTAURANT(
        id = "restaurant",
        datasetKey = "Restaurants",
        nameRes = R.string.service_restaurant,
        defaultStandardRate = 0.15,
        icon = Icons.Default.LocalDining
    ),
    DELIVERY(
        id = "delivery",
        datasetKey = "Delivery",
        nameRes = R.string.service_delivery,
        defaultStandardRate = 0.10,
        icon = Icons.Default.DeliveryDining
    ),
    TAKEOUT(
        id = "takeout",
        datasetKey = "Takeout",
        nameRes = R.string.service_takeout,
        defaultStandardRate = 0.00,
        icon = Icons.Default.LocalMall
    ),
    FOODTRUCK(
        id = "foodtruck",
        datasetKey = "Foodtrucks",
        nameRes = R.string.service_foodtruck,
        defaultStandardRate = 0.00,
        icon = Icons.Default.LocalShipping
    ),
    GROCERIES(
        id = "groceries",
        datasetKey = "Groceries",
        nameRes = R.string.service_groceries,
        defaultStandardRate = 0.00,
        icon = Icons.Default.LocalGroceryStore
    ),
    BAR(
        id = "bar",
        datasetKey = "Bar",
        nameRes = R.string.service_bar,
        defaultStandardRate = 0.10,
        icon = Icons.Default.LocalBar
    );

    /**
     * Resolves the human-readable standard tip percentage string for this service category.
     * If an [activeCountry] is provided, queries that country's specific rule; otherwise returns
     * the default percentage string (e.g. "15%").
     *
     * @param activeCountry Optional [CountryTippingData] providing regional guidelines.
     * @return Formatted percentage string (e.g. "15%" or "10% - 15%").
     */
    fun standardRateString(activeCountry: CountryTippingData? = null): String {
        if (activeCountry != null) {
            val rule = activeCountry.getRuleFor(datasetKey)
            return rule.standardTip
        }
        return "${(defaultStandardRate * 100).toInt()}%"
    }
}
