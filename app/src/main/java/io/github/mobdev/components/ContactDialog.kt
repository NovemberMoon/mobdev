package io.github.mobdev.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.mobdev.Contact
import io.github.mobdev.R

@Composable
fun ContactDialog(contact: Contact, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = contact.name, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(text = stringResource(R.string.phone_label) + contact.phoneNumber)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.email_label) + (contact.email ?: "—"))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close_btn))
            }
        }
    )
}
