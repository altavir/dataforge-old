/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.ReadOnlyStyledDocument;

/**
 * OutputPane for formatted data
 *
 * @author Alexander Nozik
 */
public class FXDataOutputPane extends AnchorPane {

    private static final int DEFAULT_TAB_STOP_SIZE = 12;
    private static final int SYMBOL_WIDTH = 8;

    private final InlineCssTextArea textArea;

    /**
     * Tab stop positions
     */
    private List<Integer> tabstops;

    /**
     * current tab stop
     */
    private int currentTab = 0;

    public FXDataOutputPane() {
        textArea = new InlineCssTextArea();
        textArea.setEditable(false);

        super.getChildren().add(textArea);
        AnchorPane.setBottomAnchor(textArea, 5d);
        AnchorPane.setTopAnchor(textArea, 5d);
        AnchorPane.setLeftAnchor(textArea, 5d);
        AnchorPane.setRightAnchor(textArea, 5d);
    }

    private synchronized void append(String text, String style) {
        if (text.contains("\n")) {
            String[] lines = text.split("\n");
            for (int i = 0; i < lines.length - 1; i++) {
                append(lines[i].trim(), style);
                newline();
            }
            append(lines[lines.length - 1], style);
        } else if (text.contains("\t")) {
            String[] tabs = text.split("\t");
            for (int i = 0; i < tabs.length - 1; i++) {
                append(tabs[i], style);
                tab();
            }
            append(tabs[tabs.length - 1], style);
        } else if (style.isEmpty()) {
            textArea.appendText(text);
        } else {
            textArea.append(ReadOnlyStyledDocument.fromString(text, style));
        }
    }

    private synchronized void tab() {
        currentTab++;
        textArea.appendText("\t");
//        double scale = getTabSize() / 4d;
//        textArea.append(ReadOnlyStyledDocument.fromString("\t", "-fx-scale-x: " + scale));
//        for (int i = 0; i < getTabSize(); i++) {
//            textArea.appendText(" ");
//        }
        //textArea.append(ReadOnlyStyledDocument.fromString("\t", "-fx-min-width: " + getTabWith()));
    }

    private synchronized void newline() {
        currentTab = 0;
        textArea.appendText("\n");
    }

    private int getTabWith() {
        return getTabSize() * SYMBOL_WIDTH;
    }

    private int getTabSize() {
        return Math.max(getTabStop(currentTab) - textArea.getCaretPosition(), 2);
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

    public synchronized void appendLine(String line) {
        Platform.runLater(() -> {
            append(line.trim(), "");
            newline();
        });
    }

    public synchronized void appendStyled(String text, String style) {
        Platform.runLater(() -> {
            append(text, style);
        });
    }

    public boolean isEmpty() {
        return textArea.getText().isEmpty();
    }

    public void clear() {
        Platform.runLater(() -> textArea.clear());
    }

    public OutputStream getOutputStream(OutputStream forward) {
        return new ByteArrayOutputStream() {

            private final StringBuilder buffer = new StringBuilder(80);
            private final String EOL = "\n";
            private final PrintStream printStream = forward == null ? null : new PrintStream(forward);

            @Override
            public synchronized void flush() throws IOException {
                String text = toString();
                if (text.length() == 0) {
                    return;
                }
                append(text);
                reset();
            }

            private synchronized void append(String text) {
                if (isEmpty()) {
                    buffer.setLength(0);
                }
                if (EOL.equals(text)) {
                    buffer.append(text);
                } else {
                    buffer.append(text);
                    clearBuffer();
                }
            }

            private synchronized void clearBuffer() {
                String line = buffer.toString();
                appendLine(line);

                if (printStream != null) {
                    printStream.print(line);
                }
                buffer.setLength(0);
            }

        };
    }

}
