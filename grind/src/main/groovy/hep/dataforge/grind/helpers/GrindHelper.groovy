package hep.dataforge.grind.helpers

import hep.dataforge.context.Encapsulated
import hep.dataforge.description.Described
import hep.dataforge.io.markup.Markup

/**
 * A helper that is injected into the shell. A helper must have public
 */
interface GrindHelper extends Encapsulated, Described {
    Markup help();

}
