package hep.dataforge.data;

/**
 * A filter that could select a subset from a DataNode without changing its type.
 */
public interface DataFilter {
    DataFilter IDENTITY = new DataFilter() {
        @Override
        public <T> DataNode<T> filter(DataNode<T> node) {
            return node;
        }
    };

    static DataFilter byPattern(String pattern) {
        return new DataFilter() {
            @Override
            public <T> DataNode<T> filter(DataNode<T> node) {
                DataSet.Builder<T> builder = DataSet.builder(node.type());
                builder.setName(node.getName());
                node.dataStream(true)
                        .filter(d -> d.getName().matches(pattern))
                        .forEach(builder::putData);
                return builder.build();
            }
        };
    }

    /**
     * Perform a selection. Resulting node contains references to the data in the initial node.
     * Node structure and meta is maintained if possible.
     *
     * @param node
     * @param <T>
     * @return
     */
    <T> DataNode<T> filter(DataNode<T> node);
}
