package com.example

import java.util.Locale

/**
 * =========================================================================================
 * FEATURE: Global Currencies & Regional Mapping Registry (CurrencyOption)
 * =========================================================================================
 *
 * Description:
 * Encapsulates ISO 4217 currency specifications, symbols, display labels, formatting
 * conventions (symbol prefix vs. suffix), and lookup mappings for over 140+ world currencies.
 *
 * Components:
 * - [CountryCurrencyMapping]: Maps ISO 3166-1 alpha-2 / alpha-3 codes to sovereign currencies.
 * - [CurrencyOption]: Enum of supported world currencies with custom symbol placement flags.
 *
 * Dependencies:
 * - Used by [LocaleFormatter] to format money strings.
 * - Used by [CountryTippingData] and [CountryDataRegistry] to resolve national currencies.
 * - Used by [TippyViewModel] for UI currency selection state.
 * =========================================================================================
 */

/**
 * Mapping record associating ISO-3166 country codes with their official currency.
 */
data class CountryCurrencyMapping(
    val country: String,
    val alpha2: String,
    val alpha3: String,
    val currencyName: String,
    val currencyCode: String
)

/**
 * Standardized CurrencyOption enum supporting 140+ global currencies.
 *
 * @property code ISO-4217 three-letter currency code (e.g. "USD", "EUR", "JPY").
 * @property symbol Graphical currency sign or abbreviation (e.g. "$", "€", "¥", "zł").
 * @property label English full name of the currency.
 * @property isSymbolAfter Indicates whether the currency symbol typically appears after the amount (e.g. "15 €" vs "$15").
 */
enum class CurrencyOption(
    val code: String,
    val symbol: String,
    val label: String,
    val isSymbolAfter: Boolean = false
) {
    AFN("AFN", "؋", "Afghan Afghani"),
    ALL("ALL", "Lek", "Albanian Lek", true),
    DZD("DZD", "DA", "Algerian Dinar", true),
    EUR("EUR", "€", "Euro"),
    AOA("AOA", "Kz", "Angolan Kwanza"),
    XCD("XCD", "EC$", "East Caribbean Dollar"),
    ARS("ARS", "$", "Argentine Peso"),
    AMD("AMD", "֏", "Armenian Dram", true),
    AUD("AUD", "A$", "Australian Dollar"),
    AZN("AZN", "₼", "Azerbaijani Manat"),
    BSD("BSD", "B$", "Bahamian Dollar"),
    BHD("BHD", "BD", "Bahraini Dinar", true),
    BDT("BDT", "৳", "Bangladeshi Taka"),
    BBD("BBD", "Bds$", "Barbadian Dollar"),
    BYN("BYN", "Br", "Belarusian Ruble", true),
    BZD("BZD", "BZ$", "Belize Dollar"),
    XOF("XOF", "CFA", "West African CFA Franc", true),
    BTN("BTN", "Nu.", "Bhutanese Ngultrum"),
    BOB("BOB", "Bs.", "Bolivian Boliviano"),
    BAM("BAM", "KM", "Bosnia and Herzegovina Convertible Mark", true),
    BWP("BWP", "P", "Botswana Pula"),
    BRL("BRL", "R$", "Brazilian Real"),
    BND("BND", "B$", "Brunei Dollar"),
    BGN("BGN", "лв", "Bulgarian Lev", true),
    BIF("BIF", "FBu", "Burundian Franc", true),
    CVE("CVE", "Esc", "Cape Verdean Escudo", true),
    KHR("KHR", "៛", "Cambodian Riel", true),
    XAF("XAF", "FCFA", "Central African CFA Franc", true),
    CAD("CAD", "C$", "Canadian Dollar"),
    CLP("CLP", "$", "Chilean Peso"),
    CNY("CNY", "¥", "Chinese Yuan"),
    COP("COP", "Col$", "Colombian Peso"),
    KMF("KMF", "CF", "Comorian Franc", true),
    CDF("CDF", "FC", "Congolese Franc", true),
    CRC("CRC", "₡", "Costa Rican Colón"),
    CUP("CUP", "$", "Cuban Peso"),
    CZK("CZK", "Kč", "Czech Koruna", true),
    DKK("DKK", "kr.", "Danish Krone", true),
    DJF("DJF", "Fdj", "Djiboutian Franc", true),
    DOP("DOP", "RD$", "Dominican Peso"),
    USD("USD", "$", "United States Dollar"),
    EGP("EGP", "E£", "Egyptian Pound"),
    ERN("ERN", "Nfk", "Eritrean Nakfa"),
    SZL("SZL", "E", "Swazi Lilangeni"),
    ETB("ETB", "Br", "Ethiopian Birr"),
    FJD("FJD", "FJ$", "Fijian Dollar"),
    GMD("GMD", "D", "Gambian Dalasi"),
    GEL("GEL", "₾", "Georgian Lari"),
    GHS("GHS", "GH₵", "Ghanaian Cedi"),
    GTQ("GTQ", "Q", "Guatemalan Quetzal"),
    GNF("GNF", "FG", "Guinean Franc", true),
    GYD("GYD", "G$", "Guyanese Dollar"),
    HTG("HTG", "G", "Haitian Gourde"),
    HNL("HNL", "L", "Honduran Lempira"),
    HKD("HKD", "HK$", "Hong Kong Dollar"),
    HUF("HUF", "Ft", "Hungarian Forint", true),
    ISK("ISK", "kr", "Icelandic Króna", true),
    INR("INR", "₹", "Indian Rupee"),
    IDR("IDR", "Rp", "Indonesian Rupiah"),
    IRR("IRR", "﷼", "Iranian Rial", true),
    IQD("IQD", "ع.د", "Iraqi Dinar", true),
    ILS("ILS", "₪", "Israeli New Shekel"),
    JMD("JMD", "J$", "Jamaican Dollar"),
    JPY("JPY", "¥", "Japanese Yen"),
    JOD("JOD", "JD", "Jordanian Dinar", true),
    KZT("KZT", "₸", "Kazakhstani Tenge"),
    KES("KES", "KSh", "Kenyan Shilling"),
    KWD("KWD", "KD", "Kuwaiti Dinar", true),
    KGS("KGS", "с", "Kyrgyzstani Som", true),
    LAK("LAK", "₭", "Lao Kip"),
    LBP("LBP", "L£", "Lebanese Pound"),
    LSL("LSL", "L", "Lesotho Loti"),
    LRD("LRD", "L$", "Liberian Dollar"),
    LYD("LYD", "LD", "Libyan Dinar", true),
    CHF("CHF", "CHF", "Swiss Franc", true),
    MGA("MGA", "Ar", "Malagasy Ariary", true),
    MWK("MWK", "MK", "Malawian Kwacha"),
    MYR("MYR", "RM", "Malaysian Ringgit"),
    MVR("MVR", "Rf", "Maldivian Rufiyaa", true),
    MRU("MRU", "UM", "Mauritanian Ouguiya", true),
    MUR("MUR", "₨", "Mauritian Rupee"),
    MXN("MXN", "Mex$", "Mexican Peso"),
    MDL("MDL", "L", "Moldovan Leu", true),
    MNT("MNT", "₮", "Mongolian Tögrög"),
    MAD("MAD", "MAD", "Moroccan Dirham", true),
    MZN("MZN", "MT", "Mozambican Metical", true),
    MMK("MMK", "K", "Myanmar Kyat"),
    NAD("NAD", "N$", "Namibian Dollar"),
    NPR("NPR", "रू", "Nepalese Rupee"),
    NZD("NZD", "NZ$", "New Zealand Dollar"),
    NIO("NIO", "C$", "Nicaraguan Córdoba"),
    NGN("NGN", "₦", "Nigerian Naira"),
    KPW("KPW", "₩", "North Korean Won"),
    MKD("MKD", "ден", "Macedonian Denar", true),
    NOK("NOK", "kr", "Norwegian Krone", true),
    OMR("OMR", "OMR", "Omani Rial", true),
    PKR("PKR", "₨", "Pakistani Rupee"),
    PAB("PAB", "B/.", "Panamanian Balboa"),
    PGK("PGK", "K", "Papua New Guinean Kina"),
    PYG("PYG", "₲", "Paraguayan Guaraní"),
    PEN("PEN", "S/", "Peruvian Sol"),
    PHP("PHP", "₱", "Philippine Peso"),
    PLN("PLN", "zł", "Polish Złoty", true),
    QAR("QAR", "QR", "Qatari Riyal", true),
    RON("RON", "lei", "Romanian Leu", true),
    RUB("RUB", "₽", "Russian Ruble", true),
    RWF("RWF", "FRw", "Rwandan Franc", true),
    WST("WST", "WS$", "Samoan Tālā"),
    STN("STN", "Db", "São Tomé and Príncipe Dobra", true),
    SAR("SAR", "SAR", "Saudi Riyal", true),
    RSD("RSD", "din.", "Serbian Dinar", true),
    SCR("SCR", "SR", "Seychellois Rupee"),
    SLE("SLE", "Le", "Sierra Leonean Leone"),
    SGD("SGD", "S$", "Singapore Dollar"),
    SBD("SBD", "SI$", "Solomon Islands Dollar"),
    SOS("SOS", "S", "Somali Shilling"),
    ZAR("ZAR", "R", "South African Rand"),
    KRW("KRW", "₩", "South Korean Won"),
    SSP("SSP", "£", "South Sudanese Pound"),
    LKR("LKR", "Rs", "Sri Lankan Rupee"),
    SDG("SDG", "SDG", "Sudanese Pound", true),
    SRD("SRD", "Sr$", "Surinamese Dollar"),
    SEK("SEK", "kr", "Swedish Krona", true),
    SYP("SYP", "£S", "Syrian Pound"),
    TJS("TJS", "SM", "Tajikistani Somoni", true),
    TZS("TZS", "TSh", "Tanzanian Shilling"),
    THB("THB", "฿", "Thai Baht"),
    TWD("TWD", "NT$", "New Taiwan Dollar"),
    TOP("TOP", "T$", "Tongan Paʻanga"),
    TTD("TTD", "TT$", "Trinidad and Tobago Dollar"),
    TND("TND", "DT", "Tunisian Dinar", true),
    TRY("TRY", "₺", "Turkish Lira"),
    TMT("TMT", "m", "Turkmenistan Manat", true),
    UGX("UGX", "USh", "Ugandan Shilling"),
    UAH("UAH", "₴", "Ukrainian Hryvnia"),
    AED("AED", "AED", "UAE Dirham", true),
    GBP("GBP", "£", "Pound Sterling"),
    UYU("UYU", "\$U", "Uruguayan Peso"),
    UZS("UZS", "so'm", "Uzbekistani Som", true),
    VUV("VUV", "VT", "Vanuatu Vatu", true),
    VED("VED", "Bs.", "Venezuelan Bolívar"),
    VND("VND", "₫", "Vietnamese Đồng", true),
    YER("YER", "﷼", "Yemeni Rial", true),
    ZMW("ZMW", "ZK", "Zambian Kwacha"),
    ZWG("ZWG", "ZiG", "Zimbabwe Gold");

    companion object {

        /**
         * Complete dataset of 150+ country-to-currency mappings from ISO alpha2, alpha3, name to currency.
         */
        val ALL_MAPPINGS: List<CountryCurrencyMapping> = listOf(
            CountryCurrencyMapping("Afghanistan", "AF", "AFG", "Afghan Afghani", "AFN"),
            CountryCurrencyMapping("Albania", "AL", "ALB", "Albanian Lek", "ALL"),
            CountryCurrencyMapping("Algeria", "DZ", "DZA", "Algerian Dinar", "DZD"),
            CountryCurrencyMapping("Andorra", "AD", "AND", "Euro", "EUR"),
            CountryCurrencyMapping("Angola", "AO", "AGO", "Angolan Kwanza", "AOA"),
            CountryCurrencyMapping("Antigua and Barbuda", "AG", "ATG", "East Caribbean Dollar", "XCD"),
            CountryCurrencyMapping("Argentina", "AR", "ARG", "Argentine Peso", "ARS"),
            CountryCurrencyMapping("Armenia", "AM", "ARM", "Armenian Dram", "AMD"),
            CountryCurrencyMapping("Australia", "AU", "AUS", "Australian Dollar", "AUD"),
            CountryCurrencyMapping("Austria", "AT", "AUT", "Euro", "EUR"),
            CountryCurrencyMapping("Azerbaijan", "AZ", "AZE", "Azerbaijani Manat", "AZN"),
            CountryCurrencyMapping("Bahamas", "BS", "BHS", "Bahamian Dollar", "BSD"),
            CountryCurrencyMapping("Bahrain", "BH", "BHR", "Bahraini Dinar", "BHD"),
            CountryCurrencyMapping("Bangladesh", "BD", "BGD", "Bangladeshi Taka", "BDT"),
            CountryCurrencyMapping("Barbados", "BB", "BRB", "Barbadian Dollar", "BBD"),
            CountryCurrencyMapping("Belarus", "BY", "BLR", "Belarusian Ruble", "BYN"),
            CountryCurrencyMapping("Belgium", "BE", "BEL", "Euro", "EUR"),
            CountryCurrencyMapping("Belize", "BZ", "BLZ", "Belize Dollar", "BZD"),
            CountryCurrencyMapping("Benin", "BJ", "BEN", "West African CFA Franc", "XOF"),
            CountryCurrencyMapping("Bhutan", "BT", "BTN", "Bhutanese Ngultrum", "BTN"),
            CountryCurrencyMapping("Bolivia", "BO", "BOL", "Bolivian Boliviano", "BOB"),
            CountryCurrencyMapping("Bosnia and Herzegovina", "BA", "BIH", "Bosnia and Herzegovina Convertible Mark", "BAM"),
            CountryCurrencyMapping("Botswana", "BW", "BWA", "Botswana Pula", "BWP"),
            CountryCurrencyMapping("Brazil", "BR", "BRA", "Brazilian Real", "BRL"),
            CountryCurrencyMapping("Brunei", "BN", "BRN", "Brunei Dollar", "BND"),
            CountryCurrencyMapping("Bulgaria", "BG", "BGR", "Bulgarian Lev", "BGN"),
            CountryCurrencyMapping("Burkina Faso", "BF", "BFA", "West African CFA Franc", "XOF"),
            CountryCurrencyMapping("Burundi", "BI", "BDI", "Burundian Franc", "BIF"),
            CountryCurrencyMapping("Cabo Verde", "CV", "CPV", "Cape Verdean Escudo", "CVE"),
            CountryCurrencyMapping("Cambodia", "KH", "KHM", "Cambodian Riel", "KHR"),
            CountryCurrencyMapping("Cameroon", "CM", "CMR", "Central African CFA Franc", "XAF"),
            CountryCurrencyMapping("Canada", "CA", "CAN", "Canadian Dollar", "CAD"),
            CountryCurrencyMapping("Central African Republic", "CF", "CAF", "Central African CFA Franc", "XAF"),
            CountryCurrencyMapping("Chad", "TD", "TCD", "Central African CFA Franc", "XAF"),
            CountryCurrencyMapping("Chile", "CL", "CHL", "Chilean Peso", "CLP"),
            CountryCurrencyMapping("China", "CN", "CHN", "Chinese Yuan", "CNY"),
            CountryCurrencyMapping("Colombia", "CO", "COL", "Colombian Peso", "COP"),
            CountryCurrencyMapping("Comoros", "KM", "COM", "Comorian Franc", "KMF"),
            CountryCurrencyMapping("Congo (Republic)", "CG", "COG", "Central African CFA Franc", "XAF"),
            CountryCurrencyMapping("Congo (DRC)", "CD", "COD", "Congolese Franc", "CDF"),
            CountryCurrencyMapping("Costa Rica", "CR", "CRI", "Costa Rican Colón", "CRC"),
            CountryCurrencyMapping("Croatia", "HR", "HRV", "Euro", "EUR"),
            CountryCurrencyMapping("Cuba", "CU", "CUB", "Cuban Peso", "CUP"),
            CountryCurrencyMapping("Cyprus", "CY", "CYP", "Euro", "EUR"),
            CountryCurrencyMapping("Czech Republic", "CZ", "CZE", "Czech Koruna", "CZK"),
            CountryCurrencyMapping("Denmark", "DK", "DNK", "Danish Krone", "DKK"),
            CountryCurrencyMapping("Djibouti", "DJ", "DJI", "Djiboutian Franc", "DJF"),
            CountryCurrencyMapping("Dominica", "DM", "DMA", "East Caribbean Dollar", "XCD"),
            CountryCurrencyMapping("Dominican Republic", "DO", "DOM", "Dominican Peso", "DOP"),
            CountryCurrencyMapping("Ecuador", "EC", "ECU", "United States Dollar", "USD"),
            CountryCurrencyMapping("Egypt", "EG", "EGY", "Egyptian Pound", "EGP"),
            CountryCurrencyMapping("El Salvador", "SV", "SLV", "United States Dollar", "USD"),
            CountryCurrencyMapping("Equatorial Guinea", "GQ", "GNQ", "Central African CFA Franc", "XAF"),
            CountryCurrencyMapping("Eritrea", "ER", "ERI", "Eritrean Nakfa", "ERN"),
            CountryCurrencyMapping("Estonia", "EE", "EST", "Euro", "EUR"),
            CountryCurrencyMapping("Eswatini", "SZ", "SWZ", "Swazi Lilangeni", "SZL"),
            CountryCurrencyMapping("Ethiopia", "ET", "ETH", "Ethiopian Birr", "ETB"),
            CountryCurrencyMapping("Fiji", "FJ", "FJI", "Fijian Dollar", "FJD"),
            CountryCurrencyMapping("Finland", "FI", "FIN", "Euro", "EUR"),
            CountryCurrencyMapping("France", "FR", "FRA", "Euro", "EUR"),
            CountryCurrencyMapping("Gabon", "GA", "GAB", "Central African CFA Franc", "XAF"),
            CountryCurrencyMapping("Gambia", "GM", "GMB", "Gambian Dalasi", "GMD"),
            CountryCurrencyMapping("Georgia", "GE", "GEO", "Georgian Lari", "GEL"),
            CountryCurrencyMapping("Germany", "DE", "DEU", "Euro", "EUR"),
            CountryCurrencyMapping("Ghana", "GH", "GHA", "Ghanaian Cedi", "GHS"),
            CountryCurrencyMapping("Greece", "GR", "GRC", "Euro", "EUR"),
            CountryCurrencyMapping("Grenada", "GD", "GRD", "East Caribbean Dollar", "XCD"),
            CountryCurrencyMapping("Guatemala", "GT", "GTM", "Guatemalan Quetzal", "GTQ"),
            CountryCurrencyMapping("Guinea", "GN", "GIN", "Guinean Franc", "GNF"),
            CountryCurrencyMapping("Guinea-Bissau", "GW", "GNB", "West African CFA Franc", "XOF"),
            CountryCurrencyMapping("Guyana", "GY", "GUY", "Guyanese Dollar", "GYD"),
            CountryCurrencyMapping("Haiti", "HT", "HTI", "Haitian Gourde", "HTG"),
            CountryCurrencyMapping("Honduras", "HN", "HND", "Honduran Lempira", "HNL"),
            CountryCurrencyMapping("Hong Kong", "HK", "HKG", "Hong Kong Dollar", "HKD"),
            CountryCurrencyMapping("Hungary", "HU", "HUN", "Hungarian Forint", "HUF"),
            CountryCurrencyMapping("Iceland", "IS", "ISL", "Icelandic Króna", "ISK"),
            CountryCurrencyMapping("India", "IN", "IND", "Indian Rupee", "INR"),
            CountryCurrencyMapping("Indonesia", "ID", "IDN", "Indonesian Rupiah", "IDR"),
            CountryCurrencyMapping("Iran", "IR", "IRN", "Iranian Rial", "IRR"),
            CountryCurrencyMapping("Iraq", "IQ", "IRQ", "Iraqi Dinar", "IQD"),
            CountryCurrencyMapping("Ireland", "IE", "IRL", "Euro", "EUR"),
            CountryCurrencyMapping("Israel", "IL", "ISR", "Israeli New Shekel", "ILS"),
            CountryCurrencyMapping("Italy", "IT", "ITA", "Euro", "EUR"),
            CountryCurrencyMapping("Ivory Coast", "CI", "CIV", "West African CFA Franc", "XOF"),
            CountryCurrencyMapping("Jamaica", "JM", "JAM", "Jamaican Dollar", "JMD"),
            CountryCurrencyMapping("Japan", "JP", "JPN", "Japanese Yen", "JPY"),
            CountryCurrencyMapping("Jordan", "JO", "JOR", "Jordanian Dinar", "JOD"),
            CountryCurrencyMapping("Kazakhstan", "KZ", "KAZ", "Kazakhstani Tenge", "KZT"),
            CountryCurrencyMapping("Kenya", "KE", "KEN", "Kenyan Shilling", "KES"),
            CountryCurrencyMapping("Kiribati", "KI", "KIR", "Australian Dollar", "AUD"),
            CountryCurrencyMapping("Kuwait", "KW", "KWT", "Kuwaiti Dinar", "KWD"),
            CountryCurrencyMapping("Kyrgyzstan", "KG", "KGZ", "Kyrgyzstani Som", "KGS"),
            CountryCurrencyMapping("Laos", "LA", "LAO", "Lao Kip", "LAK"),
            CountryCurrencyMapping("Latvia", "LV", "LVA", "Euro", "EUR"),
            CountryCurrencyMapping("Lebanon", "LB", "LBN", "Lebanese Pound", "LBP"),
            CountryCurrencyMapping("Lesotho", "LS", "LSO", "Lesotho Loti", "LSL"),
            CountryCurrencyMapping("Liberia", "LR", "LBR", "Liberian Dollar", "LRD"),
            CountryCurrencyMapping("Libya", "LY", "LBY", "Libyan Dinar", "LYD"),
            CountryCurrencyMapping("Liechtenstein", "LI", "LIE", "Swiss Franc", "CHF"),
            CountryCurrencyMapping("Lithuania", "LT", "LTU", "Euro", "EUR"),
            CountryCurrencyMapping("Luxembourg", "LU", "LUX", "Euro", "EUR"),
            CountryCurrencyMapping("Madagascar", "MG", "MDG", "Malagasy Ariary", "MGA"),
            CountryCurrencyMapping("Malawi", "MW", "MWI", "Malawian Kwacha", "MWK"),
            CountryCurrencyMapping("Malaysia", "MY", "MYS", "Malaysian Ringgit", "MYR"),
            CountryCurrencyMapping("Maldives", "MV", "MDV", "Maldivian Rufiyaa", "MVR"),
            CountryCurrencyMapping("Mali", "ML", "MLI", "West African CFA Franc", "XOF"),
            CountryCurrencyMapping("Malta", "MT", "MLT", "Euro", "EUR"),
            CountryCurrencyMapping("Marshall Islands", "MH", "MHL", "United States Dollar", "USD"),
            CountryCurrencyMapping("Mauritania", "MR", "MRT", "Mauritanian Ouguiya", "MRU"),
            CountryCurrencyMapping("Mauritius", "MU", "MUS", "Mauritian Rupee", "MUR"),
            CountryCurrencyMapping("Mexico", "MX", "MEX", "Mexican Peso", "MXN"),
            CountryCurrencyMapping("Micronesia", "FM", "FSM", "United States Dollar", "USD"),
            CountryCurrencyMapping("Moldova", "MD", "MDA", "Moldovan Leu", "MDL"),
            CountryCurrencyMapping("Monaco", "MC", "MCO", "Euro", "EUR"),
            CountryCurrencyMapping("Mongolia", "MN", "MNG", "Mongolian Tögrög", "MNT"),
            CountryCurrencyMapping("Montenegro", "ME", "MNE", "Euro", "EUR"),
            CountryCurrencyMapping("Morocco", "MA", "MAR", "Moroccan Dirham", "MAD"),
            CountryCurrencyMapping("Mozambique", "MZ", "MOZ", "Mozambican Metical", "MZN"),
            CountryCurrencyMapping("Myanmar", "MM", "MMR", "Myanmar Kyat", "MMK"),
            CountryCurrencyMapping("Namibia", "NA", "NAM", "Namibian Dollar", "NAD"),
            CountryCurrencyMapping("Nauru", "NR", "NRU", "Australian Dollar", "AUD"),
            CountryCurrencyMapping("Nepal", "NP", "NPL", "Nepalese Rupee", "NPR"),
            CountryCurrencyMapping("Netherlands", "NL", "NLD", "Euro", "EUR"),
            CountryCurrencyMapping("New Zealand", "NZ", "NZL", "New Zealand Dollar", "NZD"),
            CountryCurrencyMapping("Nicaragua", "NI", "NIC", "Nicaraguan Córdoba", "NIO"),
            CountryCurrencyMapping("Niger", "NE", "NER", "West African CFA Franc", "XOF"),
            CountryCurrencyMapping("Nigeria", "NG", "NGA", "Nigerian Naira", "NGN"),
            CountryCurrencyMapping("North Korea", "KP", "PRK", "North Korean Won", "KPW"),
            CountryCurrencyMapping("North Macedonia", "MK", "MKD", "Macedonian Denar", "MKD"),
            CountryCurrencyMapping("Norway", "NO", "NOR", "Norwegian Krone", "NOK"),
            CountryCurrencyMapping("Oman", "OM", "OMN", "Omani Rial", "OMR"),
            CountryCurrencyMapping("Pakistan", "PK", "PAK", "Pakistani Rupee", "PKR"),
            CountryCurrencyMapping("Palau", "PW", "PLW", "United States Dollar", "USD"),
            CountryCurrencyMapping("Palestine", "PS", "PSE", "Israeli New Shekel", "ILS"),
            CountryCurrencyMapping("Panama", "PA", "PAN", "Panamanian Balboa", "PAB"),
            CountryCurrencyMapping("Papua New Guinea", "PG", "PNG", "Papua New Guinean Kina", "PGK"),
            CountryCurrencyMapping("Paraguay", "PY", "PRY", "Paraguayan Guaraní", "PYG"),
            CountryCurrencyMapping("Peru", "PE", "PER", "Peruvian Sol", "PEN"),
            CountryCurrencyMapping("Philippines", "PH", "PHL", "Philippine Peso", "PHP"),
            CountryCurrencyMapping("Poland", "PL", "POL", "Polish Złoty", "PLN"),
            CountryCurrencyMapping("Portugal", "PT", "PRT", "Euro", "EUR"),
            CountryCurrencyMapping("Qatar", "QA", "QAT", "Qatari Riyal", "QAR"),
            CountryCurrencyMapping("Romania", "RO", "ROU", "Romanian Leu", "RON"),
            CountryCurrencyMapping("Russia", "RU", "RUS", "Russian Ruble", "RUB"),
            CountryCurrencyMapping("Rwanda", "RW", "RWA", "Rwandan Franc", "RWF"),
            CountryCurrencyMapping("Saint Kitts and Nevis", "KN", "KNA", "East Caribbean Dollar", "XCD"),
            CountryCurrencyMapping("Saint Lucia", "LC", "LCA", "East Caribbean Dollar", "XCD"),
            CountryCurrencyMapping("Saint Vincent and the Grenadines", "VC", "VCT", "East Caribbean Dollar", "XCD"),
            CountryCurrencyMapping("Samoa", "WS", "WSM", "Samoan Tālā", "WST"),
            CountryCurrencyMapping("San Marino", "SM", "SMR", "Euro", "EUR"),
            CountryCurrencyMapping("Sao Tome and Principe", "ST", "STP", "São Tomé and Príncipe Dobra", "STN"),
            CountryCurrencyMapping("Saudi Arabia", "SA", "SAU", "Saudi Riyal", "SAR"),
            CountryCurrencyMapping("Senegal", "SN", "SEN", "West African CFA Franc", "XOF"),
            CountryCurrencyMapping("Serbia", "RS", "SRB", "Serbian Dinar", "RSD"),
            CountryCurrencyMapping("Seychelles", "SC", "SYC", "Seychellois Rupee", "SCR"),
            CountryCurrencyMapping("Sierra Leone", "SL", "SLE", "Sierra Leonean Leone", "SLE"),
            CountryCurrencyMapping("Singapore", "SG", "SGP", "Singapore Dollar", "SGD"),
            CountryCurrencyMapping("Slovakia", "SK", "SVK", "Euro", "EUR"),
            CountryCurrencyMapping("Slovenia", "SI", "SVN", "Euro", "EUR"),
            CountryCurrencyMapping("Solomon Islands", "SB", "SLB", "Solomon Islands Dollar", "SBD"),
            CountryCurrencyMapping("Somalia", "SO", "SOM", "Somali Shilling", "SOS"),
            CountryCurrencyMapping("South Africa", "ZA", "ZAF", "South African Rand", "ZAR"),
            CountryCurrencyMapping("South Korea", "KR", "KOR", "South Korean Won", "KRW"),
            CountryCurrencyMapping("South Sudan", "SS", "SSD", "South Sudanese Pound", "SSP"),
            CountryCurrencyMapping("Spain", "ES", "ESP", "Euro", "EUR"),
            CountryCurrencyMapping("Sri Lanka", "LK", "LKA", "Sri Lankan Rupee", "LKR"),
            CountryCurrencyMapping("Sudan", "SD", "SDN", "Sudanese Pound", "SDG"),
            CountryCurrencyMapping("Suriname", "SR", "SUR", "Surinamese Dollar", "SRD"),
            CountryCurrencyMapping("Sweden", "SE", "SWE", "Swedish Krona", "SEK"),
            CountryCurrencyMapping("Switzerland", "CH", "CHE", "Swiss Franc", "CHF"),
            CountryCurrencyMapping("Syria", "SY", "SYR", "Syrian Pound", "SYP"),
            CountryCurrencyMapping("Taiwan", "TW", "TWN", "New Taiwan Dollar", "TWD"),
            CountryCurrencyMapping("Tajikistan", "TJ", "TJK", "Tajikistani Somoni", "TJS"),
            CountryCurrencyMapping("Tanzania", "TZ", "TZA", "Tanzanian Shilling", "TZS"),
            CountryCurrencyMapping("Thailand", "TH", "THA", "Thai Baht", "THB"),
            CountryCurrencyMapping("Timor-Leste", "TL", "TLS", "United States Dollar", "USD"),
            CountryCurrencyMapping("Togo", "TG", "TGO", "West African CFA Franc", "XOF"),
            CountryCurrencyMapping("Tonga", "TO", "TON", "Tongan Paʻanga", "TOP"),
            CountryCurrencyMapping("Trinidad and Tobago", "TT", "TTO", "Trinidad and Tobago Dollar", "TTD"),
            CountryCurrencyMapping("Tunisia", "TN", "TUN", "Tunisian Dinar", "TND"),
            CountryCurrencyMapping("Turkey", "TR", "TUR", "Turkish Lira", "TRY"),
            CountryCurrencyMapping("Turkmenistan", "TM", "TKM", "Turkmenistan Manat", "TMT"),
            CountryCurrencyMapping("Tuvalu", "TV", "TUV", "Australian Dollar", "AUD"),
            CountryCurrencyMapping("Uganda", "UG", "UGA", "Ugandan Shilling", "UGX"),
            CountryCurrencyMapping("Ukraine", "UA", "UKR", "Ukrainian Hryvnia", "UAH"),
            CountryCurrencyMapping("United Arab Emirates", "AE", "ARE", "UAE Dirham", "AED"),
            CountryCurrencyMapping("United Kingdom", "GB", "GBR", "Pound Sterling", "GBP"),
            CountryCurrencyMapping("United States", "US", "USA", "United States Dollar", "USD"),
            CountryCurrencyMapping("Uruguay", "UY", "URY", "Uruguayan Peso", "UYU"),
            CountryCurrencyMapping("Uzbekistan", "UZ", "UZB", "Uzbekistani Som", "UZS"),
            CountryCurrencyMapping("Vanuatu", "VU", "VUT", "Vanuatu Vatu", "VUV"),
            CountryCurrencyMapping("Vatican City", "VA", "VAT", "Euro", "EUR"),
            CountryCurrencyMapping("Venezuela", "VE", "VEN", "Venezuelan Bolívar", "VED"),
            CountryCurrencyMapping("Vietnam", "VN", "VNM", "Vietnamese Đồng", "VND"),
            CountryCurrencyMapping("Yemen", "YE", "YEM", "Yemeni Rial", "YER"),
            CountryCurrencyMapping("Zambia", "ZM", "ZMB", "Zambian Kwacha", "ZMW"),
            CountryCurrencyMapping("Zimbabwe", "ZW", "ZWE", "Zimbabwe Gold", "ZWG")
        )

        private val MAP_BY_ALPHA2: Map<String, CountryCurrencyMapping> =
            ALL_MAPPINGS.associateBy { it.alpha2.uppercase(Locale.US) }

        private val MAP_BY_ALPHA3: Map<String, CountryCurrencyMapping> =
            ALL_MAPPINGS.associateBy { it.alpha3.uppercase(Locale.US) }

        private val MAP_BY_NAME: Map<String, CountryCurrencyMapping> =
            ALL_MAPPINGS.associateBy { it.country.lowercase(Locale.US) }

        private val MAP_BY_CODE: Map<String, CurrencyOption> =
            values().associateBy { it.code.uppercase(Locale.US) }

        /**
         * Resolves a [CurrencyOption] enum entry by its 3-letter ISO-4217 code (case-insensitive).
         *
         * @param code ISO-4217 code (e.g. "USD", "EUR", "JPY").
         * @return Matching [CurrencyOption], or null if not found.
         */
        fun fromCode(code: String?): CurrencyOption? {
            if (code.isNullOrBlank()) return null
            return MAP_BY_CODE[code.trim().uppercase(Locale.US)]
        }

        /**
         * Resolves currency by country ISO alpha-2 code, ISO alpha-3 code, or country name.
         *
         * Dependencies:
         * - [MAP_BY_ALPHA2]: Fast lookup by 2-letter ISO code.
         * - [MAP_BY_ALPHA3]: Fast lookup by 3-letter ISO code.
         * - [MAP_BY_NAME]: Fast lookup by exact country name.
         * - [ALL_MAPPINGS]: Substring search fallback.
         *
         * @param countryInput ISO code or country name.
         * @return Matching [CurrencyOption] or null.
         */
        fun forCountry(countryInput: String?): CurrencyOption? {
            if (countryInput.isNullOrBlank()) return null
            val clean = countryInput.trim()
            val upper = clean.uppercase(Locale.US)
            val lower = clean.lowercase(Locale.US)

            val mapping = MAP_BY_ALPHA2[upper]
                ?: MAP_BY_ALPHA3[upper]
                ?: MAP_BY_NAME[lower]
                ?: ALL_MAPPINGS.firstOrNull { it.country.contains(clean, ignoreCase = true) }

            return mapping?.let { fromCode(it.currencyCode) }
        }

        /**
         * Finds the exact currency or nearest regional fallback currency for an ISO country or matrix archetype.
         *
         * Dependencies:
         * - [MAP_BY_ALPHA2]: Primary mapping lookup.
         * - Falls back to regional macro-currencies (EUR for European matrices, USD for Americas/Asia/Middle East).
         *
         * @param countryIso 2-letter ISO country code.
         * @param matrixCode Matrix archetype code (A through E).
         * @return Resolved [CurrencyOption].
         */
        fun getNearestCurrencyFor(countryIso: String, matrixCode: String): CurrencyOption {
            val upperIso = countryIso.uppercase(Locale.US).trim()

            // 1. Direct country mapping lookup
            MAP_BY_ALPHA2[upperIso]?.let { mapping ->
                fromCode(mapping.currencyCode)?.let { return it }
            }

            // 2. Regional matrix fallback
            return when (matrixCode) {
                "C" -> EUR // West-/Nordeuropa & Ozeanien
                "B" -> EUR // Osteuropa, Balkan & Zentralasien fallback
                "D" -> USD // Asia fallback
                "E" -> USD // Middle East / North Africa international fallback
                "A" -> USD // Latin America / Africa fallback
                else -> USD
            }
        }
    }
}
