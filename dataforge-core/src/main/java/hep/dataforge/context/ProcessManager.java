/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import hep.dataforge.names.Name;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author Alexander Nozik
 */
public class ProcessManager implements Encapsulated {

    /**
     * root process map
     */
    private DFProcess rootProcess;

    /**
     * A context for this process manager
     */
    private Context context;

    protected ExecutorService executor;

    @Override
    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
        if (context.getParent() == null) {
            rootProcess = new DFProcess(this, "");
        } else {
            rootProcess = context.getParent().processManager().getRootProcess().addChild(context.getName(), null);
        }
    }

    protected DFProcess buildProcess(String processName, CompletableFuture future) {
        return rootProcess.addChild(processName, future);
    }

    public DFProcess findProcess(String processName) {
        return rootProcess.findProcess(processName);
    }

    public DFProcess getRootProcess() {
        return rootProcess;
    }

    /**
     * A public executor for process with given name. Any submitted command is
     * posted to the root process automatically
     *
     * @param processName
     * @return
     */
    public Executor executor(String... processName) {
        return (Runnable command) -> {
            post(Name.join(processName).toString(), command);
        };
    }

    /**
     * Post a runnable to the process manager as a started process
     *
     * @param processName
     * @param runnable
     * @return
     */
    public DFProcess post(String processName, Runnable runnable) {
        return post(processName, CompletableFuture.runAsync(runnable, (Runnable command) -> execute(processName, command)));
    }

    public <U> DFProcess<U> post(String processName, Supplier<U> sup) {
        return post(processName, CompletableFuture.supplyAsync(sup, (Runnable command) -> execute(processName, command)));
    }

    public DFProcess post(String processName, Consumer<Callback> con) {
        Callback callback = callback(processName);
        return post(processName, () -> con.accept(callback));
    }

    public <U> DFProcess<U> post(String processName, Function<Callback, U> func) {
        Callback callback = callback(processName);
        return post(processName, () -> func.apply(callback));
    }

    public <U> DFProcess<U> post(String processName) {
        getContext().getLogger().debug("Posting empty process with name '{}' to the process manager", processName);
        return buildProcess(processName, null);
    }

    public Callback callback(String processName) {
        return new Callback(this, processName);
    }

    /**
     * WARNING. Task comes with its own executor
     *
     * @param <U>
     * @param processName
     * @param task
     * @return
     */
    protected synchronized <U> DFProcess<U> post(String processName, CompletableFuture<U> task) {
        getContext().getLogger().debug("Posting process with name '{}' to the process manager", processName);
        CompletableFuture future = task
                .whenComplete((U res, Throwable ex) -> {
                    onProcessFinished(processName);
                    if (res != null) {
                        onProcessResult(processName, res);
                    }
                    if (ex != null) {
                        onProcessException(processName, ex);
                    }
                });
        return buildProcess(processName, future);
    }

    /**
     * Internal execution method. By default uses new thread for every new
     * process.
     *
     * @param processName
     * @param runnable
     */
    protected void execute(String processName, Runnable runnable) {
        getExecutor(processName).execute(() -> {
            Thread.currentThread().setName(processName);
            runnable.run();
        });
    }

    /**
     * Get executor for given process name. By default uses one thread pool
     * executor for all processes
     *
     * @param processName
     * @return
     */
    protected Executor getExecutor(String processName) {
        if (this.executor == null) {
            getContext().getLogger().info("Initializing executor");
            this.executor = Executors.newWorkStealingPool();
        }
        return executor;
    }

    /**
     * This method is called internally on process start
     *
     * @param processName
     */
    protected void onProcessStarted(String processName) {
        getContext().getLogger().debug("Process '{}' started", processName);
    }

    /**
     * This method is called internally on process finish (exceptionally or not)
     *
     * @param processName
     */
    protected void onProcessFinished(String processName) {
        getContext().getLogger().debug("Process '{}' finished", processName);
        updateProcess(processName, p -> p.setProgressToMax());
        synchronized (this) {
            if (rootProcess.isDone() && executor != null) {
                executor.shutdown();
                executor = null;
                getContext().getLogger().info("All processes complete. Shuting executor down");
            }
        }
    }

    /**
     * This method is called internally on process exception
     *
     * @param processName
     * @param exception
     */
    protected void onProcessException(String processName, Throwable exception) {
        getContext().getLogger().error(String.format("Process '%s' finished with exception: %s", processName, exception.getMessage()),
                exception);
    }

    /**
     * This method is called internally on process successful finish with result
     * (null result does not count)
     *
     * @param processName
     * @param result
     */
    protected void onProcessResult(String processName, Object result) {
        getContext().getLogger().debug("Process '{}' produced a result: {}", processName, result);
    }

    private synchronized void updateProcess(String processName, Consumer<DFProcess> consumer) {
        DFProcess p = rootProcess.findProcess(processName);
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
        findProcess(processName).cancel(interrupt);
    }

    /**
     * Clean completed processes for the root process
     */
    public void cleanup() {
        if (rootProcess != null) {
            this.rootProcess.cleanup();
        }
    }

    /**
     * A process manager callback
     */
    public static class Callback {

        private final ProcessManager manager;
        private final String processName;

        public Callback(ProcessManager manager, String processName) {
            this.manager = manager;
            this.processName = processName;
        }

        public ProcessManager getManager() {
            return manager;
        }

        public String processName() {
            return processName;
        }

        public DFProcess getProcess() {
            return getManager().findProcess(processName());
        }

        public void updateProcess(Consumer<DFProcess> consumer) {
            getManager().updateProcess(processName(), consumer);
        }

        public void setProgress(double progress) {
            updateProcess(p -> p.setProgress(progress));
        }

        public void setProgressToMax() {
            updateProcess(p -> p.setProgressToMax());
        }

        public void setMaxProgress(double progress) {
            updateProcess(p -> p.setMaxProgress(progress));
        }

        public void increaseProgress(double incProgress) {
            updateProcess(p -> p.increaseProgress(incProgress));
        }

        public void updateTitle(String title) {
            updateProcess(p -> p.setTitle(title));
        }

        public void updateMessage(String message) {
            updateProcess(p -> p.setMessage(message));
        }

    }
}
