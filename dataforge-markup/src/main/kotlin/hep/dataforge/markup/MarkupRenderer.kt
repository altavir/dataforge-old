package hep.dataforge.markup.markup

/**
 * Created by darksnake on 30-Dec-16.
 */
interface MarkupRenderer {
    fun render(mark: Markup)

    fun render(builder: MarkupBuilder) {
        render(builder.build())
    }

    fun render(action: MarkupBuilder.() -> Unit) {
        val builder = MarkupBuilder()
        builder.action()
        render(builder)
    }
}
