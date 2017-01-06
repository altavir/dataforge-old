package hep.dataforge.io.text;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A simple renderer using basic PrintStream without color support
 * Created by darksnake on 03-Jan-17.
 */
public class SimpleMarkupRenderer extends StreamMarkupRenderer {
    private final PrintStream stream;

    public SimpleMarkupRenderer() {
        this.stream = System.out;
    }

    public SimpleMarkupRenderer(PrintStream stream) {
        this.stream = stream;
    }

    public SimpleMarkupRenderer(OutputStream stream) {
        this.stream = new PrintStream(stream);
    }

    @Override
    protected void printText(String string) {
        stream.print(string);
    }

    @Override
    protected void ln() {
        stream.println();
        stream.flush();
    }
}
