package hep.dataforge.io.markup

import hep.dataforge.description.NodeDef
import hep.dataforge.description.NodeDefs
import hep.dataforge.description.ValueDef
import hep.dataforge.io.markup.Markup.Companion.MARKUP_STYLE_NODE
import hep.dataforge.io.markup.Markup.Companion.MARKUP_TYPE_KEY
import hep.dataforge.kodex.buildMeta
import hep.dataforge.kodex.node
import hep.dataforge.kodex.stringValue
import hep.dataforge.meta.*
import hep.dataforge.meta.MetaNode.DEFAULT_META_NAME
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
abstract class Markup(val parent: Markup? = null) : MetaMorph, Metoid, ValueProvider {

    val type: String by stringValue(MARKUP_TYPE_KEY)

    /**
     * The style declared in this specific node
     */
    protected val selfStyle: Meta by node(MARKUP_STYLE_NODE)

    val content: List<Markup>
        get() = meta.getMetaList(MARKUP_CONTENT_NODE).map { morph(it, this) }


    /**
     * A combination of style declared in thins node and parent style
     * @return
     */
    val style: Laminate
        get() {
            return parent?.style?.withFirstLayer(selfStyle) ?: Laminate(selfStyle)
        }

    override fun toMeta(): Meta {
        return meta
    }

    override fun optValue(path: String): Optional<Value> {
        return meta.optValue(path)
    }

    companion object : MorphProvider<Markup> {
        /**
         * A generic container type.
         */
        const val MARKUP_GROUP_TYPE = "group"
        const val MARKUP_STYLE_NODE = "style"
        const val MARKUP_CONTENT_NODE = "content"
        const val MARKUP_TYPE_KEY = "type"

        override fun morph(meta: Meta): Markup {
            return morph(meta, null)
        }

        fun morph(meta: Meta, parent: Markup? = null): Markup {
            return GenericMarkup(meta, parent)
        }
    }
}

class GenericMarkup(private val _meta: Meta, parent: Markup? = null) : Markup(parent) {
    
    constructor(type: String, style: Meta, parent: Markup? = null, vararg content: Markup) : this(
            buildMeta("markup") {
                MARKUP_TYPE_KEY to type
                if (!style.isEmpty) putNode(MARKUP_STYLE_NODE, style)
                content.forEach {
                    putNode(MARKUP_CONTENT_NODE, it.toMeta())
                }
            }, parent
    )

    override fun getMeta(): Meta {
        return _meta
    }
}

//class Markup(private val _meta: Meta, val parent: Markup? = null) : Metoid {
//
//    override fun getMeta(): Meta {
//        return _meta
//    }
//
//    /**
//     * Get type of this block. If type is not defined, use group type.
//     *
//     * @return
//     */
//    val type: String by stringValue(MARKUP_TYPE_KEY)
//
//    /**
//     * Get the parent element for this one. If null, then this is a root element
//     *
//     * @return
//     */
//    //TODO add caching to avoid reconstruction of the tree each time this method is called
//
//    /**
//     * Style ignores values outside `style` node
//     * @return
//     */
//    val style: Laminate
//        get() {
//            val laminate = Laminate(meta.getMetaOrEmpty(MARKUP_STYLE_NODE))
//
//            return if(parent == null){
//                laminate
//            } else{
//                laminate.withLayer(parent.style)
//            }
//        }
//
//    /**
//     * Stream of child nodes of this node in case it serves as a group
//     *
//     * @return
//     */
//    val content: Stream<Markup>
//        get() = meta.getMetaList(MARKUP_CONTENT_NODE).stream().map { Markup(it) }
//
//    /**
//     * Get type of this block. If type is not defined use externally inferred type.
//     *
//     * @return
//     */
//    fun getType(infer: Function<Markup, String>): String {
//        return meta.getString(MARKUP_TYPE_KEY) { infer.apply(this) }
//    }
//
//
//    override fun optValue(path: String): Optional<Value> {
//        return Optionals.either(meta.optValue(path)).or { style.optValue(path) }.opt()
//    }
//

//

//}
