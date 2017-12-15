package hep.dataforge.context;

import hep.dataforge.exceptions.ContextLockException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Lock class for context
 */
public class ContextLock implements ContextAware {

    private final Context context;
    /**
     * A set of objects that lock this context
     */
    private final Set<Object> lockers = new HashSet<>();

    public ContextLock(Context context) {
        this.context = context;
    }

    public synchronized void lock(Object object) {
        this.lockers.add(object);
    }

    public synchronized void unlock(Object object) {
        this.lockers.remove(object);
    }

    public boolean isLocked() {
        return !lockers.isEmpty();
    }

    /**
     * Throws {@link ContextLockException} if context is locked
     */
    public void tryModify(){
        lockers.stream().findFirst().ifPresent(lock -> {
            throw new ContextLockException(lock);
        });
    }

    /**
     * Apply thread safe lockable object modification
     *
     * @param mod
     */
    public synchronized <T> T modify(Callable<T> mod) {
        tryModify();
        try {
            return context.getDispatcher().submit(mod).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void modify(Runnable mod) {
        tryModify();
        try {
            context.getDispatcher().submit(mod).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Context getContext() {
        return context;
    }
}
