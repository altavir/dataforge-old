package hep.dataforge.fx.output;

import hep.dataforge.io.markup.GenericMarkupRenderer;
import hep.dataforge.io.markup.Markup;

/**
 * An FX panel markup renderer
 * Created by darksnake on 19-Mar-17.
 */
public class FXMarkupRenderer extends GenericMarkupRenderer {
    private final FXOutputPane out;

    public FXMarkupRenderer(FXOutputPane out) {
        this.out = out;
    }


    @Override
    protected void renderText(String text, String color, Markup element) {
        out.appendColored(text, color);
    }

    @Override
    protected void listItem(int level, String bullet, Markup element) {
        out.newline();
        for (int i = 0; i < level; i++) {
           out.tab();
        }
        out.append(bullet);
        doRender(element);
    }

    @Override
    protected void tableRow(Markup element) {
        element.getContent().forEach(cell -> {
            doRender(cell);
            out.tab();
        });

        if (element.getBoolean("header", false)) {
            out.newline();
        }

        out.newline();
    }
}
