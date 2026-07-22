package dev.lucas.portfolio.feature.tripplanner.ui.trips

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lucas.portfolio.feature.tripplanner.R
import dev.lucas.portfolio.feature.tripplanner.data.repository.TripRepository
import dev.lucas.portfolio.feature.tripplanner.domain.Trip
import dev.lucas.portfolio.feature.tripplanner.domain.TripGalleryPhoto
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripPhotoViewerDialog
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripTopHeader
import dev.lucas.portfolio.feature.tripplanner.ui.common.formatTripCurrency
import dev.lucas.portfolio.feature.tripplanner.di.Dispatcher
import dev.lucas.portfolio.feature.tripplanner.di.TripPlannerDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

internal data class TripListItem(
    val trip: Trip,
    val galleryPreview: List<TripGalleryPhoto>,
)

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repo: TripRepository,
    @Dispatcher(TripPlannerDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    internal val tripItems: StateFlow<List<TripListItem>> = combine(
        repo.allTrips(),
        repo.allGalleryPreviewPhotos(limit = 6),
    ) { trips, photos ->
        val photosByTrip = photos.groupBy { it.tripId }
        trips.map { trip ->
            TripListItem(
                trip = trip,
                galleryPreview = photosByTrip[trip.id].orEmpty().take(6),
            )
        }
    }
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripsScreen(
    viewModel: TripsViewModel = hiltViewModel(),
    onOpenTrip: (Long) -> Unit,
    showHeader: Boolean = true,
    scrollToTopSignal: Int = 0,
) {
    val tripItems by viewModel.tripItems.collectAsStateWithLifecycle()
    var previewPhoto by remember { mutableStateOf<TripGalleryPhoto?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    Scaffold { _ ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            state          = listState,
            contentPadding = PaddingValues(
                top = if (showHeader) 0.dp else 16.dp,
                bottom = 24.dp,
            ),
        ) {
            if (showHeader) {
                item {
                    TripsHeroHeader(
                        tripCount = tripItems.size,
                    )
                }
            }
            if (tripItems.isEmpty()) {
                item { TripsEmptyState() }
            } else {
                itemsIndexed(tripItems, key = { _, item -> item.trip.id }) { _, item ->
                    TripCard(
                        trip     = item.trip,
                        galleryPreview = item.galleryPreview,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        onClick  = { onOpenTrip(item.trip.id) },
                        onPhotoClick = { previewPhoto = it },
                    )
                }
            }
        }
    }

    previewPhoto?.let { photo ->
        TripPhotoViewerDialog(
            imagePath = photo.imagePath,
            caption = photo.caption,
            onDismiss = { previewPhoto = null },
        )
    }
}

@Composable
private fun TripsHeroHeader(tripCount: Int, modifier: Modifier = Modifier) {
    TripTopHeader(
        icon = Icons.Rounded.FlightTakeoff,
        title = stringResource(R.string.trip_my_trips),
        subtitle = if (tripCount == 0) stringResource(R.string.trip_empty_trips)
            else if (tripCount == 1) stringResource(R.string.trip_one_planned, tripCount)
            else stringResource(R.string.trip_many_planned, tripCount),
        modifier = modifier,
    )
}

@Composable
private fun TripCard(
    trip: Trip,
    galleryPreview: List<TripGalleryPhoto>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPhotoClick: (TripGalleryPhoto) -> Unit,
) {
    val fmt = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    ElevatedCard(
        modifier  = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(30.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        trip.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "${trip.destinationFlag} ${trip.destinationCountry}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Box(
                            Modifier
                                .size(3.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(0.72f),
                            )
                            Text(
                                "${fmt.format(Date(trip.startDate))} → ${fmt.format(Date(trip.endDate))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary.copy(0.82f),
                                maxLines = 1,
                            )
                        }
                    }
                }

                Icon(
                    Icons.Rounded.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp),
                )
            }

            if (galleryPreview.isNotEmpty()) {
                TinderPhotoCarousel(
                    photos = galleryPreview,
                    onPhotoClick = onPhotoClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TinderPhotoCarousel(
    photos: List<TripGalleryPhoto>,
    onPhotoClick: (TripGalleryPhoto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var topIndex by remember(photos) { mutableStateOf(0) }
    val offsetX = remember(photos) { Animatable(0f) }

    BoxWithConstraints(
        modifier = modifier.height(230.dp),
        contentAlignment = Alignment.Center,
    ) {
        val widthPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxWidth.toPx() }
        val threshold = widthPx * 0.28f
        val dragProgress = (kotlin.math.abs(offsetX.value) / threshold).coerceIn(0f, 1f)

        if (photos.size > 1) {
            val behindPhoto = photos[(topIndex + 1) % photos.size]
            TinderCard(
                photo = behindPhoto,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val s = 0.92f + 0.08f * dragProgress
                        scaleX = s
                        scaleY = s
                        alpha = 0.7f + 0.3f * dragProgress
                    },
            )
        }

        val topPhoto = photos[topIndex]
        TinderCard(
            photo = topPhoto,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = (offsetX.value / widthPx) * 14f
                }
                .pointerInput(topIndex, photos) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (kotlin.math.abs(offsetX.value) > threshold && photos.size > 1) {
                                    val target = if (offsetX.value > 0) widthPx * 1.4f else -widthPx * 1.4f
                                    offsetX.animateTo(target, tween(240))
                                    topIndex = (topIndex + 1) % photos.size
                                    offsetX.snapTo(0f)
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.6f))
                                }
                            }
                        },
                        onHorizontalDrag = { change, drag ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + drag) }
                        },
                    )
                }
                .clickable { onPhotoClick(topPhoto) },
        )

        if (photos.size > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                tonalElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    photos.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (i == topIndex) 7.dp else 5.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (i == topIndex) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TinderCard(
    photo: TripGalleryPhoto,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
    ) {
        AsyncImage(
            model = photo.imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (photo.caption.isNotBlank()) {
            Text(
                photo.caption,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TripsEmptyState() {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("✈️", fontSize = 56.sp)
        Text(stringResource(R.string.trip_empty_trips), style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Text(stringResource(R.string.trip_empty_trips_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDatePickerDialog(
    initialMillis: Long,
    minMillis: Long? = null,
    maxMillis: Long? = null,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val minSelectableMillis = remember(minMillis) { minMillis?.let(::startOfUtcDayMillis) }
    val maxSelectableMillis = remember(maxMillis) { maxMillis?.let(::endOfUtcDayMillis) }
    val boundedInitialMillis = remember(initialMillis, minSelectableMillis, maxSelectableMillis) {
        initialMillis.coerceIn(
            minSelectableMillis ?: Long.MIN_VALUE,
            maxSelectableMillis ?: Long.MAX_VALUE,
        )
    }
    val selectableDates = remember(minSelectableMillis, maxSelectableMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val lowerBound = minSelectableMillis ?: Long.MIN_VALUE
                val upperBound = maxSelectableMillis ?: Long.MAX_VALUE
                return utcTimeMillis in lowerBound..upperBound
            }
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = boundedInitialMillis,
        selectableDates = selectableDates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton    = {
            TextButton(onClick = {
                state.selectedDateMillis
                    ?.coerceIn(
                        minSelectableMillis ?: Long.MIN_VALUE,
                        maxSelectableMillis ?: Long.MAX_VALUE,
                    )
                    ?.let(onConfirm) ?: onDismiss()
            }) {
                Text(stringResource(R.string.trip_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.trip_cancel)) } },
    ) { DatePicker(state = state) }
}

private const val UTC_DAY_MILLIS = 86_400_000L

private fun startOfUtcDayMillis(value: Long): Long =
    Math.floorDiv(value, UTC_DAY_MILLIS) * UTC_DAY_MILLIS

private fun endOfUtcDayMillis(value: Long): Long =
    startOfUtcDayMillis(value) + UTC_DAY_MILLIS - 1L
