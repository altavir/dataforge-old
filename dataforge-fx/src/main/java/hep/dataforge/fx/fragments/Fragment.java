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
 * A general GUI fragment
 * Created by darksnake on 09-Oct-16.
 */
public abstract class Fragment implements FXObject {

    /**
     * Build fragment from scene Node
     * @param title
     * @param sup
     * @return
     */
    public static Fragment buildFromNode(String title, Supplier<Node> sup) {
        return new Fragment() {
            @Override
            protected Parent buildRoot() {
                if (title != null) {
                    setTitle(title);
                }
                Node node = sup.get();
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

    private BooleanProperty showing = new SimpleBooleanProperty(false);
    private StringProperty title = new SimpleStringProperty("");
    private DoubleProperty preferredWidth = new SimpleDoubleProperty();
    private DoubleProperty preferredHeight = new SimpleDoubleProperty();
    private Parent root;

    public Fragment() {
    }

    protected Fragment(String title, double width, double height) {
        setTitle(title);
        setPreferredWidth(width);
        setPreferredHeight(height);
    }

    protected abstract Parent buildRoot();

    @Override
    public Parent getFXNode() {
        if (root == null) {
            root = buildRoot();
            if (preferredWidth.getValue() == null) {
                setPreferredWidth(root.prefWidth(-1));
            }
            if (preferredHeight.getValue() == null) {
                setPreferredHeight(root.prefHeight(-1));
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
        return showing.get();
    }

    public BooleanProperty showingProperty() {
        return showing;
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

    public double getPreferredWidth() {
        return preferredWidth.get();
    }

    public DoubleProperty preferredWidthProperty() {
        return preferredWidth;
    }

    public void setPreferredWidth(double preferredWidth) {
        this.preferredWidth.set(preferredWidth);
    }

    public double getPreferredHeight() {
        return preferredHeight.get();
    }

    public DoubleProperty preferredHeightProperty() {
        return preferredHeight;
    }

    public void setPreferredHeight(double preferredHeight) {
        this.preferredHeight.set(preferredHeight);
    }

    public void show() {
        this.showing.set(true);
    }

    public void hide() {
        this.showing.set(false);
    }
}
