package hep.dataforge.fx

import hep.dataforge.context.Global
import hep.dataforge.goals.Goal
import hep.dataforge.kodex.Coal
import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.image.Image
import kotlinx.coroutines.experimental.CommonPool
import tornadofx.*
import java.util.*
import java.util.concurrent.Executor
import java.util.function.BiConsumer
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.CoroutineContext

val dfIcon: Image = Image(Global::class.java.getResourceAsStream("/img/df.png"))

val uiExecutor = Executor { command -> Platform.runLater(command) }

class GoalMonitor {
    val titleProperty = SimpleStringProperty("")
    var title by titleProperty

    val messageProperty = SimpleStringProperty("")
    var message by messageProperty

    val progressProperty = SimpleDoubleProperty(1.0)
    var progress by progressProperty

    val maxProgressProperty = SimpleDoubleProperty(1.0)
    var maxProgress by maxProgressProperty

    fun updateProgress(progress: Double, maxProgress: Double) {
        this.progress = progress
        this.maxProgress = maxProgress
    }
}

private val monitors: MutableMap<UIComponent, MutableMap<String, GoalMonitor>> = HashMap();

/**
 * Get goal monitor for give UI component
 */
fun UIComponent.getMonitor(id: String): GoalMonitor {
    synchronized(monitors) {
        return monitors.getOrPut(this) {
            HashMap()
        }.getOrPut(id) {
            GoalMonitor()
        }
    }
}

fun <R> UIComponent.runGoal(id: String, dispatcher: CoroutineContext = CommonPool, block: suspend GoalMonitor.() -> R): Coal<R> {
    val monitor = getMonitor(id);
    return Coal<R>(Collections.emptyList(), dispatcher, id) { block.invoke(monitor) }.start();
}

infix fun <R> Goal<R>.ui(func: (R) -> Unit): Goal<R> {
    this.onComplete(uiExecutor, BiConsumer { res, err ->
        if (res != null) {
            func.invoke(res);
        }
    });
    return this;
}




//fun TableView<Values>.table(table: Table){
//
//}