package dev.lucas.portfolio.feature.tripplanner.data.repository

import dev.lucas.portfolio.core.common.di.Dispatcher
import dev.lucas.portfolio.core.common.di.PortfolioDispatchers
import dev.lucas.portfolio.feature.tripplanner.data.remote.CountriesApi
import dev.lucas.portfolio.feature.tripplanner.data.remote.toDestination
import dev.lucas.portfolio.feature.tripplanner.domain.Destination
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoverRepository @Inject constructor(
    private val api: CountriesApi,
    @Dispatcher(PortfolioDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {

    private val cacheMutex = Mutex()
    @Volatile
    private var cachedDestinations: List<Destination>? = null

    suspend fun cachedDestinations(): List<Destination> = loadDestinations()

    private suspend fun loadDestinations(): List<Destination> {
        cachedDestinations?.let { return it }

        return cacheMutex.withLock {
            cachedDestinations ?: fetchDestinations().also { cachedDestinations = it }
        }
    }

    suspend fun refreshDestinations(): List<Destination> =
        cacheMutex.withLock {
            fetchDestinations().also { cachedDestinations = it }
        }

    private suspend fun fetchDestinations(): List<Destination> = withContext(ioDispatcher) {
        val response = api.flagsUnicode()
        if (response.error || response.data.isEmpty()) {
            throw IllegalStateException("API returned empty list")
        }
        response.data
            .asSequence()
            .filter { it.name.isNotBlank() }
            .map { it.toDestination() }
            .sortedBy { it.name }
            .toList()
    }
}
