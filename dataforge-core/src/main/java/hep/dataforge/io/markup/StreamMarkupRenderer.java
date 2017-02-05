package hep.dataforge.io.markup;

import hep.dataforge.io.IOUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by darksnake on 05-Jan-17.
 */
public abstract class StreamMarkupRenderer extends GenericMarkupRenderer {

    boolean lineStack = false;

    protected abstract void printText(String string);

    protected final void print(@NotNull String string) {
        if (!string.isEmpty()) {
            lineStack = false;
        }
        printText(string);
    }

    /**
     * New line ignoring stacking
     */
    protected abstract void ln();

    /**
     * New line with ability to stack together
     *
     * @param stack
     */
    protected final void ln(boolean stack) {
        if (stack) {
            if (!lineStack) {
                ln();
                lineStack = true;
            }
        } else {
            ln();
            lineStack = false;
        }
    }

    /**
     * Pre-format text using element meta
     *
     * @param string
     * @param element
     * @return
     */
    protected String format(String string, Markup element) {
        if (element.hasValue("textWidth")) {
            return IOUtils.formatWidth(string, element.getInt("textWidth"));
        } else {
            return string;
        }
    }

    @Override
    protected synchronized void renderText(String text, String color, Markup element) {
        print(format(text, element));
    }

    @Override
    protected void list(Markup element) {
        super.list(element);
        ln(true);
    }

    @Override
    protected void listItem(int level, String bullet, Markup element) {
        ln(true);
        for (int i = 0; i < level; i++) {
            print("\t");
        }
        print(bullet);
        doRender(element);
    }

    @Override
    protected void tableRow(Markup element) {
        element.getContent().forEach(cell -> {
            doRender(cell);
            print("\t");
        });

        if (element.getBoolean("header", false)) {
            ln(false);
        }

        ln(true);
    }
}
