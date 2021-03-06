package org.promethist.core

import org.promethist.common.messaging.MessageSender
import org.promethist.core.model.*
import org.promethist.core.repository.CommunityRepository
import org.promethist.core.type.Attributes
import org.slf4j.Logger
import java.util.*

data class Context(
    var pipeline: Pipeline,
    val userProfile: Profile,
    val session: Session,
    val turn: Turn,
    val logger: Logger,
    var locale: Locale? = null,
    val communityStorage: CommunityRepository,
    val messageSender: MessageSender,
    val communities: MutableMap<String, Community> = mutableMapOf(),
    var intentModels: List<Model> = listOf(),
    var dialogueEvent: DialogueEvent? = null,
    var sleepTimeout: Int = 0
) {
    // aliases
    val input get() = turn.input
    val user get() = session.user
    val attributes get() = turn.attributes
    val application get() = session.application
    val previousTurns get() = session.turns.reversed()
    val sessionEnded get() = session.dialogueStack.isEmpty()
    fun processPipeline() = pipeline.process(this)
    @Deprecated("Use turn.expectedPhrases instead", replaceWith = ReplaceWith("turn.expectedPhrases"))
    val expectedPhrases get() = turn.expectedPhrases

    fun getAttributes(name: String): Attributes =
        if (name.startsWith("user") || name.startsWith("clientUser"))
            userProfile.attributes
        else if (name.startsWith("turn") || name.startsWith("clientTurn"))
            turn.attributes
        else
            session.attributes

    fun sendMessage(subject: String, text: String) = with (user) {
        messageSender.sendMessage(MessageSender.Recipient(username, "$name $surname"), subject, text)
    }
}