package hep.dataforge.fx.works;

import javafx.concurrent.Task;

import java.util.concurrent.CompletableFuture;

/**
 * The Worker wrapper for a CompletableFuture
 *
 * @param <V>
 */
public class FutureWorker<V> extends Task<V> {
    private final CompletableFuture<V> future;
    private Handler handler = new Handler();

    public FutureWorker(CompletableFuture<V> future) {
        this.future = future;
    }

    @Override
    protected V call() throws Exception {
        return future.get();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    public Handler getHandler() {
        return handler;
    }

    public class Handler {

        private Handler() {
        }

        public void setTitle(String title) {
            updateTitle(title);
        }

        public void setMessage(String message) {
            updateMessage(message);
        }

        public void setProgress(double done, double max) {
            updateProgress(done, max);
        }
    }

}
