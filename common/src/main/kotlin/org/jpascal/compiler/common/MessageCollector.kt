package org.jpascal.compiler.common

class MessageCollector {
    private val messages = mutableListOf<Message>()

    fun addMessage(message: Message) {
        messages.add(message)
    }

    fun getMessages(): List<Message> = messages
}