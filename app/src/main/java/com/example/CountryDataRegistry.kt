package com.example

/**
 * =========================================================================================
 * FEATURE: Offline Country Tipping Data Registry (CountryDataRegistry)
 * =========================================================================================
 *
 * Description:
 * Serves as the high-speed, embedded on-device database and fallback registry of tipping
 * customs, currency standards, and service category rules across 190+ countries worldwide.
 *
 * Matrix Archetypes:
 * - Matrix A: High tipping standard / North America & Latin America (15-20%+ customary).
 * - Matrix B: Eastern Europe, Balkans & Central Asia (rounding up or 5-10%).
 * - Matrix C: Western / Northern Europe & Oceania (service included, 5-10% for great service).
 * - Matrix D: East Asia (Japan, South Korea) where tipping is generally uncommon or inappropriate.
 * - Matrix E: Middle East & North Africa (Baksheesh culture, cash tips customary).
 *
 * Dependencies:
 * - [com.example.CountryTippingData]: Model representing each country's profile.
 * - [com.example.ServiceRule]: Encapsulates per-service category tipping expectations and note keys.
 * - [com.example.CurrencyOption]: Currency code resolution for each country.
 * =========================================================================================
 */
object CountryDataRegistry {

    /** Default fallback country instance (United States) */
    private val defaultUsCountry: CountryTippingData by lazy {
        createCountry("USA", "US", "A", "Matrix A / Custom North America", "USD", true, mapOf(
            "Restaurants" to ServiceRule("Restaurants", "15% - 18%", "20%", "25%", "30%+", "Tipping is practically mandatory; servers rely on tips for income.", notesKey = "note_us_restaurant", serviceTypeKey = "service_restaurants"),
            "Delivery" to ServiceRule("Delivery", "10% - 15%", "18%", "22%", "25%+", "Standard to tip delivery drivers via app or cash.", notesKey = "note_us_delivery", serviceTypeKey = "service_delivery"),
            "Takeout" to ServiceRule("Takeout", "0% - 10%", "15%", "20%", "25%", "Digital screens prompt for tips completely optional.", notesKey = "note_us_takeout", serviceTypeKey = "service_takeout"),
            "Foodtrucks" to ServiceRule("Foodtrucks", "10%", "15%", "20%", "25%", "Tip jars or digital screen prompts are common.", notesKey = "note_us_foodtruck", serviceTypeKey = "service_foodtruck"),
            "Groceries" to ServiceRule("Groceries", "0%", "Round up", "$2 - $5", "$10", "Optional for grocery baggers or curbside pickup.", notesKey = "note_us_groceries", serviceTypeKey = "service_groceries"),
            "Bar" to ServiceRule("Bar", "$1 - $2 per drink", "15%", "20%", "25%", "Standard to tip per drink or open a tab.", notesKey = "note_us_bar", serviceTypeKey = "service_bar")
        ))
    }

    /** Pre-indexed fast lookup map keyed by uppercase ISO country code */
    private val countryByIsoMap: Map<String, CountryTippingData> by lazy {
        countryList.associateBy { it.iso.uppercase() }
    }

    /**
     * Returns the default country configuration (US - Matrix A).
     *
     * @return [CountryTippingData] representing United States.
     */
    fun getDefaultCountry(): CountryTippingData = defaultUsCountry

    /**
     * Returns the complete list of all supported sovereign nations and territories.
     *
     * @return List of [CountryTippingData].
     */
    fun getAllCountries(): List<CountryTippingData> = countryList

    /**
     * Retrieves country tipping profile by 2-character ISO code (case-insensitive).
     * Falls back to [defaultUsCountry] if ISO is not recognized or not found.
     *
     * @param iso 2-letter ISO country code (e.g. "FR", "JP", "US").
     * @return Matching [CountryTippingData].
     */
    fun getCountryByIso(iso: String): CountryTippingData {
        val clean = iso.trim().uppercase()
        return countryByIsoMap[clean] ?: defaultUsCountry
    }

    private val countryList: List<CountryTippingData> by lazy {
        listOf(
            createCountry("Afghanistan", "AF", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Albania", "AL", "B", "Matrix B", "ALL", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 5% - 10%", "15%", "20%", "25%+", "Tipping is highly appreciated and becoming standard in tourist areas.", notesKey = "note_al_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / 5%", "10%", "15%", "20%+", "Cash tips to drivers are highly welcome.", notesKey = "note_al_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Not expected for pickup.", notesKey = "note_al_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Tip jars are optional.", notesKey = "note_al_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "Round up", "No tipping standard.", notesKey = "note_al_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "5%", "10%", "15%", "Keep the change or round up.", notesKey = "note_al_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Algeria", "DZ", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Andorra", "AD", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Angola", "AO", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Antigua and Barbuda", "AG", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Argentina", "AR", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", "ARS", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Bar-Trinkgeld wird wegen der Inflation deutlich bevorzugt.", notesKey = "note_ar_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "5% - 10%", "15%", "20%", "25%", "App-Fahrer freuen sich sehr über Bargeld.", notesKey = "note_ar_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "Round up", "5%", "Nicht erwartet.", notesKey = "note_ar_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Kleingeld in die Kaffeekasse.", notesKey = "note_ar_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "Small change", "50 ARS", "100 ARS", "200 ARS", "Kleine Scheine für die Einpacker.", notesKey = "note_ar_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10%", "15%", "20%", "Per Drink oder auf den Deckel.", notesKey = "note_ar_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Armenia", "AM", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Australia", "AU", "C", "Matrix C", "AUD", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 0%", "10%", "15%", "20%+", "Tipping is not customary; living wages are high.", notesKey = "note_au_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "Round up", "$2 - $5", "10%", "Optional on apps.", notesKey = "note_au_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_au_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_au_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_au_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "Round up", "Change", "$5", "Rarely expected at bars.", notesKey = "note_au_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Austria", "AT", "C", "Matrix C", "EUR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "5% - 10%", "15%", "20%", "25%+", "Tipping is expected; round up or add 5-10% to the bill.", notesKey = "note_at_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / 1-2 €", "3€", "5€", "10€", "Cash given directly to driver is preferred.", notesKey = "note_at_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Not expected for counter pickup.", notesKey = "note_at_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Round up small change.", notesKey = "note_at_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "Round up", "No tipping.", notesKey = "note_at_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up / 5%", "10%", "15%", "20%", "Round up to the nearest Euro.", notesKey = "note_at_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Azerbaijan", "AZ", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Bahamas", "BS", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Bahrain", "BH", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Bangladesh", "BD", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Barbados", "BB", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Belarus", "BY", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Belgium", "BE", "C", "Matrix C", "EUR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 5%", "10%", "15%", "20%+", "Service charge is legally included; tips are for exceptional service.", notesKey = "note_be_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / 1-2 €", "3€", "5€", "10€", "Appreciated for bad weather or fast delivery.", notesKey = "note_be_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "5%", "10%", "Not expected.", notesKey = "note_be_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "5%", "10%", "Not expected.", notesKey = "note_be_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_be_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "5%", "10%", "15%", "Small extra change welcome.", notesKey = "note_be_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Belize", "BZ", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Benin", "BJ", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Bhutan", "BT", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Bolivia", "BO", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Bosnia and Herzegovina", "BA", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Botswana", "BW", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Brazil", "BR", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", "BRL", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Oft als 'Serviço' direkt auf der Rechnung; extra ist optional.", notesKey = "note_br_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / 2-5 BRL", "10 BRL", "20 BRL", "50 BRL", "Dem Fahrer bar zustecken wird bevorzugt.", notesKey = "note_br_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "Round up", "5%", "An der Theke unüblich.", notesKey = "note_br_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Optional über Wechselgeld.", notesKey = "note_br_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "2 - 5 BRL", "10 BRL", "15 BRL", "25 BRL", "Wichtig: Einpacker an der Kasse arbeiten oft nur für Trinkgeld.", notesKey = "note_br_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10%", "15%", "20%", "Meist in der Gesamtrechnung enthalten.", notesKey = "note_br_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Brunei", "BN", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Bulgaria", "BG", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Burkina Faso", "BF", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Burundi", "BI", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Cabo Verde", "CV", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Cambodia", "KH", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Cameroon", "CM", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Canada", "CA", "A", "Matrix A / Custom North America", "CAD", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "15%", "18%", "22%", "25%+", "Tipping is expected; servers rely on tips for income.", notesKey = "note_ca_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "10%", "15%", "20%", "25%", "Standard selection in delivery apps.", notesKey = "note_ca_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Card terminals prompt for tips but optional.", notesKey = "note_ca_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Tip jars are optional.", notesKey = "note_ca_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping standard.", notesKey = "note_ca_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "1 CAD / Drink", "15%", "20%", "25%", "Tip per drink or add to total tab.", notesKey = "note_ca_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Central African Republic", "CF", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Chad", "TD", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Chile", "CL", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("China", "CN", "D", "Matrix D", "CNY", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "0%", "0%", "0%", "0%", "Tipping is not customary; 10% service charge in luxury hotels.", notesKey = "note_cn_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "0%", "0%", "0%", "No tipping expected.", notesKey = "note_cn_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_cn_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_cn_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_cn_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_cn_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Colombia", "CO", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", "COP", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Heißt 'Propina Voluntaria', steht meist schon auf der Rechnung.", notesKey = "note_co_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "2.000 COP", "5.000 COP", "10.000 COP", "20.000 COP", "Fester kleiner Betrag für den Kurier.", notesKey = "note_co_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "Round up", "Unüblich.", notesKey = "note_co_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Nur bei gutem Service.", notesKey = "note_co_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "Small change", "2.000 COP", "5.000 COP", "10.000 COP", "Für Einpacker oder Kofferraum-Helfer.", notesKey = "note_co_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10%", "15%", "20%", "Wird oft aufgeschlagen.", notesKey = "note_co_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Comoros", "KM", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Congo (Congo-Brazzaville)", "CG", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Congo (Democratic Republic)", "CD", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Costa Rica", "CR", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Croatia", "HR", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Cuba", "CU", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Cyprus", "CY", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Czech Republic", "CZ", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Côte d'Ivoire", "CI", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Denmark", "DK", "C", "Matrix C", "DKK", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 0%", "5% - 10%", "15%", "20%+", "Service is included in the bill by law; tipping is rare.", notesKey = "note_dk_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "Round up", "5%", "10%", "Rarely expected.", notesKey = "note_dk_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_dk_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_dk_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_dk_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "Round up", "5%", "10%", "No tipping standard.", notesKey = "note_dk_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Djibouti", "DJ", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Dominica", "DM", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Dominican Republic", "DO", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Ecuador", "EC", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Egypt", "EG", "E", "Matrix E: Naher Osten & Nordafrika", "EGP", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Trotz Servicegebühr auf der Rechnung geht das Geld selten an die Kellner.", notesKey = "note_eg_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "10 EGP", "20 EGP", "50 EGP", "100 EGP", "Essen ohne Trinkgeld wird ungern geliefert.", notesKey = "note_eg_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "10 EGP", "20 EGP", "Kleines Bakschisch erwünscht.", notesKey = "note_eg_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "Round up", "10 EGP", "20 EGP", "50 EGP", "Straßenstände erwarten ein Aufrunden.", notesKey = "note_eg_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "5 EGP", "10 EGP", "20 EGP", "50 EGP", "Einpacker und Parkhelfer leben ausschließlich davon.", notesKey = "note_eg_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "10%", "15%", "20%", "25%", "Sehr wichtig in Hotelanlagen und Lounges.", notesKey = "note_eg_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("El Salvador", "SV", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Equatorial Guinea", "GQ", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Eritrea", "ER", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Estonia", "EE", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Eswatini", "SZ", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Ethiopia", "ET", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Fiji", "FJ", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Finland", "FI", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("France", "FR", "C", "Matrix C", "EUR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 5%", "10%", "15%", "20%+", "Service compris (15%) is included; leave small cash for good service.", notesKey = "note_fr_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / 1-2 €", "3€", "5€", "10€", "Leave a small coin for the courier.", notesKey = "note_fr_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "5%", "No tipping.", notesKey = "note_fr_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "5%", "10%", "Rarely expected.", notesKey = "note_fr_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_fr_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "5%", "10%", "15%", "Leave small change on the counter.", notesKey = "note_fr_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Gabon", "GA", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Gambia", "GM", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Georgia", "GE", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Germany", "DE", "C", "Matrix C", "EUR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "5% - 10%", "15%", "20%", "25%+", "Tell the server the total amount including tip before paying.", notesKey = "note_de_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / 1-2 €", "3€", "5€", "10€", "Cash tips preferred for drivers.", notesKey = "note_de_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Tip jar common but optional.", notesKey = "note_de_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Tip jar common but optional.", notesKey = "note_de_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "Round up", "No tipping standard.", notesKey = "note_de_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up / 5%", "10%", "15%", "20%", "Add a small amount per round or round up the tab.", notesKey = "note_de_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Ghana", "GH", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Greece", "GR", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Grenada", "GD", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Guatemala", "GT", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Guinea", "GN", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Guinea-Bissau", "GW", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Guyana", "GY", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Haiti", "HT", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Honduras", "HN", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Hong Kong", "HK", "D", "Matrix D: Ost- & Südostasien", "HKD", false),
            createCountry("Hungary", "HU", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Iceland", "IS", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("India", "IN", "B", "Matrix B / Custom", "INR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "5% - 10%", "12% - 15%", "20%", "25%+", "Check if Service Charge (often 10%) is already added.", notesKey = "note_in_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "₹20 - ₹50", "₹100", "₹200", "₹500", "Very standard to tip delivery personnel in cash or via app.", notesKey = "note_in_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Not required.", notesKey = "note_in_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Not required.", notesKey = "note_in_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "₹10 - ₹20", "Base change", "₹50", "Small tips for grocery delivery boys are traditional.", notesKey = "note_in_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "5% - 10%", "15%", "20%", "25%", "Appreciated for lounge or active table service.", notesKey = "note_in_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Indonesia", "ID", "D", "Matrix D: Ost- & Südostasien", "IDR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 5%", "10%", "15%", "20%+", "In touristischen Regionen wie Bali sind 10% gern gesehen.", notesKey = "note_id_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "5.000 IDR", "10.000 IDR", "20.000 IDR", "50.000 IDR", "Per GoJek/Grab-App oder in bar.", notesKey = "note_id_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "Round up", "Nicht erwartet.", notesKey = "note_id_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5.000 IDR", "10.000 IDR", "Nur bei kleinen Ständen (Warungs) zum Aufrunden.", notesKey = "note_id_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "Small change", "5.000 IDR", "Höchstens für Parkeinweiser vor dem Markt.", notesKey = "note_id_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10.000 IDR", "25.000 IDR", "50.000 IDR", "Besonders in Beach Clubs üblich.", notesKey = "note_id_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Iran", "IR", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Iraq", "IQ", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Ireland", "IE", "C", "Matrix C", "EUR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10% - 15%", "20%", "25%", "30%+", "Check for service charges first; standard in sit-down venues.", notesKey = "note_ie_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / €1-€2", "€3-€5", "10%", "15%", "Common for local food deliveries.", notesKey = "note_ie_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Not required.", notesKey = "note_ie_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Not required.", notesKey = "note_ie_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_ie_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0% / Round up", "Change", "€1-€2", "€5", "Rare for pub drinks unless table service is provided.", notesKey = "note_ie_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Israel", "IL", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Italy", "IT", "C", "Matrix C", "EUR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 5%", "10%", "15%", "20%+", "Look out for pane e coperto (bread/cover charge) which is not a tip.", notesKey = "note_it_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / 1-2 €", "3€", "5€", "10€", "Appreciated by food delivery riders.", notesKey = "note_it_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "5%", "No tipping.", notesKey = "note_it_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "5%", "10%", "Rarely expected.", notesKey = "note_it_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_it_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "5%", "10%", "15%", "Small coins left on the counter.", notesKey = "note_it_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Jamaica", "JM", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Japan", "JP", "D", "Matrix D", "JPY", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "0%", "0%", "0%", "0%", "Tipping is considered insulting or confusing; good service is standard.", notesKey = "note_jp_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "0%", "0%", "0%", "No tipping allowed.", notesKey = "note_jp_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping allowed.", notesKey = "note_jp_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping allowed.", notesKey = "note_jp_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping allowed.", notesKey = "note_jp_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "0%", "0%", "0%", "No tipping allowed.", notesKey = "note_jp_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Jordan", "JO", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Kazakhstan", "KZ", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Kenya", "KE", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Kiribati", "KI", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Kuwait", "KW", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Kyrgyzstan", "KG", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Laos", "LA", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Latvia", "LV", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Lebanon", "LB", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Lesotho", "LS", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Liberia", "LR", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Libya", "LY", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Liechtenstein", "LI", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Lithuania", "LT", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Luxembourg", "LU", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Madagascar", "MG", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Malawi", "MW", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Malaysia", "MY", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Maldives", "MV", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Mali", "ML", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Malta", "MT", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Marshall Islands", "MH", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Mauritania", "MR", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Mauritius", "MU", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Mexico", "MX", "A", "Matrix A", "MXN", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10% - 15%", "18%", "20%", "25%+", "Propina is highly expected; sometimes automatically added.", notesKey = "note_mx_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "5% - 10%", "15%", "20%", "25%", "Cash tips to drivers are highly custom.", notesKey = "note_mx_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Tip jar or screen option.", notesKey = "note_mx_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Tip jars common.", notesKey = "note_mx_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "5 - 10 Pesos", "20 Pesos", "50 Pesos", "100 Pesos", "Baggers often work solely for tips.", notesKey = "note_mx_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "10% - 15%", "18%", "20%", "25%", "Standard to tip per round or on the final bill.", notesKey = "note_mx_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Micronesia", "FM", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Moldova", "MD", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Monaco", "MC", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Mongolia", "MN", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Montenegro", "ME", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Morocco", "MA", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Mozambique", "MZ", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Myanmar", "MM", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Namibia", "NA", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Nauru", "NR", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Nepal", "NP", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Netherlands", "NL", "C", "Matrix C", "EUR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "5% - 10%", "15%", "20%", "25%+", "Service is included but rounding up is very common.", notesKey = "note_nl_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / 1-2 €", "3€", "5€", "10€", "Appreciated by food couriers.", notesKey = "note_nl_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "5%", "Not standard.", notesKey = "note_nl_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "5%", "10%", "Optional change.", notesKey = "note_nl_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_nl_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "5%", "10%", "15%", "Keep the change.", notesKey = "note_nl_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("New Zealand", "NZ", "C", "Matrix C: West-/Nordeuropa & Ozeanien", "NZD", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "0%", "10%", "15%", "20%+", "Wie in Australien: Kein Trinkgeldzwang, da Löhne hoch sind.", notesKey = "note_nz_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "Round up", "2 NZD", "5 NZD", "Über Apps rein optional.", notesKey = "note_nz_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "Wird nicht erwartet.", notesKey = "note_nz_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "Keine Erwartungshaltung.", notesKey = "note_nz_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "Absolut unüblich.", notesKey = "note_nz_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "Round up", "Change", "5 NZD", "Am Tresen wird kein Geld liegen gelassen.", notesKey = "note_nz_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Nicaragua", "NI", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Niger", "NE", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Nigeria", "NG", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("North Macedonia", "MK", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Norway", "NO", "C", "Matrix C", "NOK", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 0%", "5% - 10%", "15%", "20%+", "Service charge is legally included; tips are optional.", notesKey = "note_no_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "Round up", "10-20 NOK", "50 NOK", "Optional via food apps.", notesKey = "note_no_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping expected.", notesKey = "note_no_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_no_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_no_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "Round up", "Change", "10 NOK", "Rounding up for good service.", notesKey = "note_no_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Oman", "OM", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Pakistan", "PK", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Palau", "PW", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Panama", "PA", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Papua New Guinea", "PG", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Paraguay", "PY", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Peru", "PE", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Philippines", "PH", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Poland", "PL", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Portugal", "PT", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Qatar", "QA", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Romania", "RO", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Russia", "RU", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Rwanda", "RW", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Saint Kitts and Nevis", "KN", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Saint Lucia", "LC", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Saint Vincent", "VC", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Samoa", "WS", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("San Marino", "SM", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Sao Tome and Principe", "ST", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Saudi Arabia", "SA", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Senegal", "SN", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Serbia", "RS", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Seychelles", "SC", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Sierra Leone", "SL", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Singapore", "SG", "D", "Matrix D: Ost- & Südostasien", "SGD", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "0%", "5%", "10%", "15%", "10% Service Charge und 9% GST sind fast immer inklusive.", notesKey = "note_sg_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "Round up", "2 SGD", "5 SGD", "Offiziell nicht erwartet, aber nett.", notesKey = "note_sg_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "Kein Trinkgeld.", notesKey = "note_sg_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "In Hawker Centres ist Trinkgeld strikt unüblich.", notesKey = "note_sg_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "Kein Trinkgeld.", notesKey = "note_sg_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "Round up", "5 SGD", "10 SGD", "Bars schlagen die Servicegebühr meist direkt drauf.", notesKey = "note_sg_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Slovakia", "SK", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Slovenia", "SI", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Solomon Islands", "SB", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Somalia", "SO", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("South Africa", "ZA", "A", "Matrix A", "ZAR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "10% is the standard minimum for decent service.", notesKey = "note_za_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / R10 - R20", "R30", "R50", "R100", "Appreciated by delivery riders.", notesKey = "note_za_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "Round up", "5%", "Not customary.", notesKey = "note_za_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "Round up", "5%", "Not customary.", notesKey = "note_za_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "R5 - R10", "R20", "R50", "R100", "Tip the parking guards (Car Guards) and grocery baggers.", notesKey = "note_za_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10%", "15%", "20%", "Common to leave small change.", notesKey = "note_za_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("South Korea", "KR", "D", "Matrix D", "KRW", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "0%", "0%", "0%", "0%", "No tipping culture; high quality service is included.", notesKey = "note_kr_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_kr_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_kr_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_kr_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_kr_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_kr_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Spain", "ES", "C", "Matrix C", "EUR", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 5%", "10%", "15%", "20%+", "Service is included; 5-10% for exceptional fine dining.", notesKey = "note_es_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up", "5%", "10%", "15%", "Small change for riders.", notesKey = "note_es_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_es_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_es_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_es_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "Change", "5%", "10%", "Leave small change on the counter.", notesKey = "note_es_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Sri Lanka", "LK", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Sudan", "SD", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Suriname", "SR", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Sweden", "SE", "C", "Matrix C", "SEK", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 0%", "5% - 10%", "15%", "20%+", "Service is included; tipping is strictly optional.", notesKey = "note_se_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "Round up", "5%", "10%", "Not expected.", notesKey = "note_se_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_se_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_se_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_se_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "Round up", "5%", "10%", "Not expected.", notesKey = "note_se_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Switzerland", "CH", "C", "Matrix C", "CHF", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 5%", "10%", "15%", "20%+", "Service is legally included; rounding up is normal.", notesKey = "note_ch_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up", "5%", "10%", "15%", "Appreciated by couriers.", notesKey = "note_ch_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "5%", "Not standard.", notesKey = "note_ch_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "5%", "Not standard.", notesKey = "note_ch_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_ch_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "5%", "10%", "15%", "Round up to the nearest franc.", notesKey = "note_ch_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Syria", "SY", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Taiwan", "TW", "D", "Matrix D: Ost- & Südostasien", "TWD", false),
            createCountry("Tajikistan", "TJ", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Tanzania", "TZ", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Thailand", "TH", "D", "Matrix D: Ost- & Südostasien", "THB", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up", "10%", "15%", "20%+", "In lokalen Garküchen Aufrunden; in Touristen-Zonen 10%.", notesKey = "note_th_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up", "20 Baht", "50 Baht", "100 Baht", "Das Wechselgeld wird dem Fahrer überlassen.", notesKey = "note_th_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "Absolut unüblich.", notesKey = "note_th_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "20 Baht", "50 Baht", "Garküchen erwarten kein Trinkgeld.", notesKey = "note_th_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "Kein Trinkgeld üblich.", notesKey = "note_th_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "20 Baht", "50 Baht", "100 Baht", "Kleingeld auf dem Tresen liegen lassen.", notesKey = "note_th_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Timor-Leste", "TL", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Togo", "TG", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Tonga", "TO", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Trinidad and Tobago", "TT", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Tunisia", "TN", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Turkey", "TR", "E", "Matrix E: Naher Osten & Nordafrika", "TRY", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "5% - 10%", "15%", "20%", "25%+", "Bahşiş wird in bar auf dem Tisch hinterlassen (nicht per Karte).", notesKey = "note_tr_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up", "20 TL", "50 TL", "100 TL", "Dem Kurier bar zustecken.", notesKey = "note_tr_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "Round up", "5%", "'Kutu' (Trinkgeldbox) steht oft an der Kasse.", notesKey = "note_tr_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Aufrunden ist ausreichend.", notesKey = "note_tr_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "10 TL", "20 TL", "Nur wenn Taschen zum Auto getragen werden.", notesKey = "note_tr_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10%", "15%", "20%", "Direkt beim Bezahlen pro Runde abwickeln.", notesKey = "note_tr_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Turkmenistan", "TM", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Tuvalu", "TV", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("USA", "US", "A", "Matrix A / Custom North America", "USD", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "15% - 18%", "20%", "25%", "30%+", "Tipping is practically mandatory; servers rely on tips for income.", notesKey = "note_us_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "10% - 15%", "18%", "22%", "25%+", "Standard to tip delivery drivers via app or cash.", notesKey = "note_us_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0% - 10%", "15%", "20%", "25%", "Digital screens prompt for tips completely optional.", notesKey = "note_us_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "10%", "15%", "20%", "25%", "Tip jars or digital screen prompts are common.", notesKey = "note_us_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "Round up", "$2 - $5", "$10", "Optional for grocery baggers or curbside pickup.", notesKey = "note_us_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "$1 - $2 per drink", "15%", "20%", "25%", "Standard to tip per drink or open a tab.", notesKey = "note_us_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Uganda", "UG", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Ukraine", "UA", "B", "Matrix B / Custom", "UAH", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Standard expectation for sit-down service.", notesKey = "note_matrix_b_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up", "5%", "10%", "15%", "Couriers appreciate small tips.", notesKey = "note_matrix_b_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Optional tip jars.", notesKey = "note_matrix_b_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Rounding up is common.", notesKey = "note_matrix_b_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "Round up", "No tipping standard inside stores.", notesKey = "note_matrix_b_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "10%", "15%", "20%", "25%", "Tipping around 10% is standard in bars and pubs.", notesKey = "note_ua_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("United Arab Emirates", "AE", "E", "Matrix E", "AED", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Service charge is usually included but extra is traditional for servers.", notesKey = "note_ae_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "5 - 10 AED", "15 AED", "20 AED", "50 AED", "Very common via app or cash.", notesKey = "note_ae_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "Round up", "5%", "Not standard.", notesKey = "note_ae_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "Round up", "5%", "Not standard.", notesKey = "note_ae_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "5 AED", "10 AED", "20 AED", "Small tips for grocery loading helpers are common.", notesKey = "note_ae_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10%", "15%", "20%", "Appreciated at luxury venues.", notesKey = "note_ae_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("United Kingdom", "GB", "C", "Matrix C", "GBP", true, mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10% - 12.5%", "15%", "20%", "25%+", "An optional 12.5% service charge is frequently added.", notesKey = "note_gb_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up / £1-£2", "£3", "£5", "10%", "Common for app food deliveries.", notesKey = "note_gb_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "5%", "Not expected.", notesKey = "note_gb_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Optional tip jars.", notesKey = "note_gb_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_gb_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "Round up", "Change", "£5", "Rarely expected unless table service.", notesKey = "note_gb_bar", serviceTypeKey = "service_bar")
            )),
            createCountry("Uruguay", "UY", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Uzbekistan", "UZ", "B", "Matrix B: Osteuropa, Balkan & Zentralasien", null, false),
            createCountry("Vanuatu", "VU", "C", "Matrix C: West-/Nordeuropa & Ozeanien", null, false),
            createCountry("Venezuela", "VE", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Vietnam", "VN", "D", "Matrix D: Ost- & Südostasien", null, false),
            createCountry("Yemen", "YE", "E", "Matrix E: Naher Osten & Nordafrika", null, false),
            createCountry("Zambia", "ZM", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false),
            createCountry("Zimbabwe", "ZW", "A", "Matrix A: Lateinamerika, Karibik & Subsahara-Afrika", null, false)
        )
    }

    /**
     * Factory function to instantiate a [CountryTippingData] record with intelligent currency and rule fallbacks.
     *
     * Feature:
     * - Resolves local currency code from explicit argument, ISO code lookup, country name lookup, or regional matrix fallback.
     * - Assigns custom category service rules or falls back to baseline [getMatrixServices].
     *
     * Dependencies:
     * - [CurrencyOption.forCountry], [CurrencyOption.getNearestCurrencyFor].
     * - [getMatrixServices].
     */
    private fun createCountry(
        country: String,
        iso: String,
        baseMatrix: String,
        matrixName: String,
        localCurrency: String?,
        hasCustomRules: Boolean,
        customServices: Map<String, ServiceRule>? = null
    ): CountryTippingData {
        val resolvedCurrency = localCurrency?.takeIf { it.isNotBlank() }
            ?: CurrencyOption.forCountry(iso)?.code
            ?: CurrencyOption.forCountry(country)?.code
            ?: CurrencyOption.getNearestCurrencyFor(iso, baseMatrix).code
        val services = customServices ?: getMatrixServices(baseMatrix)
        return CountryTippingData(
            country = country,
            iso = iso,
            baseMatrix = baseMatrix,
            matrixName = matrixName,
            localCurrency = resolvedCurrency,
            hasCustomRules = hasCustomRules,
            services = services
        )
    }

    /**
     * Generates standard service tipping rules for a cultural matrix archetype (A through E).
     *
     * Feature:
     * - Matrix A: Americas & high-tip culture.
     * - Matrix B: Eastern Europe & Central Asia.
     * - Matrix C: Western Europe & Oceania.
     * - Matrix D: East Asia (tipping uncommon/discouraged).
     * - Matrix E: Middle East & North Africa (Baksheesh culture).
     *
     * @param matrixCode Matrix archetype letter ("A", "B", "C", "D", "E").
     * @return Map of category name to [ServiceRule].
     */
    private fun getMatrixServices(matrixCode: String): Map<String, ServiceRule> {
        return when (matrixCode) {
            "A" -> mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Propina is highly expected; cash preferred.", notesKey = "note_matrix_a_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "5% - 10%", "15%", "20%", "25%", "Hand cash directly to the driver.", notesKey = "note_matrix_a_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "Round up", "5%", "Not expected for counter pickup.", notesKey = "note_matrix_a_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Optional change jars.", notesKey = "note_matrix_a_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "Small Change", "Small Bill", "Large Bill", "Premium", "Baggers often work solely for tips.", notesKey = "note_matrix_a_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10%", "15%", "20%", "Tip per round or on final tab.", notesKey = "note_matrix_a_bar", serviceTypeKey = "service_bar")
            )
            "B" -> mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Standard expectation for sit-down service.", notesKey = "note_matrix_b_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up", "5%", "10%", "15%", "Couriers appreciate small tips.", notesKey = "note_matrix_b_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Optional tip jars.", notesKey = "note_matrix_b_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Rounding up is common.", notesKey = "note_matrix_b_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "Round up", "No tipping standard inside stores.", notesKey = "note_matrix_b_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "5%", "10%", "15%", "Leave change on the counter.", notesKey = "note_matrix_b_bar", serviceTypeKey = "service_bar")
            )
            "C" -> mapOf(
                "Restaurants" to ServiceRule("Restaurants", "Round up / 5%", "10%", "15%", "20%+", "Service charge included; tips are a bonus.", notesKey = "note_matrix_c_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Round up", "Small Coin", "Small Fixed", "MVP", "Common during bad weather.", notesKey = "note_matrix_c_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "Round up", "5%", "No expectation for pickup.", notesKey = "note_matrix_c_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "Round up", "5%", "Pure rounding up.", notesKey = "note_matrix_c_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "Strictly uncommon.", notesKey = "note_matrix_c_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "Small Change", "5%", "10%", "Change left at the bar counter.", notesKey = "note_matrix_c_bar", serviceTypeKey = "service_bar")
            )
            "D" -> mapOf(
                "Restaurants" to ServiceRule("Restaurants", "0%", "0%", "0%", "0%", "Tipping is uncommon/insulting. 10% service charge in luxury hotels.", notesKey = "note_matrix_d_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "0%", "0%", "0%", "0%", "No tipping expected.", notesKey = "note_matrix_d_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_matrix_d_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_matrix_d_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_matrix_d_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "0%", "0%", "0%", "0%", "No tipping.", notesKey = "note_matrix_d_bar", serviceTypeKey = "service_bar")
            )
            "E" -> mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Baksheesh is customary; leave cash on the table.", notesKey = "note_matrix_e_restaurant", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "Small Fixed", "Medium Fixed", "Generous", "MVP", "Couriers expect hand money.", notesKey = "note_matrix_e_delivery", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "Round up", "5%", "10%", "Tip box at counter.", notesKey = "note_matrix_e_takeout", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "Round up", "Small Fixed", "5%", "10%", "Rounding up appreciated.", notesKey = "note_matrix_e_foodtruck", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "Small Coins", "Medium", "Large", "MVP", "Helpers live on these tips.", notesKey = "note_matrix_e_groceries", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "10%", "15%", "20%", "Appreciated in hotel lounges.", notesKey = "note_matrix_e_bar", serviceTypeKey = "service_bar")
            )
            else -> mapOf(
                "Restaurants" to ServiceRule("Restaurants", "10%", "15%", "20%", "25%+", "Default tipping guideline.", notesKey = "note_default_guideline", serviceTypeKey = "service_restaurants"),
                "Delivery" to ServiceRule("Delivery", "5%", "10%", "15%", "20%", "Couriers appreciate small tips.", notesKey = "note_default_guideline", serviceTypeKey = "service_delivery"),
                "Takeout" to ServiceRule("Takeout", "0%", "0%", "5%", "10%", "No expectation for pickup.", notesKey = "note_default_guideline", serviceTypeKey = "service_takeout"),
                "Foodtrucks" to ServiceRule("Foodtrucks", "0%", "Round up", "5%", "10%", "Tip jars optional.", notesKey = "note_default_guideline", serviceTypeKey = "service_foodtruck"),
                "Groceries" to ServiceRule("Groceries", "0%", "0%", "0%", "0%", "No tipping standard.", notesKey = "note_default_guideline", serviceTypeKey = "service_groceries"),
                "Bar" to ServiceRule("Bar", "Round up", "5%", "10%", "15%", "Small change welcome.", notesKey = "note_default_guideline", serviceTypeKey = "service_bar")
            )
        }
    }
}
