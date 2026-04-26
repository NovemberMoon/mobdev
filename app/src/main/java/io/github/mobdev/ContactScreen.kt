package io.github.mobdev

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import io.github.mobdev.components.ContactDialog
import io.github.mobdev.components.ContactItem
import io.github.mobdev.components.PermissionDenied

@Composable
fun ContactsScreen(viewModel: ContactsViewModel) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasPermission = isGranted }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (hasPermission) {
                LaunchedEffect(Unit) { viewModel.loadContacts() }
                ContactsList(viewModel)
            } else {
                PermissionDenied(onRequestClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) })
            }
        }
    }
}

@Composable
fun ContactsList(viewModel: ContactsViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    if (contacts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_contacts_found))
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(contacts) { contact ->
                ContactItem(contact = contact) { selectedContact = contact }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }

    selectedContact?.let { contact ->
        ContactDialog(contact = contact, onDismiss = { selectedContact = null })
    }
}