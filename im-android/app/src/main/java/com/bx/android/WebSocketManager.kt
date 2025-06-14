package com.bx.android

import android.os.Handler
import android.os.Looper
import okhttp3.*
import com.bx.android.model.Message
import com.bx.android.model.MessageStore
import org.json.JSONObject

object WebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val heartBeatInterval = 20_000L
    private val handler = Handler(Looper.getMainLooper())
    private val heartBeatTask = object : Runnable {
        override fun run() {
            send("{\"cmd\":1,\"data\":{}}")
            handler.postDelayed(this, heartBeatInterval)
        }
    }

    fun connect(url: String, accessToken: String) {
        val request = Request.Builder().url(url).build()
        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                webSocket = ws
                val loginMsg = "{\"cmd\":0,\"data\":{\"accessToken\":\"$accessToken\"}}"
                send(loginMsg)
                handler.postDelayed(heartBeatTask, heartBeatInterval)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                parseMessage(text)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                handler.removeCallbacks(heartBeatTask)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                handler.removeCallbacks(heartBeatTask)
            }
        })
    }

    private fun parseMessage(text: String) {
        try {
            val json = JSONObject(text)
            val data = json.optJSONObject("data") ?: return
            val conversationId = data.optLong("conversationId")
            val msgId = data.optLong("id")
            val senderId = data.optLong("senderId")
            val content = data.optString("content")
            val timestamp = data.optLong("timestamp")
            val msg = Message(msgId, conversationId, senderId, content, timestamp)
            MessageStore.addMessage(msg)
        } catch (_: Exception) {
        }
    }

    fun send(text: String) {
        webSocket?.send(text)
    }

    fun close() {
        webSocket?.close(1000, null)
        handler.removeCallbacks(heartBeatTask)
    }
}
