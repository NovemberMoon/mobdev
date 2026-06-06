package io.github.mobdev.data

import io.github.mobdev.network.LoginRequest
import io.github.mobdev.network.MessageDto
import io.github.mobdev.network.NetworkClient
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ChatRepository {

    suspend fun login(name: String, pwd: String): String {
        val responseBody = NetworkClient.api.login(LoginRequest(name, pwd))
        return responseBody.string().replace("password:", "").trim()
    }

    suspend fun getChannels(): List<String> {
        return NetworkClient.api.getChannels()
    }

    suspend fun getMessages(channel: String, lastId: String?): List<MessageDto> {
        return NetworkClient.api.getMessages(channelName = channel, lastKnownId = lastId)
    }

    suspend fun sendMessage(message: MessageDto) {
        NetworkClient.api.sendMessage(message)
    }

    suspend fun sendImage(msg: RequestBody, picture: MultipartBody.Part) {
        NetworkClient.api.sendImage(msg, picture)
    }

    suspend fun logout() {
        NetworkClient.api.logout()
    }
}