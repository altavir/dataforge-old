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

import hep.dataforge.kodex.buildMeta
import hep.dataforge.kodex.mutableIntValue
import hep.dataforge.kodex.mutableStringValue
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.values.Value
import java.util.*
import kotlin.collections.ArrayList

@DslMarker
annotation class MarkupDSL

/**
 * User level DSL to build markup
 */
@MarkupDSL
open class KMarkup(override val parent: Markup? = null) : Markup {

    override val type: String = Markup.MARKUP_GROUP_TYPE

    override var style: MetaBuilder = MetaBuilder(Markup.MARKUP_STYLE_NODE)
    override var content: MutableList<Markup> = ArrayList()

    /**
     * Add given markup as a child
     */
    fun add(markup: Markup): Markup {
        return markup.also {
            content.add(it)
        }
    }

    /**
     * Add a plain text child
     */
    operator fun String.unaryPlus(): KTextMarkup {
        return text(this)

    }

    @JvmOverloads
    fun text(text: String = "", color: String = "", action: KTextMarkup.() -> Unit = {}): KTextMarkup {
        return KTextMarkup(parent = this).apply {
            this.text = text
            this.color = color;
        }.apply(action).also { add(it) }
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
                    add(it)
                }
    }

//    fun ln() {
//        +"\n"
//    }

    override fun toMeta(): MetaBuilder {
        return buildMeta("markup") {
            setNode("style", style)
            "type" to type
            content.forEach {
                putNode(Markup.MARKUP_CONTENT_NODE, it.toMeta())
            }
        }
    }

    override fun optValue(path: String): Optional<Value> {
        return styleStack.optValue(path)
    }

}

class KTextMarkup(parent: Markup?) : KMarkup(parent) {

    var text: String = ""
    var color by style.mutableStringValue()
    override val type = Markup.TEXT_TYPE

    //var textWidth by meta.mutableIntValue(def = -1)

    override fun toMeta(): MetaBuilder {
        return super.toMeta().setValue("text", text)
    }

    companion object {
        @JvmOverloads
        fun create(text: String, parent: Markup? = null): KTextMarkup {
            return KTextMarkup(parent).apply {
                this.text = text
            }
        }
    }
}

class KHeaderMarkup(parent: Markup?) : KMarkup(parent) {
    override val type = Markup.HEADER_TYPE
    var level by style.mutableIntValue(def = 1)
}

class KListMarkup(parent: Markup?) : KMarkup(parent) {

    override val type = Markup.LIST_TYPE
    var level by style.mutableIntValue()
    var bullet by style.mutableStringValue(def = "*")

    fun item(action: KMarkup.() -> Unit) {
        add(KMarkup(parent = this).apply(action))
    }
}

class KTableMarkup(parent: Markup?) : KMarkup(parent) {

    override val type = Markup.TABLE_TYPE
    fun row(action: KMarkup.() -> Unit) {
        add(KMarkup(this).apply(action))
    }

}

fun markup(dsl: KMarkup.() -> Unit): KMarkup {
    return KMarkup().apply(dsl)
}