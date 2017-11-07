package hep.dataforge.fx.configuration

import javafx.collections.ObservableList

/**
 * A node, containing relative representation of configuration node and description
 * Created by darksnake on 01-May-17.
 */
abstract class ConfigFX(val name: String, val parent: ConfigFXNode? = null) {

//    val parentProperty: ObjectProperty<ConfigFXNode?> = SimpleObjectProperty(parent)

//    val parent: ConfigFXNode?
//        get() = parentProperty.get()

//    val nameProperty: StringProperty = SimpleStringProperty(name)
//
//    val name: String?
//        get() = nameProperty.get()

    //    abstract val descriptor: ObjectBinding< out Desc>
    //abstract val description: StringBinding;

    abstract val description: String
    abstract fun getChildren(): ObservableList<ConfigFX>
    /**
     * remove itself from parent
     */
    abstract fun remove()

    open fun invalidate() {
        TODO()
    }

//    abstract fun descriptorPresent(): ObservableBooleanValue
//    abstract fun valuePresent(): ObservableBooleanValue


//    protected open fun invalidate() {
//        setChanged()
//        notifyObservers()
//    }
}
