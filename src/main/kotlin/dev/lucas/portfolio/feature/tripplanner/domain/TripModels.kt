package dev.lucas.portfolio.feature.tripplanner.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Trip(
    val id: Long = 0,
    val name: String,
    val destinationCountry: String,
    val destinationFlag: String,
    val startDate: Long,
    val endDate: Long,
    val budgetCents: Long,
    val currencyCode: String = "BRL",
    val coverImagePath: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
) {
    val budgetBrl: Double get() = budgetCents / 100.0
    val formattedStart: String get() = dateStr(startDate)
    val formattedEnd: String get() = dateStr(endDate)

    private fun dateStr(ts: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ts))
}

enum class ItineraryCategory(val emoji: String, val label: String) {
    TRANSPORT("✈️", "Transporte"),
    LODGING("🏨", "Hospedagem"),
    FOOD("🍽️", "Alimentação"),
    ACTIVITY("🎭", "Atividade"),
    SHOPPING("🛍️", "Compras"),
    OTHER("📍", "Outro"),
}

data class ItineraryItem(
    val id: Long = 0,
    val tripId: Long,
    val day: Int,
    val timeStr: String,
    val title: String,
    val category: ItineraryCategory = ItineraryCategory.ACTIVITY,
    val imagePath: String? = null,
    val description: String = "",
    val isCompleted: Boolean = false,
)

enum class ExpenseCategory(val emoji: String, val label: String) {
    TRANSPORT("✈️", "Transporte"),
    LODGING("🏨", "Hospedagem"),
    FOOD("🍽️", "Alimentação"),
    ACTIVITY("🎭", "Atividade"),
    SHOPPING("🛍️", "Compras"),
    OTHER("💸", "Outros"),
}

data class Expense(
    val id: Long = 0,
    val tripId: Long,
    val category: ExpenseCategory,
    val amountCents: Long,
    val description: String,
    val imagePath: String? = null,
    val date: Long = System.currentTimeMillis(),
) {
    val amountBrl: Double get() = amountCents / 100.0
}

data class TripGalleryPhoto(
    val id: Long = 0,
    val tripId: Long,
    val itineraryItemId: Long? = null,
    val imagePath: String,
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

data class Destination(
    val name: String,
    val flag: String,
    val region: String,
    val isLiked: Boolean = false,
)
