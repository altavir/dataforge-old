package hep.dataforge.markup.markup

/**
 * A renderer that renders to string
 */
class StringMarkupRenderer : StreamMarkupRenderer() {
    private var builder = StringBuilder()


    @Synchronized override fun printText(string: String) {
        builder.append(string)
    }

    @Synchronized override fun ln() {
        builder.append("\n")
    }

    override fun toString(): String {
        return builder.toString()
    }

    @Synchronized
    fun reset() {
        this.builder = StringBuilder()
    }
}
