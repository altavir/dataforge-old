package hep.dataforge.grind.terminal

import hep.dataforge.io.text.Markup
import hep.dataforge.io.text.StreamMarkupRenderer
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
    protected synchronized void renderText(String text, String color, Markup element) {
        AttributedStyle style;
        if (element.getStyle().getBoolean("bold", false)) {
            style = AttributedStyle.BOLD;
        } else {
            style = AttributedStyle.DEFAULT;
        }

        if (element.getStyle().getBoolean("italic", false)) {
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
