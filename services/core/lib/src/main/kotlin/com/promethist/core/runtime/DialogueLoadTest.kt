package com.promethist.core.runtime

import com.promethist.core.Context
import com.promethist.core.model.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

object DialogueLoadTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val logger: Logger = LoggerFactory.getLogger("dialogue-model-load-test")
        val loader = LocalFileLoader(File("test/dialogue"))
        val dialogueName = "product/some-dialogue/1"
        val dialogue = loader.newObject<Dialogue>("$dialogueName/model", "ble", 1, false)
        dialogue.validate()
        println(dialogue.describe())

        val user = User(username = "tester@promethist.ai", name = "Tester", surname = "Tester", nickname = "Tester")

        val context = Context(
                Profile(name = "tester@promethist.ai", user_id = user._id),
                Session(
                        datetime = Date(),
                        sessionId = "T-E-S-T",
                        user = user,
                        application = Application(name = "test", dialogueName = "product/some-dialogue/1", ttsVoice = "Grace")
                ),
                Turn(Turn.Input("some message")),
                logger
        )
        val func = dialogue.functions.first()
        println("calling $func:")
        println(func.exec(context))

        val subDialogue = dialogue.subDialogues.first().createDialogue(context)
        println("sub-dialogue: $subDialogue")
        println(subDialogue.describe())
    }
}