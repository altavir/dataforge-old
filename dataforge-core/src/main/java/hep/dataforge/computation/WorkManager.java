/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import hep.dataforge.context.Context;
import hep.dataforge.context.Encapsulated;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author Alexander Nozik
 */
public class WorkManager implements Encapsulated, WorkListener {

    protected ExecutorService parallelExecutor;
    protected ExecutorService singleThreadExecutor;
    /**
     * root process
     */
    private Work root;
    /**
     * A context for this process manager
     */
    private Context context;

    @Override
    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
        if (context.getParent() == null) {
            root = new Work(this, "");
            root.setTitle("ROOT");
        } else {
            root = context.getParent().workManager().getRoot().addChild(context.getName(), null);
        }
    }

    protected Work build(String processName, CompletableFuture future) {
        return root.addChild(processName, future);
    }

    public Work find(String processName) {
        return root.findProcess(processName);
    }

    public Work getRoot() {
        return root;
    }

    /**
     * Internal execution method. By default uses new thread for every new
     * process.
     *
     * @param processName
     * @param runnable
     */
    protected void execute(String processName, Runnable runnable) {
        parallelExecutor().execute(() -> {
            Thread.currentThread().setName(processName);
            runnable.run();
        });
    }

    /**
     * Post a runnable to the process manager as a started process
     *
     * @param processName
     * @param runnable
     * @return
     */
    public Work submit(String processName, Runnable runnable) {
        return WorkManager.this.submit(processName, CompletableFuture.runAsync(runnable, (Runnable command) -> execute(processName, command)));
    }
//
//    public <U> Work<U> post(String processName, Supplier<U> sup) {
//        return post(processName, CompletableFuture.supplyAsync(sup, (Runnable command) -> execute(processName, command)));
//    }

    public Work submit(String processName, Consumer<Callback> con) {
        Callback callback = callback(processName);
        return WorkManager.this.submit(processName, () -> con.accept(callback));
    }

    public Work submit(String processName) {
        getContext().getLogger().debug("Posting empty process with name '{}' to the process manager", processName);
        return build(processName, null);
    }
    
    /**
     * Post the task to the manager and return corresponding future;
     * @param <U>
     * @param processName
     * @param func
     * @return 
     */
    public <U> CompletableFuture<U> post(String processName, Function<Callback, U> func) {
        Callback callback = callback(processName);
        CompletableFuture<U> future = CompletableFuture.supplyAsync(()->func.apply(callback));
        submit(processName, future);
        return future;
    }    

    public Callback callback(String processName) {
        return new Callback(this, processName);
    }

    /**
     * WARNING. Task comes with its own parallelExecutor
     *
     * @param processName
     * @param task
     * @return
     */
    @Override
    public synchronized Work submit(String processName, CompletableFuture<?> task) {
        getContext().getLogger().debug("Posting process with name '{}' to the process manager", processName);
        return build(processName, task);
    }

    /**
     * Get parallelExecutor for given process name. By default uses one thread
     * pool parallelExecutor for all processes
     *
     * @return
     */
    public ExecutorService parallelExecutor() {
        if (this.parallelExecutor == null) {
            getContext().getLogger().info("Initializing parallel executor");
            this.parallelExecutor = Executors.newWorkStealingPool();
        }
        return parallelExecutor;
    }

    /**
     * An executor for tasks that do not allow parallelization
     *
     * @return
     */
    public ExecutorService singleThreadExecutor() {
        if (this.singleThreadExecutor == null) {
            getContext().getLogger().info("Initializing single thread executor");
            this.singleThreadExecutor = Executors.newSingleThreadExecutor();
        }
        return singleThreadExecutor;
    }

    /**
     * This method is called internally on process start
     *
     * @param processName
     */
    protected void onStarted(String processName) {
        getContext().getLogger().debug("Process '{}' started", processName);
    }

    /**
     * This method is called internally on process finish (exceptionally or not)
     *
     * @param processName
     */
    protected void onFinished(String processName) {
        getContext().getLogger().debug("Process '{}' finished", processName);
        if (root.isDone() && parallelExecutor != null) {
//            parallelExecutor.shutdown();
//            parallelExecutor = null;
            getContext().getLogger().info("All processes complete.");
        }

    }

    private synchronized void update(String processName, Consumer<Work> consumer) {
        Work p = root.findProcess(processName);
        if (p != null) {
            consumer.accept(p);
        } else {
            getContext().getLogger().warn("Can't find process with name {}", processName);
        }
    }

    /**
     * Externally cancel process with the given name. Empty name corresponds to
     * the root process
     *
     * @param processName
     * @param interrupt
     */
    public void cancel(String processName, boolean interrupt) {
        find(processName).cancel(interrupt);
    }

    /**
     * Clean completed processes for the root process
     */
    public void cleanup() {
        if (root != null) {
            this.root.cleanup();
        }
    }

    /**
     * Set current progress
     *
     * @param processName
     * @param progress
     */
    @Override
    public void setProgress(String processName, double progress) {
        update(processName, w -> w.setProgress(progress));
    }

    /**
     * Set max Progress
     *
     * @param processName
     * @param progress
     */
    @Override
    public void setMaxProgress(String processName, double progress) {
        update(processName, w -> w.setMaxProgress(progress));
    }

    /**
     * notify process finished (set current progress to max)
     *
     * @param processName
     */
    @Override
    public void finish(String processName) {
        update(processName, w -> w.setProgressToMax());
    }

    /**
     * Set process title
     *
     * @param processName
     * @param title
     */
    @Override
    public void updateTitle(String processName, String title) {
        update(processName, w -> w.setTitle(title));
    }

    /**
     * Set Process message
     *
     * @param processName
     * @param message
     */
    @Override
    public void updateMessage(String processName, String message) {
        update(processName, w -> w.setMessage(message));
    }

    /**
     * terminate all works and shutdown executors
     */
    public void shutdown() {
        this.root.cancel(true);
        if (parallelExecutor != null) {
            parallelExecutor.shutdownNow();
            parallelExecutor = null;
        }
        if (singleThreadExecutor != null) {
            singleThreadExecutor.shutdownNow();
            singleThreadExecutor = null;
        }
    }

    /**
     * A process manager callback
     */
    public static class Callback {

        private final WorkManager manager;
        private final String workName;

        public Callback(WorkManager manager, String processName) {
            this.manager = manager;
            this.workName = processName;
        }

        public WorkManager getManager() {
            return manager;
        }

        public String workName() {
            return workName;
        }

        public Work work() {
            return getManager().find(workName());
        }

        public void update(Consumer<Work> consumer) {
            getManager().update(workName(), consumer);
        }

        public void setProgress(double progress) {
            update(p -> p.setProgress(progress));
        }

        public void setProgressToMax() {
            update(p -> p.setProgressToMax());
        }

        public void setMaxProgress(double progress) {
            update(p -> p.setMaxProgress(progress));
        }

        public void increaseProgress(double incProgress) {
            update(p -> p.increaseProgress(incProgress));
        }

        public void updateTitle(String title) {
            update(p -> p.setTitle(title));
        }

        public void updateMessage(String message) {
            update(p -> p.setMessage(message));
        }

    }
}
