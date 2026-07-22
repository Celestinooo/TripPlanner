package dev.lucas.portfolio.feature.tripplanner.ui.common

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

val supportedTripCurrencies = listOf("BRL", "USD", "EUR", "GBP", "JPY", "ARS")

fun currencySymbol(currencyCode: String): String = runCatching {
    Currency.getInstance(currencyCode).symbol
}.getOrElse { currencyCode }

fun formatTripCurrency(cents: Long, currencyCode: String): String {
    val amount = cents / 100.0
    return runCatching {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = Currency.getInstance(currencyCode)
        }.format(amount)
    }.getOrElse {
        "${currencySymbol(currencyCode)} ${"%.2f".format(amount)}"
    }
}

fun formatTripCurrency(amount: Double, currencyCode: String): String =
    formatTripCurrency((amount * 100).toLong(), currencyCode)
