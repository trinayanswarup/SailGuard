package com.sailguard.app.data.repository

import com.sailguard.app.data.model.SailyPlan
import com.sailguard.app.data.model.UsageStyle

enum class Region(val displayName: String, val emoji: String, val description: String) {
    EUROPE  ("Europe",   "🇪🇺", "EU, UK & more"),
    ASIA    ("Asia",     "🌏", "East & Southeast Asia"),
    AMERICAS("Americas", "🌎", "North & South America"),
    GLOBAL  ("Global",   "🌐", "200+ destinations")
}

object PlanRepository {

    data class CountryInfo(val name: String, val code: String, val flag: String)

    // ── Pricing tiers ─────────────────────────────────────────────────────────
    // Each tier: list of (dataGB, priceUSD) for fixed plans + unlimited 15-day base

    private data class TierDef(
        val dataPlans: List<Pair<Double, Double>>,
        val unlimitedBase15d: Double
    )

    private val T_BALTIC = TierDef(
        listOf(1.0 to 3.99, 3.0 to 7.99, 5.0 to 11.99, 10.0 to 19.99, 20.0 to 29.99),
        unlimitedBase15d = 18.99)

    private val T_E_EU = TierDef(
        listOf(1.0 to 4.49, 3.0 to 9.99, 5.0 to 15.99, 10.0 to 27.99, 20.0 to 42.99),
        unlimitedBase15d = 29.99)

    private val T_EUROPE = TierDef(
        listOf(1.0 to 4.99, 3.0 to 12.49, 5.0 to 19.49, 10.0 to 35.99, 50.0 to 95.99),
        unlimitedBase15d = 49.99)

    private val T_UK = TierDef(
        listOf(1.0 to 5.99, 3.0 to 14.99, 5.0 to 22.99, 10.0 to 39.99, 20.0 to 59.99),
        unlimitedBase15d = 54.99)

    private val T_AS_CHE = TierDef(
        listOf(1.0 to 4.99, 3.0 to 10.99, 5.0 to 16.99, 10.0 to 28.99, 20.0 to 44.99),
        unlimitedBase15d = 32.99)

    private val T_AS_MID = TierDef(
        listOf(1.0 to 6.49, 3.0 to 14.99, 5.0 to 23.99, 10.0 to 42.99, 20.0 to 59.99),
        unlimitedBase15d = 49.99)

    private val T_AS_PRE = TierDef(
        listOf(1.0 to 7.99, 3.0 to 17.99, 5.0 to 28.99, 10.0 to 46.99, 20.0 to 64.99),
        unlimitedBase15d = 69.99)

    private val T_AM_N = TierDef(
        listOf(1.0 to 8.99, 3.0 to 19.99, 5.0 to 33.99, 10.0 to 49.99, 20.0 to 66.99),
        unlimitedBase15d = 89.99)

    private val T_AM_L = TierDef(
        listOf(1.0 to 6.99, 3.0 to 15.99, 5.0 to 25.99, 10.0 to 44.99, 20.0 to 61.99),
        unlimitedBase15d = 64.99)

    private val T_GLOBAL = TierDef(
        listOf(1.0 to 8.99, 3.0 to 19.99, 5.0 to 33.99, 10.0 to 49.99, 20.0 to 66.99),
        unlimitedBase15d = 89.99)

    // ── Country master list ───────────────────────────────────────────────────

    private data class CountryEntry(val info: CountryInfo, val tier: TierDef, val region: Region)

    private val allEntries: List<CountryEntry> = listOf(
        // ── Europe – Baltic
        CountryEntry(CountryInfo("Estonia",   "EE", "🇪🇪"), T_BALTIC, Region.EUROPE),
        CountryEntry(CountryInfo("Latvia",    "LV", "🇱🇻"), T_BALTIC, Region.EUROPE),
        CountryEntry(CountryInfo("Lithuania", "LT", "🇱🇹"), T_BALTIC, Region.EUROPE),
        // ── Europe – Eastern EU
        CountryEntry(CountryInfo("Albania",              "AL", "🇦🇱"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Bosnia & Herzegovina", "BA", "🇧🇦"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Bulgaria",             "BG", "🇧🇬"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Croatia",              "HR", "🇭🇷"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Czech Republic",       "CZ", "🇨🇿"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Hungary",              "HU", "🇭🇺"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Kosovo",               "XK", "🇽🇰"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Moldova",              "MD", "🇲🇩"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Montenegro",           "ME", "🇲🇪"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("North Macedonia",      "MK", "🇲🇰"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Poland",               "PL", "🇵🇱"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Romania",              "RO", "🇷🇴"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Serbia",               "RS", "🇷🇸"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Slovakia",             "SK", "🇸🇰"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Slovenia",             "SI", "🇸🇮"), T_E_EU, Region.EUROPE),
        CountryEntry(CountryInfo("Ukraine",              "UA", "🇺🇦"), T_E_EU, Region.EUROPE),
        // ── Europe – Western / Nordic
        CountryEntry(CountryInfo("Austria",       "AT", "🇦🇹"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Belgium",       "BE", "🇧🇪"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Cyprus",        "CY", "🇨🇾"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Denmark",       "DK", "🇩🇰"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Finland",       "FI", "🇫🇮"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("France",        "FR", "🇫🇷"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Germany",       "DE", "🇩🇪"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Greece",        "GR", "🇬🇷"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Iceland",       "IS", "🇮🇸"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Ireland",       "IE", "🇮🇪"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Italy",         "IT", "🇮🇹"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Liechtenstein", "LI", "🇱🇮"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Luxembourg",    "LU", "🇱🇺"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Malta",         "MT", "🇲🇹"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Netherlands",   "NL", "🇳🇱"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Norway",        "NO", "🇳🇴"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Portugal",      "PT", "🇵🇹"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Spain",         "ES", "🇪🇸"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Sweden",        "SE", "🇸🇪"), T_EUROPE, Region.EUROPE),
        CountryEntry(CountryInfo("Switzerland",   "CH", "🇨🇭"), T_EUROPE, Region.EUROPE),
        // ── Europe – UK
        CountryEntry(CountryInfo("United Kingdom", "GB", "🇬🇧"), T_UK, Region.EUROPE),
        // ── Asia – Cheap
        CountryEntry(CountryInfo("Bangladesh", "BD", "🇧🇩"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Cambodia",   "KH", "🇰🇭"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("India",      "IN", "🇮🇳"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Indonesia",  "ID", "🇮🇩"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Laos",       "LA", "🇱🇦"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Myanmar",    "MM", "🇲🇲"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Nepal",      "NP", "🇳🇵"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Pakistan",   "PK", "🇵🇰"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Philippines","PH", "🇵🇭"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Sri Lanka",  "LK", "🇱🇰"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Thailand",   "TH", "🇹🇭"), T_AS_CHE, Region.ASIA),
        CountryEntry(CountryInfo("Vietnam",    "VN", "🇻🇳"), T_AS_CHE, Region.ASIA),
        // ── Asia – Mid
        CountryEntry(CountryInfo("Brunei",   "BN", "🇧🇳"), T_AS_MID, Region.ASIA),
        CountryEntry(CountryInfo("China",    "CN", "🇨🇳"), T_AS_MID, Region.ASIA),
        CountryEntry(CountryInfo("Malaysia", "MY", "🇲🇾"), T_AS_MID, Region.ASIA),
        CountryEntry(CountryInfo("Maldives", "MV", "🇲🇻"), T_AS_MID, Region.ASIA),
        CountryEntry(CountryInfo("Mongolia", "MN", "🇲🇳"), T_AS_MID, Region.ASIA),
        // ── Asia – Premium
        CountryEntry(CountryInfo("Hong Kong",   "HK", "🇭🇰"), T_AS_PRE, Region.ASIA),
        CountryEntry(CountryInfo("Japan",       "JP", "🇯🇵"), T_AS_PRE, Region.ASIA),
        CountryEntry(CountryInfo("Macao",       "MO", "🇲🇴"), T_AS_PRE, Region.ASIA),
        CountryEntry(CountryInfo("Singapore",   "SG", "🇸🇬"), T_AS_PRE, Region.ASIA),
        CountryEntry(CountryInfo("South Korea", "KR", "🇰🇷"), T_AS_PRE, Region.ASIA),
        CountryEntry(CountryInfo("Taiwan",      "TW", "🇹🇼"), T_AS_PRE, Region.ASIA),
        // ── Americas – North
        CountryEntry(CountryInfo("Canada",        "CA", "🇨🇦"), T_AM_N, Region.AMERICAS),
        CountryEntry(CountryInfo("United States", "US", "🇺🇸"), T_AM_N, Region.AMERICAS),
        // ── Americas – Latin
        CountryEntry(CountryInfo("Argentina",          "AR", "🇦🇷"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Bahamas",            "BS", "🇧🇸"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Barbados",           "BB", "🇧🇧"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Bolivia",            "BO", "🇧🇴"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Brazil",             "BR", "🇧🇷"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Chile",              "CL", "🇨🇱"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Colombia",           "CO", "🇨🇴"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Costa Rica",         "CR", "🇨🇷"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Cuba",               "CU", "🇨🇺"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Dominican Republic", "DO", "🇩🇴"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Ecuador",            "EC", "🇪🇨"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("El Salvador",        "SV", "🇸🇻"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Guatemala",          "GT", "🇬🇹"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Honduras",           "HN", "🇭🇳"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Jamaica",            "JM", "🇯🇲"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Mexico",             "MX", "🇲🇽"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Nicaragua",          "NI", "🇳🇮"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Panama",             "PA", "🇵🇦"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Paraguay",           "PY", "🇵🇾"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Peru",               "PE", "🇵🇪"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Trinidad & Tobago",  "TT", "🇹🇹"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Uruguay",            "UY", "🇺🇾"), T_AM_L, Region.AMERICAS),
        CountryEntry(CountryInfo("Venezuela",          "VE", "🇻🇪"), T_AM_L, Region.AMERICAS),
        // ── Global – Middle East
        CountryEntry(CountryInfo("Bahrain",      "BH", "🇧🇭"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Israel",       "IL", "🇮🇱"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Jordan",       "JO", "🇯🇴"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Kuwait",       "KW", "🇰🇼"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Lebanon",      "LB", "🇱🇧"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Oman",         "OM", "🇴🇲"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Qatar",        "QA", "🇶🇦"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Saudi Arabia", "SA", "🇸🇦"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Turkey",       "TR", "🇹🇷"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("UAE",          "AE", "🇦🇪"), T_GLOBAL, Region.GLOBAL),
        // ── Global – Africa
        CountryEntry(CountryInfo("Egypt",        "EG", "🇪🇬"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Ethiopia",     "ET", "🇪🇹"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Ghana",        "GH", "🇬🇭"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Kenya",        "KE", "🇰🇪"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Morocco",      "MA", "🇲🇦"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Nigeria",      "NG", "🇳🇬"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("South Africa", "ZA", "🇿🇦"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Tanzania",     "TZ", "🇹🇿"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Tunisia",      "TN", "🇹🇳"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Uganda",       "UG", "🇺🇬"), T_GLOBAL, Region.GLOBAL),
        // ── Global – Oceania
        CountryEntry(CountryInfo("Australia",       "AU", "🇦🇺"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Fiji",            "FJ", "🇫🇯"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("New Zealand",     "NZ", "🇳🇿"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Papua New Guinea","PG", "🇵🇬"), T_GLOBAL, Region.GLOBAL),
        // ── Global – Central Asia & Caucasus
        CountryEntry(CountryInfo("Armenia",    "AM", "🇦🇲"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Azerbaijan", "AZ", "🇦🇿"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Georgia",    "GE", "🇬🇪"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Kazakhstan", "KZ", "🇰🇿"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Kyrgyzstan", "KG", "🇰🇬"), T_GLOBAL, Region.GLOBAL),
        CountryEntry(CountryInfo("Uzbekistan", "UZ", "🇺🇿"), T_GLOBAL, Region.GLOBAL)
    )

    // ── Derived collections ───────────────────────────────────────────────────

    val countries: List<CountryInfo> = allEntries.map { it.info }.sortedBy { it.name }

    private val plansByCountry: Map<String, List<SailyPlan>> =
        allEntries.associate { e -> e.info.name to buildPlans(e.info.name, e.info.code, e.tier) }

    private val regionByCountry: Map<String, Region> =
        allEntries.associate { e -> e.info.name to e.region }

    fun regionForCountry(name: String): Region = regionByCountry[name] ?: Region.GLOBAL

    fun countriesInRegion(region: Region): List<CountryInfo> =
        allEntries.filter { it.region == region }.map { it.info }.sortedBy { it.name }

    // ── Plan access ───────────────────────────────────────────────────────────

    fun getPlansForCountry(country: String): List<SailyPlan> =
        plansByCountry[country] ?: emptyList()

    /** Cheapest data plan covering expected usage (slider-style dailyGb × duration × 1.2). */
    fun suggestPlan(country: String, durationDays: Int, style: UsageStyle): SailyPlan? {
        val needed = style.dailyGb * durationDays * 1.2
        val list   = getPlansForCountry(country).filter { !it.isUnlimited }
        return list.filter { it.dataGB >= needed }.minByOrNull { it.priceUSD }
            ?: list.maxByOrNull { it.dataGB }
    }

    fun flagForCountry(country: String): String =
        allEntries.find { it.info.name == country }?.info?.flag ?: "🌍"

    // ── Unlimited plan pricing ────────────────────────────────────────────────

    /**
     * Returns the price for an unlimited plan given a [base15DayPrice] and desired [days].
     * Scaled from the 15-day baseline: 7d ≈ 67 %, 30d ≈ 180 %, 90d ≈ 450 %.
     */
    fun unlimitedPriceForDays(base15DayPrice: Double, days: Int): Double {
        val multiplier = when (days) {
            7  -> 0.67
            15 -> 1.00
            30 -> 1.80
            90 -> 4.50
            else -> 1.00
        }
        return kotlin.math.round(base15DayPrice * multiplier * 100) / 100.0
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun buildPlans(country: String, code: String, tier: TierDef): List<SailyPlan> {
        val dataPlans = tier.dataPlans.mapIndexed { i, (gb, price) ->
            SailyPlan(
                id         = "${code.lowercase()}-$i",
                country    = country,
                countryCode = code,
                dataGB     = gb,
                validDays  = 30,
                priceUSD   = price
            )
        }
        val unlimited = SailyPlan(
            id          = "${code.lowercase()}-u",
            country     = country,
            countryCode = code,
            dataGB      = Double.MAX_VALUE,
            validDays   = 15,
            priceUSD    = tier.unlimitedBase15d,
            isUnlimited = true
        )
        return dataPlans + unlimited
    }
}
