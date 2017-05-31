package hep.dataforge.fx;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

/**
 * A surrogate JavaFX application to be used in case there is no global application present.
 * Created by darksnake on 23-Jan-17.
 */
public class ApplicationSurrogate extends Application {
    /**
     * The primary stage
     */
    private static CompletableFuture<Stage> stageGenerator;

    public static Stage getStage() {
        if (!isStarted()) {
            throw new RuntimeException("FX application surrogate not initialized");
        }
        try {
            return stageGenerator.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load FX surrogate primary stage", e);
        }
    }

    public static boolean isStarted() {
        return stageGenerator != null;
    }

    public static void start() {
        stageGenerator = new CompletableFuture<>();
        new Thread(()->launch(ApplicationSurrogate.class)).start();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stageGenerator.complete(stage);
    }

    @Override
    public void stop() throws Exception {
        stageGenerator = null;
        super.stop();
    }


}
