package hep.dataforge.kodex.fx.plots

import hep.dataforge.description.DescriptorUtils
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.fx.ApplicationSurrogate
import hep.dataforge.fx.FXObject
import hep.dataforge.fx.configuration.ConfigEditor
import hep.dataforge.kodex.fx.dfIcon
import hep.dataforge.meta.Configuration
import hep.dataforge.plots.PlotFrame
import hep.dataforge.plots.PlotGroup
import hep.dataforge.plots.PlotStateListener
import hep.dataforge.plots.Plottable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.TreeItem
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import tornadofx.*
import java.util.*


internal val defaultDisplay: (PlotFrame) -> Node = {
    if (it is FXObject) {
        it.fxNode
    } else {
        throw IllegalArgumentException("Can't display a plot frame since it is not an FX object")
    }
}

class PlotContainer(val plot: PlotFrame, display: (PlotFrame) -> Node = defaultDisplay) : Fragment(icon = ImageView(dfIcon)) {

    private val configWindows = HashMap<Configuration, Stage>()


    val sideBarExpandedProperty = SimpleBooleanProperty(true)
    var sideBarExpanded by sideBarExpandedProperty

    val progresProperty = SimpleDoubleProperty(1.0)
    var progres by progresProperty


    lateinit var sidebar: VBox;

    override val root = borderpane {
        center {
            val sideBarPosition = SimpleDoubleProperty(0.8)
            splitpane(orientation = Orientation.HORIZONTAL) {
                stackpane {
                    borderpane {
                        minHeight = 300.0
                        minWidth = 300.0
                        center = display(plot)
                    }
                    val collapseButton = button(text = "<<") {
                        graphicTextGap = 0.0
                        opacity = 0.4
                        textAlignment = TextAlignment.JUSTIFY
                        StackPane.setAlignment(this, Pos.TOP_RIGHT)
                        font = Font.font("System Bold", 12.0)
                        action {
                            when {
                                sideBarExpandedProperty.get() -> setDividerPosition(0, 1.0)
                                sideBarPosition.value > 0 -> setDividerPosition(0, sideBarPosition.value)
                                else -> setDividerPosition(0, sideBarPosition.get())
                            }
                        }
                    }
                    borderpane {
                        isMouseTransparent = true
                        center = progressindicator(progresProperty) {
                            maxWidth = 50.0
                            prefWidth = 50.0
                            alignment = Pos.CENTER
                            visibleWhen(progresProperty.lessThan(1.0))
                        }
                    }
                    sideBarExpandedProperty.addListener { _, _, newValue ->
                        if (newValue) {
                            collapseButton.text = ">>"
                        } else {
                            collapseButton.text = "<<"
                        }
                    }
                    setDividerPositions(sideBarPosition.get())
                }
                sidebar = vbox {
                    button(text = "Frame config") {
                        minWidth = 0.0
                        maxWidth = Double.MAX_VALUE
                        action {
                            displayConfigurator("Plot frame configuration", plot.config, DescriptorUtils.buildDescriptor(plot))
                        }
                    }
                    treeview<Plottable> {
                        root = TreeItem(plot.plots);
                        root.isExpanded = true
                        vgrow = Priority.ALWAYS
                        //function to populate tree view
                        fun populate() = populate { parent ->
                            val value = parent.value;
                            if (value is PlotGroup) {
                                value.children.values
                            } else {
                                null
                            }
                        }

                        populate()

                        plot.plots.addListener(object : PlotStateListener {
                            override fun notifyDataChanged(name: String?) {}//ignore
                            override fun notifyConfigurationChanged(name: String?) {}//ignore
                            override fun notifyGroupChanged(name: String?) {
                                //repopulate on list change
                                runLater {
                                    populate()
                                }
                            }
                        })

                        //cell format
                        cellFormat { item ->
                            graphic = hbox {
                                hgrow = Priority.ALWAYS
                                checkbox(item.config.getString("title", item.name)) {
                                    if (item == plot.plots) {
                                        text = "<<< All plots >>>"
                                    }
                                    isSelected = item.config.getBoolean("visible", true)
                                    selectedProperty().addListener { _, _, newValue ->
                                        item.config.setValue("visible", newValue)
                                    }
                                    if (item.config.hasValue("color")) {
                                        textFill = Color.valueOf(item.config.getString("color"))
                                    }

                                    item.config.addObserver { name, _, newItem ->
                                        when (name) {
                                            "title" -> if (newItem == null) {
                                                text = item.name
                                            } else {
                                                text = newItem.stringValue()
                                            }
                                            "color" -> if (newItem == null) {
                                                textFill = Color.BLACK
                                            } else {
                                                textFill = Color.valueOf(newItem.stringValue())
                                            }
                                            "visible" -> if (newItem == null) {
                                                isSelected = true
                                            } else {
                                                isSelected = newItem.booleanValue()
                                            }
                                        }
                                    }
                                }

                                pane {
                                    hgrow = Priority.ALWAYS
                                }

                                button("...") {
                                    minWidth = 0.0
                                    action {
                                        displayConfigurator(item.name + " configuration", item.config, DescriptorUtils.buildDescriptor(item))
                                    }
                                }


                            }
                            text = null;
                        }

                        // global context menu
                        contextmenu {
                            item("Show all") {
                                action { plot.plots.forEach { it.configureValue("visible", true) } }
                            }
                            item("Hide all") {
                                action { plot.plots.forEach { it.configureValue("visible", false) } }
                            }
                        }
                    }
                }


                dividers[0].positionProperty().addListener { _, _, newValue ->
                    sideBarExpandedProperty.set(newValue.toDouble() < 0.99)
                    if (newValue.toDouble() < 0.98) {
                        sideBarPosition.set(newValue.toDouble())
                    }
                }
            }
        }
    }

    fun addToSideBar(index: Int, vararg nodes: Node) {
        sidebar.children.addAll(index, Arrays.asList(*nodes))
    }

    fun addToSideBar(vararg nodes: Node) {
        sidebar.children.addAll(Arrays.asList(*nodes))
    }


    /**
     * Display configurator in separate scene
     *
     * @param config
     * @param desc
     */
    private fun displayConfigurator(header: String, config: Configuration, desc: NodeDescriptor) {
        configWindows.getOrPut(config) {
            Stage().apply {
                scene = Scene(ConfigEditor.build(config, desc))
                height = 400.0
                width = 400.0
                title = header
                setOnCloseRequest { configWindows.remove(config) }
                initOwner(root.scene.window)
            }
        }.apply {
            show()
            toFront()
        }
    }

    companion object {

        /**
         * for testing
         */
        fun display(plot: PlotFrame, title: String = "", width: Double = 800.0, height: Double = 600.0) {
            Platform.setImplicitExit(false)
            ApplicationSurrogate.start()

            val container = PlotContainer(plot)

            Platform.runLater {
                ApplicationSurrogate.getStage().apply {
                    this.width = width
                    this.height = height
                    this.title = title
                    this.scene = Scene(container.root, width, height)
                    this.show()
                }
            }

            Platform.setImplicitExit(true)
        }
    }
}