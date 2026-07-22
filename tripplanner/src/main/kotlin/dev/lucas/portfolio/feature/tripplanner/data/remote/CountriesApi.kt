package dev.lucas.portfolio.feature.tripplanner.data.remote

import dev.lucas.portfolio.feature.tripplanner.domain.Destination
import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class CountriesNowFlagsResponse(
    val error: Boolean = false,
    val data: List<CountryFlagDto> = emptyList(),
)

@Serializable
data class CountryFlagDto(
    val name: String = "",
    val iso2: String = "",
    val iso3: String = "",
    val unicodeFlag: String = "",
)

fun CountryFlagDto.toDestination() = Destination(
    name   = name,
    flag   = unicodeFlag.ifBlank { "🏳" },
    region = continentName(iso2),
)

private fun continentName(iso2: String): String = when (iso2.uppercase()) {
    in africaIso2   -> "Africa"
    in americasIso2 -> "Americas"
    in asiaIso2     -> "Asia"
    in europeIso2   -> "Europe"
    in oceaniaIso2  -> "Oceania"
    else            -> ""
}

private val africaIso2 = setOf(
    "DZ", "AO", "BJ", "BW", "BF", "BI", "CM", "CV", "CF", "TD", "KM", "CG", "CD", "CI", "DJ",
    "EG", "GQ", "ER", "SZ", "ET", "GA", "GM", "GH", "GN", "GW", "KE", "LS", "LR", "LY", "MG",
    "MW", "ML", "MR", "MU", "YT", "MA", "MZ", "NA", "NE", "NG", "RE", "RW", "SH", "ST", "SN",
    "SC", "SL", "SO", "ZA", "SS", "SD", "TZ", "TG", "TN", "UG", "EH", "ZM", "ZW",
)

private val americasIso2 = setOf(
    "AI", "AG", "AR", "AW", "BS", "BB", "BZ", "BM", "BO", "BQ", "BR", "CA", "KY", "CL", "CO",
    "CR", "CU", "CW", "DM", "DO", "EC", "SV", "FK", "GF", "GL", "GD", "GP", "GT", "GY", "HT",
    "HN", "JM", "MQ", "MX", "MS", "NI", "PA", "PY", "PE", "PR", "BL", "KN", "LC", "MF", "PM",
    "VC", "SX", "SR", "TT", "TC", "US", "UY", "VE", "VG", "VI",
)

private val asiaIso2 = setOf(
    "AF", "AM", "AZ", "BH", "BD", "BT", "BN", "KH", "CN", "CX", "CC", "CY", "GE", "HK", "IN",
    "ID", "IR", "IQ", "IL", "JP", "JO", "KZ", "KW", "KG", "LA", "LB", "MO", "MY", "MV", "MN",
    "MM", "NP", "KP", "OM", "PK", "PS", "PH", "QA", "SA", "SG", "KR", "LK", "SY", "TW", "TJ",
    "TH", "TL", "TR", "TM", "AE", "UZ", "VN", "YE",
)

private val europeIso2 = setOf(
    "AX", "AL", "AD", "AT", "BY", "BE", "BA", "BG", "HR", "CZ", "DK", "EE", "FO", "FI", "FR",
    "DE", "GI", "GR", "GG", "VA", "HU", "IS", "IE", "IM", "IT", "JE", "XK", "LV", "LI", "LT",
    "LU", "MT", "MD", "MC", "ME", "NL", "MK", "NO", "PL", "PT", "RO", "RU", "SM", "RS", "SK",
    "SI", "ES", "SJ", "SE", "CH", "UA", "GB",
)

private val oceaniaIso2 = setOf(
    "AS", "AU", "CK", "FJ", "PF", "GU", "KI", "MH", "FM", "NR", "NC", "NZ", "NU", "NF", "MP",
    "PW", "PG", "PN", "WS", "SB", "TK", "TO", "TV", "UM", "VU", "WF",
)

interface CountriesApi {
    @GET("api/v0.1/countries/flag/unicode")
    suspend fun flagsUnicode(): CountriesNowFlagsResponse
}
