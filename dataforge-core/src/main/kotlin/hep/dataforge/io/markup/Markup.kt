package hep.dataforge.io.markup

import hep.dataforge.description.Described
import hep.dataforge.description.NodeDef
import hep.dataforge.description.NodeDefs
import hep.dataforge.description.ValueDef
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaNode.DEFAULT_META_NAME
import hep.dataforge.meta.SimpleConfigurable
import hep.dataforge.utils.Optionals
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

/**
 * Basic markup element wrapper. A markup element must have a type designation, fields specific for this markup element (like 'text' for text markup).
 * The structure of intrinsic meta could be as complex as needed as long as its upper layer nodes do not have name conflict with standard markup nodes like 'style'
 * 'style' element contains optional style information.
 * Created by darksnake on 30-Dec-16.
 */
@ValueDef(name = "type", info = "The type of this block")
@NodeDefs(
        NodeDef(name = DEFAULT_META_NAME, info = "Meta specific for this element"),
        NodeDef(name = "style", info = "Style override")
)
class Markup : SimpleConfigurable, Described, ValueProvider {

    /**
     * Get type of this block. If type is not defined, use group type.
     *
     * @return
     */
    val type: String
        get() = meta.getString(MARKUP_TYPE_KEY, MARKUP_GROUP_TYPE)

    /**
     * Get the parent element for this one. If null, then this is a root element
     *
     * @return
     */
    //TODO add caching to avoid reconstruction of the tree each time this method is called
    val parent: Markup?
        get() = config.parent?.let { Markup(it) }

    /**
     * Style ignores values outside `style` node
     * @return
     */
    val style: Laminate
        get() {
            var laminate = Laminate(meta
                    .getMetaOrEmpty(MARKUP_STYLE_NODE))
                    .withDescriptor(descriptor)

            val parent = parent

            if (parent != null) {
                laminate = laminate.withLayer(parent.style)
            }
            return laminate
        }

    /**
     * Stream of child nodes of this node in case it serves as a group
     *
     * @return
     */
    val content: Stream<Markup>
        get() = config.getMetaList(MARKUP_CONTENT_NODE).stream().map { Markup(it) }

    constructor(c: Configuration) : super(c) {}

    constructor(meta: Meta) : super(Configuration(meta)) {}

    /**
     * Get type of this block. If type is not defined use externally inferred type.
     *
     * @return
     */
    fun getType(infer: Function<Markup, String>): String {
        return meta.getString(MARKUP_TYPE_KEY) { infer.apply(this) }
    }

    override fun applyConfig(config: Meta) {
        //do nothing
    }

    override fun optValue(path: String): Optional<Value> {
        return Optionals.either(meta.optValue(path)).or { style.optValue(path) }.opt()
    }

    override fun toString(): String {
        val r = StringMarkupRenderer()
        r.render(this)
        return r.toString()
    }

    companion object {
        /**
         * A generic container type.
         */
        val MARKUP_GROUP_TYPE = "group"
        val MARKUP_STYLE_NODE = "style"
        val MARKUP_CONTENT_NODE = "c"
        val MARKUP_TYPE_KEY = "type"
    }
}
