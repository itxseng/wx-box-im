package com.bx.android.model

object MessageStore {
    private val conversations = mutableMapOf<Long, Conversation>()

    fun addConversation(conversation: Conversation) {
        conversations[conversation.id] = conversation
    }

    fun addMessage(message: Message) {
        val conv = conversations.getOrPut(message.conversationId) {
            Conversation(message.conversationId, "会话${message.conversationId}")
        }
        conv.addMessage(message)
    }

    fun getConversations(): List<Conversation> = conversations.values.toList()
}
