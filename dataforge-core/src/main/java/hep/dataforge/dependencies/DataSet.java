/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.dependencies;

import hep.dataforge.content.Named;
import hep.dataforge.meta.Meta;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 * A simple static representation of DataNode
 *
 * @author Alexander Nozik
 * @param <T>
 */
public class DataSet<T> implements DataNode<T> {

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
                .<Pair<String, Data<? extends T>>>map((Map.Entry<String, Data<? extends T>> entry) -> 
                        new Pair<>(entry.getKey(), entry.getValue()));
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

    public static class Builder<T> {

        private String name = "";
        private Meta meta = Meta.buildEmpty();
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
            if (type.isInstance(data.dataType())) {
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

        public DataSet<T> build() {
            return new DataSet<>(name, meta, type, dataMap);
        }

    }

}
