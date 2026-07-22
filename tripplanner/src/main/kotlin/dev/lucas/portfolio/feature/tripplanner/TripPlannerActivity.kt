package dev.lucas.portfolio.feature.tripplanner

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.lucas.portfolio.feature.tripplanner.data.datastore.TripPrefsRepository
import dev.lucas.portfolio.feature.tripplanner.navigation.TripPlannerNavHost
import dev.lucas.portfolio.feature.tripplanner.theme.ThemeMode
import dev.lucas.portfolio.feature.tripplanner.theme.TripPlannerTheme
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class TripPlannerActivity : ComponentActivity() {

    @Inject lateinit var tripPrefsRepo: TripPrefsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by tripPrefsRepo.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            val language by tripPrefsRepo.language.collectAsStateWithLifecycle("system")
            val baseContext = LocalContext.current
            val localizedContext = remember(baseContext, language) {
                baseContext.localizedFor(language)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
            ) {
                TripPlannerTheme(
                    themeMode = themeMode,
                    onSetThemeMode = { mode ->
                        lifecycleScope.launch { tripPrefsRepo.setThemeMode(mode) }
                    },
                ) {
                    TripPlannerNavHost(onBack = { finish() })
                }
            }
        }
    }
}

private fun Context.localizedFor(language: String): Context {
    if (language == "system") return this
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return ContextThemeWrapper(this, theme).apply {
        applyOverrideConfiguration(config)
    }
}
