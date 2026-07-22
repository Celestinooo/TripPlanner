package dev.lucas.portfolio.feature.tripplanner.di

import javax.inject.Qualifier

enum class TripPlannerDispatchers { IO, Default }

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: TripPlannerDispatchers)
