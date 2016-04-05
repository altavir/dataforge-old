/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.Name;
import hep.dataforge.names.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An interface to store current executions in process manager. It represents a
 * future with number of child processes. Parent process is completed when all
 * the child processes are completed. When parent is canceled, child processes
 * are also canceled.
 *
 * @author Alexander Nozik
 */
public class Process<T> implements Future<T>, Named {

    /**
     * An empty process that does not perform any actions, but can hold children
     *
     * @param name
     * @return
     */
    static Process<Void> empty(String name) {
        return new Process(name, CompletableFuture.completedFuture(null));
    }

    private final Map<String, Process> children = new HashMap<>();
    private final String name;
    private final CompletableFuture<T> future;

    Process(String name, CompletableFuture<T> future) {
        this.name = name;
        this.future = future;
    }

    public Collection<Process> subProcesses() {
        return children.values();
    }

    /**
     * Find a subprocess with given relative name
     *
     * @param name
     * @return
     */
    public Process findProcess(Name name) {
        if (this.children.containsKey(name.toString())) {
            return children.get(name.toString());
        } else if (children.containsKey(name.getFirst().toString())) {
            return children.get(name.getFirst().toString()).findProcess(name.cutFirst());
        } else {
            throw new NameNotFoundException(name.toString());
        }
    }

    /**
     * Add a subprocess to this process. Child name is inherited from this
     * process name as {@code name.childName}
     *
     * @param name
     * @param future
     * @return
     */
    <U> Process<U> createChild(Name childName, CompletableFuture<U> future) {
        if (childName.length() == 1) {
            if (children.containsKey(childName.toString()) && !children.get(childName.toString()).isDone()) {
                throw new RuntimeException("Process with given name already running");
            } else {
                Process child = new Process(Name.of(getName()).append(childName).toString(), future);
                this.children.put(childName.toString(), child);
                return child;
            }
        } else {
            String first = childName.getFirst().toString();
            Process child;
            if(children.containsKey(first)){
                child = children.get(first); 
            } else {
                child = empty(Name.of(getName()).append(first).toString());
                children.put(first, child);
            }
            return child.createChild(childName.cutFirst(), future);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning)
                && children.values().stream()
                .map((Process child) -> child.cancel(mayInterruptIfRunning)).allMatch(b -> b == true);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * Shows if this process is either completed successfully or canceled
     *
     * @return
     */
    @Override
    public boolean isDone() {
        return future.isDone() && children.values().stream()
                .map((Process child) -> child.isDone()).allMatch(b -> b == true);
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public String getName() {
        return name;
    }

//    /**
//     * Remove finished processes from children
//     */
//    public void cleanup(){
//        this.children.r
//    }
    
}
