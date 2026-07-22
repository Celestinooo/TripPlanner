package dev.lucas.portfolio.feature.tripplanner.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import dev.lucas.portfolio.feature.tripplanner.domain.Expense
import dev.lucas.portfolio.feature.tripplanner.domain.ExpenseCategory
import dev.lucas.portfolio.feature.tripplanner.domain.ItineraryCategory
import dev.lucas.portfolio.feature.tripplanner.domain.ItineraryItem
import dev.lucas.portfolio.feature.tripplanner.domain.Trip
import dev.lucas.portfolio.feature.tripplanner.domain.TripGalleryPhoto
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val destinationCountry: String,
    val destinationFlag: String,
    val startDate: Long,
    val endDate: Long,
    val budgetCents: Long,
    @ColumnInfo(name = "currency_code") val currencyCode: String = "BRL",
    @ColumnInfo(name = "cover_image_path") val coverImagePath: String? = null,
    val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "itinerary_items")
data class ItineraryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trip_id") val tripId: Long,
    val day: Int,
    @ColumnInfo(name = "time_str") val timeStr: String,
    val title: String,
    val category: String = ItineraryCategory.ACTIVITY.name,
    @ColumnInfo(name = "image_path") val imagePath: String? = null,
    val description: String = "",
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trip_id") val tripId: Long,
    val category: String,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    val description: String,
    @ColumnInfo(name = "image_path") val imagePath: String? = null,
    val date: Long = System.currentTimeMillis(),
)

@Entity(tableName = "trip_gallery_photos")
data class TripGalleryPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trip_id") val tripId: Long,
    @ColumnInfo(name = "itinerary_item_id") val itineraryItemId: Long? = null,
    @ColumnInfo(name = "image_path") val imagePath: String,
    val caption: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)

fun TripEntity.toDomain() = Trip(
    id = id, name = name, destinationCountry = destinationCountry,
    destinationFlag = destinationFlag, startDate = startDate, endDate = endDate,
    budgetCents = budgetCents, currencyCode = currencyCode, coverImagePath = coverImagePath,
    notes = notes, createdAt = createdAt,
)

fun Trip.toEntity() = TripEntity(
    id = id, name = name, destinationCountry = destinationCountry,
    destinationFlag = destinationFlag, startDate = startDate, endDate = endDate,
    budgetCents = budgetCents, currencyCode = currencyCode, coverImagePath = coverImagePath,
    notes = notes, createdAt = createdAt,
)

fun ItineraryItemEntity.toDomain() = ItineraryItem(
    id = id, tripId = tripId, day = day, timeStr = timeStr, title = title,
    category = runCatching { ItineraryCategory.valueOf(category) }.getOrDefault(ItineraryCategory.ACTIVITY),
    imagePath = imagePath, description = description, isCompleted = isCompleted,
)

fun ItineraryItem.toEntity() = ItineraryItemEntity(
    id = id, tripId = tripId, day = day, timeStr = timeStr, title = title,
    category = category.name, imagePath = imagePath, description = description,
    isCompleted = isCompleted,
)

fun ExpenseEntity.toDomain() = Expense(
    id = id, tripId = tripId,
    category = runCatching { ExpenseCategory.valueOf(category) }.getOrDefault(ExpenseCategory.OTHER),
    amountCents = amountCents, description = description, imagePath = imagePath, date = date,
)

fun Expense.toEntity() = ExpenseEntity(
    id = id, tripId = tripId, category = category.name,
    amountCents = amountCents, description = description, imagePath = imagePath, date = date,
)

fun TripGalleryPhotoEntity.toDomain() = TripGalleryPhoto(
    id = id, tripId = tripId, itineraryItemId = itineraryItemId,
    imagePath = imagePath, caption = caption, createdAt = createdAt,
)

fun TripGalleryPhoto.toEntity() = TripGalleryPhotoEntity(
    id = id, tripId = tripId, itineraryItemId = itineraryItemId,
    imagePath = imagePath, caption = caption, createdAt = createdAt,
)

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY created_at DESC")
    fun allTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun tripById(id: Long): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :id")
    fun tripByIdFlow(id: Long): Flow<TripEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("SELECT * FROM itinerary_items WHERE trip_id = :tripId ORDER BY day ASC, time_str ASC")
    fun itemsForTrip(tripId: Long): Flow<List<ItineraryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItineraryItemEntity): Long

    @Update
    suspend fun updateItem(item: ItineraryItemEntity)

    @Delete
    suspend fun deleteItem(item: ItineraryItemEntity)

    @Query("SELECT * FROM expenses WHERE trip_id = :tripId ORDER BY date DESC")
    fun expensesForTrip(tripId: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT SUM(amount_cents) FROM expenses WHERE trip_id = :tripId")
    fun totalSpentCents(tripId: Long): Flow<Long?>

    @Query(
        """
        SELECT * FROM trip_gallery_photos AS photo
        WHERE (
            SELECT COUNT(*) FROM trip_gallery_photos AS newer
            WHERE newer.trip_id = photo.trip_id
              AND newer.created_at >= photo.created_at
        ) <= :limit
        ORDER BY created_at DESC
        """
    )
    fun allGalleryPreviewPhotos(limit: Int): Flow<List<TripGalleryPhotoEntity>>

    @Query("SELECT * FROM trip_gallery_photos WHERE trip_id = :tripId ORDER BY created_at DESC")
    fun galleryForTrip(tripId: Long): Flow<List<TripGalleryPhotoEntity>>

    @Query("SELECT * FROM trip_gallery_photos WHERE trip_id = :tripId ORDER BY created_at DESC LIMIT :limit")
    fun galleryPreviewForTrip(tripId: Long, limit: Int): Flow<List<TripGalleryPhotoEntity>>

    @Query("SELECT COUNT(*) FROM trip_gallery_photos WHERE trip_id = :tripId")
    fun galleryCountForTrip(tripId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryPhoto(photo: TripGalleryPhotoEntity): Long

    @Delete
    suspend fun deleteGalleryPhoto(photo: TripGalleryPhotoEntity)
}

@Database(
    entities = [TripEntity::class, ItineraryItemEntity::class, ExpenseEntity::class, TripGalleryPhotoEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
}
