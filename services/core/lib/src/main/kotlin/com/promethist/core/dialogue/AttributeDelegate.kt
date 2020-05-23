package com.promethist.core.dialogue

import com.promethist.core.Context
import com.promethist.core.type.Attributes
import com.promethist.core.type.Value
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

abstract class AttributeDelegate<V: Any>(private val clazz: KClass<*>, val namespace: (() -> String)? = null, val default: (Context.() -> V)) {

    abstract val attributes: Attributes

    operator fun getValue(thisRef: Dialogue, property: KProperty<*>): V =
            attributes[namespace?.invoke() ?: "default"].getOrPut(property.name) {
                Value.pack(default.invoke(Dialogue.threadContext().context))
            }.let {
                if (!clazz.isSubclassOf(Value::class) && (it is Value<*>)) {
                    it.value
                } else {
                    it
                }
            } as V

    operator fun setValue(thisRef: Dialogue, property: KProperty<*>, any: V) {
        attributes[namespace?.invoke() ?: "default"][property.name] = Value.pack(any)
    }
}