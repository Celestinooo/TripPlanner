package dev.lucas.portfolio.feature.tripplanner.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucas.portfolio.feature.tripplanner.R
import dev.lucas.portfolio.feature.tripplanner.ui.common.tripClearFocusOnTap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(val name: String, val email: String) : AuthState
    data class Error(val msg: String) : AuthState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginScreen(onLoggedIn: () -> Unit, onBack: () -> Unit) {
    var email     by remember { mutableStateOf("dev@portfolio.com") }
    var password  by remember { mutableStateOf("demo1234") }
    var showPass  by remember { mutableStateOf(false) }
    var authState by remember { mutableStateOf<AuthState>(AuthState.Idle) }
    val scope = rememberCoroutineScope()

    fun signIn() {
        scope.launch {
            authState = AuthState.Loading
            delay(1600)
            authState = if (email.isNotBlank() && password.isNotBlank()) {
                AuthState.Success(email.substringBefore("@").ifBlank { email }, email)
            } else {
                AuthState.Error("invalid")
            }
        }
    }

    fun signInGoogle() {
        scope.launch {
            authState = AuthState.Loading
            delay(1200)
            authState = AuthState.Success("Google", "google")
        }
    }

    if (authState is AuthState.Success) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            delay(800)
            onLoggedIn()
        }
    }

    Scaffold(
        modifier = Modifier.tripClearFocusOnTap(),
        topBar = {
            TopAppBar(
                title = { Text("TripPlanner") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.trip_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = authState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "auth",
            ) { state ->
                when (state) {
                    AuthState.Loading -> LoadingCard()
                    is AuthState.Success -> SuccessCard(state.name)
                    else -> LoginForm(
                        email        = email,
                        password     = password,
                        showPassword = showPass,
                        error        = (state as? AuthState.Error)?.msg,
                        onEmail      = { email = it },
                        onPassword   = { password = it },
                        onTogglePass = { showPass = !showPass },
                        onSignIn     = ::signIn,
                        onGoogle     = ::signInGoogle,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    email: String, password: String, showPassword: Boolean, error: String?,
    onEmail: (String) -> Unit, onPassword: (String) -> Unit,
    onTogglePass: () -> Unit, onSignIn: () -> Unit, onGoogle: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                )),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.FlightTakeoff, null, Modifier.size(44.dp), tint = Color.White) }

        Text(stringResource(R.string.trip_login_welcome), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.trip_login_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = email, onValueChange = onEmail,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Rounded.Email, null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )
        OutlinedTextField(
            value = password, onValueChange = onPassword,
            label = { Text(stringResource(R.string.trip_login_password)) },
            leadingIcon = { Icon(Icons.Rounded.Lock, null) },
            trailingIcon = {
                IconButton(onClick = onTogglePass) {
                    Icon(if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        AnimatedVisibility(visible = error != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(if (error == "invalid") stringResource(R.string.trip_login_invalid) else error ?: "", Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Icon(Icons.AutoMirrored.Rounded.Login, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.trip_login_email_button))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text("  ${stringResource(R.string.trip_login_or)}  ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.weight(1f))
        }
        OutlinedButton(onClick = onGoogle, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 18.sp)
            Spacer(Modifier.width(10.dp)); Text(stringResource(R.string.trip_login_google))
        }
    }
}

@Composable
private fun LoadingCard() {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.4f, targetValue = 1f, label = "alpha",
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CircularProgressIndicator(Modifier.size(48.dp))
        Text(stringResource(R.string.trip_login_loading), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = pulse))
    }
}

@Composable
private fun SuccessCard(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("✅", fontSize = 56.sp)
        Text(stringResource(R.string.trip_login_success, name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.trip_login_redirect), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
