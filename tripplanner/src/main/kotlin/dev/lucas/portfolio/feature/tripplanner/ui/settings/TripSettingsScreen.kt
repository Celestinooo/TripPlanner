package dev.lucas.portfolio.feature.tripplanner.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lucas.portfolio.feature.tripplanner.R
import dev.lucas.portfolio.feature.tripplanner.data.datastore.TripPrefsRepository
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripImagePickerLauncher
import dev.lucas.portfolio.feature.tripplanner.ui.common.TripTopHeader
import dev.lucas.portfolio.feature.tripplanner.ui.common.supportedTripCurrencies
import dev.lucas.portfolio.feature.tripplanner.ui.common.tripClearFocusOnTap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripSettingsUiState(
    val userName:      String  = "",
    val currency:      String  = "BRL",
    val notifications: Boolean = true,
    val defaultRegion: String  = "Todas",
    val language:      String  = "system",
    val avatarPath:    String? = null,
    val bannerPath:    String? = null,
)

@HiltViewModel
class TripSettingsViewModel @Inject constructor(
    private val repo: TripPrefsRepository,
) : ViewModel() {

    private val baseState = combine(
        repo.userName, repo.currency, repo.notifications, repo.defaultRegion,
    ) { name, currency, notif, region ->
        TripSettingsUiState(name, currency, notif, region)
    }

    val state: StateFlow<TripSettingsUiState?> = combine(
        baseState, repo.language, repo.userAvatar, repo.userBanner,
    ) { state, language, avatar, banner ->
        state.copy(language = language, avatarPath = avatar, bannerPath = banner)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun saveSettings(
        userName: String,
        currency: String,
        notifications: Boolean,
        defaultRegion: String,
        language: String,
        avatarPath: String?,
        bannerPath: String?,
    ) = viewModelScope.launch {
        repo.setUserName(userName)
        repo.setCurrency(currency)
        repo.setNotifications(notifications)
        repo.setDefaultRegion(defaultRegion)
        repo.setLanguage(language)
        repo.setUserAvatar(avatarPath)
        repo.setUserBanner(bannerPath)
    }
}

private val regions   = listOf("Todas", "Americas", "Europe", "Asia", "Africa", "Oceania")
private val languages = listOf("system", "pt", "en")

private val SettingsAmber  = Color(0xFFF9A825)
private val SettingsTeal   = Color(0xFF00897B)
private val SettingsPurple = Color(0xFF8E24AA)
private val SettingsBlue   = Color(0xFF1E88E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripSettingsScreen(
    showHeader: Boolean = true,
    scrollToTopSignal: Int = 0,
    viewModel: TripSettingsViewModel = hiltViewModel(),
) {
    val loadedState by viewModel.state.collectAsStateWithLifecycle()
    val state = loadedState
    if (state == null) {
        Scaffold(
            modifier = Modifier.tripClearFocusOnTap(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { _ ->
            Box(Modifier.fillMaxSize())
        }
        return
    }
    var nameInput          by remember(state.userName)      { mutableStateOf(state.userName) }
    var currencyInput      by remember(state.currency)      { mutableStateOf(state.currency) }
    var regionInput        by remember(state.defaultRegion) { mutableStateOf(state.defaultRegion) }
    var languageInput      by remember(state.language)      { mutableStateOf(state.language) }
    var notificationsInput by remember(state.notifications) { mutableStateOf(state.notifications) }
    var avatarInput        by remember(state.avatarPath)    { mutableStateOf(state.avatarPath) }
    var bannerInput        by remember(state.bannerPath)    { mutableStateOf(state.bannerPath) }

    val hasChanges = nameInput != state.userName ||
        currencyInput != state.currency ||
        regionInput != state.defaultRegion ||
        languageInput != state.language ||
        notificationsInput != state.notifications ||
        avatarInput != state.avatarPath ||
        bannerInput != state.bannerPath

    val listState = rememberLazyListState()
    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    Scaffold(
        modifier = Modifier.tripClearFocusOnTap(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { _ ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            state          = listState,
            contentPadding = PaddingValues(top = if (showHeader) 0.dp else 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showHeader) {
                item {
                    TripTopHeader(
                        icon     = Icons.Rounded.Settings,
                        title    = stringResource(R.string.trip_settings_title),
                        subtitle = stringResource(R.string.trip_settings_subtitle),
                    )
                }
            }

            item {
                ProfileHeaderCard(
                    avatarPath    = avatarInput,
                    bannerPath    = bannerInput,
                    name          = nameInput,
                    onNameChange  = { nameInput = it },
                    onAvatarPicked = { avatarInput = it },
                    onBannerPicked = { bannerInput = it },
                    modifier      = Modifier.padding(horizontal = 16.dp),
                )
            }

            item {
                PrefCard(
                    icon     = Icons.Rounded.AttachMoney,
                    title    = stringResource(R.string.trip_settings_currency),
                    iconBg   = SettingsAmber.copy(alpha = 0.16f),
                    iconTint = SettingsAmber,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        CompactDropdownAnchor(
                            value      = currencyInput,
                            expanded   = expanded,
                            valueStyle = MaterialTheme.typography.labelSmall,
                            modifier   = Modifier.menuAnchor().width(92.dp).height(44.dp),
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            supportedTripCurrencies.forEach { c ->
                                DropdownMenuItem(text = { Text(c) }, onClick = { currencyInput = c; expanded = false })
                            }
                        }
                    }
                }
            }

            item {
                PrefCard(
                    icon     = Icons.Rounded.Public,
                    title    = stringResource(R.string.trip_settings_region),
                    iconBg   = SettingsTeal.copy(alpha = 0.16f),
                    iconTint = SettingsTeal,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        CompactDropdownAnchor(
                            value    = regionLabel(regionInput),
                            expanded = expanded,
                            modifier = Modifier.menuAnchor().width(112.dp).height(44.dp),
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            regions.forEach { r ->
                                DropdownMenuItem(text = { Text(regionLabel(r)) }, onClick = { regionInput = r; expanded = false })
                            }
                        }
                    }
                }
            }

            item {
                PrefCard(
                    icon     = Icons.Rounded.Translate,
                    title    = stringResource(R.string.trip_settings_language),
                    iconBg   = SettingsPurple.copy(alpha = 0.16f),
                    iconTint = SettingsPurple,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        CompactDropdownAnchor(
                            value    = languageLabel(languageInput),
                            expanded = expanded,
                            modifier = Modifier.menuAnchor().width(148.dp).height(44.dp),
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            languages.forEach { language ->
                                DropdownMenuItem(
                                    text    = { Text(languageLabel(language)) },
                                    onClick = { languageInput = language; expanded = false },
                                )
                            }
                        }
                    }
                }
            }

            item {
                PrefCard(
                    icon     = Icons.Rounded.Notifications,
                    title    = stringResource(R.string.trip_settings_notifications),
                    iconBg   = SettingsBlue.copy(alpha = 0.16f),
                    iconTint = SettingsBlue,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Switch(checked = notificationsInput, onCheckedChange = { notificationsInput = it })
                }
            }

            item {
                Button(
                    enabled  = hasChanges,
                    onClick  = {
                        viewModel.saveSettings(
                            userName      = nameInput.trim(),
                            currency      = currencyInput,
                            notifications = notificationsInput,
                            defaultRegion = regionInput,
                            language      = languageInput,
                            avatarPath    = avatarInput,
                            bannerPath    = bannerInput,
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                    shape    = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        stringResource(R.string.trip_settings_save),
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun languageLabel(language: String): String = when (language) {
    "pt" -> stringResource(R.string.trip_settings_language_pt)
    "en" -> stringResource(R.string.trip_settings_language_en)
    else -> stringResource(R.string.trip_settings_language_system)
}

@Composable
private fun regionLabel(region: String): String = when (region) {
    "Americas" -> stringResource(R.string.trip_region_americas)
    "Europe"   -> stringResource(R.string.trip_region_europe)
    "Asia"     -> stringResource(R.string.trip_region_asia)
    "Africa"   -> stringResource(R.string.trip_region_africa)
    "Oceania"  -> stringResource(R.string.trip_region_oceania)
    else       -> stringResource(R.string.trip_region_all)
}

@Composable
private fun CompactDropdownAnchor(
    value: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelMedium,
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(10.dp),
        color    = MaterialTheme.colorScheme.surface,
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier          = Modifier.padding(start = 10.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text     = value,
                modifier = Modifier.weight(1f),
                style    = valueStyle,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    avatarPath: String?,
    bannerPath: String?,
    name: String,
    onNameChange: (String) -> Unit,
    onAvatarPicked: (String) -> Unit,
    onBannerPicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val avatarSize   = 92.dp
    val bannerHeight = 84.dp
    ElevatedCard(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        TripImagePickerLauncher(onImageReady = onAvatarPicked) { launch ->
            Box(Modifier.fillMaxWidth()) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TripImagePickerLauncher(onImageReady = onBannerPicked, cropAspectRatio = 3.2f) { bannerLaunch ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(bannerHeight)
                                .clickable { bannerLaunch() },
                        ) {
                            if (bannerPath != null) {
                                AsyncImage(
                                    model          = bannerPath,
                                    contentDescription = null,
                                    modifier       = Modifier.fillMaxSize(),
                                    contentScale   = ContentScale.Crop,
                                )
                            } else {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.tertiary,
                                                )
                                            )
                                        )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.32f))
                                    .clickable { bannerLaunch() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Rounded.CameraAlt,
                                    contentDescription = stringResource(R.string.trip_settings_change_photo),
                                    modifier           = Modifier.size(15.dp),
                                    tint               = Color.White,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(avatarSize / 2 + 8.dp))
                    Text(
                        name.ifBlank { stringResource(R.string.trip_settings_name_placeholder) },
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.trip_settings_traveler_name),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value         = name,
                        onValueChange = onNameChange,
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        leadingIcon   = { Icon(Icons.Rounded.Person, null) },
                        placeholder   = { Text(stringResource(R.string.trip_settings_name_placeholder)) },
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = bannerHeight - avatarSize / 2)
                        .size(avatarSize)
                        .clickable { launch() },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (avatarPath != null) {
                            AsyncImage(
                                model              = avatarPath,
                                contentDescription = null,
                                modifier           = Modifier.fillMaxSize(),
                                contentScale       = ContentScale.Crop,
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Person, null,
                                modifier = Modifier.size(44.dp),
                                tint     = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.CameraAlt,
                            contentDescription = stringResource(R.string.trip_settings_change_photo),
                            modifier           = Modifier.size(15.dp),
                            tint               = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrefCard(
    icon: ImageVector,
    title: String,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconContainer(icon, iconBg, iconTint)
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                modifier  = Modifier.weight(1f),
                style     = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            trailing()
        }
    }
}

@Composable
private fun IconContainer(icon: ImageVector, bg: Color, tint: Color) {
    Box(
        modifier         = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
    }
}
