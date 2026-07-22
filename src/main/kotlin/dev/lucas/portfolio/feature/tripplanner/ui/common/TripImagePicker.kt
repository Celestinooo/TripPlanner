package dev.lucas.portfolio.feature.tripplanner.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.lucas.portfolio.feature.tripplanner.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal class TripImageStore(private val context: Context) {
    fun decode(source: Uri, maxDimension: Int = WORKING_MAX_DIMENSION): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        val longest = max(bounds.outWidth, bounds.outHeight)
        if (longest <= 0) return null

        var sample = 1
        while (longest / sample > maxDimension) sample *= 2

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        }
    }

    fun saveCroppedImage(source: Bitmap, crop: TripCropState): String {
        val dir = File(context.filesDir, "trip_images").apply { mkdirs() }
        val file = File(dir, "trip_image_${System.currentTimeMillis()}.jpg")

        val vpW = crop.viewportSize.width.coerceAtLeast(1)
        val vpH = crop.viewportSize.height.coerceAtLeast(1)
        val fitScale = min(vpW / source.width.toFloat(), vpH / source.height.toFloat())
        val totalScale = (fitScale * crop.scale).coerceAtLeast(0.0001f)
        val centerX = vpW / 2f
        val centerY = vpH / 2f

        val left = ((0f - centerX - crop.offset.x) / totalScale + source.width / 2f)
            .roundToInt()
            .coerceIn(0, source.width - 1)
        val top = ((0f - centerY - crop.offset.y) / totalScale + source.height / 2f)
            .roundToInt()
            .coerceIn(0, source.height - 1)
        val right = ((vpW - centerX - crop.offset.x) / totalScale + source.width / 2f)
            .roundToInt()
            .coerceIn(left + 1, source.width)
        val bottom = ((vpH - centerY - crop.offset.y) / totalScale + source.height / 2f)
            .roundToInt()
            .coerceIn(top + 1, source.height)

        val cropped = Bitmap.createBitmap(source, left, top, right - left, bottom - top)
        val scaledDown = downscaleToMax(cropped, STORED_MAX_DIMENSION)
        file.outputStream().use { output ->
            scaledDown.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        if (scaledDown !== cropped) cropped.recycle()
        return file.absolutePath
    }

    private fun downscaleToMax(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= maxDimension) return bitmap
        val ratio = maxDimension.toFloat() / longest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).roundToInt().coerceAtLeast(1),
            (bitmap.height * ratio).roundToInt().coerceAtLeast(1),
            true,
        )
    }

    private companion object {
        const val WORKING_MAX_DIMENSION = 2048
        const val STORED_MAX_DIMENSION = 1440
    }
}

internal data class TripCropState(
    val scale: Float,
    val offset: Offset,
    val viewportSize: IntSize,
)

@Composable
internal fun TripImagePickerLauncher(
    onImageReady: (String) -> Unit,
    cropAspectRatio: Float = 1f,
    content: @Composable (launch: () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember(context) { TripImageStore(context) }
    var pendingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var saving by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            pendingBitmap = withContext(Dispatchers.IO) { store.decode(uri) }
        }
    }

    content {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    pendingBitmap?.let { bitmap ->
        TripCropDialog(
            bitmap = bitmap,
            aspectRatio = cropAspectRatio,
            onDismiss = {
                if (!saving) pendingBitmap = null
            },
            onConfirm = { crop ->
                if (saving) return@TripCropDialog
                saving = true
                scope.launch {
                    val path = withContext(Dispatchers.IO) { store.saveCroppedImage(bitmap, crop) }
                    onImageReady(path)
                    pendingBitmap = null
                    saving = false
                }
            },
        )
    }
}

@Composable
private fun TripCropDialog(
    bitmap: Bitmap,
    aspectRatio: Float,
    onDismiss: () -> Unit,
    onConfirm: (TripCropState) -> Unit,
) {
    var scale by remember(bitmap) { mutableStateOf(1f) }
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember(bitmap) { mutableStateOf(IntSize.Zero) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.trip_crop_free_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(R.string.trip_crop_free_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onSizeChanged { viewportSize = it }
                        .pointerInput(bitmap) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        contentScale = ContentScale.Fit,
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.trip_cancel))
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                TripCropState(
                                    scale = scale,
                                    offset = offset,
                                    viewportSize = viewportSize.takeIf { it.width > 0 && it.height > 0 }
                                        ?: IntSize(1, 1),
                                )
                            )
                        },
                    ) {
                        Text(stringResource(R.string.trip_crop_use_image))
                    }
                }
            }
        }
    }
}
