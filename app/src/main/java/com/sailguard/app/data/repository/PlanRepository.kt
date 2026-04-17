package com.sailguard.app.data.repository

import com.sailguard.app.data.model.SailyPlan
import com.sailguard.app.data.model.UsageStyle

object PlanRepository {

    data class CountryInfo(val name: String, val code: String, val flag: String)

    val countries: List<CountryInfo> = listOf(
        CountryInfo("United States",  "US", "🇺🇸"),
        CountryInfo("United Kingdom", "GB", "🇬🇧"),
        CountryInfo("Japan",          "JP", "🇯🇵"),
        CountryInfo("Thailand",       "TH", "🇹🇭"),
        CountryInfo("France",         "FR", "🇫🇷"),
        CountryInfo("Germany",        "DE", "🇩🇪"),
        CountryInfo("Italy",          "IT", "🇮🇹"),
        CountryInfo("Spain",          "ES", "🇪🇸"),
        CountryInfo("Australia",      "AU", "🇦🇺"),
        CountryInfo("Canada",         "CA", "🇨🇦"),
        CountryInfo("South Korea",    "KR", "🇰🇷"),
        CountryInfo("Singapore",      "SG", "🇸🇬"),
        CountryInfo("UAE",            "AE", "🇦🇪"),
        CountryInfo("India",          "IN", "🇮🇳"),
        CountryInfo("Indonesia",      "ID", "🇮🇩"),
        CountryInfo("Vietnam",        "VN", "🇻🇳"),
        CountryInfo("Philippines",    "PH", "🇵🇭"),
        CountryInfo("Mexico",         "MX", "🇲🇽"),
        CountryInfo("Brazil",         "BR", "🇧🇷"),
        CountryInfo("Netherlands",    "NL", "🇳🇱")
    )

    private fun plans(country: String, code: String, vararg tiers: Triple<Double, Int, Double>): List<SailyPlan> =
        tiers.mapIndexed { i, (gb, days, price) ->
            SailyPlan("${code.lowercase()}-$i", country, code, gb, days, price)
        }

    private val allPlans: Map<String, List<SailyPlan>> = mapOf(
        "United States"  to plans("United States",  "US",
            Triple(1.0,7,9.99), Triple(3.0,15,19.99), Triple(5.0,30,29.99),
            Triple(10.0,30,49.99), Triple(20.0,30,79.99)),
        "United Kingdom" to plans("United Kingdom", "GB",
            Triple(1.0,7,8.99), Triple(3.0,15,17.99), Triple(5.0,30,26.99),
            Triple(10.0,30,44.99), Triple(20.0,30,74.99)),
        "Japan"          to plans("Japan",          "JP",
            Triple(1.0,7,11.99), Triple(3.0,15,22.99), Triple(5.0,30,34.99),
            Triple(10.0,30,59.99), Triple(20.0,30,99.99)),
        "Thailand"       to plans("Thailand",       "TH",
            Triple(1.0,7,7.99), Triple(3.0,15,14.99), Triple(5.0,30,22.99),
            Triple(10.0,30,39.99), Triple(20.0,30,64.99)),
        "France"         to plans("France",         "FR",
            Triple(1.0,7,8.99), Triple(3.0,15,17.99), Triple(5.0,30,26.99),
            Triple(10.0,30,44.99)),
        "Germany"        to plans("Germany",        "DE",
            Triple(1.0,7,8.99), Triple(3.0,15,17.99), Triple(5.0,30,26.99),
            Triple(10.0,30,44.99)),
        "Italy"          to plans("Italy",          "IT",
            Triple(1.0,7,9.99), Triple(3.0,15,18.99), Triple(5.0,30,28.99),
            Triple(10.0,30,47.99)),
        "Spain"          to plans("Spain",          "ES",
            Triple(1.0,7,8.99), Triple(3.0,15,17.99), Triple(5.0,30,26.99),
            Triple(10.0,30,44.99)),
        "Australia"      to plans("Australia",      "AU",
            Triple(1.0,7,9.99), Triple(3.0,15,19.99), Triple(5.0,30,29.99),
            Triple(10.0,30,49.99)),
        "Canada"         to plans("Canada",         "CA",
            Triple(1.0,7,9.99), Triple(3.0,15,19.99), Triple(5.0,30,29.99),
            Triple(10.0,30,49.99)),
        "South Korea"    to plans("South Korea",    "KR",
            Triple(1.0,7,11.99), Triple(3.0,15,22.99), Triple(5.0,30,34.99),
            Triple(10.0,30,59.99)),
        "Singapore"      to plans("Singapore",      "SG",
            Triple(1.0,7,9.99), Triple(3.0,15,19.99), Triple(5.0,30,29.99),
            Triple(10.0,30,49.99)),
        "UAE"            to plans("UAE",            "AE",
            Triple(1.0,7,12.99), Triple(3.0,15,24.99), Triple(5.0,30,36.99),
            Triple(10.0,30,64.99)),
        "India"          to plans("India",          "IN",
            Triple(1.0,7,6.99), Triple(3.0,15,12.99), Triple(5.0,30,18.99),
            Triple(10.0,30,32.99)),
        "Indonesia"      to plans("Indonesia",      "ID",
            Triple(1.0,7,7.99), Triple(3.0,15,14.99), Triple(5.0,30,21.99),
            Triple(10.0,30,36.99)),
        "Vietnam"        to plans("Vietnam",        "VN",
            Triple(1.0,7,7.99), Triple(3.0,15,14.99), Triple(5.0,30,21.99),
            Triple(10.0,30,36.99)),
        "Philippines"    to plans("Philippines",    "PH",
            Triple(1.0,7,8.99), Triple(3.0,15,16.99), Triple(5.0,30,24.99),
            Triple(10.0,30,42.99)),
        "Mexico"         to plans("Mexico",         "MX",
            Triple(1.0,7,7.99), Triple(3.0,15,14.99), Triple(5.0,30,21.99),
            Triple(10.0,30,36.99)),
        "Brazil"         to plans("Brazil",         "BR",
            Triple(1.0,7,8.99), Triple(3.0,15,16.99), Triple(5.0,30,24.99),
            Triple(10.0,30,42.99)),
        "Netherlands"    to plans("Netherlands",    "NL",
            Triple(1.0,7,8.99), Triple(3.0,15,17.99), Triple(5.0,30,26.99),
            Triple(10.0,30,44.99))
    )

    fun getPlansForCountry(country: String): List<SailyPlan> = allPlans[country] ?: emptyList()

    /** Returns cheapest plan that covers expected usage + 20 % buffer. */
    fun suggestPlan(country: String, durationDays: Int, style: UsageStyle): SailyPlan? {
        val needed = style.dailyGb * durationDays * 1.2
        val list   = getPlansForCountry(country)
        return list.filter { it.dataGB >= needed }.minByOrNull { it.priceUSD }
            ?: list.maxByOrNull { it.dataGB }
    }

    fun flagForCountry(country: String): String =
        countries.find { it.name == country }?.flag ?: "🌍"
}
