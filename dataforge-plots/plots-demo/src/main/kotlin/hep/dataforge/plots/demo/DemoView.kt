package hep.dataforge.plots.demo

import hep.dataforge.plots.data.PlottableData
import hep.dataforge.plots.fx.PlotContainer
import hep.dataforge.plots.jfreechart.JFreeChartFrame
import hep.dataforge.tables.ValueMap
import hep.dataforge.tables.XYAdapter
import hep.dataforge.values.Values
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.scene.control.TableView
import javafx.util.converter.DoubleStringConverter
import tornadofx.*

class DemoView : View("Plot demonstration") {
    private val plottable = PlottableData("default");

    private val frame = JFreeChartFrame().apply {
        add(plottable)
    }


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
    val data = FXCollections.observableArrayList<PlotData>()

    override val root = borderpane {
        PlotContainer.centerIn(this).apply {
            plot = frame
        }
        left {
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
                            dataChanged()
                        }
                    }
                }
            }
        }
    }

    fun dataChanged() {
        plottable.fillData(data.stream().map { it.toValues() })
    }
}
