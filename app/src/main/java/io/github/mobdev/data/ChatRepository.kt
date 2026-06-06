package io.github.mobdev.data

import android.content.Context
import io.github.mobdev.data.db.AppDatabase
import io.github.mobdev.data.db.ChannelEntity
import io.github.mobdev.data.db.toDto
import io.github.mobdev.data.db.toEntity
import io.github.mobdev.network.LoginRequest
import io.github.mobdev.network.MessageDto
import io.github.mobdev.network.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ChatRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.chatDao()
    private val syncMutex = Mutex()

    suspend fun login(name: String, pwd: String): String {
        val responseBody = NetworkClient.api.login(LoginRequest(name, pwd))
        return responseBody.string().replace("password:", "").trim()
    }

    fun getChannelsFlow(): Flow<List<String>> {
        return dao.getChannelsFlow().map { entities ->
            entities.map { it.name }
        }
    }

    suspend fun fetchChannelsFromNetwork() {
        val networkChannels = NetworkClient.api.getChannels()
        val entities = networkChannels.map { ChannelEntity(it) }
        dao.insertChannels(entities)
    }

    suspend fun addChannelLocal(channelName: String) {
        dao.insertChannels(listOf(ChannelEntity(channelName)))
    }

    fun getMessagesFlow(channel: String): Flow<List<MessageDto>> {
        return dao.getMessagesForChannel(channel).map { entities ->
            entities.map { it.toDto() }
        }
    }

    suspend fun fetchMessagesFromNetwork(channel: String, isPagination: Boolean = false) {
        val lastId = if (isPagination) dao.getOldestMessageId(channel) else null
        val networkMessages =
            NetworkClient.api.getMessages(channelName = channel, lastKnownId = lastId)

        val entities = networkMessages.map { it.toEntity(channelName = channel) }
        dao.insertMessages(entities)
    }

    suspend fun sendMessage(message: MessageDto, channel: String) {
        val pendingEntity = message.toEntity(channelName = channel, isPending = true)
        dao.insertMessage(pendingEntity)
        syncPendingMessages()
        fetchMessagesFromNetwork(channel)
    }

    suspend fun sendImage(msg: RequestBody, picture: MultipartBody.Part, channel: String) {
        NetworkClient.api.sendImage(msg, picture)
        fetchMessagesFromNetwork(channel)
    }

    suspend fun syncPendingMessages() {
        syncMutex.withLock {
            val pendingList = dao.getPendingMessages()
            for (pending in pendingList) {
                try {
                    NetworkClient.api.sendMessage(pending.toDto())
                    dao.deletePendingMessage(pending.localId)
                } catch (e: Exception) {
                }
            }
        }
    }

    suspend fun logout() {
        NetworkClient.api.logout()
        dao.clearAll()
    }
}
