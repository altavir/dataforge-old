package hep.dataforge.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public interface FXObject {
    Node getFXNode();

    default Parent getParent() {
        Node node = getFXNode();
        if (node instanceof Parent) {
            return (Parent) node;
        } else {
            BorderPane pane = new BorderPane();
            pane.setCenter(node);
            return pane;
        }
    }

}
