package dev.lucas.portfolio.feature.tripplanner.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.lucas.portfolio.feature.tripplanner.data.local.TripDao
import dev.lucas.portfolio.feature.tripplanner.data.local.TripDatabase
import dev.lucas.portfolio.feature.tripplanner.data.remote.CountriesApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TripPlannerModule {

    @Provides
    @Singleton
    fun provideTripDatabase(@ApplicationContext ctx: Context): TripDatabase =
        Room.databaseBuilder(ctx, TripDatabase::class.java, "trip_planner.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTripDao(db: TripDatabase): TripDao = db.tripDao()

    @Provides
    @Singleton
    @Named("countries")
    fun provideCountriesJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues  = true
        isLenient          = true
    }

    @Provides
    @Singleton
    @Named("countries")
    fun provideCountriesOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideCountriesApi(
        @Named("countries") okHttp: OkHttpClient,
        @Named("countries") json: Json,
    ): CountriesApi = Retrofit.Builder()
        .baseUrl("https://countriesnow.space/")
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(CountriesApi::class.java)

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE trips ADD COLUMN currency_code TEXT NOT NULL DEFAULT 'BRL'")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE itinerary_items ADD COLUMN category TEXT NOT NULL DEFAULT 'ACTIVITY'")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE trips ADD COLUMN cover_image_path TEXT")
            db.execSQL("ALTER TABLE itinerary_items ADD COLUMN image_path TEXT")
            db.execSQL("ALTER TABLE expenses ADD COLUMN image_path TEXT")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS trip_gallery_photos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    trip_id INTEGER NOT NULL,
                    itinerary_item_id INTEGER,
                    image_path TEXT NOT NULL,
                    caption TEXT NOT NULL DEFAULT '',
                    created_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
