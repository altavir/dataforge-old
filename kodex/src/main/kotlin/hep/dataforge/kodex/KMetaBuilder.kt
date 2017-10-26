package hep.dataforge.kodex

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaNode.DEFAULT_META_NAME
import hep.dataforge.values.NamedValue

/**
 * Kotlin meta builder extension
 */
class KMetaBuilder(name: String) : MetaBuilder(name) {
    operator fun Meta.unaryPlus() {
        putNode(this);
    }

    operator fun String.unaryMinus() {
        removeNode(this);
        removeValue(this);
    }

    operator fun NamedValue.unaryPlus() {
        putValue(this.name, this.anonymousValue)
    }

    /**
     * Add value
     */
    infix fun String.to(value: Any) {
        putValue(this, value);
    }

    /**
     * Attach new node
     */
    fun node(name: String, transform: (KMetaBuilder.() -> Unit)? = null) {
        val node = KMetaBuilder(name);
        transform?.invoke(node)
        attachNode(node)
    }

    fun node(name: String, meta: Meta) {
        putNode(name, meta)
    }
}

fun buildMeta(name: String = DEFAULT_META_NAME, transform: (KMetaBuilder.() -> Unit)? = null): KMetaBuilder {
    val node = KMetaBuilder(name);
    transform?.invoke(node)
    return node
}