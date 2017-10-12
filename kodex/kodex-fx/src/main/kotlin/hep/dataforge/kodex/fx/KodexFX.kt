package hep.dataforge.kodex.fx

import hep.dataforge.context.Global
import hep.dataforge.tables.Table
import hep.dataforge.values.Values
import javafx.scene.control.TableView
import javafx.scene.image.Image

val dfIcon: Image = Image(Global::class.java.getResourceAsStream("/img/df.png"))


fun TableView<Values>.table(table: Table){

}