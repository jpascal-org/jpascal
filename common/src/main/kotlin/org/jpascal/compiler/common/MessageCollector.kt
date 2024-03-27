package org.jpascal.compiler.common

class MessageCollector {
    private val messages = mutableListOf<Message>()

    fun add(message: Message) {
        messages.add(message)
    }

    fun list(): List<Message> = messages
}