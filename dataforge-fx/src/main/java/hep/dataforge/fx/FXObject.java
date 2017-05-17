package hep.dataforge.fx;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Created by darksnake on 17-Apr-17.
 */
public interface FXObject {
    /**
     * Get the root node of this object
     * @return
     */
    Node getFXNode();

    default Parent getPane(){
        Node node = getFXNode();
        if(node instanceof Parent){
            return (BorderPane) node;
        } else {
            BorderPane pane = new BorderPane();
            pane.setCenter(node);
            return pane;
        }
    }
}
