package hep.dataforge.fx.fragments

import hep.dataforge.fx.FXObject
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.*
import javafx.beans.value.ObservableObjectValue
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.BorderPane

import java.util.function.Supplier

/**
 * A container for
 * Created by darksnake on 09-Oct-16.
 */
abstract class FXFragment : FXObject {

    private val title = SimpleStringProperty("")
    private val preferredWidth = SimpleDoubleProperty()
    private val preferredHeight = SimpleDoubleProperty()
    val isShowingProperty: BooleanProperty = SimpleBooleanProperty(this, "isShowing", false)
    private var root: Parent? = null

    val isShowing: Boolean
        get() = isShowingProperty.get()

    var width: Double
        get() = preferredWidth.get()
        set(preferredWidth) = this.preferredWidth.set(preferredWidth)

    var height: Double
        get() = preferredHeight.get()
        set(preferredHeight) = this.preferredHeight.set(preferredHeight)

    constructor() {}

    protected constructor(title: String, width: Double, height: Double) {
        setTitle(title)
        width = width
        height = height
    }

    protected abstract fun buildRoot(): Parent

    override fun getFXNode(): Parent {
        if (root == null) {
            root = buildRoot()
            if (preferredWidth.value == null) {
                width = root!!.prefWidth(-1.0)
            }
            if (preferredHeight.value == null) {
                height = root!!.prefHeight(-1.0)
            }
        }
        return root
    }

    /**
     * Invalidate and force to rebuild root node
     */
    fun invalidate() {
        this.root = null
    }

    fun rootProperty(): ObservableObjectValue<Parent> {
        return object : ObjectBinding<Parent>() {
            override fun computeValue(): Parent {
                return fxNode
            }
        }
    }

    fun getTitle(): String {
        return title.get()
    }

    fun titleProperty(): StringProperty {
        return title
    }

    fun setTitle(title: String?) {
        this.title.set(title)
    }

    fun widthProperty(): DoubleProperty {
        return preferredWidth
    }

    fun heightProperty(): DoubleProperty {
        return preferredHeight
    }

    companion object {

        /**
         * Build fragment from scene Node
         * @param title
         * @param sup
         * @return
         */
        fun buildFromNode(title: String?, sup: Supplier<Node>): FXFragment {
            return object : FXFragment() {
                override fun buildRoot(): Parent {
                    if (title != null) {
                        setTitle(title)
                    }
                    val node = sup.get()
                    width = node.prefWidth(-1.0)
                    height = node.prefHeight(-1.0)
                    if (node is Parent) {
                        return node
                    } else {
                        val pane = BorderPane()
                        pane.center = node
                        return pane
                    }
                }
            }
        }
    }

}
