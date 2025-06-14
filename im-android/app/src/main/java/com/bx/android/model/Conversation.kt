package com.bx.android.model

import java.util.LinkedList

class Conversation(
    val id: Long,
    val name: String
) {
    private val messages = LinkedList<Message>()

    fun addMessage(message: Message) {
        messages.add(message)
    }

    fun lastMessage(): Message? = messages.lastOrNull()

    fun getMessages(): List<Message> = messages
}
