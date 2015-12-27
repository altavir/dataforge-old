/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import hep.dataforge.names.Name;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 *
 * @author Alexander Nozik
 */
public class ProcessManager implements Encapsulated {

    /**
     * A context for this plugin manager
     */
    private final Context context;
    private ScheduledExecutorService executor;
    private Map<String, ThreadGroup> groups;
    private Map<String, Thread> threads;
    private final ThreadGroup root;

    public ProcessManager(Context context) {
        this.context = context;
        root = new ThreadGroup(context.getName());
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    /**
     * Lazily initialized executor service
     *
     * @return
     */
    public ScheduledExecutorService getExecutor() {
        if (executor == null) {
            executor = buildExecutor();
            context.getLogger().info("Started executor service in context {}", getContext().getName());
        }
        return executor;
    }

    protected ScheduledExecutorService buildExecutor() {
        return new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Post anonymous task on general thread pool
     *
     * @param task
     * @return
     */
    public Future post(Runnable task) {
        return getExecutor().submit(task);
    }

    /**
     * Post anonymous task on general thread pool
     *
     * @param task
     * @return
     */
    public <T> Future<T> post(Callable<T> task) {
        return getExecutor().submit(task);
    }

    private ThreadGroup buildGroups(Name path) {
        ThreadGroup currentRoot = root;
        Name currentPath = path;
        while (path.length() > 1) {
            currentRoot = new ThreadGroup(currentRoot, currentPath.cutFirst().toString());
            this.groups.put(currentPath.cutFirst().toString(), currentRoot);
            currentPath = path.cutFirst();
        }
        return new ThreadGroup(currentRoot, path.toString());
    }

    /**
     * Create a new named thread outside common thread pool but does not start
     * it
     *
     * @param taskName
     * @param task
     * @return
     */
    public Thread createTask(String taskName, Runnable task) {
        Name name = Name.of(taskName);
        Thread thread;
        if (name.length() == 1) {
            thread = new Thread(root, task, name.toString());
        } else {
            thread = new Thread(buildGroups(name.cutLast()), task, name.getFirst().toString());
        }
        this.threads.put(taskName, thread);
        return thread;
    }

    /**
     * Create a new named thread outside common thread pool but does not and
     * start it
     *
     * @param taskName
     * @param task
     */
    public void runTask(String taskName, Runnable task) {
        createTask(taskName, task).start();
    }

    /**
     * Return a ThreadGroup with given name if it is registered, null otherwise
     * @param name
     * @return 
     */
    public ThreadGroup getThreadGroup(String name){
        return this.groups.get(name);
    }
    
    /**
     * Return a Thread with given name if it is registered, null otherwise.
     * @param name
     * @return 
     */
    public Thread getThread(String name){
        return this.threads.get(name);
    }
}
