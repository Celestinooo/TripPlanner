package dev.lucas.portfolio.feature.tripplanner.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore as RoundedExplore
import androidx.compose.material.icons.rounded.FlightTakeoff as RoundedFlightTakeoff
import androidx.compose.material.icons.rounded.Settings as RoundedSettings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.lucas.portfolio.feature.tripplanner.ui.discover.DiscoverRefreshAction
import dev.lucas.portfolio.feature.tripplanner.ui.discover.DiscoverRefreshState
import dev.lucas.portfolio.feature.tripplanner.ui.discover.DiscoverScreen
import dev.lucas.portfolio.feature.tripplanner.ui.login.LoginScreen
import dev.lucas.portfolio.feature.tripplanner.ui.settings.TripSettingsScreen
import dev.lucas.portfolio.feature.tripplanner.ui.common.HeaderIconEntrance
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripTopHeader
import dev.lucas.portfolio.feature.tripplanner.ui.trips.TripDetailSection
import dev.lucas.portfolio.feature.tripplanner.ui.trips.TripDetailScreen
import dev.lucas.portfolio.feature.tripplanner.ui.trips.TripDetailItineraryScreen
import dev.lucas.portfolio.feature.tripplanner.ui.trips.TripDetailExpensesScreen
import dev.lucas.portfolio.feature.tripplanner.ui.trips.TripDetailGalleryScreen
import dev.lucas.portfolio.feature.tripplanner.ui.trips.TripDetailViewModel
import dev.lucas.portfolio.feature.tripplanner.ui.trips.TripsScreen
import dev.lucas.portfolio.feature.tripplanner.ui.trips.tripDetailHeaderState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import dev.lucas.portfolio.feature.tripplanner.R
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable


@Serializable object TripLogin
@Serializable object TripDiscover
@Serializable object TripTrips
@Serializable data class TripDetail(val tripId: Long)
@Serializable data class TripDetailItinerary(val tripId: Long)
@Serializable data class TripDetailExpenses(val tripId: Long)
@Serializable data class TripDetailGallery(val tripId: Long)
@Serializable object TripSettings


private data class BottomNavItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: Any,
)

private data class BottomTabHeader(
    val animationKey: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val iconEntrance: HeaderIconEntrance = HeaderIconEntrance.Default,
    val iconTint: Color? = null,
    val iconContainer: Color? = null,
    val leadingBadge: (@Composable () -> Unit)? = null,
    val onBack: (() -> Unit)? = null,
    val trailing: (@Composable () -> Unit)? = null,
)

private val TabIconBg          = Color(0xFF212121)
private val TripsIconTint      = Color(0xFFFFC107)
private val TripsIconBg        = TabIconBg
private val DiscoverIconTint   = Color(0xFFFB8C00)
private val DiscoverIconBg     = TabIconBg
private val SettingsIconTint   = Color(0xFFE53935)
private val SettingsIconBg     = TabIconBg

private fun detailTrailing(
    galleryCount: Int?,
    accent: Color,
    editLabel: String,
    onEdit: (() -> Unit)?,
): (@Composable () -> Unit)? = when {
    galleryCount != null -> {
        { GalleryPostsTrailing(count = galleryCount, tint = accent) }
    }
    onEdit != null -> {
        { DetailEditButton(onEdit = onEdit, contentDescription = editLabel, tint = accent) }
    }
    else -> null
}

@Composable
private fun DiscoverRefreshButton(action: DiscoverRefreshAction, tint: Color) {
    val enabled = action.state == DiscoverRefreshState.Idle
    val containerColor by animateColorAsState(
        targetValue = when (action.state) {
            DiscoverRefreshState.Idle -> Color.Transparent
            DiscoverRefreshState.Downloading -> tint.copy(alpha = 0.10f)
            DiscoverRefreshState.Success -> tint.copy(alpha = 0.16f)
        },
        animationSpec = tween(180),
        label = "discover_refresh_container",
    )
    val iconColor by animateColorAsState(
        targetValue = if (enabled) tint else tint.copy(alpha = 0.72f),
        animationSpec = tween(180),
        label = "discover_refresh_tint",
    )
    val spin = rememberInfiniteTransition(label = "discover_refresh_spin")
    val rotation by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(820),
            repeatMode = RepeatMode.Restart,
        ),
        label = "discover_refresh_rotation",
    )

    Surface(
        modifier = Modifier.size(width = 42.dp, height = 32.dp),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        onClick = action.onClick,
        enabled = enabled,
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = action.state,
                transitionSpec = {
                    fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.82f) togetherWith
                        fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 1.12f)
                },
                label = "discover_refresh_icon",
            ) { state ->
                val icon = when (state) {
                    DiscoverRefreshState.Idle -> Icons.Rounded.CloudDownload
                    DiscoverRefreshState.Downloading -> Icons.Rounded.Sync
                    DiscoverRefreshState.Success -> Icons.Rounded.CloudDone
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .size(20.dp)
                        .then(
                            if (state == DiscoverRefreshState.Downloading) {
                                Modifier.graphicsLayer { rotationZ = rotation }
                            } else {
                                Modifier
                            }
                        ),
                )
            }
        }
    }
}

@Composable
private fun GalleryPostsTrailing(count: Int, tint: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = count.toString(),
                color = tint,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
            )
            Text(
                text = stringResource(R.string.trip_gallery_posts),
                color = tint.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun DetailEditButton(onEdit: () -> Unit, contentDescription: String, tint: Color) {
    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
        Icon(imageVector = Icons.Rounded.Edit, contentDescription = contentDescription, tint = tint)
    }
}


private fun tabColor(route: Any): Color = when (route) {
    TripTrips -> TripsIconTint
    TripDiscover -> DiscoverIconTint
    TripSettings -> SettingsIconTint
    else -> Color(0xFF9E9E9E)
}

@Composable
private fun FloatingBottomBar(
    items: List<BottomNavItem>,
    currentRoute: Any?,
    onSelect: (Any) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                FloatingNavItem(
                    icon = item.icon,
                    label = stringResource(item.labelRes),
                    color = tabColor(item.route),
                    selected = selected,
                    onClick = { onSelect(item.route) },
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    icon: ImageVector,
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) color.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(320),
        label = "nav_bg",
    )
    val content by animateColorAsState(
        targetValue = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(320),
        label = "nav_content",
    )
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = content,
            modifier = Modifier.size(22.dp),
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(expandFrom = Alignment.Start, animationSpec = tween(260)) +
                fadeIn(tween(220)),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start, animationSpec = tween(180)) +
                fadeOut(tween(140)),
        ) {
            Text(
                text = label,
                color = content,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

private val bottomNavItems = listOf(
    BottomNavItem(R.string.trip_trips_tab, Icons.Outlined.FlightTakeoff, TripTrips),
    BottomNavItem(R.string.trip_discover_title, Icons.Outlined.Explore, TripDiscover),
    BottomNavItem(R.string.trip_config_short, Icons.Outlined.Settings, TripSettings),
)

private const val BottomTabSlideMillis = 220

private fun NavDestination.isTripDetailFamily(): Boolean = hierarchy.any {
    it.hasRoute(TripDetail::class) || it.hasRoute(TripDetailItinerary::class) ||
        it.hasRoute(TripDetailExpenses::class) || it.hasRoute(TripDetailGallery::class)
}

private fun NavDestination.currentBottomTabRoute(): Any? = when {
    isTripDetailFamily() -> TripTrips
    hierarchy.any { it.hasRoute(TripTrips::class) } -> TripTrips
    hierarchy.any { it.hasRoute(TripDiscover::class) } -> TripDiscover
    hierarchy.any { it.hasRoute(TripSettings::class) } -> TripSettings
    else -> null
}

private fun tabClickIconEntrance(route: Any?): HeaderIconEntrance = when (route) {
    TripTrips -> HeaderIconEntrance.Plane
    TripDiscover -> HeaderIconEntrance.Spin
    TripSettings -> HeaderIconEntrance.SpinFast
    else -> HeaderIconEntrance.Default
}


@Composable
fun TripPlannerNavHost(onBack: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentDest = backStack?.destination
    var discoverDestinationCount by remember { mutableStateOf<Int?>(null) }
    var tripDetailEdit by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingTabIconAnimation by remember { mutableStateOf<Any?>(null) }
    var scrollToTopSignal by remember { mutableIntStateOf(0) }
    var discoverRefreshAction by remember { mutableStateOf<DiscoverRefreshAction?>(null) }

    val showBottomBar = bottomNavItems.any { item ->
            currentDest?.hierarchy?.any { it.hasRoute(item.route::class) } == true
        }
    val currentBottomTabRoute = currentDest?.currentBottomTabRoute()
    val isLoginRoute = currentDest?.hierarchy?.any { it.hasRoute(TripLogin::class) } == true
    val isAtTripsRoot = currentDest?.hasRoute(TripTrips::class) == true
    val isOverviewActive = currentDest?.hasRoute(TripDetail::class) == true
    val isItineraryActive = currentDest?.hasRoute(TripDetailItinerary::class) == true
    val isExpensesActive = currentDest?.hasRoute(TripDetailExpenses::class) == true
    val isGalleryActive = currentDest?.hasRoute(TripDetailGallery::class) == true

    val headerViewModel: TripDetailViewModel = hiltViewModel()
    val currentDetailTripId: Long? = when {
        isOverviewActive -> runCatching { backStack?.toRoute<TripDetail>()?.tripId }.getOrNull()
        isItineraryActive -> runCatching { backStack?.toRoute<TripDetailItinerary>()?.tripId }.getOrNull()
        isExpensesActive -> runCatching { backStack?.toRoute<TripDetailExpenses>()?.tripId }.getOrNull()
        isGalleryActive -> runCatching { backStack?.toRoute<TripDetailGallery>()?.tripId }.getOrNull()
        else -> null
    }
    LaunchedEffect(currentDetailTripId) {
        currentDetailTripId?.let { headerViewModel.init(it) }
    }
    val headerTrip by headerViewModel.trip.collectAsStateWithLifecycle()
    val headerItinerary by headerViewModel.itinerary.collectAsStateWithLifecycle()
    val headerExpenses by headerViewModel.expenses.collectAsStateWithLifecycle()
    val headerTotalSpent by headerViewModel.totalSpent.collectAsStateWithLifecycle()
    val headerGalleryCount by headerViewModel.galleryCount.collectAsStateWithLifecycle()
    val detailSection = when {
        isOverviewActive -> TripDetailSection.Overview
        isItineraryActive -> TripDetailSection.Itinerary
        isExpensesActive -> TripDetailSection.Expenses
        isGalleryActive -> TripDetailSection.Gallery
        else -> null
    }

    val bottomTabHeader = when {
        currentDest?.hierarchy?.any { it.hasRoute(TripDiscover::class) } == true -> BottomTabHeader(
            animationKey = "discover",
            icon = Icons.Rounded.RoundedExplore,
            title = stringResource(R.string.trip_discover_title),
            subtitle = discoverDestinationCount?.let { stringResource(R.string.trip_destinations_available, it) }
                ?: stringResource(R.string.trip_loading_destinations),
            iconEntrance = tabClickIconEntrance(pendingTabIconAnimation.takeIf { it == TripDiscover }),
            iconTint = DiscoverIconTint,
            iconContainer = DiscoverIconBg,
            trailing = discoverRefreshAction?.let { action ->
                { DiscoverRefreshButton(action = action, tint = DiscoverIconTint) }
            },
        )
        currentDest?.hierarchy?.any { it.hasRoute(TripTrips::class) } == true -> BottomTabHeader(
            animationKey = "trips",
            icon = Icons.Rounded.RoundedFlightTakeoff,
            title = stringResource(R.string.trip_my_trips),
            subtitle = stringResource(R.string.trip_planned_trips),
            iconEntrance = tabClickIconEntrance(pendingTabIconAnimation.takeIf { it == TripTrips }),
            iconTint = TripsIconTint,
            iconContainer = TripsIconBg,
        )
        currentDest?.isTripDetailFamily() == true -> {
            val detailHeader = tripDetailHeaderState(
                trip = headerTrip,
                selectedSection = detailSection ?: TripDetailSection.Overview,
                itinerary = headerItinerary,
                expenses = headerExpenses,
                totalSpent = headerTotalSpent,
                galleryCount = headerGalleryCount,
            )
            val accent = detailHeader.iconColor ?: TripsIconTint
            BottomTabHeader(
                animationKey = detailHeader.animationKey,
                icon = detailHeader.icon,
                title = detailHeader.title,
                subtitle = detailHeader.subtitle,
                iconTint = accent,
                iconContainer = TripsIconBg,
                onBack = { navController.popBackStack() },
                trailing = detailTrailing(
                    galleryCount = detailHeader.galleryCount,
                    accent = accent,
                    editLabel = stringResource(R.string.trip_edit_trip),
                    onEdit = tripDetailEdit,
                ),
            )
        }
        currentDest?.hierarchy?.any { it.hasRoute(TripSettings::class) } == true -> BottomTabHeader(
            animationKey = "settings",
            icon = Icons.Rounded.RoundedSettings,
            title = stringResource(R.string.trip_settings_title),
            subtitle = stringResource(R.string.trip_settings_subtitle),
            iconEntrance = tabClickIconEntrance(pendingTabIconAnimation.takeIf { it == TripSettings }),
            iconTint = SettingsIconTint,
            iconContainer = SettingsIconBg,
        )
        else -> null
    }

    val density = LocalDensity.current
    val collapseRangePx = with(density) { 60.dp.toPx() }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    val collapseFraction =
        if (collapseRangePx > 0f) (-headerOffsetPx / collapseRangePx).coerceIn(0f, 1f) else 0f
    val headerNestedScroll = remember(collapseRangePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val newOffset = (headerOffsetPx + available.y).coerceIn(-collapseRangePx, 0f)
                val consumed = newOffset - headerOffsetPx
                headerOffsetPx = newOffset
                return Offset(0f, consumed)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                snapHeader()
                return Velocity.Zero
            }

            private suspend fun snapHeader() {
                val target = if (-headerOffsetPx >= collapseRangePx / 2f) -collapseRangePx else 0f
                if (headerOffsetPx != target) {
                    animate(
                        initialValue = headerOffsetPx,
                        targetValue = target,
                        animationSpec = tween(220),
                    ) { value, _ -> headerOffsetPx = value }
                }
            }
        }
    }
    val currentTabKey = bottomTabHeader?.animationKey
    LaunchedEffect(currentTabKey) {
        if (headerOffsetPx != 0f) {
            animate(
                initialValue = headerOffsetPx,
                targetValue = 0f,
                animationSpec = tween(220),
            ) { value, _ -> headerOffsetPx = value }
        }
    }
    LaunchedEffect(currentBottomTabRoute, pendingTabIconAnimation) {
        if (pendingTabIconAnimation != null && pendingTabIconAnimation == currentBottomTabRoute) {
            delay(720)
            pendingTabIconAnimation = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = expandVertically(tween(200)),
                exit = shrinkVertically(tween(180)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .navigationBarsPadding()
                        .padding(top = 6.dp, bottom = 14.dp)
                        .height(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    FloatingBottomBar(
                        items = bottomNavItems,
                        currentRoute = currentBottomTabRoute,
                        onSelect = { route ->
                            if (route == currentBottomTabRoute) {
                                headerOffsetPx = 0f
                                scrollToTopSignal++
                            } else {
                                pendingTabIconAnimation = route
                                navController.navigate(route) {
                                    popUpTo(TripTrips) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(headerNestedScroll)
        ) {
            bottomTabHeader?.let { header ->
                TripTopHeader(
                    icon = header.icon,
                    title = header.title,
                    subtitle = header.subtitle,
                    animationKey = header.animationKey,
                    iconEntrance = header.iconEntrance,
                    iconTint = header.iconTint ?: MaterialTheme.colorScheme.primary,
                    iconContainer = header.iconContainer ?: MaterialTheme.colorScheme.primaryContainer,
                    leadingBadge = header.leadingBadge,
                    collapseFraction = collapseFraction,
                    onBack = header.onBack,
                    trailing = header.trailing,
                    onDoubleTap = {
                        headerOffsetPx = 0f
                        scrollToTopSignal++
                    },
                )
            }
            NavHost(
                modifier = Modifier.weight(1f),
                navController    = navController,
                startDestination = TripLogin,
                enterTransition  = { fadeIn(tween(140)) },
                exitTransition   = { fadeOut(tween(90)) },
                popEnterTransition  = { fadeIn(tween(140)) },
                popExitTransition   = { fadeOut(tween(90)) },
            ) {
                composable<TripLogin> {
                    LoginScreen(
                        onLoggedIn = {
                            navController.navigate(TripTrips) {
                                popUpTo<TripLogin> { inclusive = true }
                            }
                        },
                        onBack = onBack,
                    )
                }
                composable<TripDiscover>(
                    enterTransition = {
                        fadeIn(tween(BottomTabSlideMillis))
                    },
                    exitTransition = {
                        fadeOut(tween(BottomTabSlideMillis / 2))
                    },
                    popEnterTransition = {
                        fadeIn(tween(BottomTabSlideMillis))
                    },
                    popExitTransition = {
                        fadeOut(tween(BottomTabSlideMillis / 2))
                    },
                ) {
                    DiscoverScreen(
                        onOpenTrip = { tripId ->
                            navController.navigate(TripTrips) {
                                popUpTo(TripTrips) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            navController.navigate(TripDetail(tripId))
                        },
                        showHeader = false,
                        onDestinationCountChanged = { discoverDestinationCount = it },
                        onRefreshActionChanged = { discoverRefreshAction = it },
                        scrollToTopSignal = scrollToTopSignal,
                    )
                }
                composable<TripTrips>(
                    enterTransition = {
                        fadeIn(tween(BottomTabSlideMillis))
                    },
                    exitTransition = {
                        fadeOut(tween(BottomTabSlideMillis / 2))
                    },
                    popEnterTransition = {
                        fadeIn(tween(BottomTabSlideMillis))
                    },
                    popExitTransition = {
                        fadeOut(tween(BottomTabSlideMillis / 2))
                    },
                ) {
                    TripsScreen(
                        onOpenTrip = { tripId ->
                            navController.navigate(TripDetail(tripId))
                        },
                        showHeader = false,
                        scrollToTopSignal = scrollToTopSignal,
                    )
                }
                composable<TripDetail> { backStack ->
                    val route = backStack.toRoute<TripDetail>()
                    TripDetailScreen(
                        tripId = route.tripId,
                        scrollToTopSignal = scrollToTopSignal,
                        onBack = { navController.popBackStack() },
                        onOpenItinerary = { navController.navigate(TripDetailItinerary(route.tripId)) },
                        onOpenExpenses = { navController.navigate(TripDetailExpenses(route.tripId)) },
                        onOpenGallery = { navController.navigate(TripDetailGallery(route.tripId)) },
                        isActive = isOverviewActive,
                        onEditChange = { onEditClick -> tripDetailEdit = onEditClick },
                    )
                }
                composable<TripDetailItinerary> { backStack ->
                    val route = backStack.toRoute<TripDetailItinerary>()
                    TripDetailItineraryScreen(
                        tripId = route.tripId,
                        scrollToTopSignal = scrollToTopSignal,
                    )
                }
                composable<TripDetailExpenses> { backStack ->
                    val route = backStack.toRoute<TripDetailExpenses>()
                    TripDetailExpensesScreen(
                        tripId = route.tripId,
                        scrollToTopSignal = scrollToTopSignal,
                    )
                }
                composable<TripDetailGallery> { backStack ->
                    val route = backStack.toRoute<TripDetailGallery>()
                    TripDetailGalleryScreen(
                        tripId = route.tripId,
                        scrollToTopSignal = scrollToTopSignal,
                    )
                }
                composable<TripSettings>(
                    enterTransition = {
                        fadeIn(tween(BottomTabSlideMillis))
                    },
                    exitTransition = {
                        fadeOut(tween(BottomTabSlideMillis / 2))
                    },
                    popEnterTransition = {
                        fadeIn(tween(BottomTabSlideMillis))
                    },
                    popExitTransition = {
                        fadeOut(tween(BottomTabSlideMillis / 2))
                    },
                ) {
                    TripSettingsScreen(
                        showHeader = false,
                        scrollToTopSignal = scrollToTopSignal,
                    )
                }
            }
        }
    }

    BackHandler(enabled = !isLoginRoute && isAtTripsRoot) {
        onBack()
    }
}
