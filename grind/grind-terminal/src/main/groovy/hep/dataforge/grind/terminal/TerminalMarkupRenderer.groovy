package hep.dataforge.grind.terminal

import hep.dataforge.io.markup.Markup
import hep.dataforge.io.markup.StreamMarkupRenderer
import hep.dataforge.meta.Meta
import org.jline.terminal.Terminal
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStyle

/**
 * Markup rendered for terminal output
 * Created by darksnake on 04-Jan-17.
 */
class TerminalMarkupRenderer extends StreamMarkupRenderer {
    Terminal terminal;
    boolean forceANSI = false;

    TerminalMarkupRenderer(Terminal terminal) {
        this.terminal = terminal
    }

    @Override
    protected synchronized void text(String text, String color = null, Markup element = new Markup(Meta.empty())) {
        AttributedStyle style;
        if (element.getBoolean("bold", false)) {
            style = AttributedStyle.BOLD;
        } else {
            style = AttributedStyle.DEFAULT;
        }

        if (element.getBoolean("italic", false)) {
            style = style.italic();
        }

        switch (color) {
            case "red":
                style = style.foreground(AttributedStyle.RED);
                break;
            case "green":
                style = style.foreground(AttributedStyle.GREEN);
                break;
            case "yellow":
                style = style.foreground(AttributedStyle.YELLOW);
                break;
            case "blue":
                style = style.foreground(AttributedStyle.BLUE);
                break;
            case "magenta":
                style = style.foreground(AttributedStyle.MAGENTA);
                break;
            case "cyan":
                style = style.foreground(AttributedStyle.CYAN);
                break;
        }
        if (forceANSI) {
            printText(new AttributedString(format(text, element), style).toAnsi());
        } else {
            printText(new AttributedString(format(text, element), style).toAnsi(terminal));
        }
    }

    @Override
    protected void printText(String string) {
        terminal.print(string);
    }

    @Override
    protected void ln() {
        terminal.println()
        terminal.flush()
    }
}
