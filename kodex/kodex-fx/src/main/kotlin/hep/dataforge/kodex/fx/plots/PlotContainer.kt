package hep.dataforge.kodex.fx.plots

import hep.dataforge.description.DescriptorUtils
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.fx.ApplicationSurrogate
import hep.dataforge.fx.FXObject
import hep.dataforge.fx.configuration.ConfigEditor
import hep.dataforge.kodex.fx.TableDisplay
import hep.dataforge.kodex.fx.dfIcon
import hep.dataforge.meta.Configuration
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.plots.*
import hep.dataforge.plots.data.DataPlot
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

class PlotContainer(val frame: PlotFrame, display: (PlotFrame) -> Node = defaultDisplay) : Fragment(icon = ImageView(dfIcon)) {

    private val configWindows = HashMap<Configuration, Stage>()
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
                        StackPane.setAlignment(this, Pos.CENTER)
                        visibleWhen(progressProperty.lessThan(1.0))
                    }

                }
                sidebar = vbox {
                    button(text = "Frame config") {
                        minWidth = 0.0
                        maxWidth = Double.MAX_VALUE
                        action {
                            displayConfigurator("Plot frame configuration", frame.config, DescriptorUtils.buildDescriptor(frame))
                        }
                    }
                    treeview<Plottable> {
                        minWidth = 0.0
                        root = PlotTreeItem(frame.plots);
                        vgrow = Priority.ALWAYS

                        //cell format
                        cellFormat { item ->
                            val cell = this
                            graphic = hbox {
                                hgrow = Priority.ALWAYS
                                checkbox(item.config.getString("title", item.name)) {
                                    minWidth = 0.0
                                    if (item == frame.plots) {
                                        text = "<<< All plots >>>"
                                    }
                                    isSelected = item.config.getBoolean("visible", true)
                                    selectedProperty().addListener { _, _, newValue ->
                                        item.config.setValue("visible", newValue)
                                    }


                                    if (frame is XYPlotFrame) {
                                        frame.getActualColor(getFullName(cell.treeItem)).ifPresent {
                                            textFill = Color.valueOf(it.stringValue())
                                        }
                                    } else if (item.config.hasValue("color")) {
                                        textFill = Color.valueOf(item.config.getString("color"))
                                    }

                                    item.config.addObserver { name, _, newItem ->
                                        when (name) {
                                            "title" -> text = if (newItem == null) {
                                                item.name
                                            } else {
                                                newItem.stringValue()
                                            }
                                            "color" -> textFill = if (newItem == null) {
                                                Color.BLACK
                                            } else {
                                                Color.valueOf(newItem.stringValue())
                                            }
                                            "visible" -> isSelected = newItem?.booleanValue() ?: true
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
                                        if (cell.treeItem is PlotTreeItem) {
                                            item("Sort") {
                                                action { (cell.treeItem as PlotTreeItem).sort() }
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

                    }
                }

                dividers[0].position = 1.0

                dividers[0].positionProperty().onChange {
                    if (it.toDouble() < 0.9) {
                        sideBarPositionProperty.set(it.toDouble())
                    }
                    sideBarExpanded = it.toDouble() < 0.99
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

    private fun displayData(plot: DataPlot) {
        dataWindows.getOrPut(plot) {
            Stage().apply {
                scene = Scene(TableDisplay(PlotUtils.extractData(plot, Meta.empty())).root)
                height = 400.0
                width = 400.0
                title = "Data: ${plot.name}"
                setOnCloseRequest { dataWindows.remove(plot) }
                initOwner(root.scene.window)
            }
        }.apply {
            show()
            toFront()
        }
    }

    private class PlotTreeItem(plot: Plottable, graphics: Node? = null) : TreeItem<Plottable>(plot, graphics) {
        init {
            (value as? PlotGroup)?.let { plt ->
                children.setAll(plt.children.map { PlotTreeItem(it) })
                plot.addListener { name ->
                    if (Name.of(name).length() == 1) {
                        (value as? PlotGroup)?.let { plt ->
                            children.setAll(plt.children.map { PlotTreeItem(it) })
                        }
                    }
                }
            }

            isExpanded = true
        }

        override fun isLeaf(): Boolean {
            return value !is PlotGroup
        }

        fun sort() {
            children.sortBy { it.value.name }
        }
    }

    private fun getFullName(item: TreeItem<Plottable>): String {
        if (item.parent == null || item.parent.value.name.isEmpty()) {
            return item.value.name;
        } else {
            return Name.joinString(getFullName(item.parent), item.value.name)
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