/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.output;

import hep.dataforge.fx.FXObject;
import hep.dataforge.fx.FXUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.ReadOnlyStyledDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputPane for formatted data
 *
 * @author Alexander Nozik
 */
public class FXOutputPane implements FXObject {

    private static final int DEFAULT_TAB_STOP_SIZE = 15;

    private final AnchorPane root;
    private final InlineCssTextArea textArea = new InlineCssTextArea();

    private final IntegerProperty maxLinesProperty = new SimpleIntegerProperty(-1);

    /**
     * Tab stop positions
     */
    private ObservableList<Integer> tabstops;

    /**
     * current tab stop
     */
    private int currentTab = 0;

    public FXOutputPane() {
//        textArea = new InlineCssTextArea();
        textArea.setEditable(false);
//        textArea.setWrapText(true);
        root = new AnchorPane(textArea);
        AnchorPane.setBottomAnchor(textArea, 5d);
        AnchorPane.setTopAnchor(textArea, 5d);
        AnchorPane.setLeftAnchor(textArea, 5d);
        AnchorPane.setRightAnchor(textArea, 5d);
    }

    public void setWrapText(boolean wrapText) {
        textArea.setWrapText(wrapText);
    }

    public BooleanProperty wrapTextProperty() {
        return textArea.wrapTextProperty();
    }

    /**
     * The root Anchor pane
     *
     * @return
     */
    public AnchorPane getRoot() {
        return root;
    }

    public IntegerProperty maxLinesProperty() {
        return maxLinesProperty;
    }

    public void setMaxLines(int maxLines) {
        this.maxLinesProperty.set(maxLines);
    }

    /**
     * Append a text using given css style. Automatically detect newlines and tabs
     * @param text
     * @param style
     */
    private synchronized void append(String text, String style) {
        // Unifying newlines
        String t = text.replace("\r\n", "\n");

        FXUtils.runNow(() -> {
            if (t.contains("\n")) {
                String[] lines = t.split("\n");
                for (int i = 0; i < lines.length - 1; i++) {
                    append(lines[i].trim(), style);
                    newline();
                }
                append(lines[lines.length - 1], style);
                if (t.endsWith("\n")) {
                    newline();
                }
            } else if (t.contains("\t")) {
                String[] tabs = t.split("\t");
                for (int i = 0; i < tabs.length - 1; i++) {
                    append(tabs[i], style);
                    tab();
                }
                if(tabs.length>0) {
                    append(tabs[tabs.length - 1], style);
                }
            } else if (style.isEmpty()) {
                textArea.appendText(t);
            } else {
                textArea.append(ReadOnlyStyledDocument.fromString(t, style));
            }
        });
    }

    /**
     * Append tabulation
     */
    public synchronized void tab() {
        FXUtils.runNow(() -> {
            currentTab++;
//        textArea.appendText("\t");
            for (int i = 0; i < getTabSize(); i++) {
                textArea.appendText(" ");
            }
        });
    }

    private int countLines() {
        return (int) textArea.getText().chars().filter((int value) -> value == '\n').count();
    }

    /**
     * Append newLine
     */
    public synchronized void newline() {
        FXUtils.runNow(() -> {
            while (maxLinesProperty.get() > 0 && countLines() >= maxLinesProperty.get()) {
                //FIXME bad way to count and remove lines
                textArea.replaceText(0, textArea.getText().indexOf("\n") + 1, "");
            }
            currentTab = 0;
            textArea.appendText("\r\n");

        });
    }

    private int getTabSize() {
        return Math.max(getTabStop(currentTab) - textArea.getCaretColumn(), 2);
    }

    private int getTabStop(int num) {
        if (tabstops == null) {
            return num * DEFAULT_TAB_STOP_SIZE;
        } else if (tabstops.size() < num) {
            return tabstops.get(tabstops.size() - 1) + (num - tabstops.size()) * DEFAULT_TAB_STOP_SIZE;
        } else {
            return tabstops.get(num);
        }
    }

    public void append(String text) {
        append(text, "");
    }

    public void appendColored(String text, String color) {
        append(text, "-fx-fill: " + color + ";");
    }

    public void appendLine(String line) {
        append(line.trim(), "");
        newline();
    }

    public void appendStyled(String text, String style) {
        append(text, style);
    }

    public boolean isEmpty() {
        return textArea.getText().isEmpty();
    }

    public void clear() {
        FXUtils.runNow(textArea::clear);
    }

    public OutputStream getStream() {
        return new ByteArrayOutputStream(1024) {
            @Override
            public synchronized void flush() throws IOException {
                String text = toString();
                if (text.length() == 0) {
                    return;
                }
                append(text);
                reset();
            }
        };
    }

    @Override
    public Node getFXNode() {
        return root;
    }
}
