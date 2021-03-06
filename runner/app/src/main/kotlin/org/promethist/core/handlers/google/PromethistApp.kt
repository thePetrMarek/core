package org.promethist.core.handlers.google

import com.google.actions.api.ActionRequest
import com.google.actions.api.ActionsSdkApp
import com.google.actions.api.ForIntent
import com.google.actions.api.response.ResponseBuilder
import com.google.api.services.actions_fulfillment.v2.model.BasicCard
import com.google.api.services.actions_fulfillment.v2.model.Image
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse
import org.promethist.client.BotContext
import org.promethist.common.AppConfig
import org.promethist.core.BotCore
import org.promethist.core.Response
import org.promethist.core.model.TtsConfig
import org.promethist.core.type.Dynamic
import org.promethist.util.LoggerDelegate
import java.util.*

class PromethistApp : ActionsSdkApp() {

    companion object {
        val appKey = object : ThreadLocal<String>() {
            override fun initialValue() = "default"
        }
    }

    val appTitle = AppConfig.instance["title"]

    private val logger by LoggerDelegate()

    inner class ContextualBlock(val request: ActionRequest, val context: BotContext) {
        fun addResponse(response: Response) = getResponseBuilder(request).apply {
            add(SimpleResponse().apply {
                displayText = response.text()
                ssml = response.ssml(TtsConfig.Provider.Google)
            })
            response.items.forEach {
                if (it.image != null) {
                    add(BasicCard().apply {
                        title = appTitle
                        if (!it.text.isNullOrBlank())
                            subtitle = it.text
                        image = Image().apply {
                            url = it.image
                            accessibilityText = "(alt)"
                        }
                    })
                }
            }
            context.sessionId ?: endConversation()
        }
    }

    private fun withContext(request: ActionRequest, block: ContextualBlock.() -> ResponseBuilder): ResponseBuilder {
        val context = BotCore.context(
                request.sessionId!!,
                "google-device",
                appKey.get(),
                null,
                Locale.ENGLISH/*, request.appRequest?.user?.locale?*/,
                Dynamic(
                        "clientType" to "google-assistant:${AppConfig.version}"
                )
        )
        logger.info("${this::class.simpleName}.withContext(request=$request, context=$context)")
        return block(ContextualBlock(request, context))
    }


    @ForIntent("actions.intent.MAIN")
    fun onMainIntent(request: ActionRequest) = withContext(request) {
        val response = BotCore.doIntro(context)
        addResponse(response)
    }.build()

    @ForIntent("actions.intent.TEXT")
    fun onTextIntent(request: ActionRequest) = withContext(request) {
        val text = request.getArgument("text")!!.textValue
        val response = BotCore.doText(context, text)
        addResponse(response)
    }.build()

}