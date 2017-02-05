package hep.dataforge.io.markup;

import hep.dataforge.meta.Meta;

/**
 * Something that could be represented as a text markup
 * Created by darksnake on 05-Feb-17.
 */
public interface Markedup {
    /**
     * Represent this object as a text markup with default configuration
     *
     * @return a markup
     */
    default Markup markup() {
        return markup(Meta.empty());
    }

    /**
     * Represent this object as a text markup
     *
     * @param configuration configuration of the markup
     * @return a markup
     */
    Markup markup(Meta configuration);
}
