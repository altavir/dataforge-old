/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.work;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 * @param <T>
 */
public abstract class AbstractGoal<T> implements Goal<T> {

    //TODO replace RuntimeExceptions with specific exceptions
    public static String DEFAULT_SLOT = "";

    private final Map<String, Binding> bindings = new HashMap<>();
    private final CompletableFuture<T> result = new GoalResult<>();
    private final ExecutorService executor;
    private Future<?> computation;

    public AbstractGoal(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void bindInput(Goal dependency, String inputSlot) {
        if (!this.bindings.containsKey(inputSlot)) {
            createBinding(inputSlot, Object.class);
        }
        bindings.get(inputSlot).bind(dependency);
    }

    //PENDING add default bining results?
    protected final void createBinding(String slot, Binding binding) {
        this.bindings.put(slot, binding);
    }

    protected final void createBinding(String slot, Class type) {
        this.bindings.put(slot, new SimpleBinding(type));
    }

    protected final void createListBinding(String slot, Class type) {
        this.bindings.put(slot, new ListBinding(type));
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    @Override
    public synchronized void start() {
        if (!isStarted()) {
            this.computation = executor.submit(() -> {
                try {
                    Map<String, ?> data = gatherData();
                    //check if goal is already complete externally
                    if (!result.isDone()) {
                        this.result.complete(compute(data));
                    } else {
                        getLogger().warn("Goal already complete");
                    }
                } catch (Exception ex) {
                    if (!result.isDone()) {
                        this.result.completeExceptionally(ex);
                    } else {
                        getLogger().warn("Goal already complete");
                    }
                }
            });
        }
    }

    /**
     * Abort internal computation process without canceling result. Use with care
     */
    protected void abort() {
        if(isStarted()){
            this.computation.cancel(true);
            this.computation = null;
        }
    }

    protected boolean isStarted() {
        return this.computation != null;
    }

    /**
     * Abort current computation if it is in progress and set result.
     * Useful for caching purposes.
     * @param result 
     */
    protected synchronized final void complete(T result) {
        abort();
        this.result.complete(result);
    }

    protected Map<String, ?> gatherData() {
        Map<String, Object> data = new ConcurrentHashMap<>();
        bindings.forEach((slot, binding) -> {
            if (!binding.isBound()) {
                throw new RuntimeException("Required slot " + slot + " not boud");
            }
            data.put(slot, binding.getResult());
        });
        return data;
    }

    protected abstract T compute(Map<String, ?> data);

    @Override
    public CompletableFuture<T> result() {
        return result;
    }

    protected interface Binding<T> {

        /**
         * Start bound goal and return its result
         *
         * @return
         */
        T getResult();

        boolean isBound();

        void bind(Goal goal);
    }

    protected class SimpleBinding<T> implements Binding<T> {

        private final Class<T> type;
        private Goal goal;

        public SimpleBinding(Class<T> type) {
            this.type = type;
        }

        @Override
        public T getResult() {
            goal.start();
            Object res = goal.result().join();
            if (type.isInstance(res)) {
                return (T) res;
            } else {
                throw new RuntimeException("Type mismatch in goal result");
            }
        }

        @Override
        public boolean isBound() {
            return goal != null;
        }

        @Override
        public synchronized void bind(Goal goal) {
            if (isBound()) {
                throw new RuntimeException("Goal already bound");
            }
            this.goal = goal;
        }
    }

    protected class ListBinding<T> implements Binding<Set<T>> {

        private final Class<T> type;
        private final Set<Goal> goals = new HashSet<>();

        public ListBinding(Class<T> type) {
            this.type = type;
        }

        @Override
        public Set<T> getResult() {
            return goals.stream().parallel().map(goal -> {
                goal.start();
                Object res = goal.result().join();
                if (type.isInstance(res)) {
                    return (T) res;
                } else {
                    throw new RuntimeException("Type mismatch in goal result");
                }
            }).collect(Collectors.toSet());
        }

        @Override
        public boolean isBound() {
            return !goals.isEmpty();
        }

        @Override
        public synchronized void bind(Goal goal) {
            this.goals.add(goal);
        }

    }
    
    protected class GoalResult<T> extends CompletableFuture<T>{
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if(mayInterruptIfRunning){
                abort();
            }
            return super.cancel(mayInterruptIfRunning); 
        }
    }

}
