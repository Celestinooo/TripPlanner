package dev.lucas.portfolio.feature.tripplanner.ui.trips

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lucas.portfolio.feature.tripplanner.R
import dev.lucas.portfolio.feature.tripplanner.data.repository.TripRepository
import dev.lucas.portfolio.feature.tripplanner.domain.Expense
import dev.lucas.portfolio.feature.tripplanner.domain.ExpenseCategory
import dev.lucas.portfolio.feature.tripplanner.domain.ItineraryCategory
import dev.lucas.portfolio.feature.tripplanner.domain.ItineraryItem
import dev.lucas.portfolio.feature.tripplanner.domain.Trip
import dev.lucas.portfolio.feature.tripplanner.domain.TripGalleryPhoto
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripConfirmDialog
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripImagePickerLauncher
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripPhotoViewerDialog
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripSheetHeader
import dev.lucas.portfolio.feature.tripplanner.ui.common.currencySymbol
import dev.lucas.portfolio.feature.tripplanner.ui.common.formatTripCurrency
import dev.lucas.portfolio.feature.tripplanner.ui.common.supportedTripCurrencies
import dev.lucas.portfolio.feature.tripplanner.ui.common.tripClearFocusOnTap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

internal data class TripDetailHeaderState(
    val animationKey: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val galleryCount: Int? = null,
    val iconColor: Color? = null,
)

internal enum class TripDetailSection {
    Overview, Itinerary, Expenses, Gallery
}

private val PanelAmber  = Color(0xFFFFB300)
private val PanelBlue   = Color(0xFF1E88E5)
private val PanelGreen  = Color(0xFF2E9E4F)
private val PanelPurple = Color(0xFF8E24AA)


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val repo: TripRepository,
) : ViewModel() {

    private val _tripId = MutableStateFlow(-1L)

    val trip: StateFlow<Trip?> = _tripId
        .flatMapLatest { id ->
            if (id < 0) flowOf<Trip?>(null)
            else repo.tripByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val itinerary: StateFlow<List<ItineraryItem>> = _tripId
        .flatMapLatest { id -> if (id < 0) flowOf(emptyList()) else repo.itemsForTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expenses: StateFlow<List<Expense>> = _tripId
        .flatMapLatest { id -> if (id < 0) flowOf(emptyList()) else repo.expensesForTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalSpent: StateFlow<Long> = _tripId
        .flatMapLatest { id -> if (id < 0) flowOf(0L) else repo.totalSpentCents(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val galleryPreview: StateFlow<List<TripGalleryPhoto>> = _tripId
        .flatMapLatest { id -> if (id < 0) flowOf(emptyList()) else repo.galleryPreviewForTrip(id, limit = 8) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gallery: StateFlow<List<TripGalleryPhoto>> = _tripId
        .flatMapLatest { id -> if (id < 0) flowOf(emptyList()) else repo.galleryForTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val galleryCount: StateFlow<Int> = _tripId
        .flatMapLatest { id -> if (id < 0) flowOf(0) else repo.galleryCountForTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun init(id: Long) { if (_tripId.value != id) _tripId.value = id }

    fun updateTrip(
        name: String,
        country: String,
        flag: String,
        startMs: Long,
        endMs: Long,
        budgetCents: Long,
        currencyCode: String,
        coverImagePath: String?,
    ) = viewModelScope.launch {
        trip.value?.let {
            repo.updateTrip(
                it.copy(
                    name = name,
                    destinationCountry = country,
                    destinationFlag = flag,
                    startDate = startMs,
                    endDate = endMs,
                    budgetCents = budgetCents,
                    currencyCode = currencyCode,
                    coverImagePath = coverImagePath,
                )
            )
        }
    }

    fun deleteTrip() = viewModelScope.launch { trip.value?.let { repo.deleteTrip(it) } }

    fun addItineraryItem(title: String, day: Int, time: String, category: ItineraryCategory, imagePath: String?) = viewModelScope.launch {
        repo.saveItem(ItineraryItem(tripId = _tripId.value, day = day, timeStr = time, title = title, category = category, imagePath = imagePath))
    }

    fun editItineraryItem(item: ItineraryItem, title: String, day: Int, time: String, category: ItineraryCategory, imagePath: String?) =
        viewModelScope.launch { repo.updateItem(item.copy(title = title, day = day, timeStr = time, category = category, imagePath = imagePath)) }

    fun toggleItem(item: ItineraryItem) = viewModelScope.launch {
        repo.updateItem(item.copy(isCompleted = !item.isCompleted))
    }

    fun deleteItem(item: ItineraryItem) = viewModelScope.launch { repo.deleteItem(item) }

    fun addExpense(category: ExpenseCategory, amount: Double, desc: String, imagePath: String?) = viewModelScope.launch {
        repo.saveExpense(Expense(tripId = _tripId.value, category = category,
            amountCents = (amount * 100).toLong(), description = desc, imagePath = imagePath))
    }

    fun editExpense(expense: Expense, category: ExpenseCategory, amount: Double, desc: String, imagePath: String?) =
        viewModelScope.launch {
            repo.updateExpense(
                expense.copy(
                    category = category,
                    amountCents = (amount * 100).toLong(),
                    description = desc,
                    imagePath = imagePath,
                )
            )
        }

    fun deleteExpense(expense: Expense) = viewModelScope.launch { repo.deleteExpense(expense) }

    fun addGalleryPhoto(imagePath: String, itineraryItemId: Long?, caption: String) = viewModelScope.launch {
        repo.saveGalleryPhoto(
            TripGalleryPhoto(
                tripId = _tripId.value,
                itineraryItemId = itineraryItemId,
                imagePath = imagePath,
                caption = caption,
            )
        )
    }

    fun editGalleryPhoto(photo: TripGalleryPhoto, imagePath: String, itineraryItemId: Long?, caption: String) =
        viewModelScope.launch {
            repo.saveGalleryPhoto(
                photo.copy(
                    imagePath = imagePath,
                    itineraryItemId = itineraryItemId,
                    caption = caption,
                )
            )
        }

    fun deleteGalleryPhoto(photo: TripGalleryPhoto) = viewModelScope.launch { repo.deleteGalleryPhoto(photo) }
}



@Composable
private fun ReportEditWhenActive(
    onEdit: (() -> Unit)?,
    isActive: Boolean,
    onEditChange: ((() -> Unit)?) -> Unit,
) {
    LaunchedEffect(onEdit, isActive) {
        onEditChange(if (isActive) onEdit else null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDetailScreen(
    tripId: Long,
    scrollToTopSignal: Int = 0,
    onBack: () -> Unit,
    onOpenItinerary: () -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenGallery: () -> Unit,
    isActive: Boolean = true,
    onEditChange: ((() -> Unit)?) -> Unit = {},
    viewModel: TripDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(tripId) { viewModel.init(tripId) }

    val trip       by viewModel.trip.collectAsStateWithLifecycle()
    val itinerary  by viewModel.itinerary.collectAsStateWithLifecycle()
    val expenses   by viewModel.expenses.collectAsStateWithLifecycle()
    val totalSpent by viewModel.totalSpent.collectAsStateWithLifecycle()
    val galleryPreview by viewModel.galleryPreview.collectAsStateWithLifecycle()
    val galleryCount by viewModel.galleryCount.collectAsStateWithLifecycle()

    var showDeleteTrip by remember { mutableStateOf(false) }
    var showEditTrip by remember { mutableStateOf(false) }

    val onHeaderEdit = if (trip != null) { { showEditTrip = true } } else null

    ReportEditWhenActive(onHeaderEdit, isActive, onEditChange)
    DisposableEffect(Unit) { onDispose { onEditChange(null) } }

    Scaffold(modifier = Modifier.tripClearFocusOnTap()) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            TripOverview(
                trip = trip,
                itinerary = itinerary,
                expenses = expenses,
                gallery = galleryPreview,
                totalSpent = totalSpent,
                onOpenItinerary = onOpenItinerary,
                onOpenExpenses = onOpenExpenses,
                onOpenGallery = onOpenGallery,
                scrollToTopSignal = scrollToTopSignal,
            )
        }
    }

    if (showEditTrip) {
        trip?.let { currentTrip ->
            EditTripSheet(
                trip = currentTrip,
                onDismiss = { showEditTrip = false },
                onSave = { name, country, flag, startMs, endMs, budget, currencyCode, coverImagePath ->
                    viewModel.updateTrip(name, country, flag, startMs, endMs, budget, currencyCode, coverImagePath)
                    showEditTrip = false
                },
                onDelete = {
                    showEditTrip = false
                    showDeleteTrip = true
                },
            )
        }
    }

    if (showDeleteTrip) {
        TripConfirmDialog(
            title        = stringResource(R.string.trip_remove_trip),
            body         = stringResource(R.string.trip_delete_trip_body, trip?.name ?: ""),
            confirmLabel = stringResource(R.string.trip_delete),
            destructive  = true,
            onConfirm    = { viewModel.deleteTrip(); showDeleteTrip = false; onBack() },
            onDismiss    = { showDeleteTrip = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDetailItineraryScreen(
    tripId: Long,
    scrollToTopSignal: Int = 0,
    viewModel: TripDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(tripId) { viewModel.init(tripId) }

    val trip      by viewModel.trip.collectAsStateWithLifecycle()
    val itinerary by viewModel.itinerary.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.tripClearFocusOnTap()) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ItineraryTab(
                items     = itinerary,
                tripStart = trip?.startDate ?: System.currentTimeMillis(),
                tripEnd   = trip?.endDate ?: trip?.startDate ?: System.currentTimeMillis(),
                onToggle  = viewModel::toggleItem,
                onDelete  = viewModel::deleteItem,
                onAdd     = { t, d, time, category, imagePath -> viewModel.addItineraryItem(t, d, time, category, imagePath) },
                onEdit    = { item, t, d, time, category, imagePath -> viewModel.editItineraryItem(item, t, d, time, category, imagePath) },
                scrollToTopSignal = scrollToTopSignal,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDetailExpensesScreen(
    tripId: Long,
    scrollToTopSignal: Int = 0,
    viewModel: TripDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(tripId) { viewModel.init(tripId) }

    val trip       by viewModel.trip.collectAsStateWithLifecycle()
    val expenses   by viewModel.expenses.collectAsStateWithLifecycle()
    val totalSpent by viewModel.totalSpent.collectAsStateWithLifecycle()
    val currency   = trip?.currencyCode ?: "BRL"

    Scaffold(modifier = Modifier.tripClearFocusOnTap()) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ExpensesTab(
                expenses   = expenses,
                totalSpent = totalSpent,
                budget     = trip?.budgetCents ?: 0L,
                currency   = currency,
                onAdd      = { cat, amt, desc, imagePath -> viewModel.addExpense(cat, amt, desc, imagePath) },
                onEdit     = { expense, cat, amt, desc, imagePath -> viewModel.editExpense(expense, cat, amt, desc, imagePath) },
                onDelete   = viewModel::deleteExpense,
                scrollToTopSignal = scrollToTopSignal,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDetailGalleryScreen(
    tripId: Long,
    scrollToTopSignal: Int = 0,
    viewModel: TripDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(tripId) { viewModel.init(tripId) }

    val trip         by viewModel.trip.collectAsStateWithLifecycle()
    val itinerary    by viewModel.itinerary.collectAsStateWithLifecycle()
    val gallery      by viewModel.gallery.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.tripClearFocusOnTap()) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            GalleryTab(
                trip = trip,
                photos = gallery,
                itinerary = itinerary,
                onAdd = viewModel::addGalleryPhoto,
                onEdit = viewModel::editGalleryPhoto,
                onDelete = viewModel::deleteGalleryPhoto,
                scrollToTopSignal = scrollToTopSignal,
            )
        }
    }
}


@Composable
internal fun tripDetailHeaderState(
    trip: Trip?,
    selectedSection: TripDetailSection,
    itinerary: List<ItineraryItem>,
    expenses: List<Expense>,
    totalSpent: Long,
    galleryCount: Int,
): TripDetailHeaderState {
    val currentTrip = trip
    if (currentTrip == null) {
        return TripDetailHeaderState(
            animationKey = "trip-loading",
            icon = Icons.Rounded.FlightTakeoff,
            title = stringResource(R.string.trip_trip),
            subtitle = stringResource(R.string.trip_loading_trip),
            iconColor = PanelAmber,
        )
    }

    return when (selectedSection) {
        TripDetailSection.Overview -> {
            val dateFormatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
            TripDetailHeaderState(
                animationKey = "trip-overview-${currentTrip.id}",
                icon = Icons.Rounded.FlightTakeoff,
                title = currentTrip.name,
                subtitle = "${currentTrip.destinationFlag} ${currentTrip.destinationCountry} · 📅 ${
                    dateFormatter.format(Date(currentTrip.startDate))
                } → ${dateFormatter.format(Date(currentTrip.endDate))}",
                iconColor = PanelAmber,
            )
        }
        TripDetailSection.Itinerary -> {
            val completed = itinerary.count { it.isCompleted }
            TripDetailHeaderState(
                animationKey = "trip-itinerary-${currentTrip.id}",
                icon = Icons.Rounded.EditCalendar,
                title = stringResource(R.string.trip_itinerary),
                subtitle = stringResource(R.string.trip_itinerary_header_summary, itinerary.size, completed),
                iconColor = PanelBlue,
            )
        }
        TripDetailSection.Expenses -> {
            TripDetailHeaderState(
                animationKey = "trip-expenses-${currentTrip.id}",
                icon = Icons.Rounded.AttachMoney,
                title = stringResource(R.string.trip_expenses),
                subtitle = stringResource(
                    R.string.trip_expenses_header_summary,
                    expenses.size,
                    formatTripCurrency(totalSpent, currentTrip.currencyCode),
                ),
                iconColor = PanelGreen,
            )
        }
        TripDetailSection.Gallery -> {
            TripDetailHeaderState(
                animationKey = "trip-gallery-${currentTrip.id}",
                icon = Icons.Rounded.PhotoLibrary,
                title = stringResource(R.string.trip_gallery),
                subtitle = currentTrip.name,
                galleryCount = galleryCount,
                iconColor = PanelPurple,
            )
        }
    }
}

@Composable
private fun TripOverview(
    trip: Trip?,
    itinerary: List<ItineraryItem>,
    expenses: List<Expense>,
    gallery: List<TripGalleryPhoto>,
    totalSpent: Long,
    onOpenItinerary: () -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenGallery: () -> Unit,
    scrollToTopSignal: Int = 0,
) {
    val currentTrip = trip
    if (currentTrip == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.trip_loading_trip), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val currency = currentTrip.currencyCode
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GalleryOverviewPanel(
                photos = gallery,
                itinerary = itinerary,
                onClick = onOpenGallery,
            )
        }

        item {
            TripTimelineCard(
                tripStart = currentTrip.startDate,
                items = itinerary,
                onAdd = onOpenItinerary,
                onEdit = onOpenItinerary,
                onViewMore = onOpenItinerary,
            )
        }

        item {
            ExpenseTimelineCard(
                expenses = expenses,
                currency = currency,
                onAdd = onOpenExpenses,
                onEdit = onOpenExpenses,
                onViewMore = onOpenExpenses,
            )
        }

    }
}

@Composable
private fun TripTimelineCard(
    tripStart: Long,
    items: List<ItineraryItem>,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onViewMore: () -> Unit,
) {
    val visibleLimit = 4
    val upcoming = remember(items) { items.take(visibleLimit) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    ElevatedCard(
        onClick = onViewMore,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PanelBlue.copy(alpha = 0.15f),
                    ) {
                        Icon(
                            Icons.Rounded.EditCalendar,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp).size(18.dp),
                            tint = PanelBlue,
                        )
                    }
                    Text(
                        stringResource(R.string.trip_next_activities),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = PanelBlue,
                    )
                }
            }

            if (upcoming.isEmpty()) {
                Surface(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("🧭", style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.trip_itinerary_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    upcoming.forEachIndexed { index, item ->
                        TripTimelineItem(
                            item = item,
                            dateLabel = dateFormatter.format(Date(tripStart + (item.day - 1) * 86_400_000L)),
                            showConnector = index < upcoming.lastIndex,
                        )
                    }
                }
                if (items.size > visibleLimit) {
                    TextButton(
                        onClick = onViewMore,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                    ) {
                        Text(stringResource(R.string.trip_view_more), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TripTimelineItem(
    item: ItineraryItem,
    dateLabel: String,
    showConnector: Boolean,
) {
    val statusColor by animateColorAsState(
        targetValue = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "timeline_status_color",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TripImageThumb(
                imagePath = item.imagePath,
                fallback = item.category.emoji,
                size = 34,
                modifier = Modifier.padding(top = 10.dp),
            )
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(statusColor.copy(alpha = 0.28f)),
                )
            }
        }

        Surface(
            modifier = Modifier.weight(1f).padding(bottom = if (showConnector) 10.dp else 0.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.width(54.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        item.timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        itineraryCategoryLabel(item.category),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (item.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                )
            }
        }
    }
}

@Composable
private fun ExpenseTimelineCard(
    expenses: List<Expense>,
    currency: String,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onViewMore: () -> Unit,
) {
    val visibleLimit = 4
    val recent = remember(expenses) { expenses.take(visibleLimit) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    ElevatedCard(
        onClick = onViewMore,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PanelGreen.copy(alpha = 0.15f),
                    ) {
                        Icon(
                            Icons.Rounded.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp).size(18.dp),
                            tint = PanelGreen,
                        )
                    }
                    Text(
                        stringResource(R.string.trip_recent_expenses),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = PanelGreen,
                    )
                }
            }

            if (recent.isEmpty()) {
                Surface(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("💸", style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.trip_register_expenses_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.tertiary)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recent.forEach { expense ->
                        ExpenseTimelineItem(
                            expense = expense,
                            currency = currency,
                            dateLabel = dateFormatter.format(Date(expense.date)),
                        )
                    }
                }
                if (expenses.size > visibleLimit) {
                    TextButton(
                        onClick = onViewMore,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                    ) {
                        Text(stringResource(R.string.trip_view_more), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseTimelineItem(
    expense: Expense,
    currency: String,
    dateLabel: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TripImageThumb(imagePath = expense.imagePath, fallback = expense.category.emoji, size = 34)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "$dateLabel · ${expenseCategoryLabel(expense.category)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                formatTripCurrency(expense.amountCents, currency),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun GalleryOverviewPanel(
    photos: List<TripGalleryPhoto>,
    itinerary: List<ItineraryItem>,
    onClick: () -> Unit,
) {
    var previewPhoto by remember { mutableStateOf<TripGalleryPhoto?>(null) }
    val itineraryById = remember(itinerary) { itinerary.associateBy { it.id } }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = PanelPurple,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.trip_gallery),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.trip_gallery_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Rounded.Edit, null, tint = PanelPurple)
            }

            if (photos.isEmpty()) {
                Surface(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Rounded.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = PanelPurple,
                        )
                        Text(
                            stringResource(R.string.trip_gallery_empty_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(photos.take(8), key = { it.id }) { photo ->
                        val linkedItem = photo.itineraryItemId?.let { itineraryById[it] }
                        GalleryCarouselItem(
                            photo = photo,
                            linkedItem = linkedItem,
                            onClick = { previewPhoto = photo },
                        )
                    }
                }
            }
        }
    }

    previewPhoto?.let { photo ->
        TripPhotoViewerDialog(
            imagePath = photo.imagePath,
            caption = photo.caption.ifBlank {
                itinerary.firstOrNull { it.id == photo.itineraryItemId }?.title
            },
            onDismiss = { previewPhoto = null },
        )
    }
}

@Composable
private fun GalleryCarouselItem(
    photo: TripGalleryPhoto,
    linkedItem: ItineraryItem?,
    onClick: () -> Unit,
) {
    val fallbackCaption = stringResource(R.string.trip_gallery_photo_without_caption)
    val primaryText = when {
        photo.caption.isNotBlank() -> photo.caption
        linkedItem != null -> linkedItem.title
        else -> fallbackCaption
    }
    val secondaryText = when {
        photo.caption.isNotBlank() && linkedItem != null -> linkedItem.title
        photo.caption.isBlank() && linkedItem != null -> itineraryCategoryLabel(linkedItem.category)
        else -> null
    }
    Surface(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            AsyncImage(
                model = photo.imagePath,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(94.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .clickable(onClick = onClick),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    primaryText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondaryText != null && linkedItem != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        TripImageThumb(
                            imagePath = linkedItem.imagePath,
                            fallback = linkedItem.category.emoji,
                            size = 20,
                        )
                        Text(
                            secondaryText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPickerActions(
    imagePath: String?,
    onImageSelected: (String) -> Unit,
    onRemoveImage: () -> Unit,
    allowRemove: Boolean = true,
) {
    TripImagePickerLauncher(onImageReady = onImageSelected) { launch ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = launch,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Rounded.CameraAlt,
                        null,
                        Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.trip_choose_photo),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (imagePath != null && allowRemove) {
                Surface(
                    onClick = onRemoveImage,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            null,
                            Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.trip_remove_photo),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTripSheet(
    trip: Trip,
    onDismiss: () -> Unit,
    onSave: (name: String, country: String, flag: String, startMs: Long, endMs: Long, budgetCents: Long, currencyCode: String, coverImagePath: String?) -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var name by remember(trip.id) { mutableStateOf(trip.name) }
    var country by remember(trip.id) { mutableStateOf(trip.destinationCountry) }
    var flag by remember(trip.id) { mutableStateOf(trip.destinationFlag) }
    var budget by remember(trip.id) { mutableStateOf("%.2f".format(Locale.US, trip.budgetBrl)) }
    var currency by remember(trip.id) { mutableStateOf(trip.currencyCode) }
    var startMs by remember(trip.id) { mutableStateOf(trip.startDate) }
    var endMs by remember(trip.id) { mutableStateOf(trip.endDate) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).navigationBarsPadding()
                .tripClearFocusOnTap(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TripSheetHeader(
                icon = Icons.Rounded.FlightTakeoff,
                accent = PanelAmber,
                title = stringResource(R.string.trip_edit_trip),
                subtitle = "${trip.destinationFlag} ${trip.destinationCountry}",
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.trip_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = flag,
                    onValueChange = { flag = it.take(4) },
                    label = { Text(stringResource(R.string.trip_icon)) },
                    modifier = Modifier.weight(0.35f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text(stringResource(R.string.trip_destination)) },
                    modifier = Modifier.weight(0.65f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TripEditDateField(stringResource(R.string.trip_departure), dateFormatter.format(Date(startMs)), { showStartPicker = true }, Modifier.weight(1f))
                TripEditDateField(stringResource(R.string.trip_return), dateFormatter.format(Date(endMs)), { showEndPicker = true }, Modifier.weight(1f))
            }
            TripCurrencySelector(
                currency = currency,
                onCurrencyChange = { currency = it },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                label = { Text(stringResource(R.string.trip_budget_label, currencySymbol(currency))) },
                leadingIcon = { Icon(Icons.Rounded.AttachMoney, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Button(
                onClick = {
                    val cents = ((budget.replace(',', '.').toDoubleOrNull() ?: 0.0) * 100).toLong()
                    onSave(name.trim(), country.trim(), flag.ifBlank { "🌍" }, startMs, endMs, cents, currency, trip.coverImagePath)
                },
                enabled = name.isNotBlank() && country.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.trip_save_trip), fontWeight = FontWeight.SemiBold)
            }
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.trip_remove_trip), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showStartPicker) {
        TripDatePickerDialog(
            initialMillis = startMs,
            onConfirm = { ms ->
                startMs = ms
                if (endMs <= startMs) endMs = startMs + 86_400_000L
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false },
        )
    }
    if (showEndPicker) {
        TripDatePickerDialog(
            initialMillis = endMs,
            onConfirm = { ms ->
                endMs = ms.coerceAtLeast(startMs + 86_400_000L)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripCurrencySelector(
    currency: String,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = currency,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.trip_currency)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            supportedTripCurrencies.forEach { code ->
                DropdownMenuItem(
                    text = { Text("$code · ${currencySymbol(code)}") },
                    onClick = {
                        onCurrencyChange(code)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TripEditDateField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = Color.Transparent,
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryTab(
    items: List<ItineraryItem>,
    tripStart: Long,
    tripEnd: Long,
    onToggle: (ItineraryItem) -> Unit,
    onDelete: (ItineraryItem) -> Unit,
    onAdd: (title: String, day: Int, time: String, category: ItineraryCategory, imagePath: String?) -> Unit,
    onEdit: (ItineraryItem, title: String, day: Int, time: String, category: ItineraryCategory, imagePath: String?) -> Unit,
    scrollToTopSignal: Int = 0,
) {
    var showAddSheet      by remember { mutableStateOf(false) }
    var editingItem       by remember { mutableStateOf<ItineraryItem?>(null) }
    var pendingDeleteItem by remember { mutableStateOf<ItineraryItem?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SectionEmptyState(
                        icon = Icons.Rounded.EditCalendar,
                        accent = PanelBlue,
                        title = stringResource(R.string.trip_no_activity),
                        hint = stringResource(R.string.trip_itinerary_hint),
                        onAdd = { showAddSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item { Spacer(Modifier.height(280.dp)) }
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                state          = listState,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(items, key = { _, it -> it.id }) { _, item ->
                    val bgColor by animateColorAsState(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        label = "bg",
                    )
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingItem = item },
                        shape     = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = 1.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
                    ) {
                        Row(
                            modifier          = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            TripImageThumb(imagePath = item.imagePath, fallback = item.category.emoji, size = 34)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PanelBlue.copy(alpha = 0.14f),
                                ) {
                                    Text(stringResource(R.string.trip_day_format, item.day, item.timeStr),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PanelBlue)
                                }
                                Text(item.title, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (item.isCompleted)
                                        MaterialTheme.colorScheme.onSurface.copy(0.4f)
                                    else MaterialTheme.colorScheme.onSurface)
                            }
                            Icon(
                                Icons.Rounded.Edit,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f),
                                modifier = Modifier.size(18.dp),
                            )
                            IconButton(
                                onClick = { onToggle(item) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = if (item.isCompleted) stringResource(R.string.trip_mark_pending) else stringResource(R.string.trip_mark_complete),
                                    tint = if (item.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick  = { showAddSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = PanelBlue,
            contentColor = Color.White,
        ) { Icon(Icons.Outlined.Add, stringResource(R.string.trip_add_activity)) }
    }

    if (showAddSheet) {
        ItineraryItemSheet(
            tripStart  = tripStart,
            tripEnd    = tripEnd,
            editingItem = null,
            onDismiss  = { showAddSheet = false },
            onSave     = { t, d, time, category, imagePath -> onAdd(t, d, time, category, imagePath); showAddSheet = false },
            onUpdate   = { _, _, _, _, _, _ -> },
        )
    }

    editingItem?.let { item ->
        ItineraryItemSheet(
            tripStart   = tripStart,
            tripEnd     = tripEnd,
            editingItem = item,
            onDismiss   = { editingItem = null },
            onSave      = { _, _, _, _, _ -> },
            onUpdate    = { it2, t, d, time, category, imagePath ->
                onEdit(it2, t, d, time, category, imagePath)
                editingItem = null
            },
            onDelete = { itemToDelete ->
                pendingDeleteItem = itemToDelete
                editingItem = null
            },
        )
    }

    pendingDeleteItem?.let { item ->
        TripConfirmDialog(
            title        = stringResource(R.string.trip_remove_activity),
            body         = stringResource(R.string.trip_remove_item_body, item.title),
            confirmLabel = stringResource(R.string.trip_remove),
            destructive  = true,
            onConfirm    = {
                onDelete(item)
                pendingDeleteItem = null
            },
            onDismiss    = { pendingDeleteItem = null },
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryItemSheet(
    tripStart: Long,
    tripEnd: Long,
    editingItem: ItineraryItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, day: Int, time: String, category: ItineraryCategory, imagePath: String?) -> Unit,
    onUpdate: (item: ItineraryItem, title: String, day: Int, time: String, category: ItineraryCategory, imagePath: String?) -> Unit,
    onDelete: ((ItineraryItem) -> Unit)? = null,
) {
    val isEditing     = editingItem != null
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = remember { SimpleDateFormat("EEE, dd/MM", Locale.getDefault()) }

    val normalizedTripEnd = remember(tripStart, tripEnd) { tripEnd.coerceAtLeast(tripStart) }
    val initialMs = remember(editingItem, tripStart, normalizedTripEnd) {
        val candidate = if (editingItem != null) {
            tripStart + (editingItem.day - 1) * 86_400_000L
        } else {
            tripStart.coerceAtLeast(System.currentTimeMillis())
        }
        candidate.coerceIn(tripStart, normalizedTripEnd)
    }
    val initialHour = remember(editingItem) {
        if (editingItem != null) {
            editingItem.timeStr.substringBefore(":").toIntOrNull() ?: 9
        } else 9
    }
    val initialMin = remember(editingItem) {
        if (editingItem != null) {
            editingItem.timeStr.substringAfter(":").toIntOrNull() ?: 0
        } else 0
    }

    var title          by remember(editingItem?.id) { mutableStateOf(editingItem?.title ?: "") }
    var category       by remember(editingItem?.id) { mutableStateOf(editingItem?.category ?: ItineraryCategory.ACTIVITY) }
    var imagePath      by remember(editingItem?.id) { mutableStateOf(editingItem?.imagePath) }
    var selectedDateMs by remember(editingItem?.id, initialMs) { mutableStateOf(initialMs) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour   by remember(editingItem?.id) { mutableStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinute by remember(editingItem?.id) { mutableStateOf(initialMin.coerceIn(0, 59)) }

    val dayNumber = remember(selectedDateMs, tripStart) {
        if (tripStart <= 0L) 1
        else ((selectedDateMs - tripStart) / 86_400_000L + 1).toInt().coerceAtLeast(1)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .tripClearFocusOnTap(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TripSheetHeader(
                icon = Icons.Rounded.CalendarToday,
                accent = PanelBlue,
                title = if (isEditing) stringResource(R.string.trip_edit_activity) else stringResource(R.string.trip_new_activity),
                subtitle = stringResource(R.string.trip_day_format, dayNumber, dateFormatter.format(Date(selectedDateMs))),
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PhotoPickerActions(
                imagePath = imagePath,
                onImageSelected = { imagePath = it },
                onRemoveImage = { imagePath = null },
            )

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.trip_activity_title)) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(R.string.trip_category),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ItineraryCategory.entries.chunked(3).forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            rowCategories.forEach { cat ->
                                val selected = cat == category
                                Surface(
                                    onClick = { category = cat },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) PanelBlue.copy(alpha = 0.14f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (selected) BorderStroke(1.5.dp, PanelBlue)
                                    else null,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(cat.emoji, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            itineraryCategoryLabel(cat),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected) PanelBlue
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.trip_date), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Surface(
                    onClick  = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color    = Color.Transparent,
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(stringResource(R.string.trip_day_label, dayNumber), style = MaterialTheme.typography.labelSmall,
                                color = PanelBlue)
                            Text(dateFormatter.format(Date(selectedDateMs)),
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(20.dp),
                            tint = PanelBlue)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.trip_time), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Surface(
                    onClick  = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color    = Color.Transparent,
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("%02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Rounded.AccessTime, null, modifier = Modifier.size(20.dp),
                            tint = PanelBlue)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    val timeStr = "%02d:%02d".format(selectedHour, selectedMinute)
                    if (editingItem != null) {
                        onUpdate(editingItem, title, dayNumber, timeStr, category, imagePath)
                    } else {
                        onSave(title, dayNumber, timeStr, category, imagePath)
                    }
                },
                enabled  = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PanelBlue,
                    contentColor = Color.White,
                ),
            ) {
                Icon(if (isEditing) Icons.Rounded.Edit else Icons.Outlined.Add, null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) stringResource(R.string.trip_save_changes) else stringResource(R.string.trip_add_activity),
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
            if (editingItem != null && onDelete != null) {
                TextButton(
                    onClick = { onDelete(editingItem) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.trip_remove_activity), fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        TripDatePickerDialog(
            initialMillis = selectedDateMs,
            minMillis     = tripStart,
            maxMillis     = normalizedTripEnd,
            onConfirm     = { ms -> selectedDateMs = ms.coerceIn(tripStart, normalizedTripEnd); showDatePicker = false },
            onDismiss     = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        TripTimePickerDialog(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            onConfirm = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun TripTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var hourText by remember(initialHour) { mutableStateOf("%02d".format(initialHour.coerceIn(0, 23))) }
    var minuteText by remember(initialMinute) { mutableStateOf("%02d".format(initialMinute.coerceIn(0, 59))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.trip_choose_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                    label = { Text("HH", style = MaterialTheme.typography.labelSmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.titleMedium,
                )
                Text(
                    ":",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                    label = { Text("MM", style = MaterialTheme.typography.labelSmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.titleMedium,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        hourText.toIntOrNull()?.coerceIn(0, 23) ?: initialHour.coerceIn(0, 23),
                        minuteText.toIntOrNull()?.coerceIn(0, 59) ?: initialMinute.coerceIn(0, 59),
                    )
                },
            ) {
                Text(stringResource(R.string.trip_confirm), style = MaterialTheme.typography.labelMedium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.trip_cancel), style = MaterialTheme.typography.labelMedium)
            }
        },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpensesTab(
    expenses: List<Expense>,
    totalSpent: Long,
    budget: Long,
    currency: String,
    onAdd: (ExpenseCategory, Double, String, String?) -> Unit,
    onEdit: (Expense, ExpenseCategory, Double, String, String?) -> Unit,
    onDelete: (Expense) -> Unit,
    scrollToTopSignal: Int = 0,
) {
    var showAddSheet         by remember { mutableStateOf(false) }
    var editingExpense       by remember { mutableStateOf<Expense?>(null) }
    var pendingDeleteExpense by remember { mutableStateOf<Expense?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            state               = listState,
            contentPadding      = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { BudgetOverviewCard(totalSpent = totalSpent, budget = budget, currency = currency) }
            if (expenses.isEmpty()) {
                item {
                    SectionEmptyState(
                        icon = Icons.Rounded.AttachMoney,
                        accent = PanelGreen,
                        title = stringResource(R.string.trip_no_expense),
                        hint = stringResource(R.string.trip_register_expenses_hint),
                        onAdd = { showAddSheet = true },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            } else {
                itemsIndexed(expenses, key = { _, e -> e.id }) { _, expense ->
                    ExpenseCard(
                        expense = expense,
                        currency = currency,
                        onClick = { editingExpense = expense },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick  = { showAddSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = PanelGreen,
            contentColor = Color.White,
        ) { Icon(Icons.Outlined.Add, stringResource(R.string.trip_add_expense)) }
    }

    if (showAddSheet) {
        ExpenseSheet(
            editingExpense = null,
            currency = currency,
            onDismiss = { showAddSheet = false },
            onSave = { cat, amt, desc, imagePath -> onAdd(cat, amt, desc, imagePath); showAddSheet = false },
            onUpdate = { _, _, _, _, _ -> },
        )
    }

    editingExpense?.let { expense ->
        ExpenseSheet(
            editingExpense = expense,
            currency = currency,
            onDismiss = { editingExpense = null },
            onSave = { _, _, _, _ -> },
            onUpdate = { current, cat, amt, desc, imagePath ->
                onEdit(current, cat, amt, desc, imagePath)
                editingExpense = null
            },
            onDelete = { expenseToDelete ->
                pendingDeleteExpense = expenseToDelete
                editingExpense = null
            },
        )
    }

    pendingDeleteExpense?.let { expense ->
        TripConfirmDialog(
            title        = stringResource(R.string.trip_remove_expense),
            body         = stringResource(R.string.trip_remove_expense_body, expense.category.emoji, expense.description),
            confirmLabel = stringResource(R.string.trip_remove),
            destructive  = true,
            onConfirm    = {
                onDelete(expense)
                pendingDeleteExpense = null
            },
            onDismiss    = { pendingDeleteExpense = null },
        )
    }
}

@Composable
private fun BudgetOverviewCard(totalSpent: Long, budget: Long, currency: String) {
    val spent = totalSpent / 100.0
    val total = budget / 100.0
    val remaining = (total - spent).coerceAtLeast(0.0)
    val pct   = if (total > 0) (spent / total).toFloat().coerceIn(0f, 1f) else 0f
    val overBudget = pct > 0.9f
    ElevatedCard(
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.trip_financial_summary), style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text(
                        if (overBudget) stringResource(R.string.trip_budget_warning) else stringResource(R.string.trip_budget_ok),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (overBudget) MaterialTheme.colorScheme.errorContainer else PanelGreen.copy(alpha = 0.16f),
                ) {
                    Text(
                        "${(pct * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (overBudget) MaterialTheme.colorScheme.onErrorContainer else PanelGreen,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier.fillMaxWidth(),
                color    = if (overBudget) MaterialTheme.colorScheme.error
                           else PanelGreen,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(0.45f),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseMetric(
                    label = stringResource(R.string.trip_spent_label),
                    value = formatTripCurrency(totalSpent, currency),
                    highlighted = true,
                    isError = overBudget,
                    accent = PanelGreen,
                    modifier = Modifier.weight(1f),
                )
                ExpenseMetric(
                    label = stringResource(R.string.trip_free_label),
                    value = formatTripCurrency((remaining * 100).toLong(), currency),
                    highlighted = false,
                    isError = false,
                    accent = PanelGreen,
                    modifier = Modifier.weight(1f),
                )
                ExpenseMetric(
                    label = stringResource(R.string.trip_budget_short),
                    value = formatTripCurrency(budget, currency),
                    highlighted = false,
                    isError = false,
                    accent = PanelGreen,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ExpenseMetric(
    label: String,
    value: String,
    highlighted: Boolean,
    isError: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (highlighted) {
            if (isError) MaterialTheme.colorScheme.errorContainer else accent.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = if (highlighted && isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    highlighted && isError -> MaterialTheme.colorScheme.onErrorContainer
                    highlighted -> accent
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun TripImageThumb(imagePath: String?, fallback: String, size: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3).dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (imagePath != null) {
            AsyncImage(model = imagePath, contentDescription = null, modifier = Modifier.fillMaxSize())
        } else {
            Text(fallback, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, currency: String, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TripImageThumb(imagePath = expense.imagePath, fallback = expense.category.emoji, size = 44)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(expense.description, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Text(expenseCategoryLabel(expense.category), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatTripCurrency(expense.amountCents, currency), style = MaterialTheme.typography.labelLarge,
                color = PanelGreen, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Rounded.Edit, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f),
                modifier = Modifier.size(18.dp))
        }
    }
}


@Composable
private fun itineraryCategoryLabel(category: ItineraryCategory): String = when (category) {
    ItineraryCategory.TRANSPORT -> stringResource(R.string.trip_itinerary_transport)
    ItineraryCategory.LODGING -> stringResource(R.string.trip_itinerary_lodging)
    ItineraryCategory.FOOD -> stringResource(R.string.trip_itinerary_food)
    ItineraryCategory.ACTIVITY -> stringResource(R.string.trip_itinerary_activity)
    ItineraryCategory.SHOPPING -> stringResource(R.string.trip_itinerary_shopping)
    ItineraryCategory.OTHER -> stringResource(R.string.trip_itinerary_other)
}

@Composable
private fun expenseCategoryLabel(category: ExpenseCategory): String = when (category) {
    ExpenseCategory.TRANSPORT -> stringResource(R.string.trip_expense_transport)
    ExpenseCategory.LODGING -> stringResource(R.string.trip_expense_lodging)
    ExpenseCategory.FOOD -> stringResource(R.string.trip_expense_food)
    ExpenseCategory.ACTIVITY -> stringResource(R.string.trip_expense_activity)
    ExpenseCategory.SHOPPING -> stringResource(R.string.trip_expense_shopping)
    ExpenseCategory.OTHER -> stringResource(R.string.trip_expense_other)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseSheet(
    editingExpense: Expense?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (ExpenseCategory, Double, String, String?) -> Unit,
    onUpdate: (Expense, ExpenseCategory, Double, String, String?) -> Unit,
    onDelete: ((Expense) -> Unit)? = null,
) {
    val isEditing = editingExpense != null
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var category    by remember(editingExpense?.id) { mutableStateOf(editingExpense?.category ?: ExpenseCategory.TRANSPORT) }
    var amount      by remember(editingExpense?.id) { mutableStateOf(editingExpense?.amountBrl?.let { "%.2f".format(Locale.US, it) } ?: "") }
    var description by remember(editingExpense?.id) { mutableStateOf(editingExpense?.description ?: "") }
    var imagePath   by remember(editingExpense?.id) { mutableStateOf(editingExpense?.imagePath) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .tripClearFocusOnTap(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TripSheetHeader(
                icon = Icons.Rounded.AttachMoney,
                accent = PanelGreen,
                title = if (isEditing) stringResource(R.string.trip_edit_expense) else stringResource(R.string.trip_new_expense),
                subtitle = "${category.emoji} ${expenseCategoryLabel(category)}",
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PhotoPickerActions(
                imagePath = imagePath,
                onImageSelected = { imagePath = it },
                onRemoveImage = { imagePath = null },
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.trip_category), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExpenseCategory.entries.chunked(3).forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            rowCategories.forEach { cat ->
                                val selected = cat == category
                                Surface(
                                    onClick = { category = cat },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) PanelGreen.copy(alpha = 0.14f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (selected) BorderStroke(1.5.dp, PanelGreen)
                                    else null,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(cat.emoji, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            expenseCategoryLabel(cat),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected) PanelGreen
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value         = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                label         = { Text(stringResource(R.string.trip_value_label, currencySymbol(currency))) },
                leadingIcon   = { Icon(Icons.Rounded.AttachMoney, null) },
                shape         = RoundedCornerShape(14.dp),
                modifier      = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text(stringResource(R.string.trip_description)) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(), singleLine = true,
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    val value = amount.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (editingExpense != null) onUpdate(editingExpense, category, value, description, imagePath)
                    else onSave(category, value, description, imagePath)
                },
                enabled  = amount.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = PanelGreen,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Rounded.AttachMoney, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) stringResource(R.string.trip_save_expense) else stringResource(R.string.trip_register_expense), style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
            }
            if (editingExpense != null && onDelete != null) {
                TextButton(
                    onClick = { onDelete(editingExpense) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.trip_remove_expense), fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryTab(
    trip: Trip?,
    photos: List<TripGalleryPhoto>,
    itinerary: List<ItineraryItem>,
    onAdd: (String, Long?, String) -> Unit,
    onEdit: (TripGalleryPhoto, String, Long?, String) -> Unit,
    onDelete: (TripGalleryPhoto) -> Unit,
    scrollToTopSignal: Int = 0,
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var editingPhoto by remember { mutableStateOf<TripGalleryPhoto?>(null) }
    var previewPhoto by remember { mutableStateOf<TripGalleryPhoto?>(null) }
    var pendingDelete by remember { mutableStateOf<TripGalleryPhoto?>(null) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0 && previewPhoto == null) gridState.animateScrollToItem(0)
    }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (photos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionEmptyState(
                        icon = Icons.Rounded.PhotoLibrary,
                        accent = PanelPurple,
                        title = stringResource(R.string.trip_gallery_empty),
                        hint = stringResource(R.string.trip_gallery_empty_hint),
                        onAdd = { showAddSheet = true },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            } else {
                gridItems(photos, key = { it.id }) { photo ->
                    InstagramGridPhoto(
                        photo = photo,
                        onClick = { previewPhoto = photo },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = PanelPurple,
            contentColor = Color.White,
        ) {
            Icon(Icons.Outlined.Add, stringResource(R.string.trip_gallery_add_photo))
        }
    }

    previewPhoto?.let { currentPreviewPhoto ->
        Dialog(
            onDismissRequest = { previewPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            InstagramPostPreviewScreen(
                photo = currentPreviewPhoto,
                linkedItem = itinerary.firstOrNull { it.id == currentPreviewPhoto.itineraryItemId },
                dateLabel = dateFormatter.format(Date(currentPreviewPhoto.createdAt)),
                onBack = { previewPhoto = null },
                onEdit = {
                    previewPhoto = null
                    editingPhoto = currentPreviewPhoto
                },
            )
        }
    }

    if (showAddSheet) {
        GalleryPhotoSheet(
            itinerary = itinerary,
            editingPhoto = null,
            onDismiss = { showAddSheet = false },
            onSave = { imagePath, itineraryItemId, caption ->
                onAdd(imagePath, itineraryItemId, caption)
                showAddSheet = false
            },
            onUpdate = { _, _, _, _ -> },
            onDelete = null,
        )
    }

    editingPhoto?.let { photo ->
        GalleryPhotoSheet(
            itinerary = itinerary,
            editingPhoto = photo,
            onDismiss = { editingPhoto = null },
            onSave = { _, _, _ -> },
            onUpdate = { currentPhoto, imagePath, itineraryItemId, caption ->
                onEdit(currentPhoto, imagePath, itineraryItemId, caption)
                editingPhoto = null
            },
            onDelete = { currentPhoto ->
                editingPhoto = null
                pendingDelete = currentPhoto
            },
        )
    }

    pendingDelete?.let { photo ->
        TripConfirmDialog(
            title = stringResource(R.string.trip_gallery_remove_photo),
            body = stringResource(R.string.trip_gallery_remove_photo_body),
            confirmLabel = stringResource(R.string.trip_remove),
            destructive = true,
            onConfirm = {
                onDelete(photo)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun SectionEmptyState(
    icon: ImageVector,
    accent: Color,
    title: String,
    hint: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onAdd,
        modifier = modifier.fillMaxWidth().height(156.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = accent)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InstagramGridPhoto(
    photo: TripGalleryPhoto,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = photo.imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (photo.caption.isNotBlank()) {
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.48f),
            ) {
                Text(
                    "Aa",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun InstagramPostPreviewScreen(
    photo: TripGalleryPhoto,
    linkedItem: ItineraryItem?,
    dateLabel: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val fallbackCaption = stringResource(R.string.trip_gallery_photo_without_caption)
    val postCaption = photo.caption.ifBlank { linkedItem?.title ?: fallbackCaption }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = photo.imagePath,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit,
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
            ) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.trip_gallery_edit_photo),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TripImageThumb(
                        imagePath = linkedItem?.imagePath,
                        fallback = linkedItem?.category?.emoji ?: "📷",
                        size = 36,
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            linkedItem?.title ?: stringResource(R.string.trip_gallery_photo_without_caption),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                        Text(
                            dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    postCaption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (linkedItem != null) {
                    Text(
                        "${linkedItem.category.emoji} ${linkedItem.title}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryPhotoSheet(
    itinerary: List<ItineraryItem>,
    editingPhoto: TripGalleryPhoto?,
    onDismiss: () -> Unit,
    onSave: (String, Long?, String) -> Unit,
    onUpdate: (TripGalleryPhoto, String, Long?, String) -> Unit,
    onDelete: ((TripGalleryPhoto) -> Unit)?,
) {
    val isEditing = editingPhoto != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var imagePath by remember(editingPhoto?.id) { mutableStateOf(editingPhoto?.imagePath) }
    var caption by remember(editingPhoto?.id) { mutableStateOf(editingPhoto?.caption ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember(editingPhoto?.id, itinerary) {
        mutableStateOf(itinerary.firstOrNull { it.id == editingPhoto?.itineraryItemId })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .tripClearFocusOnTap(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TripSheetHeader(
                icon = Icons.Rounded.PhotoLibrary,
                accent = PanelPurple,
                title = if (isEditing) stringResource(R.string.trip_gallery_edit_photo)
                    else stringResource(R.string.trip_gallery_add_photo),
                subtitle = stringResource(R.string.trip_gallery_add_photo_subtitle),
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            PhotoPickerActions(
                imagePath = imagePath,
                onImageSelected = { imagePath = it },
                onRemoveImage = { imagePath = null },
                allowRemove = false,
            )

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text(stringResource(R.string.trip_gallery_caption)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = selectedItem?.title ?: stringResource(R.string.trip_gallery_no_activity),
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text(stringResource(R.string.trip_gallery_activity_optional)) },
                    leadingIcon = selectedItem?.let { item ->
                        {
                            TripImageThumb(
                                imagePath = item.imagePath,
                                fallback = item.category.emoji,
                                size = 28,
                            )
                        }
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.trip_gallery_no_activity)) },
                        onClick = {
                            selectedItem = null
                            expanded = false
                        },
                    )
                    itinerary.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.title) },
                            leadingIcon = {
                                TripImageThumb(
                                    imagePath = item.imagePath,
                                    fallback = item.category.emoji,
                                    size = 30,
                                )
                            },
                            onClick = {
                                selectedItem = item
                                expanded = false
                            },
                        )
                    }
                }
            }

            Button(
                onClick = {
                    imagePath?.let { selectedImage ->
                        if (editingPhoto != null) {
                            onUpdate(editingPhoto, selectedImage, selectedItem?.id, caption.trim())
                        } else {
                            onSave(selectedImage, selectedItem?.id, caption.trim())
                        }
                    }
                },
                enabled = imagePath != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(if (isEditing) Icons.Rounded.Edit else Icons.Outlined.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.trip_gallery_save_photo), fontWeight = FontWeight.SemiBold)
            }
            if (editingPhoto != null && onDelete != null) {
                TextButton(
                    onClick = { onDelete(editingPhoto) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Rounded.Delete, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.trip_gallery_remove_photo), fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
