package com.promethist.core.dialogue

import com.promethist.core.type.NamedEntity
import com.promethist.core.type.StringMutableSet
import kotlin.reflect.KProperty

class NamedEntitySetAttributeDelegate<E: NamedEntity>(
        val entities: Collection<E>,
        scope: ContextualAttributeDelegate.Scope,
        namespace: (() -> String)? = null
) {
    private val attributeDelegate = ContextualAttributeDelegate<StringMutableSet>(scope, StringMutableSet::class, namespace, null)

    operator fun getValue(thisRef: Dialogue, property: KProperty<*>): MutableSet<E> {
        val names = attributeDelegate.getValue(thisRef, property)
        return object : LinkedHashSet<E>(names.mapNotNull { name -> entities.find { it.name == name } }) {
            override fun add(entity: E): Boolean {
                names.add(entity.name)
                attributeDelegate.setValue(thisRef, property, names)
                return super.add(entity)
            }

            override fun addAll(entities: Collection<E>): Boolean {
                entities.forEach { names.add(it.name) }
                attributeDelegate.setValue(thisRef, property, names)
                return super.addAll(entities)
            }

            override fun remove(entity: E): Boolean {
                names.remove(entity.name)
                attributeDelegate.setValue(thisRef, property, names)
                return super.remove(entity)
            }

            override fun clear() {
                names.clear()
                attributeDelegate.setValue(thisRef, property, names)
                super.clear()
            }
        }
    }
}