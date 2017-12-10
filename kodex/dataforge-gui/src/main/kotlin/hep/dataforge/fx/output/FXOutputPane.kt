/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.output

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import javafx.scene.layout.AnchorPane
import org.fxmisc.richtext.InlineCssTextArea
import tornadofx.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * OutputPane for formatted data
 *
 * @author Alexander Nozik
 */
class FXOutputPane: Fragment() {

    /**
     * The root Anchor pane
     *
     * @return
     */
    override val root: AnchorPane
    private val textArea = InlineCssTextArea()

    private val maxLinesProperty = SimpleIntegerProperty(-1)

    /**
     * Tab stop positions
     */
    private val tabstops: ObservableList<Int>? = null

    /**
     * current tab stop
     */
    private var currentTab = 0

    private val tabSize: Int
        get() = Math.max(getTabStop(currentTab) - textArea.caretColumn, 2)

    val isEmpty: Boolean
        get() = textArea.text.isEmpty()

    val stream: OutputStream
        get() = object : ByteArrayOutputStream(1024) {
            @Synchronized
            @Throws(IOException::class)
            override fun flush() {
                val text = toString()
                if (text.length == 0) {
                    return
                }
                append(text)
                reset()
            }
        }

    init {
        //        textArea = new InlineCssTextArea();
        textArea.isEditable = false
        //        textArea.setWrapText(true);
        root = AnchorPane(textArea)
        AnchorPane.setBottomAnchor(textArea, 5.0)
        AnchorPane.setTopAnchor(textArea, 5.0)
        AnchorPane.setLeftAnchor(textArea, 5.0)
        AnchorPane.setRightAnchor(textArea, 5.0)
    }

    fun setWrapText(wrapText: Boolean) {
        textArea.isWrapText = wrapText
    }

    fun wrapTextProperty(): BooleanProperty {
        return textArea.wrapTextProperty()
    }

    fun maxLinesProperty(): IntegerProperty {
        return maxLinesProperty
    }

    fun setMaxLines(maxLines: Int) {
        this.maxLinesProperty.set(maxLines)
    }

    /**
     * Append a text using given css style. Automatically detect newlines and tabs
     * @param text
     * @param style
     */
    @Synchronized private fun append(str: String, style: String) {
        // Unifying newlines
        val text = str.replace("\r\n", "\n")

        runLater{
            if (text.contains("\n")) {
                val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (i in 0 until lines.size - 1) {
                    append(lines[i].trim { it <= ' ' }, style)
                    newline()
                }
                append(lines[lines.size - 1], style)
                if (text.endsWith("\n")) {
                    newline()
                }
            } else if (text.contains("\t")) {
                val tabs = text.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (i in 0 until tabs.size - 1) {
                    append(tabs[i], style)
                    tab()
                }
                if (tabs.isNotEmpty()) {
                    append(tabs[tabs.size - 1], style)
                }
            } else if (style.isEmpty()) {
                textArea.appendText(text)
            } else {
                textArea.appendText(text)//(ReadOnlyStyledDocument.fromString(t, style))
            }
        }
    }

    /**
     * Append tabulation
     */
    @Synchronized
    fun tab() {
        runLater {
            currentTab++
            //        textArea.appendText("\t");
            for (i in 0 until tabSize) {
                textArea.appendText(" ")
            }
        }
    }

    private fun countLines(): Int {
        return textArea.text.chars().filter { value: Int -> value == '\n'.toInt() }.count().toInt()
    }

    /**
     * Append newLine
     */
    @Synchronized
    fun newline() {
        runLater {
            while (maxLinesProperty.get() > 0 && countLines() >= maxLinesProperty.get()) {
                //FIXME bad way to count and remove lines
                textArea.replaceText(0, textArea.text.indexOf("\n") + 1, "")
            }
            currentTab = 0
            textArea.appendText("\r\n")

        }
    }

    private fun getTabStop(num: Int): Int {
        return if (tabstops == null) {
            num * DEFAULT_TAB_STOP_SIZE
        } else if (tabstops.size < num) {
            tabstops[tabstops.size - 1] + (num - tabstops.size) * DEFAULT_TAB_STOP_SIZE
        } else {
            tabstops[num]
        }
    }

    fun append(text: String) {
        append(text, "")
    }

    fun appendColored(text: String, color: String) {
        append(text, "-fx-fill: $color;")
    }

    fun appendLine(line: String) {
        append(line.trim { it <= ' ' }, "")
        newline()
    }

    fun appendStyled(text: String, style: String) {
        append(text, style)
    }

    fun clear() {
        runLater { textArea.clear() }
    }

    companion object {

        private val DEFAULT_TAB_STOP_SIZE = 15
    }
}
