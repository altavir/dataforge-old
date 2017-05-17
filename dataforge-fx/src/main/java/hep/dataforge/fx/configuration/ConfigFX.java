package hep.dataforge.fx.configuration;

import hep.dataforge.names.Named;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ObservableList;

import java.util.Observable;
import java.util.Optional;

/**
 * Created by darksnake on 01-May-17.
 */
public abstract class ConfigFX extends Observable implements Named {


    protected ObjectProperty<ConfigFXNode> parentProperty = new SimpleObjectProperty<>();
    protected StringProperty name = new SimpleStringProperty();

    protected Optional<ConfigFXNode> getParent() {
        return Optional.ofNullable(parentProperty.get());
    }

    public String getDescription() {
        return descriptionProperty().get();
    }

    protected StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return nameProperty().get();
    }



    public abstract ObservableStringValue descriptionProperty();
    public abstract ObservableBooleanValue descriptorPresent();
    public abstract ObservableBooleanValue valuePresent();
    public abstract ObservableList<ConfigFX> getChildren();

    public abstract void remove();

    protected void invalidate(){
        setChanged();
        notifyObservers();
    }
}
