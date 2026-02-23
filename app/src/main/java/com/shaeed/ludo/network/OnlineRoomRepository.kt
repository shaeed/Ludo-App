package com.shaeed.ludo.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OnlineRoomRepository(private val baseUrl: String) {

    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Ask the server to create a new room.
     * @return the 6-character room code.
     * @throws Exception on network error or unexpected response.
     */
    suspend fun createRoom(): String = withContext(Dispatchers.IO) {
        val body = "{}".toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/rooms")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Server error ${response.code}")

        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        json.parseToJsonElement(responseBody)
            .jsonObject["roomCode"]
            ?.jsonPrimitive?.content
            ?: throw Exception("Missing roomCode in response")
    }

    /**
     * Check whether a room with [code] exists on the server.
     * @return true if the room exists.
     * @throws Exception on network error.
     */
    suspend fun validateRoom(code: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/rooms/${code.uppercase()}")
            .build()

        val response = client.newCall(request).execute()
        response.isSuccessful
    }

    /**
     * Build a WebSocket URL from the HTTP base URL.
     * e.g. "http://1.2.3.4:8080" + "ABC123" + RED + "Alice"
     *   -> "ws://1.2.3.4:8080/rooms/ABC123/ws?color=RED&name=Alice"
     */
    fun buildWsUrl(roomCode: String, playerColor: String, playerName: String): String {
        val wsBase = baseUrl
            .replace("https://", "wss://")
            .replace("http://", "ws://")
        return "$wsBase/rooms/${roomCode.uppercase()}/ws?color=${playerColor.uppercase()}&name=$playerName"
    }
}
