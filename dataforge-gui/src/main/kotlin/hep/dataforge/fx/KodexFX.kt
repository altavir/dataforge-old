package hep.dataforge.fx

import hep.dataforge.context.Global
import hep.dataforge.goals.Goal
import hep.dataforge.kodex.Coal
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import kotlinx.coroutines.experimental.CommonPool
import tornadofx.*
import java.util.*
import java.util.concurrent.Executor
import java.util.function.BiConsumer
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.CoroutineContext

val dfIcon: Image = Image(Global::class.java.getResourceAsStream("/img/df.png"))
val dfIconView = ImageView(dfIcon)

val uiExecutor = Executor { command -> Platform.runLater(command) }

class GoalMonitor {
    val titleProperty = SimpleStringProperty("")
    var title: String by titleProperty

    val messageProperty = SimpleStringProperty("")
    var message: String by messageProperty

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

/**
 * Clean up monitor
 */
private fun removeMonitor(component: UIComponent, id: String) {
    synchronized(monitors) {
        monitors[component]?.remove(id)
        if (monitors[component]?.isEmpty() == true) {
            monitors.remove(component)
        }
    }
}

fun <R> UIComponent.runGoal(id: String, dispatcher: CoroutineContext = CommonPool, block: suspend GoalMonitor.() -> R): Coal<R> {
    val monitor = getMonitor(id);
    return Coal(Collections.emptyList(), dispatcher, id) { block.invoke(monitor) }
            .apply {
                onComplete { _, _ ->
                    removeMonitor(this@runGoal, id)
                }
                run()
            }
}

infix fun <R> Goal<R>.ui(action: (R) -> Unit): Goal<R> {
    return this.apply {
        onComplete(uiExecutor, BiConsumer { res, _ ->
            if (res != null) {
                action(res);
            }
        })
    }
}

infix fun <R> Goal<R>.except(action: (Throwable) -> Unit): Goal<R> {
    return this.apply {
        onComplete(uiExecutor, BiConsumer { _, ex ->
            if (ex != null) {
                action(ex);
            }
        })
    }
}

/**
 * Add a listener that performs some update action on any window size change
 *
 * @param component
 * @param action
 */
fun addWindowResizeListener(component: Region, action: Runnable) {
    component.widthProperty().onChange { action.run() }
    component.heightProperty().onChange { action.run() }
}

fun colorToString(color: Color): String {
    return String.format("#%02X%02X%02X",
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt())
}

/**
 * Check if current thread is FX application thread to avoid runLater from
 * UI thread.
 *
 * @param r
 */
fun runNow(r: Runnable) {
    if (Platform.isFxApplicationThread()) {
        r.run()
    } else {
        Platform.runLater(r)
    }
}

fun UIComponent.bindWindow(owner: Node, toggle: BooleanProperty) {
    toggle.onChange {
        val stage = openWindow(owner = owner.scene.window)
        if (it) {
            stage?.show()
        } else {
            stage?.hide()
        }
        stage?.showingProperty()?.onChange {
            toggle.set(false)
        }
    }
}

//fun TableView<Values>.table(table: Table){
//
//}