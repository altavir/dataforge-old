package hep.dataforge.fx.plots

import hep.dataforge.context.Context
import hep.dataforge.description.Descriptors
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.fx.dfIconView
import hep.dataforge.fx.display
import hep.dataforge.fx.meta.ConfigEditor
import hep.dataforge.fx.table.TableDisplay
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.plots.*
import hep.dataforge.plots.data.DataPlot
import hep.dataforge.utils.FXObject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.TreeItem
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

class PlotContainer(val frame: PlotFrame, display: (PlotFrame) -> Node = defaultDisplay) : Fragment(icon = dfIconView) {

    private val configWindows = HashMap<hep.dataforge.meta.Configurable, Stage>()
    private val dataWindows = HashMap<Plot, Stage>()


    private val sideBarExpandedProperty = SimpleBooleanProperty(false)
    var sideBarExpanded by sideBarExpandedProperty

    private val sideBarPositionProperty = SimpleDoubleProperty(0.7)
    var sideBarPoistion by sideBarPositionProperty

    val progressProperty = SimpleDoubleProperty(1.0)
    var progress by progressProperty

    private lateinit var sidebar: VBox;

    override val root = borderpane {
        center {
            splitpane(orientation = Orientation.HORIZONTAL) {
                stackpane {
                    borderpane {
                        minHeight = 300.0
                        minWidth = 300.0
                        center = display(frame)
                    }
                    button {
                        graphicTextGap = 0.0
                        opacity = 0.4
                        textAlignment = TextAlignment.JUSTIFY
                        StackPane.setAlignment(this, Pos.TOP_RIGHT)
                        font = Font.font("System Bold", 12.0)
                        action {
                            sideBarExpanded = !sideBarExpanded;
                        }
                        sideBarExpandedProperty.addListener { _, _, expanded ->
                            if (expanded) {
                                setDividerPosition(0, sideBarPoistion);
                            } else {
                                setDividerPosition(0, 1.0);
                            }
                        }
                        textProperty().bind(
                                sideBarExpandedProperty.stringBinding {
                                    if (it == null || it) {
                                        ">>"
                                    } else {
                                        "<<"
                                    }
                                }
                        )
                    }
                    progressindicator(progressProperty) {
                        maxWidth = 50.0
                        prefWidth = 50.0
                        visibleWhen(progressProperty.lessThan(1.0))
                    }
                }
                sidebar = vbox {
                    button(text = "Frame config") {
                        minWidth = 0.0
                        maxWidth = Double.MAX_VALUE
                        action {
                            displayConfigurator("Plot frame configuration", frame, Descriptors.buildDescriptor(frame))
                        }
                    }
                    treeview<Plottable> {
                        minWidth = 0.0
                        root = fillTree(frame.plots);
                        vgrow = Priority.ALWAYS

                        //cell format
                        cellFormat { item ->
                            graphic = hbox {
                                hgrow = Priority.ALWAYS
                                checkbox(item.title) {
                                    minWidth = 0.0
                                    if (item == frame.plots) {
                                        text = "<<< All plots >>>"
                                    }
                                    isSelected = item.config.getBoolean("visible", true)
                                    selectedProperty().addListener { _, _, newValue ->
                                        item.config.setValue("visible", newValue)
                                    }


                                    if (frame is XYPlotFrame) {
                                        frame.getActualColor(getFullName(this@cellFormat.treeItem)).ifPresent {
                                            textFill = Color.valueOf(it.string)
                                        }
                                    } else if (item.config.hasValue("color")) {
                                        textFill = Color.valueOf(item.config.getString("color"))
                                    }

                                    item.config.addObserver { name, _, newItem ->
                                        when (name.toUnescaped()) {
                                            "title" -> text = if (newItem == null) {
                                                item.title
                                            } else {
                                                newItem.string
                                            }
                                            "color" -> textFill = if (newItem == null) {
                                                Color.BLACK
                                            } else {
                                                try {
                                                    Color.valueOf(newItem.string)
                                                } catch (ex: Exception) {
                                                    Color.BLACK
                                                }
                                            }
                                            "visible" -> isSelected = newItem?.boolean ?: true
                                        }
                                    }

                                    contextmenu {
                                        if (item is DataPlot) {
                                            item("Show data") {
                                                action {
                                                    displayData(item)
                                                }
                                            }
                                        } else if (item is PlotGroup) {
                                            item("Show all") {
                                                action { item.forEach { it.configureValue("visible", true) } }
                                            }
                                            item("Hide all") {
                                                action { item.forEach { it.configureValue("visible", false) } }
                                            }
                                        }
                                        if (!this@cellFormat.treeItem.isLeaf) {
                                            item("Sort") {
                                                action { this@cellFormat.treeItem.children.sortBy { it.value.title } }
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
                                        displayConfigurator(item.title + " configuration", item, Descriptors.buildDescriptor(item))
                                    }
                                }


                            }
                            text = null;
                        }

                    }
                }

                dividers[0].position = 1.0

                dividers[0].positionProperty().onChange {
                    if (it < 0.9) {
                        sideBarPositionProperty.set(it)
                    }
                    sideBarExpanded = it < 0.99
                }

                this@borderpane.widthProperty().onChange {
                    if (sideBarExpanded) {
                        dividers[0].position = sideBarPoistion
                    } else {
                        dividers[0].position = 1.0
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
    private fun displayConfigurator(header: String, obj: hep.dataforge.meta.Configurable, desc: NodeDescriptor) {
        configWindows.getOrPut(obj) {
            Stage().apply {
                scene = Scene(ConfigEditor(obj.config, "Configuration editor", desc).root)
                height = 400.0
                width = 400.0
                title = header
                setOnCloseRequest { configWindows.remove(obj) }
                initOwner(root.scene.window)
            }
        }.apply {
            show()
            toFront()
        }
    }

    private fun displayData(plot: DataPlot) {
        dataWindows.getOrPut(plot) {
            Stage().apply {
                scene = Scene(TableDisplay(PlotUtils.extractData(plot, Meta.empty())).root)
                height = 400.0
                width = 400.0
                title = "Data: ${plot.title}"
                setOnCloseRequest { dataWindows.remove(plot) }
                initOwner(root.scene.window)
            }
        }.apply {
            show()
            toFront()
        }
    }

    private inner class ContainerChangeListener(val item: TreeItem<Plottable>) : PlotListener {
        override fun dataChanged(name: Name, plot: Plot) {
        }

        override fun metaChanged(name: Name, plottable: Plottable, laminate: Laminate) {
        }

        override fun plotAdded(name: Name, plottable: Plottable) {
            if (name.length == 1) {
                item.children.add(fillTree(plottable))
            }
        }

        override fun plotRemoved(name: Name) {
            if (name.length == 1) {
                item.children.removeIf { it.value.name == name }
            }
        }

    }

    private fun fillTree(plot: Plottable): TreeItem<Plottable> {
        val item = TreeItem(plot)
        plot.addListener(ContainerChangeListener(item))
        if (plot is PlotGroup) {
            item.children.setAll(plot.children.map { fillTree(it) })
        }
        item.isExpanded = true
        return item
    }

    private fun getFullName(item: TreeItem<Plottable>): Name {
        return if (item.parent == null || item.parent.value.name.isEmpty()) {
            item.value.name;
        } else {
            Name.join(getFullName(item.parent), item.value.name)
        }
    }

}

fun Context.display(frame: PlotFrame, width: Double = 800.0, height: Double = 600.0) {
    display(PlotContainer(frame), width, height)
}