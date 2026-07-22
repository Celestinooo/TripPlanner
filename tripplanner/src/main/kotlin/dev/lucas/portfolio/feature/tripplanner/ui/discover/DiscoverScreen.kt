package dev.lucas.portfolio.feature.tripplanner.ui.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lucas.portfolio.feature.tripplanner.R
import dev.lucas.portfolio.feature.tripplanner.data.datastore.TripPrefsRepository
import dev.lucas.portfolio.feature.tripplanner.data.repository.DiscoverRepository
import dev.lucas.portfolio.feature.tripplanner.data.repository.TripRepository
import dev.lucas.portfolio.feature.tripplanner.domain.Destination
import dev.lucas.portfolio.feature.tripplanner.domain.Trip
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripSheetHeader
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripTopHeader
import dev.lucas.portfolio.feature.tripplanner.ui.common.currencySymbol
import dev.lucas.portfolio.feature.tripplanner.ui.common.supportedTripCurrencies
import dev.lucas.portfolio.feature.tripplanner.ui.common.tripClearFocusOnTap
import dev.lucas.portfolio.feature.tripplanner.ui.trips.TripDatePickerDialog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


internal enum class DiscoverRefreshState {
    Idle, Downloading, Success
}

internal data class DiscoverRefreshAction(
    val state: DiscoverRefreshState,
    val onClick: () -> Unit,
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val discoverRepo: DiscoverRepository,
    private val tripRepo: TripRepository,
    private val prefsRepo: TripPrefsRepository,
) : ViewModel() {

    val query = MutableStateFlow("")
    val selectedRegion = MutableStateFlow("Todas")
    private val allDestinations = MutableStateFlow<List<Destination>?>(null)
    private val _refreshState = MutableStateFlow(DiscoverRefreshState.Idle)
    private var regionInitialized = false

    internal val refreshState: StateFlow<DiscoverRefreshState> = _refreshState

    val currency = prefsRepo.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "BRL")

    val defaultRegion = prefsRepo.defaultRegion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Todas")

    init {
        viewModelScope.launch {
            prefsRepo.defaultRegion.collect { region ->
                if (!regionInitialized) {
                    selectedRegion.value = region.ifBlank { "Todas" }
                    regionInitialized = true
                }
            }
        }
        viewModelScope.launch {
            runCatching { discoverRepo.cachedDestinations() }
                .onSuccess { allDestinations.value = it }
                .onFailure { allDestinations.value = emptyList() }
        }
    }

    @OptIn(FlowPreview::class)
    internal val destinations: StateFlow<List<Destination>?> = combine(
        allDestinations,
        query.debounce(400),
        selectedRegion,
    ) { destinations, q, region ->
        destinations?.let { list ->
            val normalizedRegion = if (region != "Todas") region else ""
            list.filter {
                val matchesQuery = q.isBlank() ||
                    it.name.contains(q, ignoreCase = true) ||
                    it.region.contains(q, ignoreCase = true)
                val matchesRegion = normalizedRegion.isBlank() ||
                    it.region.equals(normalizedRegion, ignoreCase = true)
                matchesQuery && matchesRegion
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun search(q: String) { query.value = q }
    fun selectRegion(region: String) { selectedRegion.value = region }

    fun refreshDestinations() {
        if (_refreshState.value == DiscoverRefreshState.Downloading) return
        viewModelScope.launch {
            _refreshState.value = DiscoverRefreshState.Downloading
            runCatching { discoverRepo.refreshDestinations() }
                .onSuccess {
                    allDestinations.value = it
                    _refreshState.value = DiscoverRefreshState.Success
                    delay(2_000)
                    if (_refreshState.value == DiscoverRefreshState.Success) {
                        _refreshState.value = DiscoverRefreshState.Idle
                    }
                }
                .onFailure {
                    _refreshState.value = DiscoverRefreshState.Idle
                }
        }
    }

    fun addTrip(
        dest: Destination,
        tripName: String,
        startMs: Long,
        endMs: Long,
        budgetCents: Long,
        currencyCode: String,
        onCreated: (Long) -> Unit,
    ) =
        viewModelScope.launch {
            val tripId = tripRepo.saveTrip(
                Trip(
                    name               = tripName,
                    destinationCountry = dest.name,
                    destinationFlag    = dest.flag,
                    startDate          = startMs,
                    endDate            = endMs,
                    budgetCents        = budgetCents,
                    currencyCode       = currencyCode,
                )
            )
            onCreated(tripId)
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    onOpenTrip: (Long) -> Unit,
    showHeader: Boolean = true,
    onDestinationCountChanged: (Int?) -> Unit = {},
    onRefreshActionChanged: (DiscoverRefreshAction?) -> Unit = {},
    scrollToTopSignal: Int = 0,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    var searchText   by remember { mutableStateOf("") }
    val destinations by viewModel.destinations.collectAsStateWithLifecycle()
    val destinationCount = destinations?.size
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val selectedRegion by viewModel.selectedRegion.collectAsStateWithLifecycle()
    val refreshState by viewModel.refreshState.collectAsStateWithLifecycle()
    var selectedDest by remember { mutableStateOf<Destination?>(null) }
    val gridState = rememberLazyStaggeredGridState()
    val isInitialLoading = destinations == null
    val onRefreshClick = remember(viewModel) { { viewModel.refreshDestinations() } }

    LaunchedEffect(destinationCount) {
        onDestinationCountChanged(destinationCount)
    }
    LaunchedEffect(refreshState, onRefreshClick) {
        onRefreshActionChanged(
            DiscoverRefreshAction(
                state = refreshState,
                onClick = onRefreshClick,
            )
        )
    }
    DisposableEffect(Unit) {
        onDispose { onRefreshActionChanged(null) }
    }
    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) gridState.animateScrollToItem(0)
    }
    LaunchedEffect(selectedRegion) {
        gridState.scrollToItem(0)
    }

    LazyVerticalStaggeredGrid(
        columns               = StaggeredGridCells.Fixed(2),
        modifier              = Modifier.fillMaxSize().tripClearFocusOnTap(),
        state                 = gridState,
        contentPadding        = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing   = 10.dp,
    ) {
        if (showHeader) {
            item(span = StaggeredGridItemSpan.FullLine) {
                DiscoverHeroHeader(itemCount = destinationCount)
            }
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            OutlinedTextField(
                value         = searchText,
                onValueChange = { searchText = it; viewModel.search(it) },
                placeholder   = { Text(stringResource(R.string.trip_search_destinations)) },
                leadingIcon   = { Icon(Icons.Rounded.Search, null) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                shape         = RoundedCornerShape(16.dp),
            )
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            RegionFilterChips(
                selectedRegion = selectedRegion,
                onRegionSelected = viewModel::selectRegion,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        if (isInitialLoading) {
            for (idx in 0 until 8) {
                item { DestinationShimmerCard(imageHeight = destinationImageHeight(idx)) }
            }
        }

        destinations.orEmpty().forEachIndexed { idx, dest ->
            item(key = dest.name) {
                DestinationCard(
                    dest        = dest,
                    imageHeight = destinationImageHeight(idx),
                    accent      = destinationGradient(idx),
                    modifier    = Modifier.animateItem(),
                    onClick     = { selectedDest = dest },
                )
            }
        }
    }

    selectedDest?.let { dest ->
        AddTripFromDestSheet(
            dest      = dest,
            initialCurrency  = currency,
            onDismiss = { selectedDest = null },
            onConfirm = { tripName, startMs, endMs, budgetCents, currencyCode ->
                viewModel.addTrip(dest, tripName, startMs, endMs, budgetCents, currencyCode, onOpenTrip)
                selectedDest = null
            },
        )
    }
}


@Composable
private fun DiscoverHeroHeader(itemCount: Int?) {
    TripTopHeader(
        icon = Icons.Rounded.Explore,
        title = stringResource(R.string.trip_discover_title),
        subtitle = if (itemCount != null) stringResource(R.string.trip_destinations_available, itemCount)
            else stringResource(R.string.trip_loading_destinations),
    )
}

private val DiscoverOrange = Color(0xFFFB8C00)

private fun regionColor(region: String): Color = when (region) {
    "Americas" -> Color(0xFFE53935)
    "Europe"   -> Color(0xFF1E88E5)
    "Asia"     -> Color(0xFF2E9E4F)
    "Africa"   -> Color(0xFFF9A825)
    "Oceania"  -> Color(0xFF8E24AA)
    else       -> Color(0xFFEC407A)
}

@Composable
private fun RegionFilterChips(
    selectedRegion: String,
    onRegionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val regions = remember { listOf("Todas", "Americas", "Europe", "Asia", "Africa", "Oceania") }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(regions) { region ->
            val selected = selectedRegion == region
            val c = regionColor(region)
            FilterChip(
                selected = selected,
                onClick = { onRegionSelected(region) },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Text(
                        regionLabel(region),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = c.copy(alpha = 0.13f),
                    labelColor = c,
                    selectedContainerColor = c,
                    selectedLabelColor = Color.White,
                ),
                border = if (selected) null else BorderStroke(1.dp, c.copy(alpha = 0.35f)),
            )
        }
    }
}

@Composable
private fun regionLabel(region: String): String = when (region) {
    "Americas" -> stringResource(R.string.trip_region_americas)
    "Europe" -> stringResource(R.string.trip_region_europe)
    "Asia" -> stringResource(R.string.trip_region_asia)
    "Africa" -> stringResource(R.string.trip_region_africa)
    "Oceania" -> stringResource(R.string.trip_region_oceania)
    else -> stringResource(R.string.trip_region_all)
}


private val destinationGradients = listOf(
    listOf(Color(0xFFFF6B6B), Color(0xFFFF9A44)),
    listOf(Color(0xFF4E65FF), Color(0xFF92EFFD)),
    listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
    listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
    listOf(Color(0xFFF7971E), Color(0xFFFFD200)),
    listOf(Color(0xFFEE0979), Color(0xFFFF6A00)),
    listOf(Color(0xFF00C6FB), Color(0xFF005BEA)),
    listOf(Color(0xFFB621FE), Color(0xFF1FD1F9)),
)

private fun destinationGradient(index: Int): List<Color> =
    destinationGradients[index % destinationGradients.size]

private fun destinationImageHeight(index: Int): Dp = when (index % 6) {
    0 -> 150.dp
    1 -> 116.dp
    2 -> 178.dp
    3 -> 132.dp
    4 -> 158.dp
    else -> 122.dp
}


@Composable
private fun DestinationCard(
    dest: Destination,
    imageHeight: Dp,
    accent: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier  = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .background(
                        Brush.linearGradient(
                            colors = accent,
                            start = Offset.Zero,
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                )
                Text(dest.flag, fontSize = 46.sp)
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    dest.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (dest.region.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accent.first()),
                        )
                        Text(
                            regionLabel(dest.region),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = accent.first(),
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun DestinationShimmerCard(
    imageHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val brush = remember(base) {
        Brush.linearGradient(
            listOf(
                base.copy(alpha = 0.55f),
                base.copy(alpha = 0.25f),
                base.copy(alpha = 0.55f),
            ),
        )
    }
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(imageHeight).background(brush))
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                Box(Modifier.fillMaxWidth(0.4f).height(10.dp).clip(RoundedCornerShape(6.dp)).background(brush))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTripFromDestSheet(
    dest: Destination,
    initialCurrency: String,
    onDismiss: () -> Unit,
    onConfirm: (tripName: String, startMs: Long, endMs: Long, budgetCents: Long, currencyCode: String) -> Unit,
) {
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val defaultName = stringResource(R.string.trip_default_name, dest.name)
    var name          by remember { mutableStateOf(defaultName) }
    var budget        by remember { mutableStateOf("") }
    var currency      by remember(initialCurrency) { mutableStateOf(initialCurrency) }
    var startMs       by remember { mutableStateOf(System.currentTimeMillis()) }
    var endMs         by remember { mutableStateOf(System.currentTimeMillis() + 7 * 86_400_000L) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    val nights = remember(startMs, endMs) {
        ((endMs - startMs) / 86_400_000L).toInt().coerceAtLeast(1)
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
                .navigationBarsPadding()
                .tripClearFocusOnTap(),
        ) {
            TripSheetHeader(
                icon = Icons.Rounded.FlightTakeoff,
                accent = DiscoverOrange,
                title = dest.name,
                subtitle = "${dest.flag} ${dest.region}".trim(),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp),
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.trip_name_label)) },
                    leadingIcon = { Icon(Icons.Rounded.Person, null) },
                    shape    = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.trip_period), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DiscoverDateField(
                            label   = stringResource(R.string.trip_departure),
                            value   = dateFormatter.format(Date(startMs)),
                            onClick = { showStartPicker = true },
                            modifier = Modifier.weight(1f),
                        )
                        DiscoverDateField(
                            label   = stringResource(R.string.trip_return),
                            value   = dateFormatter.format(Date(endMs)),
                            onClick = { showEndPicker = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(0.6f)) {
                        Text(if (nights == 1) stringResource(R.string.trip_night, nights) else stringResource(R.string.trip_nights, nights),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }

                CurrencySelector(
                    currency = currency,
                    onCurrencyChange = { currency = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { c -> c.isDigit() || c == '.' } },
                    label         = { Text(stringResource(R.string.trip_budget_label, currencySymbol(currency))) },
                    leadingIcon   = { Icon(Icons.Rounded.AttachMoney, null) },
                    shape         = RoundedCornerShape(14.dp),
                    modifier      = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )

                Button(
                    onClick = {
                        val cents = ((budget.toDoubleOrNull() ?: 0.0) * 100).toLong()
                        if (name.isNotBlank()) onConfirm(name, startMs, endMs, cents, currency)
                    },
                    enabled  = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.Rounded.FlightTakeoff, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.trip_create), style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showStartPicker) {
        TripDatePickerDialog(
            initialMillis = startMs,
            onConfirm = { ms -> startMs = ms; if (endMs <= startMs) endMs = startMs + 86_400_000L; showStartPicker = false },
            onDismiss = { showStartPicker = false },
        )
    }
    if (showEndPicker) {
        TripDatePickerDialog(
            initialMillis = endMs,
            onConfirm = { ms -> endMs = ms.coerceAtLeast(startMs + 86_400_000L); showEndPicker = false },
            onDismiss = { showEndPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelector(
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
private fun DiscoverDateField(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick  = onClick,
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color    = Color.Transparent,
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(value, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
