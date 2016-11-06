/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.cache.CachePlugin;
import hep.dataforge.cache.DataCache;
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
    protected final Map<String, Meta> metas = new HashMap<>();
    private Context context;
    private DataCache cache;

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
    public Stream<Meta> getMetas() {
        return metas.values().stream();
    }

    @Override
    public Meta getMeta(String name) {
        if (!metas.containsKey(name)) {
            throw new NameNotFoundException(name);
        }
        return metas.get(name);
    }

    @Override
    public Context getContext() {
        return context;
    }

    protected void setContext(Context context) {
        this.context = context;
        this.cache = CachePlugin.buildFrom(context).getCache();
    }

//    public Meta getCachePolicy() {
//        return getMeta("@cachePolicy");
//    }

    @Override
    public <T> DataNode<T> runTask(TaskModel model) {
        Task<T> task = getTask(model.getName());
        //Cache result if cache is available and caching is not blocked
        if (cache != null && !model.meta().getBoolean("@noCache", false)) {
            return cache.cacheNode(task.run(model), model.getIdentity());
        } else {
            return task.run(model);
        }
    }

    @Override
    public void clean() {
        getContext().getLogger().info("Cleaning up cache...");
        invalidateCache();
    }

    public void invalidateCache() {
        if (cache != null) {
            cache.invalidate();
        }
    }
}
