package com.promethist.core.model

import com.promethist.core.runtime.Loader
import org.slf4j.Logger
import kotlin.random.Random
import kotlin.reflect.KProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

open class Dialogue(open val loader: Loader, open val name: String) {

    val nodes: MutableSet<Node> = mutableSetOf()
    var nextId: Int = 0
    var start = StartDialogue(nextId--)
    var stop = StopDialogue(Int.MIN_VALUE)

    abstract inner class Node(open val id: Int) {

        init { nodes.add(this) }

        override fun hashCode(): Int = id

        override fun toString(): String = "${javaClass.simpleName}(id=$id)"
    }

    abstract inner class TransitNode(override val id: Int): Node(id) {
        lateinit var next: Node

        override fun toString(): String = "${javaClass.simpleName}(id=$id, next=$next)"
    }

    data class Transition(val node: Node)

    data class Scope(val session: Session, val context: Context, val logger: Logger)

    inner class UserInput(
            override val id: Int,
            vararg intent: Intent
    ): Node(id) {
        val intents = intent
    }

    open inner class Intent(
            override val id: Int,
            vararg utterance: String
    ): TransitNode(id) {
        val utterances = utterance
    }

    inner class GlobalIntent(
             override val id: Int,
             vararg utterance: String
    ): Intent(id, *utterance)

    open inner class Response(
            override val id: Int,
            vararg text: (Scope.(Response) -> String)
    ): TransitNode(id) {
        val texts = text
        fun getText(scope: Scope, index: Int = -1): String = texts[if (index < 0) Random.nextInt(texts.size) else index](scope, this)
    }

    inner class AudioResponse(
            override val id: Int,
            val audio: String,
            vararg text: (Scope.(Response) -> String)
    ): Response(id, *text)

    inner class ImageResponse(
            override val id: Int,
            val image: String,
            vararg text: (Scope.(Response) -> String)
    ): Response(id, *text)

    inner class Function(
            override val id: Int,
            val lambda: (Scope.(Function) -> Transition)
    ): Node(id) {
        fun exec(scope: Scope): Transition = lambda(scope, this)
    }

    inner class SubDialogue(
            override val id: Int,
            val name: String,
            val lambda: (Scope.(SubDialogue) -> Dialogue)): TransitNode(id) {

        fun createDialogue(scope: Scope): Dialogue = lambda(scope, this)

        fun create(vararg arg: Any) = loader.newObject<Dialogue>("$name/model", *arg)
    }

    inner class StartDialogue(override val id: Int) : TransitNode(id)

    inner class StopDialogue(override val id: Int) : Node(id)

    inner class StopSession(override val id: Int) : Node(id)

    val intents: List<Intent> get() = nodes.filterIsInstance<Intent>()

    val globalIntents: List<GlobalIntent> get() = nodes.filterIsInstance<GlobalIntent>()

    val responses: List<Response> get() = nodes.filterIsInstance<Response>()

    val functions: List<Function> get() = nodes.filterIsInstance<Function>()

    val subDialogues: List<SubDialogue> get() = nodes.filterIsInstance<SubDialogue>()

    fun node(id: Int): Node = nodes.find { it.id == id }?:error("Node $id not found in $this")

    val nodeMap: Map<String, Node> by lazy {
        javaClass.kotlin.members.filter {
            it is KProperty && it.returnType.isSubtypeOf(Node::class.createType())
        }.map { it.name to it.call(this) as Node }.toMap()
    }

    //val nodes = nodeMap.values

    fun validate() {
        for (node in nodes) {
            try {
                node is TransitNode && node.next == null
            } catch (e: UninitializedPropertyAccessException) {
                error("${this::class.qualifiedName}.${node} missing next node reference")
            }
        }
    }

    fun describe(): String {
        val sb = StringBuilder()
        nodeMap.forEach {
            sb.append(it.key).append(" = ").appendln(it.value)
        }
        return sb.toString()
    }
}

