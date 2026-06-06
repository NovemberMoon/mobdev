package io.github.mobdev.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.mobdev.network.ImageData
import io.github.mobdev.network.MessageData
import io.github.mobdev.network.MessageDto
import io.github.mobdev.network.TextData
import java.util.UUID

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val localId: String = UUID.randomUUID().toString(),
    val serverId: String?,
    val fromUser: String,
    val channel: String,
    val text: String?,
    val imageUrl: String?,
    val time: Long,
    val isPending: Boolean = false
)

fun MessageEntity.toDto() = MessageDto(
    id = this.serverId,
    from = this.fromUser,
    to = this.channel,
    data = MessageData(
        textObj = this.text?.let { TextData(it) },
        imageObj = this.imageUrl?.let { ImageData(it) }
    ),
    time = this.time
)

fun MessageDto.toEntity(channelName: String, isPending: Boolean = false) = MessageEntity(
    localId = this.id ?: UUID.randomUUID().toString(),
    serverId = this.id,
    fromUser = this.from,
    channel = channelName,
    text = this.data.textObj?.text,
    imageUrl = this.data.imageObj?.link,
    time = this.time ?: System.currentTimeMillis(),
    isPending = isPending
)
