package hep.dataforge.grind.helpers

import hep.dataforge.context.Encapsulated
import hep.dataforge.description.Described
import hep.dataforge.io.markup.Markup
import hep.dataforge.names.Named

/**
 * A helper that is injected into the shell. A helper must have public
 */
interface GrindHelper extends Encapsulated, Named, Described {
    Markup help();

}
