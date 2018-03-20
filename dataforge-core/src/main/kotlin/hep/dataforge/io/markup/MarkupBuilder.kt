package hep.dataforge.io.markup

import hep.dataforge.kodex.set
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Metoid
import hep.dataforge.utils.GenericBuilder
import java.util.stream.Stream


/**
 * Backward compatibility  markup builder for Java and groovy. For kotlin use {@link Markup}.
 * Created by darksnake on 03-Jan-17.
 */
class MarkupBuilder : GenericBuilder<Markup, MarkupBuilder>, Metoid {

    private val markup = MarkupGroup()

    override fun self(): MarkupBuilder {
        return this
    }

    override fun build(): Markup {
        return markup
    }

    override val meta: Meta = markup.toMeta()

    /**
     * Set the style of element
     *
     * @param style
     * @return
     */
    fun setStyle(style: Meta): MarkupBuilder {
        markup.style = style.builder
        return self()
    }

    fun setContent(content: Stream<Markup>): MarkupBuilder {
        content.forEach { markup.add(it) }
        return self()
    }

    fun setContent(vararg content: MarkupBuilder): MarkupBuilder {
        return setContent(Stream.of(*content).map { it.build() })
    }

    fun content(content: Meta): MarkupBuilder {
        markup.add(Markup.morph(content))
        return self()
    }

    fun content(content: MarkupBuilder): MarkupBuilder {
        markup.add(content.markup)
        return self()
    }

    fun content(content: Markup): MarkupBuilder {
        markup.add(content)
        return self()
    }

    /**
     * Add text content
     *
     * @param text
     * @return
     */
    fun text(text: String): MarkupBuilder {
        markup.text(text)
        return self()
    }

    /**
     * Add clored text content
     *
     * @param text
     * @param color
     * @return
     */
    fun text(text: String, color: String): MarkupBuilder {
        markup.text(text) { this.color = color }
        return self()
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
        markup.text(text) { this.style["textWidth"] = width }
        return self()
    }

    /**
     * Add a list
     *
     * @param items
     * @return
     */
    fun list(vararg items: MarkupBuilder): MarkupBuilder {
        markup.list {
            items.forEach {
                add(it.markup)
            }
        }
        return self()
    }

    fun list(items: Collection<MarkupBuilder>): MarkupBuilder {
        return list(*items.toTypedArray())
    }


    fun table(vararg rows: MarkupBuilder): MarkupBuilder {
        markup.table {
            rows.forEach {row->
                row{
                    row.markup.content.forEach {
                        this.add(it)
                    }
                }
            }
        }
        return self()
    }

    fun header(text: String, level: Int): MarkupBuilder {
        markup.header(level) { text(text) }
        return self()
    }


}
