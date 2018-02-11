package hep.dataforge.io.markup

import hep.dataforge.description.NodeDef
import hep.dataforge.description.NodeDefs
import hep.dataforge.description.ValueDef
import hep.dataforge.io.markup.Markup.Companion.MARKUP_CONTENT_NODE
import hep.dataforge.io.markup.Markup.Companion.MARKUP_STYLE_NODE
import hep.dataforge.io.markup.Markup.Companion.MARKUP_TYPE_KEY
import hep.dataforge.kodex.node
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaMorph
import hep.dataforge.meta.MetaNode.DEFAULT_META_NAME
import hep.dataforge.meta.MorphProvider
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import java.util.*


/**
 * Basic markup element wrapper. A markup element must have a type designation, fields specific for this markup element (like 'text' for text markup).
 * The structure of intrinsic meta could be as complex as needed as long as its upper layer nodes do not have name conflict with standard markup nodes like 'style'
 * 'style' element contains optional style information.
 * Created by darksnake on 30-Dec-16.
 */

@ValueDef(name = MARKUP_TYPE_KEY, def = Markup.MARKUP_GROUP_TYPE, info = "The type of this block")
@NodeDefs(
        NodeDef(name = DEFAULT_META_NAME, info = "Meta specific for this element"),
        NodeDef(name = MARKUP_STYLE_NODE, info = "Style override")
)
interface Markup : MetaMorph, ValueProvider {

    val parent: Markup?
    val type: String
    val content: List<Markup>
    /**
     * Private style of this markup
     */
    val style: Meta

    /**
     * Set of styles including all ancestors
     * //TODO better name
     */
    val styleStack: Laminate
        get() {
            return parent?.styleStack?.withFirstLayer(style) ?: Laminate(style)
        }

    companion object : MorphProvider<Markup> {
        /**
         * A generic container type.
         */
        const val MARKUP_GROUP_TYPE = "group"
        const val MARKUP_STYLE_NODE = "style"
        const val MARKUP_CONTENT_NODE = "content"
        const val MARKUP_TYPE_KEY = "type"

        const val TEXT_TYPE = "text"
        const val HEADER_TYPE = "head"
        const val LIST_TYPE = "list"
        const val TABLE_TYPE = "table"
        const val ROW_TYPE = "tr"

        override fun morph(meta: Meta): Markup {
            return morph(meta, null)
        }

        fun morph(meta: Meta, parent: Markup? = null): Markup {
            return GenericMarkup(meta, parent)
        }

        fun inferType(element: Markup): String {
            return if (element.hasValue("text")) {
                TEXT_TYPE
            } else {
                when (element.parent?.type) {
                    TABLE_TYPE -> ROW_TYPE
                    else -> throw RuntimeException("Can't infer markup element type")
                }
            }
        }
    }
}

class GenericMarkup(val meta: Meta, override val parent: Markup? = null) : Markup {

    override fun optValue(path: String): Optional<Value> {
        return meta.optValue(path)
    }

    override val type = meta.getString("type") { Markup.inferType(this) }

    override val content: List<Markup>
        get() = meta.getMetaList(MARKUP_CONTENT_NODE).map { Markup.morph(it, this) }

    override val style by meta.node()

    override fun toMeta(): Meta = meta
}

