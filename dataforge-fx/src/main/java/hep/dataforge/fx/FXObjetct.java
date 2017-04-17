package hep.dataforge.fx;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 * Created by darksnake on 17-Apr-17.
 */
public interface FXObjetct {
    /**
     * Get the root node of this object
     * @return
     */
    Node getRoot();

    /**
     * Display object in AnchorPane
     *
     * @param container
     */
    default void display(AnchorPane container){
        Node root = getRoot();
        AnchorPane.setTopAnchor(root, 0d);
        AnchorPane.setBottomAnchor(root, 0d);
        AnchorPane.setRightAnchor(root, 0d);
        AnchorPane.setLeftAnchor(root, 0d);
        container.getChildren().add(root);
    }
}
