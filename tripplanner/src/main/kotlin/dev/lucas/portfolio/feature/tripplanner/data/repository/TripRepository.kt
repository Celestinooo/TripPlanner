package dev.lucas.portfolio.feature.tripplanner.data.repository

import dev.lucas.portfolio.feature.tripplanner.di.Dispatcher
import dev.lucas.portfolio.feature.tripplanner.di.TripPlannerDispatchers
import dev.lucas.portfolio.feature.tripplanner.data.local.TripDao
import dev.lucas.portfolio.feature.tripplanner.data.local.toDomain
import dev.lucas.portfolio.feature.tripplanner.data.local.toEntity
import dev.lucas.portfolio.feature.tripplanner.domain.Expense
import dev.lucas.portfolio.feature.tripplanner.domain.ItineraryItem
import dev.lucas.portfolio.feature.tripplanner.domain.Trip
import dev.lucas.portfolio.feature.tripplanner.domain.TripGalleryPhoto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val dao: TripDao,
    @Dispatcher(TripPlannerDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {

    fun allTrips(): Flow<List<Trip>> = dao.allTrips()
        .map { list -> list.map { it.toDomain() } }
        .flowOn(defaultDispatcher)

    suspend fun tripById(id: Long): Trip? = dao.tripById(id)?.toDomain()

    fun tripByIdFlow(id: Long): Flow<Trip?> = dao.tripByIdFlow(id)
        .map { it?.toDomain() }
        .flowOn(defaultDispatcher)

    suspend fun saveTrip(trip: Trip): Long = dao.insertTrip(trip.toEntity())

    suspend fun updateTrip(trip: Trip) = dao.updateTrip(trip.toEntity())

    suspend fun deleteTrip(trip: Trip) = dao.deleteTrip(trip.toEntity())

    fun itemsForTrip(tripId: Long): Flow<List<ItineraryItem>> =
        dao.itemsForTrip(tripId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(defaultDispatcher)

    suspend fun saveItem(item: ItineraryItem): Long = dao.insertItem(item.toEntity())

    suspend fun updateItem(item: ItineraryItem) = dao.updateItem(item.toEntity())

    suspend fun deleteItem(item: ItineraryItem) = dao.deleteItem(item.toEntity())

    fun expensesForTrip(tripId: Long): Flow<List<Expense>> =
        dao.expensesForTrip(tripId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(defaultDispatcher)

    suspend fun saveExpense(expense: Expense): Long = dao.insertExpense(expense.toEntity())

    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense.toEntity())

    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense.toEntity())

    fun totalSpentCents(tripId: Long): Flow<Long> =
        dao.totalSpentCents(tripId)
            .map { it ?: 0L }
            .flowOn(defaultDispatcher)

    fun allGalleryPreviewPhotos(limit: Int): Flow<List<TripGalleryPhoto>> =
        dao.allGalleryPreviewPhotos(limit)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(defaultDispatcher)

    fun galleryForTrip(tripId: Long): Flow<List<TripGalleryPhoto>> =
        dao.galleryForTrip(tripId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(defaultDispatcher)

    fun galleryPreviewForTrip(tripId: Long, limit: Int): Flow<List<TripGalleryPhoto>> =
        dao.galleryPreviewForTrip(tripId, limit)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(defaultDispatcher)

    fun galleryCountForTrip(tripId: Long): Flow<Int> =
        dao.galleryCountForTrip(tripId).flowOn(defaultDispatcher)

    suspend fun saveGalleryPhoto(photo: TripGalleryPhoto): Long =
        dao.insertGalleryPhoto(photo.toEntity())

    suspend fun deleteGalleryPhoto(photo: TripGalleryPhoto) =
        dao.deleteGalleryPhoto(photo.toEntity())
}
