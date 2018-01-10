package hep.dataforge.io.markup

import hep.dataforge.meta.Meta

/**
 * Something that could be represented as a text markup
 * Created by darksnake on 05-Feb-17.
 */
interface Markedup {
    /**
     * Represent this object as a text markup
     *
     * @return a markup
     */
    fun markup(configuration: Meta = Meta.empty()): Markup
}
