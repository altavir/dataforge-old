/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple static representation of DataNode
 *
 * @param <T>
 * @author Alexander Nozik
 */
public class DataSet<T> implements DataNode<T> {

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
    public static Builder<Object> builder() {
        return new Builder<>(Object.class);
    }

    @Override
    public Stream<NamedData<? extends T>> dataStream(boolean recursive) {
        return dataMap.entrySet().stream()
                .filter(it -> recursive || !it.getKey().contains("."))
                .map(entry -> NamedData.wrap(entry.getKey(), entry.getValue(), getMeta()));
    }

    /**
     * {@inheritDoc }
     * <p>
     * Not very effective for flat data set
     * </p>
     *
     * @return
     */
    @Override
    public Stream<DataNode<? extends T>> nodeStream(boolean recursive) {
        if (recursive) {
            throw new Error("Not implemented");
        }
        return dataStream()
                .map(data -> Name.of(data.getName())) // converting strings to Names
                .filter(name -> name.getLength() > 1) //selecting only composite names
                .map(name -> name.getFirst().toString())
                .distinct()
                .map(str -> new DataSet<>(str, meta, type, subMap(str + ".")));
    }

    private Map<String, Data<? extends T>> subMap(String prefix) {
        return dataMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(entry -> entry.getKey().substring(prefix.length()), Map.Entry::getValue));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Data<T>> optData(String name) {
        return Optional.ofNullable(dataMap.get(name))
                .map(it -> NamedData.wrap(name, it, meta))
                .map(it -> (Data<T>) it);
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Meta getMeta() {
        if (meta == null) {
            return Meta.empty();
        } else {
            return meta;
        }
    }

    @Override
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    @Override
    public Optional<DataNode<T>> optNode(String nodeName) {
        Builder<T> builder = new Builder<>(type)
                .setName(nodeName)
                .setMeta(getMeta());
        String prefix = nodeName + ".";
        dataStream()
                .filter(data -> data.getName().startsWith(prefix))
                .forEach(data -> {
                    String dataName = Name.of(data.getName()).cutFirst().toString();
                    builder.putData(dataName, data.anonymize());
                });
        if (builder.dataMap.size() > 0) {
            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }
    }

    public static class Builder<T> implements DataNode.Builder<T, DataSet<T>, Builder<T>> {

        private final Class<T> type;
        private final Map<String, Data<? extends T>> dataMap = new LinkedHashMap<>();
        private String name = "";
        private Meta meta = Meta.empty();

        private Builder(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public Builder<T> setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder<T> setMeta(Meta meta) {
            this.meta = meta;
            return this;
        }

//        public Map<String, Data<T>> getDataMap() {
//            return dataMap;
//        }

        @Override
        public Builder<T> putData(String key, Data<? extends T> data, boolean replace) {
            if (key == null || key.isEmpty()) {
                throw new AnonymousNotAlowedException();
            }
            if (type.isAssignableFrom(data.type())) {
                if (replace || !dataMap.containsKey(key)) {
                    dataMap.put(key, data);
                } else {
                    throw new RuntimeException("The data with key " + key + " already exists");
                }
            } else {
                throw new RuntimeException("Data does not satisfy class boundary");
            }
            return this;
        }

        @Override
        public Builder<T> putNode(String as, DataNode<? extends T> node) {
            if (!node.getMeta().isEmpty()) {
                LoggerFactory.getLogger(getClass()).warn("Trying to add node with meta to flat DataNode. "
                        + "Node meta could be lost. Consider using DataTree instead.");
            }
            //PENDING rewrap data including meta?
            node.dataStream().forEach(data -> putData(as + "." + data.getName(), data.anonymize()));
            return self();
        }

        @Override
        public Builder<T> removeNode(String nodeName) {
            this.dataMap.entrySet().removeIf(entry -> entry.getKey().startsWith(nodeName));
            return self();
        }

        @Override
        public Builder<T> removeData(String dataName) {
            this.dataMap.remove(dataName);
            return self();
        }

        @Override
        public DataSet<T> build() {
            return new DataSet<>(name, meta, type, dataMap);
        }

        @Override
        public Builder<T> self() {
            return this;
        }

        @Override
        public Meta getMeta() {
            return this.meta;
        }
    }

}
