package hep.dataforge.kodex.fx

import hep.dataforge.context.Global
import hep.dataforge.goals.Goal
import javafx.application.Platform
import javafx.scene.image.Image
import java.util.concurrent.Executor
import java.util.function.BiConsumer

val dfIcon: Image = Image(Global::class.java.getResourceAsStream("/img/df.png"))

val uiExecutor = Executor { command -> Platform.runLater(command) }

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