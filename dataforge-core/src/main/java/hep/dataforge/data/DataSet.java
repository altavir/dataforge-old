/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.navigation.AbstractProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 * A simple static representation of DataNode
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class DataSet<T> extends AbstractProvider implements DataNode<T> {

    /**
     * The builder bound by type of data
     *
     * @param <T>
     * @param type
     * @return
     */
    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    /**
     * Unbound builder
     *
     * @return
     */
    public static Builder builder() {
        return new Builder<>(Object.class);
    }

    private final String name;
    private final Meta meta;
    private final Class<T> type;
    private final Map<String, Data<? extends T>> dataMap;

    protected DataSet(String name, Meta meta, Class<T> type, Map<String, Data<? extends T>> dataMap) {
        this.name = name;
        this.meta = meta;
        this.type = type;
        this.dataMap = dataMap;
    }

    @Override
    public Stream<Pair<String, Data<? extends T>>> stream() {
        return dataMap.entrySet().stream()
                .<Pair<String, Data<? extends T>>>map((Map.Entry<String, Data<? extends T>> entry)
                        -> new Pair<>(entry.getKey(), entry.getValue()));
    }

    @Override
    public Data<? extends T> getData(String name) {
        return dataMap.get(name);
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public Iterator<Data<? extends T>> iterator() {
        return dataMap.values().iterator();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Meta meta() {
        return meta;
    }

    @Override
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    @Override
    protected boolean provides(String target, Name name) {
        return DATA_TARGET.equals(target) && dataMap.containsKey(name.toString());
    }

    @Override
    protected Object provide(String target, Name name) {
        if (DATA_TARGET.equals(target)) {
            return dataMap.get(name.toString());
        } else {
            return null;
        }
    }

    @Override
    public DataNode<? extends T> getNode(String nodeName) {
        Builder<T> builder = new Builder<>(type)
                .setName(nodeName)
                .setMeta(meta());
        String prefix = nodeName + ".";
        stream()
                .filter((Pair<String, Data<? extends T>> pair) -> pair.getKey().startsWith(prefix))
                .forEach((Pair<String, Data<? extends T>> pair) -> builder.putData(pair.getKey(), pair.getValue()));
        if (builder.dataMap.size() > 0) {
            return builder.build();
        } else {
            return null;
        }
    }

    public static class Builder<T> {

        private String name = "";
        private Meta meta = Meta.empty();
        private final Class<T> type;
        private final Map<String, Data<? extends T>> dataMap = new LinkedHashMap<>();

        private Builder(Class<T> type) {
            this.type = type;
        }

        public Builder<T> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> setMeta(Meta meta) {
            this.meta = meta;
            return this;
        }

        public Builder<T> putData(String key, Data<? extends T> data) {
            if (type.isAssignableFrom(data.dataType())) {
                if (!dataMap.containsKey(key)) {
                    dataMap.put(key, data);
                } else {
                    throw new RuntimeException("The data with key " + key + " already exists");
                }
            } else {
                throw new RuntimeException("Data does not satisfy class boundary");
            }
            return this;
        }

        public Builder<T> putData(NamedData<? extends T> data) {
            return putData(data.getName(), data);
        }

        public Builder<T> putAll(Collection<NamedData<? extends T>> dataCollection) {
            dataCollection.stream().forEach(it -> putData(it));
            return this;
        }

        public Builder<T> putAll(Map<String, Data<? extends T>> map) {
            this.dataMap.putAll(map);
            return this;
        }

        public Builder<T> putStatic(String key, T staticData) {
            if (!type.isInstance(staticData)) {
                throw new IllegalArgumentException("The data mast be instance of " + type.getName());
            }
            return putData(new NamedStaticData<>(key, staticData, type));
        }

        public DataSet<T> build() {
            return new DataSet<>(name, meta, type, dataMap);
        }

    }

}
