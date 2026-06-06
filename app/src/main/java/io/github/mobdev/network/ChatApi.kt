package io.github.mobdev.network

import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class LoginRequest(val name: String, val pwd: String)

data class MessageDto(
    val id: String? = null,
    val from: String,
    val to: String,
    val data: MessageData,
    val time: Long? = null
)

data class MessageData(
    @SerializedName("Text") val textObj: TextData? = null,
    @SerializedName("Image") val imageObj: ImageData? = null
)

data class TextData(val text: String)
data class ImageData(val link: String)

interface ChatApi {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): ResponseBody

    @GET("/channels")
    suspend fun getChannels(): List<String>

    @GET("/channel/{channelName}")
    suspend fun getMessages(
        @Path("channelName") channelName: String,
        @Query("limit") limit: Int = 20,
        @Query("lastKnownId") lastKnownId: String? = null,
        @Query("reverse") reverse: Boolean = true
    ): List<MessageDto>

    @POST("/messages")
    suspend fun sendMessage(@Body message: MessageDto)

    @Multipart
    @POST("/messages")
    suspend fun sendImage(
        @Part("msg") message: RequestBody,
        @Part picture: MultipartBody.Part
    )

    @POST("/logout")
    suspend fun logout()
}

object NetworkClient {
    var currentToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        if (originalRequest.url.encodedPath.contains("/login")) {
            return@Interceptor chain.proceed(originalRequest)
        }
        val newRequest = originalRequest.newBuilder().apply {
            currentToken?.let { addHeader("X-Auth-Token", it) }
        }.build()
        chain.proceed(newRequest)
    }

    private val logging =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val api: ChatApi = Retrofit.Builder()
        .baseUrl("https://faerytea.name/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ChatApi::class.java)
}