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
    private static boolean toolkitInitialized = false;

    //used to determine if any windows been spawn
    private static boolean hasChildren = false;

    private Application app;
    private Stage stage;

    public FXPlugin(@NotNull Application app, @NotNull Stage stage) {
        toolkitInitialized = true;
        this.app = app;
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
        if (this.app == null) {
            context.getLogger().info("JavaFX application not defined. Starting default stage holder.");
            startDefaultApp();
        }
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

    public synchronized Application getApp() {
        waitForApp();
        return app;
    }

    protected synchronized void setApplication(Application instance) {
        this.app = instance;
        notify();
    }

    private synchronized void waitForApp() {
        if (app == null) {
            startDefaultApp();
        }
        try {
            while (stage == null || app == null) {
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
            } else {
                Platform.runLater(() -> {
                    try {
                        new DefaultRootApplication().start(new Stage());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } else {
            getContext().getLogger().error("Trying to setup default fx stage holder over existing one");
        }
    }


    public static class DefaultRootApplication extends Application {

        private static FXPlugin plugin;

        @Override
        public void start(Stage primaryStage) throws Exception {
            plugin.setStage(primaryStage);
            plugin.setApplication(this);
        }

        @Override
        public void stop() throws Exception {
            plugin.setStage(null);
            plugin.setApplication(null);
        }
    }
}
