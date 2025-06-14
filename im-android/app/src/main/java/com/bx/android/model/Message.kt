package com.bx.android.model

data class Message(
    val id: Long,
    val conversationId: Long,
    val senderId: Long,
    val content: String,
    val timestamp: Long
)
