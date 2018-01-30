package hep.dataforge.io.markup

import java.io.OutputStream
import java.io.PrintStream

/**
 * A simple renderer using basic PrintStream without color support
 * Created by darksnake on 03-Jan-17.
 */
class SimpleMarkupRenderer(private val stream: PrintStream = System.out) : StreamMarkupRenderer() {

    constructor(stream: OutputStream) : this(PrintStream(stream))

    override fun printText(string: String) {
        stream.print(string)
    }

    override fun ln() {
        stream.println()
        stream.flush()
    }
}
