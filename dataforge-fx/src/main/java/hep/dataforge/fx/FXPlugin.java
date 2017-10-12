package hep.dataforge.fx;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.context.PluginDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.values.Value;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static hep.dataforge.values.ValueType.BOOLEAN;

/**
 * Plugin holding JavaFX application instance and its root stage
 * Created by darksnake on 28-Oct-16.
 */
@PluginDef(name = "fx", group = "hep.dataforge", info = "JavaFX window manager")
@ValueDef(name = "implicitExit", type = {BOOLEAN}, def = "false", info = "A Platform implicitExit parameter")
public class FXPlugin extends BasicPlugin {

    /**
     * the parent stage for all windows
     */
    private Stage parent;
    private Set<Stage> windows = new HashSet<>();

    @Override
    public void attach(Context context) {
        if (parent == null) {
            configureValue("implicitExit", false);
            context.getLogger().debug("FX application not found. Starting application surrogate.");
            ApplicationSurrogate.start();
            parent = ApplicationSurrogate.getStage();
        }
        super.attach(context);
    }

    @Override
    public void detach() {
        if (getContext() == Global.instance()) {
            if (windows.isEmpty()) {
                Platform.exit();
            } else {
                Platform.setImplicitExit(true);
            }
        }

        //close all windows
        Platform.runLater(() -> {
            windows.forEach(Stage::close);
        });


        super.detach();
    }

    @Override
    protected void applyValueChange(String name, Value oldItem, Value newItem) {
        super.applyValueChange(name, oldItem, newItem);
        if (Objects.equals(name, "implicitExit")) {
            Platform.setImplicitExit(newItem.booleanValue());
        }
    }

    public synchronized Stage getParent() {
        if (getContext() == null) {
            throw new RuntimeException("Plugin not attached");
        }
        return parent;
    }

    public synchronized void setParent(Stage parent) {
        this.parent = parent;
    }

    private synchronized void addStage(Stage stage) {
        Platform.setImplicitExit(true);
        this.windows.add(stage);
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
    public Stage show(Consumer<Stage> cons) {
        CompletableFuture<Stage> promise = new CompletableFuture<>();
        FXUtils.runNow(() -> {
            Stage stage = new Stage();
            stage.initOwner(getParent().getOwner());
            cons.accept(stage);
            addStage(stage);
            stage.show();
            promise.complete(stage);
        });
        return promise.join();
    }

}
