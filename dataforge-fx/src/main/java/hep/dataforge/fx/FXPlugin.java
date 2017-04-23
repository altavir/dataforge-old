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
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Plugin holding JavaFX application instance and its root stage
 * Created by darksnake on 28-Oct-16.
 */
@PluginDef(name = "fx", group = "hep.dataforge", description = "JavaFX window manager")
@ValueDef(name = "implicitExit", type = "BOOLEAN", def = "false", info = "A Platfor implicitExit parameter")
public class FXPlugin extends BasicPlugin {

    private Stage stage;
    private Set<Stage> windows = new HashSet<>();

    @Override
    public void attach(Context context) {
        if (stage == null) {
            configureValue("implicitExit", false);
            context.getLogger().debug("FX application not found. Starting application surrogate.");
            ApplicationSurrogate.start();
            stage = ApplicationSurrogate.getStage();
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
            windows.forEach(window -> window.close());
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

    public synchronized Stage getStage() {
        if (getContext() == null) {
            throw new RuntimeException("Plugin not attached");
        }
        return stage;
    }

    public synchronized void setStage(Stage stage) {
        this.stage = stage;
    }

    private synchronized void addStage(Stage stage) {
        this.windows.add(stage);
    }

    /**
     * Show new Stage in a separate window. Supplier should not show window, only construct stage.
     *
     * @param sup
     */
    public void show(Supplier<Stage> sup) {
        Platform.runLater(() -> {
            Stage newStage = sup.get();
            newStage.initOwner(getStage().getOwner());
            addStage(newStage);
            newStage.show();
        });
    }

    /**
     * Show something in a pre-constructed stage.
     *
     * @param cons
     */
    public void show(Consumer<Stage> cons) {
        Platform.runLater(() -> {
            Stage newStage = new Stage();
            newStage.initOwner(getStage().getOwner());
            cons.accept(newStage);
            addStage(newStage);
            newStage.show();
        });
    }

}
