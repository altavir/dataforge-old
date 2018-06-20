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

package hep.dataforge.markup.markup

import hep.dataforge.kodex.buildMeta
import hep.dataforge.kodex.childNodes
import hep.dataforge.kodex.mutableStringValue
import hep.dataforge.kodex.nullable
import hep.dataforge.meta.*
import kotlin.reflect.KClass

@DslMarker
annotation class MarkupDSL

/**
 * User level DSL to build markup
 */
@MarkupDSL
sealed class Markup : MetaMorph {

    var parent: Markup? = null
        set(value) {
            if (value == this) {
                throw RuntimeException(" cyclic reference")
            } else {
                field = value
            }
        }

    abstract val type: String


    var style: MetaBuilder = MetaBuilder(Markup.MARKUP_STYLE_NODE)

    /**
     * Return index of column if parent is row. Otherwise return null
     */
    val columnNumber: Int?
        get() = (parent as? RowMarkup)?.content?.indexOf(this)

    /**
     * Get the row number of this row inside parent table or list item inside parent list.
     * Return null if no parent is assigned or parent is not a table or list
     */
    val index: Int?
        get() = parent?.let {
            when (it) {
                is TableMarkup -> it.rows.indexOf(this)
                is ListMarkup -> it.content.indexOf(this)
                else -> null
            }
        }

    //TODO add private style which does included in the stack but overrides anything else

    /**
     * Set of styles including all ancestors
     * //TODO better name
     */
    val styleStack: Laminate
        get() {
            return parent?.styleStack?.withFirstLayer(style) ?: Laminate(style)
        }


    override fun toMeta(): MetaBuilder {
        return buildMeta(type) {
            if (!style.isEmpty) {
                setNode("style", style)
            }
        }
    }

//    fun ln() {
//        +"\n"
//    }

//    override fun optValue(path: String): Optional<Value> {
//        return styleStack.optValue(path)
//    }

    companion object : MorphProvider<Markup> {

        override fun morph(meta: Meta): Markup {
            return when (meta.name) {
                TEXT_TYPE -> TextMarkup.morph(meta)
                HEADER_TYPE -> HeaderMarkup.morph(meta)
                LIST_TYPE -> ListMarkup.morph(meta)
                TABLE_TYPE -> TableMarkup.morph(meta)
                ROW_TYPE -> RowMarkup.morph(meta)
                else -> throw RuntimeException("Unrecognized markup node: ${meta.name}")
            }
        }

        /**
         * A generic container type.
         */
        const val MARKUP_STYLE_NODE = "style"
        const val MARKUP_CONTENT_NODE = "content"

        const val GROUP_TYPE = "group"
        const val TEXT_TYPE = "text"
        const val HEADER_TYPE = "header"
        const val LIST_TYPE = "list"
        const val TABLE_TYPE = "table"
        const val ROW_TYPE = "row"

        /**
         * The mapping between tag names and tag classes
         */
        val TAG_MAP: MutableMap<String, KClass<out Markup>> = hashMapOf(
                GROUP_TYPE to MarkupGroup::class,
                TEXT_TYPE to TextMarkup::class,
                HEADER_TYPE to HeaderMarkup::class,
                LIST_TYPE to ListMarkup::class,
                TABLE_TYPE to TableMarkup::class,
                ROW_TYPE to RowMarkup::class
        )


    }

}

open class MarkupGroup : Markup() {

    override val type: String = Markup.GROUP_TYPE

    val content: MutableList<Markup> = ArrayList()

    /**
     * Add given markup as a child
     */
    fun add(markup: Markup) {
        this.content.add(markup.also { it.parent = this })
    }

    /**
     * Add a plain text child
     */
    operator fun String.unaryPlus(): TextMarkup {
        return text(this)

    }

    override fun toMeta(): MetaBuilder {
        return buildMeta(type) {
            if (!style.isEmpty) {
                setNode("style", style)
            }
            content.forEach {
                putNode(it.toMeta())
            }
        }
    }

    fun item(op: MarkupGroup.() -> Unit): MarkupGroup {
        return MarkupGroup().apply(op).also { add(it) }
    }

    fun text(text: String = "", color: String = "", action: TextMarkup.() -> Unit = {}): TextMarkup {
        return TextMarkup().apply {
            this.text = text
            this.color = color;
        }.apply(action).also { add(it) }
    }

    fun header(level: Int = 1, op: HeaderMarkup.() -> Unit): HeaderMarkup {
        return HeaderMarkup()
                .apply {
                    this.level = level
                }.apply(op)
                .also {
                    add(it)
                }
    }

    @JvmOverloads
    fun list(level: Int? = null, bullet: String? = null, action: ListMarkup.() -> Unit): ListMarkup {
        return ListMarkup()
                .apply {
                    level?.let { this.level = it }
                    bullet?.let { this.bullet = it }
                }.apply(action)
                .also {
                    add(it)
                }
    }

    fun table(op: TableMarkup.() -> Unit): TableMarkup {
        return TableMarkup().apply(op).also {
            add(it)
        }
    }

    /**
     * Apply the meta to create
     */
    protected fun applyMeta(meta: Meta) {
        style = meta.getMetaOrEmpty(Markup.MARKUP_STYLE_NODE).builder
        meta.childNodes.forEach {
            if (it.name != Markup.MARKUP_STYLE_NODE) {
                content.add(MetaMorph.morph(TAG_MAP[it.name]!!, it).apply { parent = this })
            }
        }
    }

    companion object : MorphProvider<MarkupGroup> {
        override fun morph(meta: Meta): MarkupGroup {
            return MarkupGroup().apply { applyMeta(meta) }
        }
    }
}

class TextMarkup() : Markup() {

    var text: String = ""
    var color: String?
        get() = styleStack.optString("color").nullable
        set(value) {
            style.setValue("color", value)
        }

    override val type = Markup.TEXT_TYPE

    //var textWidth by meta.mutableIntValue(def = -1)

    override fun toMeta(): MetaBuilder {
        return super.toMeta().setValue("text", text)
    }

    companion object : MorphProvider<TextMarkup> {

        override fun morph(meta: Meta): TextMarkup {
            return TextMarkup().apply {
                text = meta.getString("text", "")
                style = meta.getMetaOrEmpty(Markup.MARKUP_STYLE_NODE).builder
            }
        }

        fun create(text: String): TextMarkup {
            return TextMarkup().apply {
                this.text = text
            }
        }
    }
}

class HeaderMarkup() : MarkupGroup() {
    override val type = Markup.HEADER_TYPE

    var level: Int
        get() {
            return if (style.hasValue("header.level")) {
                return style.getInt("header.level")
            } else {
                styleStack.layers().find { it.hasValue("header.level") }?.getInt("header.level") ?: 0+1
            }
        }
        set(value) {
            this.style.setValue("header.level", value)
        }

    companion object : MorphProvider<HeaderMarkup> {
        override fun morph(meta: Meta): HeaderMarkup {
            return HeaderMarkup().apply { applyMeta(meta) }
        }

    }
}

class ListMarkup() : MarkupGroup() {

    override val type = Markup.LIST_TYPE

    var level: Int
        get() {
            return if (style.hasValue("list.level")) {
                style.getInt("list.level")
            } else {
                (generateSequence<Markup>(parent) { it.parent }
                        .filterIsInstance(ListMarkup::class.java)
                        .firstOrNull()?.level ?: 0) + 1
            }
        }
        set(value) {
            this.style.setValue("list.level", value)
        }

    var bullet by style.mutableStringValue(valueName = "list.bullet",def = "-")


    companion object : MorphProvider<ListMarkup> {
        override fun morph(meta: Meta): ListMarkup {
            return ListMarkup().apply { applyMeta(meta) }
        }

    }
}

class TableMarkup : Markup() {

    override val type = Markup.TABLE_TYPE

    var header: RowMarkup? = null
        set(value) {
            field = value?.also { it.parent = this }
        }

    val rows: MutableList<RowMarkup> = ArrayList()

    fun header(action: RowMarkup.() -> Unit) {
        header = RowMarkup(this).apply(action)
    }

    fun addRow(row: RowMarkup) {
        rows.add(row.also { it.parent = this })
    }

    fun row(action: RowMarkup.() -> Unit) {
        addRow(RowMarkup(this).apply(action))
    }

    companion object : MorphProvider<TableMarkup> {
        override fun morph(meta: Meta): TableMarkup {
            return TableMarkup().apply {
                style = meta.getMetaOrEmpty(Markup.MARKUP_STYLE_NODE).builder
                meta.getMetaList(Markup.ROW_TYPE).forEach {
                    row { content.add(RowMarkup.morph(it).apply { parent = this }) }
                }
            }
        }

    }
}

//TODO remove and replace by group?
class RowMarkup(parent: TableMarkup?) : MarkupGroup() {

    init {
        this.parent = parent
    }

    override val type = Markup.ROW_TYPE


    companion object : MorphProvider<RowMarkup> {
        override fun morph(meta: Meta): RowMarkup {
            return RowMarkup(null).apply { applyMeta(meta) }
        }

    }
}

fun markup(dsl: MarkupGroup.() -> Unit): Markup {
    return MarkupGroup().apply(dsl)
}