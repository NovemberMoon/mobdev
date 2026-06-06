package io.github.mobdev.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.mobdev.data.ChatRepository
import io.github.mobdev.network.MessageData
import io.github.mobdev.network.MessageDto
import io.github.mobdev.network.NetworkClient
import io.github.mobdev.network.TextData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

val Context.dataStore by preferencesDataStore(name = "user_session")

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application)
    private val dataStore = application.dataStore

    private val tokenKey = stringPreferencesKey("auth_token")
    private val userKey = stringPreferencesKey("username")

    private val _channels = MutableStateFlow<List<String>>(emptyList())
    val channels = _channels.asStateFlow()

    private var channelsJob: Job? = null

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages = _messages.asStateFlow()

    private var messagesJob: Job? = null
    private var pollingJob: Job? = null
    private var channelsPollingJob: Job? = null

    private val _requireAuth = MutableStateFlow<Boolean?>(null)
    val requireAuth = _requireAuth.asStateFlow()

    private val _currentChannel = MutableStateFlow<String?>(null)
    val currentChannel = _currentChannel.asStateFlow()

    private val _isImageUploading = MutableStateFlow(false)
    val isImageUploading = _isImageUploading.asStateFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog = _showCreateDialog.asStateFlow()

    private val _newChannelName = MutableStateFlow("")
    val newChannelName = _newChannelName.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _loginName = MutableStateFlow("")
    val loginName = _loginName.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    val loginPassword = _loginPassword.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn = _isLoggingIn.asStateFlow()

    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog = _showErrorDialog.asStateFlow()

    var currentUser: String = ""

    private var isLastPage = false
    private var isLoading = false

    init {
        viewModelScope.launch {
            try {
                val prefs = dataStore.data.first()
                val savedToken = prefs[tokenKey]
                val savedUser = prefs[userKey]

                if (!savedToken.isNullOrBlank() && !savedUser.isNullOrBlank()) {
                    NetworkClient.currentToken = savedToken
                    currentUser = savedUser
                    _requireAuth.value = false
                } else {
                    _requireAuth.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _requireAuth.value = true
            }
        }
    }

    fun login(onSuccess: () -> Unit) {
        val name = _loginName.value
        val pwd = _loginPassword.value
        if (name.isBlank() || pwd.isBlank()) return

        _isLoggingIn.value = true
        _showErrorDialog.value = false

        viewModelScope.launch {
            try {
                val cleanToken = repository.login(name, pwd)
                NetworkClient.currentToken = cleanToken
                currentUser = name

                dataStore.edit { prefs ->
                    prefs[tokenKey] = cleanToken
                    prefs[userKey] = name
                }

                _requireAuth.value = false
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _showErrorDialog.value = true
            } finally {
                _isLoggingIn.value = false
            }
        }
    }

    fun dismissErrorDialog() {
        _showErrorDialog.value = false
    }

    fun loadChannels() {
        channelsJob?.cancel()
        channelsJob = viewModelScope.launch {
            repository.getChannelsFlow().collect { localChannels ->
                _channels.value = localChannels
            }
        }

        channelsPollingJob?.cancel()
        channelsPollingJob = viewModelScope.launch {
            while (true) {
                try {
                    repository.fetchChannelsFromNetwork()
                    repository.syncPendingMessages()
                } catch (e: HttpException) {
                    if (e.code() == 401) {
                        logout()
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000)
            }
        }
    }

    fun loadInitialMessages(channel: String) {
        if (_currentChannel.value == channel && _messages.value.isNotEmpty()) return

        viewModelScope.launch {
            repository.addChannelLocal(channel)
        }

        _currentChannel.value = channel
        _inputText.value = ""
        isLastPage = false

        messagesJob?.cancel()
        pollingJob?.cancel()

        messagesJob = viewModelScope.launch {
            repository.getMessagesFlow(channel).collect { localMessages ->
                _messages.value = localMessages
            }
        }

        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    repository.syncPendingMessages()
                    repository.fetchMessagesFromNetwork(channel, isPagination = false)
                } catch (e: HttpException) {
                    if (e.code() == 401) {
                        logout()
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(5000)
            }
        }
    }

    fun loadMoreMessages() {
        if (isLoading || isLastPage || _currentChannel.value == null) return
        val channel = _currentChannel.value ?: return

        isLoading = true
        viewModelScope.launch {
            try {
                repository.fetchMessagesFromNetwork(channel, isPagination = true)
            } catch (e: HttpException) {
                e.printStackTrace()
                if (e.code() == 401) logout()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun updateLoginName(name: String) {
        _loginName.value = name
    }

    fun updateLoginPassword(pwd: String) {
        _loginPassword.value = pwd
    }

    fun setShowCreateDialog(show: Boolean) {
        _showCreateDialog.value = show
    }

    fun updateNewChannelName(name: String) {
        _newChannelName.value = name
    }

    fun sendMessage(text: String) {
        val channel = _currentChannel.value ?: return
        _inputText.value = ""

        viewModelScope.launch {
            try {
                val msg = MessageDto(
                    id = null,
                    from = currentUser,
                    to = channel,
                    data = MessageData(textObj = TextData(text), imageObj = null),
                    time = System.currentTimeMillis()
                )
                repository.sendMessage(msg, channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendImage(uri: Uri) {
        val channel = _currentChannel.value ?: return
        viewModelScope.launch {
            try {
                _isImageUploading.value = true
                val contentResolver = getApplication<Application>().contentResolver
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

                val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val bodyPart =
                    MultipartBody.Part.createFormData("picture", "upload.jpg", requestFile)

                val jsonString = """{"from":"$currentUser","to":"$channel"}"""
                val msgPart = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

                repository.sendImage(msgPart, bodyPart, channel)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isImageUploading.value = false
            }
        }
    }

    fun clearChannel() {
        messagesJob?.cancel()
        pollingJob?.cancel()
        _currentChannel.value = null
        _inputText.value = ""
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            NetworkClient.currentToken = null
            dataStore.edit { prefs -> prefs.clear() }
            _requireAuth.value = true
            clearChannel()
        }
    }
}
