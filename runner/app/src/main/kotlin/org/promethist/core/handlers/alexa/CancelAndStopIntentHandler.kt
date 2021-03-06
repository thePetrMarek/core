package org.promethist.core.handlers.alexa

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.request.Predicates.intentName
import org.promethist.core.BotCore

class CancelAndStopIntentHandler : AbstractHandler(intentName("AMAZON.StopIntent")) {

    override fun handle(input: HandlerInput) = withContext(input) {
        val speech = BotCore.doBye(context)
        context.sessionId = null
        addResponse(speech)
    }.build()
}