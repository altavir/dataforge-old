/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.cache.CachePlugin;
import hep.dataforge.context.Context;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Meta;
import hep.dataforge.workspace.tasks.Task;
import hep.dataforge.workspace.tasks.TaskModel;
import org.jetbrains.annotations.NotNull;

/**
 * A basic data caching workspace
 *
 * @author Alexander Nozik
 */
public class BasicWorkspace extends AbstractWorkspace {

    private DataTree.Builder<Object> data = DataTree.builder();
    private transient CachePlugin cache;

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public DataNode<Object> getData() {
        return data.build();
    }

    protected boolean cacheEnabled() {
        return true;
    }

    public DataNode<Object> runTask(TaskModel model) {
        //Cache result if cache is available and caching is not blocked
        if (cacheEnabled() && model.meta().getBoolean("cache.enabled", true)) {
            Task<Object> task = getTask(model.getName());
            return getCache().cacheNode(model.getName(), model.getIdentity(), task.run(model));
        } else {
            return super.runTask(model);
        }
    }

    protected synchronized CachePlugin getCache() {
        if (cache == null || cache.getContext() != this.getContext()) {
            cache = getContext().optFeature(CachePlugin.class).orElseGet(() -> {
                CachePlugin pl = new CachePlugin();
                getContext().pluginManager().load(pl);
                return pl;
            });
        }
        return cache;
    }

    @Override
    public void clean() {
        getLogger().info("Cleaning up cache...");
        invalidateCache();
    }

    public void invalidateCache() {
        if (cacheEnabled()) {
            getCache().invalidate();
        }
    }


    public static class Builder implements Workspace.Builder {

        BasicWorkspace w = new BasicWorkspace();

        @Override
        public Builder self() {
            return this;
        }

        @Override
        public Context getContext() {
            return w.getContext();
        }

        @Override
        public Builder setContext(Context ctx) {
            w.setContext(ctx);
            return self();
        }

        private DataTree.Builder getData() {
            return w.data;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Builder loadData(String as, Data<?> data) {
            if (w.getData().optNode(as).isPresent()) {
                getLogger().warn("Overriding non-empty data during workspace data fill");
            }
            getData().putData(as, data);
            return self();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Builder loadData(String as, DataNode<?> datanode) {
            if (as == null || as.isEmpty()) {
                if (!w.data.isEmpty()) {
                    getLogger().warn("Overriding non-empty root data node during workspace construction");
                }
                w.data = new DataTree.Builder(datanode);
            } else {
                getData().putNode(as, datanode);
            }
            return self();
        }

        @Override
        public Builder loadTask(Task task) {
            w.tasks.put(task.getName(), task);
            return self();
        }

        @Override
        public Builder target(String name, Meta meta) {
            w.targets.put(name, meta);
            return self();
        }

        @Override
        public Workspace build() {
            return w;
        }

    }

}
