package hep.dataforge.fx.output

import hep.dataforge.meta.Configurable
import javafx.scene.layout.BorderPane

/**
 * An interface to produce border panes for content.
 */
interface FXDisplay : Configurable {
    fun getContainer(stage: String, name: String): BorderPane
}
