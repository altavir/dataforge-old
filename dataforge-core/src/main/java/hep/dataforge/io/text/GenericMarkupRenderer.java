package hep.dataforge.io.text;

import static hep.dataforge.io.text.Markup.MARKUP_GROUP_TYPE;

/**
 * A basic renderer framework allowing to render basic markup elements: text, list and table
 * Created by darksnake on 03-Jan-17.
 */
public abstract class GenericMarkupRenderer implements MarkupRenderer {
    public static final String TEXT_TYPE = "text";
    public static final String LIST_TYPE = "list";
    public static final String TABLE_TYPE = "table";

    @Override
    public void render(Markup element) {
        switch (element.getType(this::inferType)) {
            case MARKUP_GROUP_TYPE: //render container
                element.getContent().forEach(it -> render(it));
                break;
            case TEXT_TYPE:
                text(element);
                break;
            case LIST_TYPE:
                list(element);
                break;
            case TABLE_TYPE:
                table(element);
                break;
            default:
                throw new RuntimeException("Unknown markup type: " + element.getType());
        }
    }

    protected String inferType(Markup element) {
        if (element.hasValue("text")) {
            return TEXT_TYPE;
        } else {
            return Markup.MARKUP_GROUP_TYPE;
        }
    }

    /**
     *
     * @param element
     */
    protected void text(Markup element) {
        String text = element.getString("text");
        String color = element.getString("color", "");
        renderText(text, color, element);
    }

    /**
     * Render simple text
     *
     * @param text
     * @param color
     * @param element - additional information about rendered element
     */
    protected abstract void renderText(String text, String color, Markup element);

    /**
     * Render list of elements
     *
     * @param element
     */
    protected void list(Markup element) {
        String bullet = element.getString("bullet", "- ");
        int level = getListLevel(element);
        //TODO add numbered lists with auto-incrementing bullets
        element.getContent().forEach(item -> listItem(level, bullet, item));
    }

    /**
     * Calculate the level of current list using ancestry
     *
     * @param element
     * @return
     */
    private int getListLevel(Markup element) {
        if (element.hasValue("level")) {
            return element.getInt("level");
        } else {
            int level = 1;
            Markup parent = element.getParent();
            while (parent != null) {
                if (parent.getType().equals(LIST_TYPE)) {
                    if (parent.hasValue("level")) {
                        return parent.getInt("level") + level;
                    } else {
                        level++;
                    }
                }
                parent = parent.getParent();
            }
            return level;
        }
    }

    /**
     * List item
     *
     * @param level
     * @param bullet
     * @param element
     */
    protected abstract void listItem(int level, String bullet, Markup element);


    protected void table(Markup element) {
        //TODO add header here
        element.getContent().forEach(row -> tableRow(row));
    }

    /**
     * Table row
     *
     * @param element
     */
    protected abstract void tableRow(Markup element);

//    /**
//     * A single table cell
//     *
//     * @param element
//     */
//    protected abstract void tableCell(Markup element);
}
