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
    private final Process rootProcess = Process.empty("root");

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

    protected <U> Process<U> buildProcess(String processName, CompletableFuture<U> future) {
        return rootProcess.createChild(Name.of(processName), future);
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

    public synchronized <U> Process<U> post(String processName, Supplier<U> sup) {
        getContext().getLogger().debug("Posting proess with name '{}' to the process manager", processName);
        CompletableFuture<U> future = CompletableFuture.supplyAsync(sup, (Runnable command) -> execute(processName, command))
                .whenComplete((U res, Throwable ex) -> {
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

    protected void onProcessStarted(String processName) {
        getContext().getLogger().debug("Process '{}' started", processName);
    }

    protected void onProcessFinished(String processName) {
        getContext().getLogger().debug("Process '{}' finished", processName);
    }

    protected void onProcessException(String processName, Throwable exception) {
        getContext().getLogger().debug("Process '{}' finished with exception: {}", processName, exception.getMessage());
    }

    protected void onProcessResult(String processName, Object result) {
        getContext().getLogger().debug("Process '{}' produced a result: {}", processName, result);
    }

    protected void updateProgress(String processName, double progress, String status) {

    }

//
//    private ScheduledExecutorService executor;
//    private Map<String, ThreadGroup> groups;
//    private Map<String, Thread> threads;
//    private final ThreadGroup root;
//
//
//
//    /**
//     * Lazily initialized executor service
//     *
//     * @return
//     */
//    public ScheduledExecutorService internalExecutor() {
//        if (executor == null) {
//            executor = buildExecutor();
//            context.getLogger().info("Started executor service in context {}", getContext().getName());
//        }
//        return executor;
//    }
//
//    protected ScheduledExecutorService buildExecutor() {
//        return new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
//    }
//
//    /**
//     * Post anonymous task on general thread pool
//     *
//     * @param task
//     * @return
//     */
//    public Future post(Runnable task) {
//        return internalExecutor().post(task);
//    }
//
//    /**
//     * Post anonymous task on general thread pool
//     *
//     * @param task
//     * @return
//     */
//    public <T> Future<T> post(Callable<T> task) {
//        return internalExecutor().post(task);
//    }
//
//    private ThreadGroup buildGroups(Name path) {
//        ThreadGroup currentRoot = root;
//        Name currentPath = path;
//        while (path.length() > 1) {
//            currentRoot = new ThreadGroup(currentRoot, currentPath.cutFirst().toString());
//            this.groups.put(currentPath.cutFirst().toString(), currentRoot);
//            currentPath = path.cutFirst();
//        }
//        return new ThreadGroup(currentRoot, path.toString());
//    }
//
//    /**
//     * Create a new named thread outside common thread pool but does not start
//     * it
//     *
//     * @param taskName
//     * @param task
//     * @return
//     */
//    public Thread createTask(String taskName, Runnable task) {
//        Name name = Name.of(taskName);
//        Thread thread;
//        if (name.length() == 1) {
//            thread = new Thread(root, task, name.toString());
//        } else {
//            thread = new Thread(buildGroups(name.cutLast()), task, name.getFirst().toString());
//        }
//        this.threads.put(taskName, thread);
//        return thread;
//    }
//
//    /**
//     * Create a new named thread outside common thread pool but does not and
//     * start it
//     *
//     * @param taskName
//     * @param task
//     */
//    public void runTask(String taskName, Runnable task) {
//        createTask(taskName, task).start();
//    }
//
//    /**
//     * Return a ThreadGroup with given name if it is registered, null otherwise
//     * @param name
//     * @return 
//     */
//    public ThreadGroup getThreadGroup(String name){
//        return this.groups.get(name);
//    }
//    
//    /**
//     * Return a Thread with given name if it is registered, null otherwise.
//     * @param name
//     * @return 
//     */
//    public Thread getThread(String name){
//        return this.threads.get(name);
//    }
}
