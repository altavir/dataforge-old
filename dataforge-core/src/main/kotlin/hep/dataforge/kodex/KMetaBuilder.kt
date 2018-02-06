package hep.dataforge.kodex

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaID
import hep.dataforge.meta.MetaNode.DEFAULT_META_NAME
import hep.dataforge.values.NamedValue

/**
 * Kotlin meta builder extension
 */
class KMetaBuilder(name: String = MetaBuilder.DEFAULT_META_NAME) : MetaBuilder(name) {
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
     * Short infix notation to put value
     */
    infix fun String.v(value: Any) {
        putValue(this, value);
    }

    /**
     * Short infix notation to put node
     */
    infix fun String.n(node: Meta) {
        putNode(this, node)
    }

    /**
     * Short infix notation  to put any object that could be converted to meta
     */
    infix fun String.n(node: MetaID) {
        putNode(this, node.toMeta())
    }

    fun putNode(node: MetaID) {
        putNode(node.toMeta())
    }

    fun putNode(key: String, node: MetaID) {
        putNode(key, node.toMeta())
    }

    /**
     * Attach new node
     */
    fun node(name: String, vararg values: Pair<String, Any>, transform: (KMetaBuilder.() -> Unit)? = null) {
        val node = KMetaBuilder(name);
        values.forEach {
            node.putValue(it.first, it.second)
        }
        transform?.invoke(node)
        attachNode(node)
    }
}

fun buildMeta(name: String = DEFAULT_META_NAME, transform: (KMetaBuilder.() -> Unit)? = null): KMetaBuilder {
    val node = KMetaBuilder(name);
    transform?.invoke(node)
    return node
}

fun buildMeta(name: String, vararg values: Pair<String, Any>, transform: (KMetaBuilder.() -> Unit)? = null): KMetaBuilder {
    val node = KMetaBuilder(name);
    values.forEach {
        node.putValue(it.first, it.second)
    }
    transform?.invoke(node)
    return node
}