package hep.dataforge.fx

import hep.dataforge.context.BasicPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.context.PluginDef
import hep.dataforge.description.ValueDef
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType.BOOLEAN
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import tornadofx.*
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Plugin holding JavaFX application instance and its root stage
 * Created by darksnake on 28-Oct-16.
 */
@PluginDef(name = "fx", group = "hep.dataforge", info = "JavaFX window manager")
@ValueDef(name = "implicitExit", type = arrayOf(BOOLEAN), def = "false", info = "A Platform implicitExit parameter")
class FXPlugin : BasicPlugin() {

    /**
     * the parent stage for all windows
     */
    private var parent: Stage? = null
    private val windows = HashSet<Stage>()

    override fun attach(context: Context) {
        if (parent == null) {
            configureValue("implicitExit", false)
            context.logger.debug("FX application not found. Starting application surrogate.")
            ApplicationSurrogate.start()
            parent = ApplicationSurrogate.stage
        }
        super.attach(context)
    }

    override fun detach() {
        if (context === Global.instance()) {
            if (windows.isEmpty()) {
                Platform.exit()
            } else {
                Platform.setImplicitExit(true)
            }
        }

        //close all windows
        Platform.runLater { windows.forEach(Consumer { it.close() }) }


        super.detach()
    }

    override fun applyValueChange(name: String, oldItem: Value, newItem: Value) {
        super.applyValueChange(name, oldItem, newItem)
        if (name == "implicitExit") {
            Platform.setImplicitExit(newItem.booleanValue())
        }
    }

    private fun getParent(): Stage? {
        if (context == null) {
            throw RuntimeException("Plugin not attached")
        }
        return parent
    }

    @Synchronized
    fun setParent(parent: Stage) {
        this.parent = parent
    }

    @Synchronized private fun addStage(stage: Stage) {
        Platform.setImplicitExit(true)
        this.windows.add(stage)
    }

    //    /**
    //     * Show new Stage in a separate window. Supplier should not show window, only construct stage.
    //     *
    //     * @param sup
    //     */
    //    public void show(Supplier<Stage> sup) {
    //        Platform.runLater(() -> {
    //            Stage newStage = sup.get();
    //            newStage.initOwner(getStage().getOwner());
    //            addStage(newStage);
    //            newStage.show();
    //        });
    //    }

    /**
     * Show something in a pre-constructed stage. Blocks thread until stage is created
     *
     * @param cons
     */
    fun show(cons: Consumer<Stage>): Stage {
        val promise = CompletableFuture<Stage>()
        runLater {
            val stage = Stage()
            stage.initOwner(getParent()!!.owner)
            cons.accept(stage)
            addStage(stage)
            stage.show()
            promise.complete(stage)
        }
        return promise.join()
    }

    fun show(component: UIComponent) {
        val promise = CompletableFuture<Stage>()
        runLater {
            val stage = Stage()
            stage.initOwner(getParent()!!.owner)
            stage.scene = Scene(component.root)
            addStage(stage)
            stage.show()
            promise.complete(stage)
        }
        promise.join()
    }

}
