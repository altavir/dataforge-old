package hep.dataforge.grind

import hep.dataforge.io.Output
import jline.console.ConsoleReader

import java.util.function.Consumer

/**
 * Created by darksnake on 28-Oct-16.
 */
class ConsoleOutput implements Output {
    ConsoleReader console;

    @Override
    void newline() {
        console.println()
    }

    @Override
    void print(String text) {
        console.print(text)
    }

    @Override
    void tabs(String... columns) {
        console.printColumns(Arrays.asList(columns))
    }

    @Override
    void wrap(Map<String, String> attributes, String env, Consumer<Output> body) {

    }

    @Override
    void header(int level, String text) {

    }
}
