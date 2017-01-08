package hep.dataforge.io.markup;

import hep.dataforge.description.Described;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Configuration;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleConfigurable;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Basic markup element wrapper. A markup element must have a type designation, fields specific for this markup element (like 'text' for text markup).
 * The structure of intrinsic meta could be as complex as needed as long as its upper layer nodes do not have name conflict with standard markup nodes like 'style'
 * 'style' element contains optional style information.
 * Created by darksnake on 30-Dec-16.
 */
@ValueDef(name = "type", def = "text", info = "The type of this block")
@NodeDef(name = "meta", info = "Meta specific for this element")
@NodeDef(name = "style", info = "Style override")
public class Markup extends SimpleConfigurable implements Described, ValueProvider {
    /**
     * A generic container type.
     */
    public static final String MARKUP_GROUP_TYPE = "group";
    public static final String MARKUP_STYLE_NODE = "style";
    public static final String MARKUP_CONTENT_NODE = "c";
    public static final String MARKUP_TYPE_KEY = "type";

    public Markup(Configuration c) {
        super(c);
    }

    public Markup(Meta meta) {
        super(new Configuration(meta));
    }

    /**
     * Get type of this block. If type is not defined use externally inferred type.
     *
     * @return
     */
    public String getType(Function<Markup, String> infer) {
        return meta().getString(MARKUP_TYPE_KEY, () -> infer.apply(this));
    }

    /**
     * Get type of this block. If type is not defined, use group type.
     *
     * @return
     */
    public String getType() {
        return meta().getString(MARKUP_TYPE_KEY, MARKUP_GROUP_TYPE);
    }

    /**
     * Get the parent element for this one. If null, then this is a root element
     *
     * @return
     */
    @Nullable
    public Markup getParent() {
        //TODO add caching to avoid reconstruction of the tree each time this method is called
        if (getConfig().getParent() == null) {
            return null;
        } else {
            return new Markup(getConfig().getParent());
        }
    }

    /**
     * Style ignores values outside {@code style} node
     * @return
     */
    public Laminate getStyle() {
        Laminate laminate = new Laminate();

        if (meta().hasMeta(MARKUP_STYLE_NODE)) {
            laminate.addFirstLayer(meta().getMeta(MARKUP_STYLE_NODE));
        }

        Markup parent = getParent();

        if (parent != null) {
            laminate.addLayer(parent.getStyle());
        }

        laminate.setDescriptor(getDescriptor());
        return laminate;
    }

    @Override
    protected void applyConfig(Meta config) {
        //do nothing
    }

    @Override
    public Value getValue(String path) {
        return meta().getValue(path, () -> getStyle().getValue(path));
    }

    /**
     * Stream of child nodes of this node in case it serves as a group
     *
     * @return
     */
    public Stream<Markup> getContent() {
        return getConfig().getMetaList(MARKUP_CONTENT_NODE).stream().map(it -> new Markup(it));
    }
}
