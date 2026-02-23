package com.shaeed.ludo.network

import com.shaeed.ludo.model.online.GameMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketClient(wsUrl: String) {

    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)   // no timeout — WebSockets are long-lived
        .pingInterval(30, TimeUnit.SECONDS)       // let OkHttp send keep-alive pings
        .build()

    private val _messages = Channel<GameMessage>(Channel.UNLIMITED)

    /** Incoming messages from the server. Collect in a coroutine. */
    val incomingMessages: Flow<GameMessage> = _messages.receiveAsFlow()

    private var webSocket: WebSocket? = null

    @Volatile var isConnected: Boolean = false
        private set

    private val request = Request.Builder().url(wsUrl).build()

    fun connect() {
        webSocket = httpClient.newWebSocket(request, listener)
    }

    fun send(message: GameMessage) {
        webSocket?.send(json.encodeToString(message))
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
        isConnected = false
    }

    fun close() {
        disconnect()
        httpClient.dispatcher.executorService.shutdown()
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            isConnected = true
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val msg = try {
                json.decodeFromString<GameMessage>(text)
            } catch (_: Exception) {
                return
            }
            _messages.trySend(msg)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            isConnected = false
            // ViewModel observes isConnected and can surface an error to the user
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            isConnected = false
        }
    }
}
