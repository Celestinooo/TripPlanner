package dev.lucas.portfolio.feature.tripplanner.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TripConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String = "Confirmar",
    dismissLabel: String = "Cancelar",
    destructive: Boolean = false,
    icon: ImageVector = if (destructive) Icons.Rounded.Delete else Icons.Rounded.Info,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = if (destructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text  = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape   = RoundedCornerShape(12.dp),
                colors  = if (destructive) ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError,
                ) else ButtonDefaults.buttonColors(),
            ) {
                Text(confirmLabel, modifier = Modifier.padding(horizontal = 4.dp))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape   = RoundedCornerShape(12.dp),
            ) {
                Text(
                    dismissLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
