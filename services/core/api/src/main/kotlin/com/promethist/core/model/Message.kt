package com.promethist.core.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.promethist.common.DataObject
import java.util.*

data class Message(

        /**
         * Message id.
         */
        var _id: String = createId(),

        /**
         * Reference id to previous logically preceded message (question).
         */
        var _ref: String? = null,

        /**
         * Each message has none or more items
         */
        var items: MutableList<MessageItem> = mutableListOf(),

        /**
         * Message language.
         */
        var language: Locale? = null,

        /**
         * When message was created.
         */
        val datetime: Date = Date(),

        /**
         * Sending client identification (determined by client application - e.g. device ID, user auth token etc.)
         */
        var sender: String,

        /**
         * Receiver identification. When message send by client, it is optional (can address specific part of bot
         * service, depending on its type).
         * Bot service is using this to identify client (when not set, it can work as broadcast to all clients
         * (if this will be supported by port in the future).
         */
        var recipient: String? = null,

        /**
         * Conversation session identification. Set by port. todo: control if the session was generated by port and not client
         */
        var sessionId: String? = null,

        /**
         * Identification of the end of session (graph in dialog editor)
         */
        var sessionEnded: Boolean = false,

        /**
         * Expected phrases. It will be provided to Speech-to-text engine as a hint of more probable words
         */
        var expectedPhrases: MutableList<ExpectedPhrase>? = mutableListOf(),

        /**
         * Extension properties for message. Determined by bot service and/or client application.
         */
        val attributes: PropertyMap = PropertyMap()
) {

    @JsonDeserialize(using = PropertyMap.Deserializer::class)
    class PropertyMap : DataObject() {

        class Deserializer : DataObject.Deserializer<PropertyMap>(PropertyMap::class.java) {
            //TODO support for specific data types used in message properties (if needed)
        }
    }


    data class ExpectedPhrase(val text: String? = null, val boost: Float = 1.0F) // boost can be used in google stt v1p1beta1


    fun response(items: MutableList<MessageItem>): Message {
        val sender = this.recipient?:"unknown"
        val recipient = this.sender
        return this.copy(_id = createId(), _ref = _id, sender = sender, recipient = recipient, items = items, datetime = Date(), sessionId = this.sessionId)
    }

    companion object {

        @JvmStatic
        fun createId(): String {
            return UUID.randomUUID().toString()
        }
    }
}