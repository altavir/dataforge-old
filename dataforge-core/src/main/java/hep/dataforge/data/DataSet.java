/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.navigation.AbstractProvider;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;

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
    public Stream<Pair<String, Data<? extends T>>> dataStream() {
        return dataMap.entrySet().stream()
                .<Pair<String, Data<? extends T>>>map((Map.Entry<String, Data<? extends T>> entry)
                        -> new Pair<>(entry.getKey(), entry.getValue()));
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
    public Stream<Pair<String, DataNode<? extends T>>> nodeStream() {
        return dataStream().map((pair) -> {
            Name dataName = Name.of(pair.getKey());
            if (dataName.length() > 1) {
                return dataName.cutLast().toString();

            } else {
                return "";
            }
        }).distinct().map((String str) -> {
            if (str.isEmpty()) {
                return new Pair<>("", DataSet.this);
            } else {
                return new Pair<>(str, getNode(str));
            }
        });
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
        dataStream()
                .filter((Pair<String, Data<? extends T>> pair) -> pair.getKey().startsWith(prefix))
                .forEach((Pair<String, Data<? extends T>> pair) -> builder.putData(pair.getKey(), pair.getValue()));
        if (builder.dataMap.size() > 0) {
            return builder.build();
        } else {
            return null;
        }
    }

    public static class Builder<T> implements DataNode.Builder<T, DataSet<T>, Builder<T>> {

        private String name = "";
        private Meta meta = Meta.empty();
        private final Class<T> type;
        private final Map<String, Data<? extends T>> dataMap = new LinkedHashMap<>();

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
            if (type.isAssignableFrom(data.dataType())) {
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
            node.dataStream().forEach(pair -> putData(pair.getKey(), pair.getValue()));
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
    }

}
