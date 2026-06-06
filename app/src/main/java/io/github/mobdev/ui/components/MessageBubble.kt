package io.github.mobdev.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.mobdev.network.MessageDto
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    msg: MessageDto,
    isMine: Boolean,
    onImageClick: (String) -> Unit
) {
    val timeObj = msg.time?.let { if (it < 1000000000000L) it * 1000 else it }
    val timeString =
        timeObj?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)) } ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isMine) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 6.dp,
                        bottom = 4.dp
                    )
                ) {
                    if (!isMine) {
                        Text(
                            text = msg.from,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    msg.data.textObj?.let { textObj ->
                        val timeReservedModifier = if (timeString.isNotEmpty()) {
                            Modifier.padding(end = 40.dp, bottom = 14.dp)
                        } else {
                            Modifier
                        }
                        Text(
                            text = textObj.text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = timeReservedModifier
                        )
                    }

                    msg.data.imageObj?.let { img ->
                        AsyncImage(
                            model = "https://faerytea.name/thumb/${img.link}",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val encoded = URLEncoder.encode(
                                        img.link,
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    onImageClick(encoded)
                                }
                        )
                    }
                }

                if (timeString.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeString,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        if (isMine && (msg.id == null || msg.id.length > 20)) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Ожидает отправки",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
