package hep.dataforge.fx.output;

import hep.dataforge.meta.Configurable;
import javafx.scene.layout.BorderPane;

/**
 * An interface to produce border panes for content.
 */
public interface FXDisplay extends Configurable {
    BorderPane getContainer(String stage, String name);
}
