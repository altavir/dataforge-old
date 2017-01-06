package hep.dataforge.io.text;

import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.GenericBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hep.dataforge.io.text.GenericMarkupRenderer.LIST_TYPE;

/**
 * Created by darksnake on 03-Jan-17.
 */
public class MarkupBuilder implements GenericBuilder<Markup, MarkupBuilder>, Annotated {

    public static MarkupBuilder text(String text) {
        return new MarkupBuilder().setValue("text", text);
    }

    public static MarkupBuilder text(String text, String color) {
        return text(text).setValue("color", color);
    }

    /**
     * Create list markup with given level and bullet
     *
     * @param level  ignored if not positive
     * @param bullet ignored if null
     * @return
     */
    public static MarkupBuilder list(int level, @Nullable String bullet) {
        MarkupBuilder res = new MarkupBuilder().setType(LIST_TYPE);
        if (level > 0) {
            res.setValue("level", level);
        }

        if (bullet != null) {
            res.setValue("bullet", bullet);
        }
        return res;
    }

    private final MetaBuilder builder = new MetaBuilder("markup");

    @Override
    public MarkupBuilder self() {
        return this;
    }

    @Override
    public Markup build() {
        return new Markup(builder);
    }

    @Override
    public Meta meta() {
        return builder.build();
    }

    public MarkupBuilder update(Meta config) {
        builder.update(config);
        return self();
    }

    public MarkupBuilder update(Map<String, ? extends Object> map) {
        builder.update(map);
        return self();
    }

    public MarkupBuilder setValue(String key, Object value) {
        builder.setValue(key, value);
        return self();
    }

    public MarkupBuilder setType(String type) {
        builder.setValue(Markup.MARKUP_TYPE_KEY, type);
        return self();
    }

    public MarkupBuilder setMeta(Meta meta) {
        builder.setNode(Markup.MARKUP_META_NODE, meta);
        return self();
    }

    public MarkupBuilder setStyle(Meta style) {
        builder.setNode(Markup.MARKUP_STYLE_NODE, style);
        return self();
    }

    //TODO apply style

    public MarkupBuilder setContent(Meta... content) {
        builder.setNode(Markup.MARKUP_CONTENT_NODE, content);
        return self();
    }

    public MarkupBuilder setContent(Stream<MarkupBuilder> content) {
        builder.setNode(Markup.MARKUP_CONTENT_NODE, content.map(it -> it.meta()).collect(Collectors.toList()));
        return self();
    }

    public MarkupBuilder setContent(MarkupBuilder... content) {
        return setContent(Stream.of(content));
    }

    public MarkupBuilder addContent(Meta content) {
        builder.putNode(Markup.MARKUP_CONTENT_NODE, content);
        return self();
    }

    public MarkupBuilder addContent(MarkupBuilder content) {
        builder.putNode(Markup.MARKUP_CONTENT_NODE, content.getMeta());
        return self();
    }

    public MarkupBuilder addText(String text) {
        return addContent(new MetaBuilder(Markup.MARKUP_CONTENT_NODE)
                .setValue("text", text)
        );
    }

    public MarkupBuilder addText(String text, String color) {
        return addContent(new MetaBuilder(Markup.MARKUP_CONTENT_NODE)
                .setValue("text", text)
                .setValue("color", color)
        );
    }

    public MarkupBuilder addColumn(String text, int width) {
        return addContent(new MetaBuilder(Markup.MARKUP_CONTENT_NODE)
                .setValue("text", text)
                .setValue("textWidth", width)
        );
    }

    public MarkupBuilder addList(MarkupBuilder... items) {
        return addContent(new MarkupBuilder()
                .setType(GenericMarkupRenderer.LIST_TYPE)
                .setContent(items)
        );
    }


// TODO add tables

}
