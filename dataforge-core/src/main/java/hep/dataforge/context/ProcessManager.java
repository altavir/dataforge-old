/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import hep.dataforge.names.Name;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 *
 * @author Alexander Nozik
 */
public class ProcessManager implements Encapsulated {

    /**
     * root process map
     */
    private final Process rootProcess = new Process("root");

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
    }

    protected Process buildProcess(String processName, CompletableFuture future) {
        return rootProcess.addChild(processName, future);
    }
    
    public Process findProcess(String processName){
        return rootProcess.findProcess(processName);
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
    public synchronized Process post(String processName, Runnable runnable) {
        getContext().getLogger().debug("Posting proess with name '{}' to the process manager", processName);
        CompletableFuture future = CompletableFuture.runAsync(runnable, (Runnable command) -> execute(processName, command))
                .whenComplete((Object res, Throwable ex) -> {
                    onProcessFinished(processName);
                    if (ex != null) {
                        onProcessException(processName, ex);
                    }
                });
        onProcessStarted(processName);
        return buildProcess(processName, future);
    }

    public synchronized Process post(String processName, Supplier<?> sup) {
        getContext().getLogger().debug("Posting proess with name '{}' to the process manager", processName);
        CompletableFuture future = CompletableFuture.supplyAsync(sup, (Runnable command) -> execute(processName, command))
                .whenComplete((Object res, Throwable ex) -> {
                    onProcessFinished(processName);
                    if (res != null) {
                        onProcessResult(processName, res);
                    }
                    if (ex != null) {
                        onProcessException(processName, ex);
                    }
                });
        onProcessStarted(processName);
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
        new Thread(runnable, processName).start();
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
    }

    /**
     * This method is called internally on process exception
     *
     * @param processName
     * @param exception
     */
    protected void onProcessException(String processName, Throwable exception) {
        getContext().getLogger().debug("Process '{}' finished with exception: {}", processName, exception.getMessage());
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

    /**
     * Called externally to notify status change progress for the process
     *
     * @param processName
     * @param progress
     * @param message
     */
    public void updateProgress(String processName, double progress, double maxProgress) {
        //TODO check calling thread

    }

    /**
     * Called externally to notify status change message for the process
     *
     * @param processName
     * @param progress
     * @param message
     */
    public void updateMessage(String processName, String message) {
        //TODO check calling thread

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

    public static interface ProgressCallback {
        public void updateProgress(String processName, double progress, double maxProgress);
        public void updateMessage(String processName, String message);
    }
}
