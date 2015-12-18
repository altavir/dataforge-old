/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import java.awt.BorderLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * OutputPane for formatted data
 *
 * @author Alexander Nozik
 */
public class DataOutputPane extends AnchorPane {

    private final JTextPane textPane;

    public DataOutputPane() {
        textPane = new JTextPane();
        textPane.setEditable(false);
        JPanel noWrapPanel = new JPanel(new BorderLayout());
        noWrapPanel.add(textPane);
        SwingNode node = new SwingNode();
        JScrollPane scroll = new JScrollPane(noWrapPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(32);
        node.setContent(scroll);
        getChildren().add(node);
        AnchorPane.setBottomAnchor(node, 5d);
        AnchorPane.setTopAnchor(node, 5d);
        AnchorPane.setLeftAnchor(node, 5d);
        AnchorPane.setRightAnchor(node, 5d);

        widthProperty().addListener(
                (ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
                    noWrapPanel.repaint();
                });

        heightProperty().addListener(
                (ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) -> {
                    noWrapPanel.repaint();
                });

    }

    public synchronized void appendLine(String line) {
        Platform.runLater(() -> {
            Document document = textPane.getDocument();
            try {
                document.insertString(document.getLength(), line.trim() + "\n", null);
            } catch (BadLocationException ex) {
                Logger.getLogger(DataOutputPane.class.getName()).log(Level.SEVERE, null, ex);
            }
            textPane.validate();
//            textPane.repaint();
        });
    }

    public boolean isEmpty() {
        return textPane.getText().isEmpty();
    }

    public void clear() {
        Platform.runLater(() -> textPane.setText(""));
    }

    public OutputStream getOutputStream(OutputStream forward) {
        return new ByteArrayOutputStream() {

            private final StringBuilder buffer = new StringBuilder(80);
            private final String EOL = "\n";
            private final PrintStream printStream = forward == null ? null : new PrintStream(forward);

            @Override
            public void flush() throws IOException {
                String text = toString();
                if (text.length() == 0) {
                    return;
                }
                append(text);
                reset();
            }

            private void append(String text) {
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

            private void clearBuffer() {
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
