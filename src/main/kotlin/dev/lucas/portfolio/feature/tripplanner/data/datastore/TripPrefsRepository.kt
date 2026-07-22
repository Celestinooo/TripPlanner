package dev.lucas.portfolio.feature.tripplanner.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucas.portfolio.core.designsystem.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tripDataStore: DataStore<Preferences> by preferencesDataStore("trip_prefs")

object TripPrefsKeys {
    val THEME_MODE     = stringPreferencesKey("theme_mode")
    val USER_NAME      = stringPreferencesKey("user_name")
    val USER_AVATAR    = stringPreferencesKey("user_avatar")
    val USER_BANNER    = stringPreferencesKey("user_banner")
    val CURRENCY       = stringPreferencesKey("currency")
    val NOTIFICATIONS  = booleanPreferencesKey("notifications")
    val DEFAULT_REGION = stringPreferencesKey("default_region")
    val LANGUAGE       = stringPreferencesKey("language")
}

@Singleton
class TripPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val store = context.tripDataStore

    val themeMode: Flow<ThemeMode> = store.data.map { prefs ->
        prefs[TripPrefsKeys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    val userName:      Flow<String>  = store.data.map { it[TripPrefsKeys.USER_NAME]      ?: "" }
    val userAvatar:    Flow<String?> = store.data.map { it[TripPrefsKeys.USER_AVATAR] }
    val userBanner:    Flow<String?> = store.data.map { it[TripPrefsKeys.USER_BANNER] }
    val currency:      Flow<String>  = store.data.map { it[TripPrefsKeys.CURRENCY]       ?: "BRL" }
    val notifications: Flow<Boolean> = store.data.map { it[TripPrefsKeys.NOTIFICATIONS]  ?: true }
    val defaultRegion: Flow<String>  = store.data.map { it[TripPrefsKeys.DEFAULT_REGION] ?: "Todas" }
    val language:      Flow<String>  = store.data.map { it[TripPrefsKeys.LANGUAGE]       ?: "system" }

    suspend fun setThemeMode(mode: ThemeMode) { store.edit { it[TripPrefsKeys.THEME_MODE] = mode.name } }

    suspend fun setUserName(v: String)       { store.edit { it[TripPrefsKeys.USER_NAME]      = v } }
    suspend fun setUserAvatar(v: String?)    { store.edit { if (v == null) it.remove(TripPrefsKeys.USER_AVATAR) else it[TripPrefsKeys.USER_AVATAR] = v } }
    suspend fun setUserBanner(v: String?)    { store.edit { if (v == null) it.remove(TripPrefsKeys.USER_BANNER) else it[TripPrefsKeys.USER_BANNER] = v } }
    suspend fun setCurrency(v: String)       { store.edit { it[TripPrefsKeys.CURRENCY]       = v } }
    suspend fun setNotifications(v: Boolean) { store.edit { it[TripPrefsKeys.NOTIFICATIONS]  = v } }
    suspend fun setDefaultRegion(v: String)  { store.edit { it[TripPrefsKeys.DEFAULT_REGION] = v } }
    suspend fun setLanguage(v: String)       { store.edit { it[TripPrefsKeys.LANGUAGE]       = v } }
    suspend fun clearAll()                   { store.edit { it.clear() } }
}
