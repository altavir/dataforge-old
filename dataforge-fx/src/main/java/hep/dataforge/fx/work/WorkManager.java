/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.fx.work;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Alexander Nozik
 */
@PluginDef(name = "work", group = "hep.dataforge", info = "Visual process manager")
public class WorkManager extends BasicPlugin {

    /**
     * root process
     */
    private Work root;


    @Override
    public void attach(Context context) {
        super.attach(context);
        root = new Work(this, context.getName());
        //TODO Add root work to children of parent context work
    }

    protected Work buildWork(String processName, CompletableFuture future) {
        return getRoot().addChild(processName, future);
    }

    /**
     * Get or create
     *
     * @param processName
     * @return
     */
    public Work getWork(String processName) {
        Work res = root.find(processName);
        return res == null ? getRoot().addChild(processName) : res;
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
        getContext().parallelExecutor().execute(() -> {
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
    public Work startWork(String processName, Runnable runnable) {
        return this.buildWork(processName, CompletableFuture.runAsync(runnable, (Runnable command) -> execute(processName, command)));
    }

    public Work startWork(String processName, Consumer<Work> con) {
        Work work = getWork(processName);
        work.setFuture(CompletableFuture.runAsync(() -> con.accept(work), (Runnable command) -> execute(processName, command)));
        return work;
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
//        if (root.isDone() && parallelExecutor != null) {
//            getContext().getLogger().info("All processes complete.");
//        }

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
     * terminate all works and shutdown executors
     */
    public void shutdown() {
        this.root.cancel(true);
    }

}
