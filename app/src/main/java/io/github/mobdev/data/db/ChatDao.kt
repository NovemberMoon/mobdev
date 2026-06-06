package io.github.mobdev.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getChannelsFlow(): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("SELECT * FROM messages WHERE channel = :channel ORDER BY time DESC")
    fun getMessagesForChannel(channel: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE isPending = 1")
    suspend fun getPendingMessages(): List<MessageEntity>

    @Query("DELETE FROM messages WHERE localId = :localId")
    suspend fun deletePendingMessage(localId: String)

    @Query("SELECT serverId FROM messages WHERE channel = :channel AND serverId IS NOT NULL ORDER BY time ASC LIMIT 1")
    suspend fun getOldestMessageId(channel: String): String?

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
