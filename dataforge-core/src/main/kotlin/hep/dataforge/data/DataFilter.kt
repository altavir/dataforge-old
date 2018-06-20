package hep.dataforge.data

/**
 * A filter that could select a subset from a DataNode without changing its type.
 */
interface DataFilter {

    /**
     * Perform a selection. Resulting node contains references to the data in the initial node.
     * Node structure and meta is maintained if possible.
     *
     * @param node
     * @param <T>
     * @return
     */
    fun <T: Any> filter(node: DataNode<T>): DataNode<T>

    companion object {
        val IDENTITY: DataFilter = object : DataFilter {
            override fun <T: Any> filter(node: DataNode<T>): DataNode<T> {
                return node
            }
        }

        fun byPattern(pattern: String): DataFilter {
            return object : DataFilter {
                override fun <T: Any> filter(node: DataNode<T>): DataNode<T> {
                    return DataSet.edit(node.type).apply {
                        name = node.name
                        node.dataStream(true)
                                .filter { d -> d.name.matches(pattern.toRegex()) }
                                .forEach { add(it) }

                    }.build()
                }
            }
        }
    }
}
