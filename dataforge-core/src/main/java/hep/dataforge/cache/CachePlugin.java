/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.cache;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.PluginDef;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.data.NamedData;
import hep.dataforge.description.ValueDef;
import hep.dataforge.goals.Goal;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.workspace.identity.Identity;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Alexander Nozik
 */
@PluginDef(name = "dataCache", group = "hep.dataforge", description = "Data caching plugin")
@ValueDef(name = "cacheManager", info = "The fully qualified name of cache manager class")
public class CachePlugin extends BasicPlugin {

    private Predicate<Data> bypass = data -> false;
    private CacheManager manager;

    /**
     * Set cache bypass condition for data
     *
     * @param bypass
     */
    public void setBypass(Predicate<Data> bypass) {
        this.bypass = bypass;
    }

    public Predicate<Data> getBypass() {
        return bypass;
    }

    public void setManager(CacheManager manager) {
        this.manager = manager;
    }

    public synchronized CacheManager getManager() {
        if (manager == null) {
            if (getConfig().hasValue("cacheManager")) {
                try {
                    manager = (CacheManager) Class.forName(getConfig().getString("cacheManager")).newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load explicit cache manager", e);
                }
            } else {
                manager = new DefaultCacheManager(getContext());
            }
        }
        return manager;
    }

    public <V> Data<V> cache(String cacheName, Identity id, Data<V> data) {
        if (bypass.test(data)) {
            return data;
        } else {
            Cache<Identity, V> cache = getCache(cacheName, data.type());
            Goal<V> cachedGoal = new Goal<V>() {
                CompletableFuture<V> result = new CompletableFuture<V>();

                @Override
                public Stream<Goal> dependencies() {
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
                        //TODO check for exception or cancellation
                        result.complete(data.get());
                    } else if (cache.containsKey(id)) {
                        CompletableFuture.supplyAsync(() -> cache.get(id)).whenComplete((res, err) -> {
                            if (err != null) {
                                //TODO reevaluate on cache failure
                                result.completeExceptionally(err);
                            } else {
                                result.complete(res);
                            }
                        });
                    } else {
                        data.getGoal().run();
                        data.getGoal().result().whenComplete((res, err) -> {
                            if (err != null) {
                                result.completeExceptionally(err);
                            } else {
                                cache.put(id, res);
                                result.complete(res);
                            }
                        });
                    }
                }

                @Override
                public CompletableFuture<V> result() {
                    return result;
                }

                @Override
                public void onStart(Runnable hook) {
                    //ignore
                }
            };
            return new Data<V>(cachedGoal, data.type(), data.meta());
        }
    }

    public <V> DataNode<V> cacheNode(String cacheName, Identity nodeId, DataNode<V> node) {
        DataTree.Builder<V> builder = DataTree.builder(node.type()).setName(node.getName()).setMeta(node.getMeta());
        //recursively caching nodes
        node.nodeStream(false).forEach(child -> {
            builder.putNode(cacheNode(Name.joinString(cacheName, child.getName()), nodeId, child));
        });
        //caching direct data children
        node.dataStream(false).forEach((NamedData<? extends V> datum) -> {
            builder.putData(datum.getName(), cache(cacheName, nodeId.and(datum.getName()), datum));
        });
        return builder.build();
    }

    private <V> Cache<Identity, V> getCache(String name, Class<V> type) {
        return getManager().getCache(name, Identity.class, type);
    }

    @Override
    protected synchronized void applyConfig(Meta config) {
        if (manager != null) {
            manager.close();
        }
        manager = null;
        super.applyConfig(config);
    }

    public void invalidate(String cacheName) {
        getManager().destroyCache(cacheName);
    }

    public void invalidate() {
        getManager().close();
    }
}
