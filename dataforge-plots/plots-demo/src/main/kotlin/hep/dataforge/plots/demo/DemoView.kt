package hep.dataforge.plots.demo

import hep.dataforge.plots.data.PlottableData
import hep.dataforge.plots.fx.PlotContainer
import hep.dataforge.plots.jfreechart.JFreeChartFrame
import hep.dataforge.tables.ValueMap
import hep.dataforge.tables.XYAdapter
import hep.dataforge.values.Values
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import javafx.util.converter.DoubleStringConverter
import tornadofx.*

class DemoView : View("Plot demonstration") {

    private val frame = JFreeChartFrame()


    class PlotData() {
        private val names = arrayOf(XYAdapter.X_AXIS, XYAdapter.Y_AXIS, XYAdapter.Y_ERROR_KEY)

        val yErrProperty = SimpleDoubleProperty()
        var yErr: Double? by yErrProperty

        val yProperty = SimpleDoubleProperty()
        var y: Double? by yProperty

        val xProperty = SimpleDoubleProperty()
        var x: Double? by xProperty

        fun toValues(): Values {
            return ValueMap.of(names, x, y, yErr)
        }
    }

    //private val dataMap = FXCollections.observableHashMap<String, ObservableList<PlotData>>()
    val dataMap = FXCollections.observableHashMap<String, ObservableList<PlotData>>().apply {
        addListener{change: MapChangeListener.Change<out String, out ObservableList<PlotData>> ->
            dataChanged(change.key)
        }
    };

    lateinit var dataPane: TabPane;

    override val root = borderpane {
        PlotContainer.centerIn(this).apply {
            plot = frame
        }
        top {
            toolbar {
                val nameField = textfield()
                button("+"){
                    action {
                        createDataSet(nameField.text)
                    }
                }
            }
        }
        left {
            dataPane = tabpane()

        }
    }

    fun createDataSet(plotName: String) {
        val data = FXCollections.observableArrayList<PlotData>()
        dataMap.put(plotName, data)
        dataPane.tab(plotName) {
            setOnClosed {
                dataMap.remove(plotName)
            }
            borderpane {
                top {
                    toolbar {
                        button("+") {
                            action {
                                data.add(PlotData())
                            }
                        }
                    }
                }
                center {
                    tableview(data) {
                        isEditable = true
                        column("x", PlotData::x).useTextField(DoubleStringConverter())
                        column("y", PlotData::y).useTextField(DoubleStringConverter())
                        column("yErr", PlotData::yErr).useTextField(DoubleStringConverter())
                        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                        onEditCommit {
                            dataChanged(plotName)
                        }
                    }
                }
            }
        }
        dataChanged(plotName)
    }

    private fun dataChanged(plotName: String) {
        synchronized(this) {
            if (dataMap.containsKey(plotName)) {
                if (!frame.opt(plotName).isPresent) {
                    frame.add(PlottableData(plotName))
                }

                (frame.get(plotName) as PlottableData).fillData(dataMap[plotName]!!.stream().map { it.toValues() })
            } else {
                frame.remove(plotName)
            }
        }
    }
}