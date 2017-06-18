package hep.dataforge.fx.fragments;

import hep.dataforge.fx.FXObject;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.util.function.Supplier;

/**
 * A container for
 * Created by darksnake on 09-Oct-16.
 */
public abstract class FXFragment implements FXObject {

    /**
     * Build fragment from scene Node
     * @param title
     * @param sup
     * @return
     */
    public static FXFragment buildFromNode(String title, Supplier<Node> sup) {
        return new FXFragment() {
            @Override
            protected Parent buildRoot() {
                if (title != null) {
                    setTitle(title);
                }
                Node node = sup.get();
                setWidth(node.prefWidth(-1));
                setHeight(node.prefHeight(-1));
                if (node instanceof Parent) {
                    return (Parent) node;
                } else {
                    BorderPane pane = new BorderPane();
                    pane.setCenter(node);
                    return pane;
                }
            }
        };
    }

    private StringProperty title = new SimpleStringProperty("");
    private DoubleProperty preferredWidth = new SimpleDoubleProperty();
    private DoubleProperty preferredHeight = new SimpleDoubleProperty();
    private BooleanProperty isShowing = new SimpleBooleanProperty(this, "isShowing", false);
    private Parent root;

    public FXFragment() {
    }

    protected FXFragment(String title, double width, double height) {
        setTitle(title);
        setWidth(width);
        setHeight(height);
    }

    protected abstract Parent buildRoot();

    @Override
    public Parent getFXNode() {
        if (root == null) {
            root = buildRoot();
            if (preferredWidth.getValue() == null) {
                setWidth(root.prefWidth(-1));
            }
            if (preferredHeight.getValue() == null) {
                setHeight(root.prefHeight(-1));
            }
        }
        return root;
    }

    /**
     * Invalidate and force to rebuild root node
     */
    public void invalidate() {
        this.root = null;
    }

    public ObservableObjectValue<Parent> rootProperty() {
        return new ObjectBinding<Parent>() {
            @Override
            protected Parent computeValue() {
                return getFXNode();
            }
        };
    }

    public boolean isShowing() {
        return isShowing.get();
    }

    public BooleanProperty isShowingProperty() {
        return isShowing;
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public double getWidth() {
        return preferredWidth.get();
    }

    public DoubleProperty widthProperty() {
        return preferredWidth;
    }

    public void setWidth(double preferredWidth) {
        this.preferredWidth.set(preferredWidth);
    }

    public double getHeight() {
        return preferredHeight.get();
    }

    public DoubleProperty heightProperty() {
        return preferredHeight;
    }

    public void setHeight(double preferredHeight) {
        this.preferredHeight.set(preferredHeight);
    }

}
