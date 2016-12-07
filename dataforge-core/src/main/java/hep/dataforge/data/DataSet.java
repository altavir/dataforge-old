/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.navigation.AbstractProvider;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A simple static representation of DataNode
 *
 * @param <T>
 * @author Alexander Nozik
 */
public class DataSet<T> extends AbstractProvider implements DataNode<T> {

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
    public static Builder builder() {
        return new Builder<>(Object.class);
    }

    @Override
    public Stream<NamedData<? extends T>> dataStream(boolean recursive) {
        return dataMap.entrySet().stream()
                .filter(it -> recursive || !it.getKey().contains("."))
                .map((Map.Entry<String, Data<? extends T>> entry)
                        -> NamedData.wrap(entry.getKey(), entry.getValue(), meta()));
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
        return dataStream()
                .map(data -> {
                    Name dataName = Name.of(data.getName());
                    if (dataName.length() > 1) {
                        return dataName.cutLast();
                    } else {
                        return Name.EMPTY;
                    }
                })
                .filter(it -> recursive || it.length() == 1)
                .map(it -> it.toString())
                .distinct()
                .map((String str) -> {
                    if (str.isEmpty()) {
                        return DataSet.this;
                    } else {
                        return new NodeWrapper<>(getNode(str).get(), str, meta());
                    }
                });
    }

    @Override
    public Optional<Data<? extends T>> getData(String name) {
        return Optional.ofNullable(dataMap.get(name));
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
    public Meta meta() {
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
    public Optional<DataNode<? extends T>> getNode(String nodeName) {
        Builder<T> builder = new Builder<>(type)
                .setName(nodeName)
                .setMeta(meta());
        String prefix = nodeName + ".";
        dataStream()
                .filter(data -> data.getName().startsWith(prefix))
                .forEach(data -> builder.putData(data));
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

        public Map<String, Data<? extends T>> getDataMap() {
            return dataMap;
        }

        @Override
        public Builder<T> putData(String key, Data<? extends T> data, boolean replace) {
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
            if (!node.meta().isEmpty()) {
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
        public Meta meta() {
            return this.meta;
        }
    }

}
