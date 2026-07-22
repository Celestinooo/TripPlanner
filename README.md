# TripPlanner

A self-contained Android module for trip planning — built with the modern Android tech stack as part of a modular portfolio project.

---

## Features

- **Login** — email/password and Google sign-in flow with session state
- **Dashboard** — trip summary with budget overview, recent trips, and destination count
- **Discover** — paginated list of destinations from a REST API with region filtering, search, and like toggle
- **Trips** — full CRUD for trips with itinerary, expenses, photo gallery, and budget tracking
- **Trip Detail** — day-by-day itinerary, categorized expenses, gallery with Instagram-style grid preview, and budget progress
- **Settings** — traveler profile, currency, default region, language (EN/PT), notifications, and theme (light/dark/system)

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (type-safe routes) |
| State management | ViewModel + StateFlow + Compose collectAsStateWithLifecycle |
| Local database | Room (trips, itinerary, expenses, gallery) |
| Preferences | DataStore Preferences |
| Network | Retrofit + OkHttp + Kotlin Serialization |
| Image loading | Coil |
| Pagination | Paging 3 |
| Dependency injection | Hilt |
| Language | Kotlin 2.0 |
| Min SDK | 26 |
| Target SDK | 35 |

---

## Architecture

```
feature/tripplanner/
├── data/
│   ├── datastore/       # TripPrefsRepository — theme, language, currency, profile
│   ├── local/           # Room database, DAOs, entities
│   ├── remote/          # Retrofit API (countriesnow.space)
│   └── repository/      # TripRepository, DiscoverRepository
├── di/                  # Hilt module
├── domain/              # Domain models (Trip, Itinerary, Expense, Destination, etc.)
├── navigation/          # TripPlannerNavHost — type-safe NavGraph
└── ui/
    ├── common/          # Shared composables (TopHeader, ImagePicker, AlertDialog, etc.)
    ├── login/           # LoginScreen + LoginViewModel
    ├── discover/        # DiscoverScreen + DiscoverViewModel
    ├── trips/           # TripsScreen, TripDetailScreen + ViewModels
    └── settings/        # TripSettingsScreen + TripSettingsViewModel
```

MVVM with unidirectional data flow: `Repository → ViewModel (StateFlow) → Composable`.

---

## Integration

This module is designed to be embedded in a multi-module Android project as a Gradle module.

**Dependencies required from the host project:**

```kotlin
// build.gradle.kts (host app or core modules)
implementation(project(":core:designsystem")) // ThemeMode, PortfolioTheme, design tokens
implementation(project(":core:navigation"))   // type-safe route definitions
implementation(project(":core:common"))       // coroutine dispatchers, extensions
```

**Starting the module:**

```kotlin
// AndroidManifest.xml (host app)
<activity android:name="dev.lucas.portfolio.feature.tripplanner.TripPlannerActivity" />
```

```kotlin
// From any screen in the host app
startActivity(Intent(context, TripPlannerActivity::class.java))
```

**Hilt setup:** the host application must be annotated with `@HiltAndroidApp`.

---

## API

Destination data is fetched from the public [CountriesNow API](https://countriesnow.space):

```
GET https://countriesnow.space/api/v0.1/countries/flag/images
```

No API key required.

---

## Localization

Full support for English (`values/`) and Brazilian Portuguese (`values-pt/`). Language can be changed at runtime from the Settings screen.

---

## Author

**Lucas Celestino**
[github.com/Celestinooo](https://github.com/Celestinooo)
