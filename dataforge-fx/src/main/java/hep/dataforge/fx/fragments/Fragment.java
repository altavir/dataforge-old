package hep.dataforge.fx.fragments;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.Parent;

/**
 * A general GUI fragment
 * Created by darksnake on 09-Oct-16.
 */
public abstract class Fragment{
    private BooleanProperty showing = new SimpleBooleanProperty(false);
    private StringProperty title = new SimpleStringProperty("");
    private DoubleProperty preferedWidth = new SimpleDoubleProperty(400);
    private DoubleProperty preferedHeight = new SimpleDoubleProperty(400);
    private Parent root;

    public Fragment() {
    }

    protected Fragment(String title, double width, double height) {
        setTitle(title);
        setPreferedWidth(width);
        setPreferedHeight(height);
    }

    protected abstract Parent buildRoot();

    public Parent getRoot() {
        if(root == null){
            root = buildRoot();
        }
        return root;
    }

    /**
     * Invalidate and force to rebuild root node
     */
    public void invalidate(){
        this.root = null;
    }

    public ObservableObjectValue<Parent> rootProperty(){
        return new ObjectBinding<Parent>() {
            @Override
            protected Parent computeValue() {
                return getRoot();
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

    public double getPreferedWidth() {
        return preferedWidth.get();
    }

    public DoubleProperty preferedWidthProperty() {
        return preferedWidth;
    }

    public void setPreferedWidth(double preferedWidth) {
        this.preferedWidth.set(preferedWidth);
    }

    public double getPreferedHeight() {
        return preferedHeight.get();
    }

    public DoubleProperty preferedHeightProperty() {
        return preferedHeight;
    }

    public void setPreferedHeight(double preferedHeight) {
        this.preferedHeight.set(preferedHeight);
    }

    public void show(){
        this.showing.set(true);
    }

    public void hide(){
        this.showing.set(false);
    }
}
