/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx;

import hep.dataforge.context.GlobalContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.function.Supplier;

/**
 * The root application for all separate javafx windows. It does not have its
 * own main window
 *
 * @author Alexander Nozik
 */
public class RootApplication extends Application {

    private static InstanceHolder holder;

    public static RootApplication instance() {
        synchronized (GlobalContext.instance()) {
            if (holder == null) {
                startup();
            }
            return holder.getInstance();
        }
    }

    public static Stage primaryStage() {

        synchronized (GlobalContext.instance()) {
            if (holder == null) {
                startup();
            }
            return holder.getStage();
        }
    }

    public static void startup() {
        holder = new InstanceHolder();
        new Thread(() -> {
            launch();
        }).start();
    }

    /**
     * Show new Stage in a separate window. Supplier should not show window, only construct stage.
     *
     * @param sup
     */
    public static void show(Supplier<Stage> sup) {
        Stage primaStage = primaryStage();
        Platform.runLater(() -> {
            Stage newStage = sup.get();
            newStage.initOwner(primaStage.getOwner());
            newStage.show();
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        holder.setStage(primaryStage);
        holder.setInstance(this);
    }

    @Override
    public void stop() throws Exception {
        holder.setStage(null);
        holder.setInstance(null);

    }

    private static class InstanceHolder {

        private RootApplication instance;
        private Stage stage;

        public synchronized RootApplication getInstance() {
            while (instance == null) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return instance;
        }

        public synchronized void setInstance(RootApplication instance) {
            this.instance = instance;
            notify();
        }

        public synchronized Stage getStage() {
            while (stage == null) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return stage;
        }

        public synchronized void setStage(Stage stage) {
            this.stage = stage;
            notify();
        }

        public boolean isEmpty() {
            return stage == null && instance == null;
        }
    }

}
