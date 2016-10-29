package hep.dataforge.io;

import java.io.PrintWriter;
import java.util.Map;
import java.util.function.Consumer;

import static hep.dataforge.io.IOUtils.ANSI_RESET;

/**
 * Created by darksnake on 28-Oct-16.
 */
//TODO implement special features
public class TextOutput implements Output {
    // text color if supported
    public static final String COLOR_ATTRIBUTE = "color";
    // an offset before the text
    public static final String OFFSET_ATTRIBUTE = "offset";
    // a bullet before the text
    public static final String BULLET_ATTRIBUTE = "offset";


    private PrintWriter writer;
    private boolean useColor;
    private boolean allowANSI;
    private boolean lineStart = true;


    private String wrapANSI(String str, String ansiColor) {
        if (allowANSI) {
            return ansiColor + str + ANSI_RESET;
        } else {
            return str;
        }
    }

    private void spaces(int num) {
        for (int i = 0; i < num; i++) {
            print(" ");
        }
    }

    private void startLine(Map<String, String> attributes) {

    }

    @Override
    public synchronized void newline() {
        writer.println();
    }

    @Override
    public void print(String text) {

    }

    @Override
    public synchronized void print(Map<String, String> attributes, String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            startLine(attributes);
            writer.print(text);
            writer.println();
        }
        writer.print(text);
    }

    @Override
    public void tabs(String... columns) {
        println(String.join("\t", columns));
    }

    @Override
    public void tabs(Map<String, String> attributes, String... columns) {
        tabs(columns);
    }

    @Override
    public synchronized void wrap(Map<String, String> attributes, String env, Consumer<Output> body) {
        if (attributes.containsKey(COLOR_ATTRIBUTE)) {
            print(attributes.get(COLOR_ATTRIBUTE));
        }
        body.accept(this);
        if (attributes.containsKey(COLOR_ATTRIBUTE)) {
            print(ANSI_RESET);
        }

    }

    @Override
    public void header(int level, String text) {
        println(text);
    }
}
