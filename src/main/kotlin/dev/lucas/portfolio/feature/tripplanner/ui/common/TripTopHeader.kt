package dev.lucas.portfolio.feature.tripplanner.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp

enum class HeaderIconEntrance {
    Default,
    Plane,
    Spin,
    SpinFast,
}

private data class HeaderTransitionState(
    val animationKey: Any,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val iconEntrance: HeaderIconEntrance = HeaderIconEntrance.Default,
)

private const val ContentEnterMillis = 140
private const val ContentExitMillis = 90

@Composable
internal fun TripTopHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    animationKey: Any = title,
    iconEntrance: HeaderIconEntrance = HeaderIconEntrance.Default,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconContainer: Color = MaterialTheme.colorScheme.primaryContainer,
    collapseFraction: Float = 0f,
    leadingBadge: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
) {
    val transitionState = HeaderTransitionState(animationKey, icon, title, subtitle, iconEntrance)
    val isInternal = onBack != null
    val useCompactTransition = isInternal
    val f = collapseFraction.coerceIn(0f, 1f)

    val startPad = 20.dp
    val endPad = 20.dp
    val iconBox = 36.dp
    val gap = 12.dp

    val leading = if (onBack != null) 40.dp else 0.dp

    val height        = lerp(124.dp, 64.dp, f)
    val iconX         = lerp(startPad, startPad + leading, f)
    val iconCenterY   = lerp(34.dp, 32.dp, f)
    val titleX        = lerp(startPad + leading, startPad + leading + iconBox + gap, f)
    val titleTop      = lerp(54.dp, 20.dp, f)
    val sideCenterY   = lerp(68.dp, 32.dp, f)
    val subtitleAlpha = (1f - f * 1.8f).coerceIn(0f, 1f)
    val titleFontSize = lerp(if (isInternal) 22.sp else 24.sp, 18.sp, f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .pointerInput(onDoubleTap) {
                if (onDoubleTap != null) {
                    detectTapGestures(onDoubleTap = { onDoubleTap() })
                }
            },
    ) {
        val trailingReserve = if (trailing != null) 44.dp else 0.dp
        val blockWidth = (maxWidth - titleX - endPad - trailingReserve).coerceAtLeast(48.dp)

        Box(
            modifier = Modifier
                .offset { IntOffset(iconX.roundToPx(), (iconCenterY - iconBox / 2).roundToPx()) }
                .size(iconBox)
                .background(iconContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
          if (leadingBadge != null) {
            leadingBadge()
          } else {
            AnimatedContent(
                targetState = transitionState,
                contentKey = { it.animationKey },
                transitionSpec = {
                    val entrance = targetState.iconEntrance
                    when {
                        entrance == HeaderIconEntrance.Default && useCompactTransition ->
                            fadeIn(tween(ContentEnterMillis)) togetherWith fadeOut(tween(ContentExitMillis))
                        entrance == HeaderIconEntrance.Default ->
                            fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.72f) togetherWith
                                fadeOut(tween(110)) + scaleOut(tween(110), targetScale = 1.16f)
                        else -> {
                            val enterMillis = when (entrance) {
                                HeaderIconEntrance.SpinFast -> 300
                                HeaderIconEntrance.Spin -> 640
                                HeaderIconEntrance.Plane -> 520
                                HeaderIconEntrance.Default -> ContentEnterMillis
                            }
                            fadeIn(tween(enterMillis)) togetherWith fadeOut(tween((enterMillis / 2).coerceAtLeast(1)))
                        }
                    }
                },
                label = "trip_header_icon",
            ) { header ->
                val entrance = header.iconEntrance

                val spinMillis = if (entrance == HeaderIconEntrance.SpinFast) 320 else 660
                val rotation by transition.animateFloat(
                    transitionSpec = { tween(spinMillis, easing = FastOutSlowInEasing) },
                    label = "icon_spin",
                ) { state ->
                    when (entrance) {
                        HeaderIconEntrance.Spin, HeaderIconEntrance.SpinFast ->
                            if (state == EnterExitState.Visible) 0f else -360f
                        else -> 0f
                    }
                }

                val planeProgress by transition.animateFloat(
                    transitionSpec = { tween(520, easing = FastOutSlowInEasing) },
                    label = "icon_plane",
                ) { state ->
                    if (entrance == HeaderIconEntrance.Plane && state != EnterExitState.Visible) 1f else 0f
                }

                Icon(
                    imageVector = header.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            if (entrance == HeaderIconEntrance.Plane) {
                                translationX = -planeProgress * 26.dp.toPx()
                                translationY = planeProgress * 16.dp.toPx()
                                rotationZ = -planeProgress * 14f
                            } else {
                                rotationZ = rotation
                            }
                        },
                )
            }
          }
        }

        if (onBack != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(startPad.roundToPx(), (sideCenterY - 16.dp).roundToPx()) }
                    .size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = { onBack.invoke() }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(titleX.roundToPx(), titleTop.roundToPx()) }
                .width(blockWidth),
        ) {
            AnimatedContent(
                targetState = transitionState,
                contentKey = { it.animationKey },
                transitionSpec = {
                    if (initialState.animationKey == targetState.animationKey) {
                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    } else if (useCompactTransition) {
                        fadeIn(tween(ContentEnterMillis)) togetherWith fadeOut(tween(ContentExitMillis))
                    } else {
                        fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.98f) togetherWith
                            fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 1.01f)
                    }
                },
                label = "trip_header_text",
            ) { header ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = header.title,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = header.subtitle,
                        style = if (isInternal) MaterialTheme.typography.labelMedium
                        else MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer { alpha = subtitleAlpha },
                    )
                }
            }
        }

        trailing?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(-endPad.roundToPx(), (sideCenterY - 16.dp).roundToPx()) },
            ) { it() }
        }
    }
}
