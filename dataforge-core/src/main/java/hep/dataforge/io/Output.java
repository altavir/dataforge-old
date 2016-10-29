package hep.dataforge.io;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A generalized formatted output
 * Created by darksnake on 28-Oct-16.
 */
public interface Output {

    /**
     * Print text and add newline
     *
     * @param text
     */
    default void println(String text) {
        print(text);
        newline();
    }

    /**
     * add nenwline
     */
    void newline();

    /**
     * print text
     *
     * @param text
     */
    void print(String text);

    /**
     * Print text with given attributes
     *
     * @param text
     * @param attributes
     */
    default void print(Map<String, String> attributes, String text) {
        wrap(attributes, "text", (output) -> output.print(text));
    }

    /**
     * Print text in columns with automatic tab sizes
     *
     * @param columns
     */
    void tabs(String... columns);

    /**
     * Print text in columns with given attributes
     *
     * @param columns
     */
    default void tabs(Map<String, String> attributes, String... columns) {
        wrap(attributes, "row", (output) -> output.tabs(columns));
    }

    /**
     * Wrap everything inside body closure into some environment using given attributes
     *
     * @param attributes attributes
     * @param env        the type of the environment
     * @param body       content closure
     */
    void wrap(Map<String, String> attributes, String env, Consumer<Output> body);

    /**
     * Print a header
     *
     * @param level
     * @param text
     */
    void header(int level, String text);

    default void header(int level, Map<String, String> attributes, String text) {
        wrap(attributes, "header", (output) -> output.header(level, text));
    }
}
