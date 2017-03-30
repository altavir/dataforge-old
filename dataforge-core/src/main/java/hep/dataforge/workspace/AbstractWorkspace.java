/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.cache.CachePlugin;
import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractWorkspace implements Workspace {

    protected final Map<String, Task> tasks = new HashMap<>();
    protected final Map<String, Meta> targets = new HashMap<>();
    private Context context;
    private transient CachePlugin cache;

    @Override
    public <T> Task<T> getTask(String taskName) {
        if (!tasks.containsKey(taskName)) {
            throw new NameNotFoundException(taskName);
        }
        return tasks.get(taskName);
    }

    @Override
    public Stream<Task> getTasks() {
        return tasks.values().stream();
    }

    @Override
    public Stream<Meta> getTargets() {
        return targets.values().stream();
    }

    @Override
    public Meta getTarget(String name) {
        if (!targets.containsKey(name)) {
            throw new NameNotFoundException(name);
        }
        return targets.get(name);
    }

    @Override
    public boolean hasTarget(String name) {
        return targets.containsKey(name);
    }

    @Override
    public Context getContext() {
        return context;
    }

    protected void setContext(Context context) {
        this.context = context;
    }

    protected synchronized CachePlugin getCache() {
        if (cache == null || cache.getContext() != this.getContext()) {
            cache = context.pluginManager().getOrLoadPlugin("hep.dataforge:dataCache");
        }
        return cache;
    }

    @Override
    public <T> DataNode<T> runTask(TaskModel model) {
        Task<T> task = getTask(model.getName());
        //Cache result if cache is available and caching is not blocked
        if (cacheEnabled() && model.meta().getBoolean("cache.enabled", true)) {
            return getCache().cacheNode(model.getName(), model.getIdentity(),task.run(model));
        } else {
            return task.run(model);
        }
    }

    protected boolean cacheEnabled() {
        return true;
    }

    @Override
    public void clean() {
        getContext().getLogger().info("Cleaning up cache...");
        invalidateCache();
    }

    public void invalidateCache() {
        if (cacheEnabled()) {
            getCache().invalidate();
        }
    }
}
