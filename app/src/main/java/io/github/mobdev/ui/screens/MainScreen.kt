package io.github.mobdev.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mobdev.R
import io.github.mobdev.ui.ChatViewModel

@Composable
fun MainScreen(viewModel: ChatViewModel, onImageClick: (String) -> Unit) {
    val currentChannel by viewModel.currentChannel.collectAsState()
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

    val chatListState = rememberLazyListState()
    val messagesListState = rememberLazyListState()

    BackHandler(enabled = currentChannel != null) {
        viewModel.clearChannel()
    }

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                ChatListContent(viewModel, listState = chatListState)
            }
            HorizontalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            )
            Box(modifier = Modifier.weight(2f)) {
                if (currentChannel == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.select_chat),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    MessagesContent(viewModel, onImageClick, listState = messagesListState)
                }
            }
        }
    } else {
        if (currentChannel == null) {
            ChatListContent(viewModel, listState = chatListState)
        } else {
            MessagesContent(viewModel, onImageClick, listState = messagesListState)
        }
    }
}