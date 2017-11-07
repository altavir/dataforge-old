/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.fragments

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ToggleButton
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import org.slf4j.LoggerFactory
import java.util.function.Supplier

/**
 * A separate window. In future could possibly be attachable
 *
 * @author Alexander Nozik
 */
class FragmentWindow : AutoCloseable {

    private val fragment: FXFragment
    private var stage: Stage? = null
    private var owner: Supplier<Window>? = null
    private val xPos = SimpleDoubleProperty()
    private val yPos = SimpleDoubleProperty()


    constructor(fragment: FXFragment) {
        this.fragment = fragment
        owner = null
    }

    constructor(fragment: FXFragment, owner: Supplier<Window>) {
        this.fragment = fragment
        setOwner(owner)
        fragment.isShowingProperty.addListener { observable, oldValue, newValue ->
            if (newValue != getStage().isShowing) {
                if (newValue!!) {
                    show()
                } else {
                    hide()
                }
            }
        }
    }

    private fun setOwner(owner: Supplier<Window>) {
        this.owner = owner
        if (stage != null) {
            try {
                stage!!.initOwner(owner.get())
            } catch (ex: Exception) {
                LoggerFactory.getLogger(javaClass).error("Failed to set window owner", ex)
            }

        }
    }

    /**
     * Helper method to create stage with given size and title
     *
     * @param root
     * @param title
     * @param width
     * @param height
     * @return
     */
    protected fun buildStage(root: Parent, title: String, width: Double, height: Double): Stage {
        val stage = Stage()
        stage.title = title
        stage.scene = Scene(root, width, height)
        stage.sizeToScene()
        return stage
    }

    fun getStage(): Stage {
        if (stage == null) {
            stage = buildStage(fragment.getFxNode(), fragment.title, fragment.width, fragment.height)
            stage!!.setOnShown { event: WindowEvent -> onShow() }

            fragment.heightProperty().bind(stage!!.heightProperty())
            fragment.widthProperty().bind(stage!!.widthProperty())
            xPos.bind(stage!!.xProperty())
            yPos.bind(stage!!.yProperty())

            stage!!.setOnHidden { event: WindowEvent -> onHide() }
            if (owner != null) {
                try {
                    stage!!.initOwner(owner!!.get())
                } catch (ex: Exception) {
                    LoggerFactory.getLogger(javaClass).error("Failed to set window owner", ex)
                }

            }
        }
        return stage
    }

    /**
     * hide and destroy the stage
     */
    override fun close() {
        if (stage != null) {
            hide()
            stage!!.close()
            stage = null
        }
    }

    fun hide() {
        getStage().hide()
    }

    fun show() {
        val stage = getStage()
        stage.show()
        stage.width = fragment.width
        stage.height = fragment.height
        stage.x = xPos.doubleValue()
        stage.y = yPos.doubleValue()
    }

    protected fun onShow() {
        fragment.isShowingProperty.set(true)
    }

    protected fun onHide() {
        fragment.isShowingProperty.set(false)
    }

    companion object {

        /**
         * Build a separate window containing the fragment and bind it to a button
         * @param button
         * @param fragmentBuilder
         * @return
         */
        fun build(button: ToggleButton, fragmentBuilder: Supplier<FXFragment>): FragmentWindow {
            val fragment = fragmentBuilder.get()
            fragment.isShowingProperty.bindBidirectional(button.selectedProperty())
            return FragmentWindow(fragment, { button.scene.window })
        }
    }

}
