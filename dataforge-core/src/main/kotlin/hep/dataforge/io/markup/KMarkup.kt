/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package hep.dataforge.io.markup

import hep.dataforge.kodex.mutableIntValue
import hep.dataforge.kodex.mutableNode
import hep.dataforge.kodex.mutableStringValue
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.values.Value
import java.util.*

@DslMarker
annotation class MarkupDSL

/**
 * User level DSL to build markup
 */
@MarkupDSL
open class KMarkup(override val parent: Markup? = null, val meta: MetaBuilder = MetaBuilder("markup")) : Markup {
    override var style: MetaBuilder by meta.mutableNode(Markup.MARKUP_STYLE_NODE)
    override var type
        get() = meta.getString("type") { Markup.inferType(this) }
        set(value) {
            meta.setValue("type", value)
        }

    override var content: List<KMarkup>
        get() = meta.getMetaList(Markup.MARKUP_CONTENT_NODE).map { KMarkup(parent = this, meta = it.builder) }
        set(value) {
            meta.setNode(Markup.MARKUP_CONTENT_NODE, value.map { it.meta })
        }

    /**
     * Create a sealed markup from this builder
     */
    fun build(): Markup {
        return GenericMarkup(meta.build())
    }

    protected fun addContent(content: Meta) {
        meta.putNode(Markup.MARKUP_CONTENT_NODE, content)
    }

    /**
     * Add given markup as a child
     */
    fun addContent(markup: KMarkup): KMarkup {
        return markup.also {
            this.meta.attachNode(markup.meta.rename(Markup.MARKUP_CONTENT_NODE))
        }
    }

    /**
     * Add a plain text child
     */
    operator fun String.unaryPlus(): KTextMarkup {
        return text(this)

    }

//    /**
//     * Add given markup as a child
//     */
//    operator fun Markup.unaryPlus() {
//        return addContent(this.toMeta())
//    }

    @JvmOverloads
    fun text(text: String = "", color: String = "", action: KTextMarkup.() -> Unit = {}): KTextMarkup {
        return KTextMarkup(parent = this).apply {
            this.text = text
            this.color = color;
        }.apply(action).also { addContent(it) }
    }

    @JvmOverloads
    fun list(level: Int? = null, bullet: String? = null, action: KListMarkup.() -> Unit = {}): KListMarkup {
        return KListMarkup(parent = this)
                .apply {
                    level?.let { this.level = it }
                    bullet?.let { this.bullet = it }
                }
                .apply(action)
                .also {
                    addContent(it)
                }
    }

    fun ln() {
        +"\n"
    }

    override fun toMeta(): Meta {
        return meta
    }

    override fun optValue(path: String): Optional<Value> {
        return meta.optValue(path)
    }

}

class KTextMarkup(parent: Markup?, meta: MetaBuilder = MetaBuilder("text")) : KMarkup(parent, meta) {
    init {
        type = Markup.TEXT_TYPE
    }

    var text: String by meta.mutableStringValue()
    var color by style.mutableStringValue()

    //var textWidth by meta.mutableIntValue(def = -1)

    companion object {
        @JvmOverloads
        fun create(text: String, parent: Markup? = null): KTextMarkup{
            return KTextMarkup(parent).apply {
                this.text = text
            }
        }
    }
}

class KHeaderMarkup(parent: Markup?, meta: MetaBuilder = MetaBuilder("header")) : KMarkup(parent, meta) {
    init {
        type = Markup.HEADER_TYPE
    }

    var level by meta.mutableIntValue(def = 1)
}

class KListMarkup(parent: Markup?, meta: MetaBuilder = MetaBuilder("list")) : KMarkup(parent, meta) {
    init {
        type = Markup.LIST_TYPE
    }

    var level by meta.mutableIntValue()
    var bullet by meta.mutableStringValue(def = "*")

    fun item(action: KMarkup.() -> Unit) {
        addContent(KMarkup(parent = this).apply(action))
    }
}

class KTableMarkup(parent: Markup?, meta: MetaBuilder = MetaBuilder("table")) : KMarkup(parent, meta) {
    init {
        type = Markup.TABLE_TYPE
    }


    fun row(action: KMarkup.() -> Unit) {
        addContent(KMarkup(this).apply(action))
    }

}

fun markup(dsl: KMarkup.() -> Unit): Markup {
    return KMarkup().apply(dsl).build()
}