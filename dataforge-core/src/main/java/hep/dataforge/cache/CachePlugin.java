/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.cache;

import hep.dataforge.context.*;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.data.NamedData;
import hep.dataforge.goals.Goal;
import hep.dataforge.goals.GoalListener;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Alexander Nozik
 */
@PluginDef(name = "immutable", group = "hep.dataforge", info = "Data caching plugin")
public class CachePlugin extends BasicPlugin {

    private Predicate<Data> bypass = data -> false;
    private CacheManager manager;

    /**
     * Set immutable bypass condition for data
     *
     * @param bypass
     */
    public void setBypass(Predicate<Data> bypass) {
        this.bypass = bypass;
    }

    public Predicate<Data> getBypass() {
        return bypass;
    }

//    public void setManager(CacheManager manager) {
//        this.manager = manager;
//    }



    @Override
    public void attach(Context context) {
        super.attach(context);
        try {
            manager = Caching.getCachingProvider(context.getClassLoader()).getCacheManager();
            context.getLogger().info("Loaded immutable manager" + manager.toString());
        } catch (CacheException ex) {
            context.getLogger().warn("Cache provider not found. Will use default immutable implementation.");
            manager = new DefaultCacheManager(getContext(), getMeta());
        }
    }

    @Override
    public void detach() {
        super.detach();
        if (manager != null) {
            manager.close();
            manager = null;
        }
    }

    public synchronized CacheManager getManager() {
        if (manager == null) {
            throw new IllegalStateException("Cache plugin not attached");
        }
        return manager;
    }

    public <V> Data<V> cache(String cacheName, Meta id, Data<V> data) {
        if (bypass.test(data) || !Serializable.class.isAssignableFrom(data.type())) {
            return data;
        } else {
            Cache<Meta, V> cache = getCache(cacheName, data.type());
            Goal<V> cachedGoal = new Goal<V>() {
                CompletableFuture<V> result = new CompletableFuture<>();

                @Override
                public Stream<Goal<?>> dependencies() {
                    if (cache.containsKey(id)) {
                        return Stream.empty();
                    } else {
                        return Stream.of(data.getGoal());
                    }
                }

                @Override
                public void run() {
                    //TODO add executor
                    if (data.getGoal().isDone()) {
                        data.getInFuture().thenAccept(val -> result.complete(val));
                    } else if (cache.containsKey(id)) {
                        getLogger().info("Cached result found. Restoring data from immutable for id {}", id.hashCode());
                        CompletableFuture.supplyAsync(() -> cache.get(id)).whenComplete((res, err) -> {
                            if (res != null) {
                                result.complete(res);
                            } else {
                                evalData();
                            }

                            if (err != null) {
                                getLogger().error("Failed to load data from immutable", err);
                            }
                        });
                    } else {
                        evalData();
                    }
                }

                private void evalData() {
                    data.getGoal().run();
                    data.getGoal().result().whenComplete((res, err) -> {
                        if (err != null) {
                            result.completeExceptionally(err);
                        } else {
                            result.complete(res);
                            try {
                                cache.put(id, res);
                            } catch (Exception ex) {
                                getContext().getLogger().error("Failed to put result into the immutable", ex);
                            }
                        }
                    });
                }

                @Override
                public CompletableFuture<V> result() {
                    return result;
                }

                @Override
                public boolean isRunning() {
                    return !result.isDone();
                }

                @Override
                public void registerListener(GoalListener<V> listener) {
                    //do nothing
                }
            };
            return new Data<V>(cachedGoal, data.type(), data.getMeta());
        }
    }

    public <V> DataNode<V> cacheNode(String cacheName, Meta nodeId, DataNode<V> node) {
        DataTree.Builder<V> builder = DataTree.builder(node.type()).setName(node.getName()).setMeta(node.getMeta());
        //recursively caching nodes
        node.nodeStream(false).forEach(child -> {
            builder.putNode(cacheNode(Name.joinString(cacheName, child.getName()), nodeId, child));
        });
        //caching direct data children
        node.dataStream(false).forEach((NamedData<? extends V> datum) -> {
            builder.putData(datum.getName(), cache(cacheName, nodeId.getBuilder().setValue("dataName", datum.getName()), datum));
        });
        return builder.build();
    }

    private <V> Cache<Meta, V> getCache(String name, Class<V> type) {
        Cache<Meta, V> cache = getManager().getCache(name, Meta.class, type);
        if (cache == null) {
            cache = getManager().createCache(name, new MetaCacheConfiguration<>(getMeta(), type));
        }
        return cache;
    }

//    @Override
//    protected synchronized void applyConfig(Meta config) {
//        //reset the manager
//        if (manager != null) {
//            manager.close();
//        }
//        manager = null;
//        super.applyConfig(config);
//    }

    public void invalidate(String cacheName) {
        getManager().destroyCache(cacheName);
    }

    public void invalidate() {
        getManager().getCacheNames().forEach(this::invalidate);
    }

    public static class Factory implements PluginFactory {

        @Override
        public PluginTag getTag() {
            return Plugin.resolveTag(CachePlugin.class);
        }

        @Override
        public Plugin build(Meta meta) {
            return new CachePlugin();
        }
    }
}
