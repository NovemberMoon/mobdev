package io.github.mobdev.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mobdev.R
import io.github.mobdev.ui.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListContent(viewModel: ChatViewModel, listState: LazyListState) {
    val channels by viewModel.channels.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()

    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    val newChannelName by viewModel.newChannelName.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadChannels() }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowCreateDialog(false) },
            title = { Text(stringResource(R.string.new_chat)) },
            text = {
                OutlinedTextField(
                    value = newChannelName,
                    onValueChange = { viewModel.updateNewChannelName(it) },
                    label = { Text(stringResource(R.string.chat_name_hint)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newChannelName.isNotBlank()) {
                        val fullChannel =
                            if (newChannelName.endsWith("@channel")) newChannelName else "$newChannelName@channel"
                        viewModel.loadInitialMessages(fullChannel)
                        viewModel.setShowCreateDialog(false)
                        viewModel.updateNewChannelName("")
                    }
                }) { Text(stringResource(R.string.create)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setShowCreateDialog(false)
                }) { Text(stringResource(R.string.btn_close)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat_list_title)) },
                actions = {
                    TextButton(onClick = { viewModel.logout() }) {
                        Text(
                            stringResource(R.string.btn_logout),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.setShowCreateDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_chat))
            }
        }
    ) { padding ->
        LazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(channels) { channel ->
                val isSelected = channel == currentChannel
                val bgColor =
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background

                Text(
                    text = channel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .clickable { viewModel.loadInitialMessages(channel) }
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}