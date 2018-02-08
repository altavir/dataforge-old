package hep.dataforge.io.markup

import hep.dataforge.kodex.toList
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.Metoid
import hep.dataforge.utils.GenericBuilder
import java.util.stream.Stream


/**
 * Created by darksnake on 03-Jan-17.
 */
@Deprecated("to be replaced by KMarkup")
class MarkupBuilder : GenericBuilder<Markup, MarkupBuilder>, Metoid {

    private val builder = MetaBuilder("markup")

    override fun self(): MarkupBuilder {
        return this
    }

    override fun build(): Markup {
        return Markup.morph(builder)
    }

    override fun getMeta(): Meta {
        return builder.build()
    }

    fun update(config: Meta): MarkupBuilder {
        builder.update(config)
        return self()
    }

    /**
     * Directly update markup fields
     *
     * @param map
     * @return
     */
    fun update(map: Map<String, Any>): MarkupBuilder {
        builder.update(map)
        return self()
    }

    /**
     * Directly update markup fields
     *
     * @param key
     * @param value
     * @return
     */
    fun setValue(key: String, value: Any): MarkupBuilder {
        builder.setValue(key, value)
        return self()
    }

    /**
     * Set the type of the element
     *
     * @param type
     * @return
     */
    fun setType(type: String): MarkupBuilder {
        builder.setValue(Markup.MARKUP_TYPE_KEY, type)
        return self()
    }

    /**
     * Set the style of element
     *
     * @param style
     * @return
     */
    fun setStyle(style: Meta): MarkupBuilder {
        builder.setNode(Markup.MARKUP_STYLE_NODE, style)
        return self()
    }

    //TODO apply style

    /**
     * Add content nodes to this markup
     *
     * @param content
     * @return
     */
    fun setContent(vararg content: Meta): MarkupBuilder {
        builder.setNode(Markup.MARKUP_CONTENT_NODE, *content)
        return self()
    }

    fun setContent(content: Stream<MarkupBuilder>): MarkupBuilder {
        builder.setNode(Markup.MARKUP_CONTENT_NODE, content.map<Meta>{ it.meta }.toList())
        return self()
    }

    fun setContent(vararg content: MarkupBuilder): MarkupBuilder {
        return setContent(Stream.of(*content))
    }

    fun content(content: Meta): MarkupBuilder {
        builder.putNode(Markup.MARKUP_CONTENT_NODE, content)
        return self()
    }

    fun content(content: MarkupBuilder): MarkupBuilder {
        builder.putNode(Markup.MARKUP_CONTENT_NODE, content.meta)
        return self()
    }

    fun content(content: Markup): MarkupBuilder {
        builder.putNode(Markup.MARKUP_CONTENT_NODE, content.toMeta())
        return self()
    }

    /**
     * Add text content
     *
     * @param text
     * @return
     */
    fun text(text: String): MarkupBuilder {
        return content(MetaBuilder(Markup.MARKUP_CONTENT_NODE)
                .setValue("text", text)
        )
    }

    /**
     * Add clored text content
     *
     * @param text
     * @param color
     * @return
     */
    fun text(text: String, color: String): MarkupBuilder {
        return content(MetaBuilder(Markup.MARKUP_CONTENT_NODE)
                .setValue("text", text)
                .setValue("color", color)
        )
    }

    /**
     * Add a new line or a paragraph break
     *
     * @return
     */
    fun ln(): MarkupBuilder {
        return text("\n")
    }

    /**
     * Add a fixed width text
     *
     * @param text
     * @param width
     * @return
     */
    fun column(text: String, width: Int): MarkupBuilder {
        return content(MetaBuilder(Markup.MARKUP_CONTENT_NODE)
                .setValue("text", text)
                .setValue("textWidth", width)
        )
    }

    /**
     * Add a list
     *
     * @param items
     * @return
     */
    fun list(vararg items: MarkupBuilder): MarkupBuilder {
        return content(MarkupBuilder()
                .setType(Markup.LIST_TYPE)
                .setContent(*items)
        )
    }

    fun list(items: Collection<MarkupBuilder>): MarkupBuilder {
        return list(*items.toTypedArray())
    }


    fun table(vararg rows: MarkupBuilder): MarkupBuilder {
        return content(MarkupBuilder()
                .setType(Markup.TABLE_TYPE)
                .setContent(*rows)
        )
    }

    fun header(text: String, level: Int): MarkupBuilder {
        return content(MarkupBuilder().setType("header").setValue("level", level).setValue("text", text))
    }

    companion object {

        //    public static MarkupBuilder create(String text) {
        //        return new MarkupBuilder().text(text);
        //    }
        //
        //    public static MarkupBuilder create(String text, String color) {
        //        return new MarkupBuilder().text(text, color);
        //    }

        /**
         * Create list markup with given level and bullet
         *
         * @param level  ignored if not positive
         * @param bullet ignored if null
         * @return
         */
        fun list(level: Int, bullet: String?): MarkupBuilder {
            val res = MarkupBuilder().setType(Markup.LIST_TYPE)
            if (level > 0) {
                res.setValue("level", level)
            }

            if (bullet != null) {
                res.setValue("bullet", bullet)
            }
            return res
        }
    }

}
