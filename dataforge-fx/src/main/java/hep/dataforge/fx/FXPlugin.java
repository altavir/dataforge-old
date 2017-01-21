package hep.dataforge.fx;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.context.PluginDef;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Plugin holding JavaFX application instance and its root stage
 * Created by darksnake on 28-Oct-16.
 */
@PluginDef(name = "fx", group = "hep.dataforge", description = "JavaFX application holder")
public class FXPlugin extends BasicPlugin {

    //used to determine if application toolkit is initialized
    private boolean toolkitInitialized = false;

    //used to determine if any windows been spawn
    private boolean hasChildren = false;

    private Stage stage;

    public FXPlugin(@NotNull Stage stage) {
        toolkitInitialized = true;
        this.stage = stage;
    }

    public FXPlugin() {

    }

    @Override
    public void attach(Context context) {
        if (!context.equals(Global.instance())) {
            context.getLogger().warn("Starting fx plugin not in global context");
        }
        Platform.setImplicitExit(false);
        super.attach(context);
    }

    @Override
    public void detach() {
        super.detach();
        if (hasChildren) {
            Platform.setImplicitExit(true);
        } else {
            Platform.exit();
        }
    }

    private synchronized void waitForApp() {
        if (!toolkitInitialized) {
            getContext().getLogger().info("JavaFX application not started. Starting default stage holder.");
            startDefaultApp();
        }
        try {
            while (stage == null || !toolkitInitialized) {
                wait(2000);
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized Stage getStage() {
        waitForApp();
        return stage;
    }

    public synchronized void setStage(Stage stage) {
        this.stage = stage;
        notify();
    }

    /**
     * Show new Stage in a separate window. Supplier should not show window, only construct stage.
     *
     * @param sup
     */
    public void show(Supplier<Stage> sup) {
        Stage primaStage = getStage();
        Platform.runLater(() -> {
            Stage newStage = sup.get();
            newStage.initOwner(primaStage.getOwner());
            newStage.show();
            hasChildren = true;
        });
    }

    private synchronized void startDefaultApp() {
        if (DefaultRootApplication.plugin == null) {
            DefaultRootApplication.plugin = this;
            if (!toolkitInitialized) {
                new Thread(() -> {
                    Application.launch(DefaultRootApplication.class);
                    toolkitInitialized = true;
                }).start();
            }
        } else {
            getContext().getLogger().error("Trying to setup default fx stage holder over existing one");
        }
    }


    /**
     * An application (Singleton) used when no apparent java fx application being started
     */
    public static class DefaultRootApplication extends Application {

        private static FXPlugin plugin;

        @Override
        public void start(Stage primaryStage) throws Exception {
            plugin.setStage(primaryStage);
            plugin.toolkitInitialized = true;
        }
    }
}
