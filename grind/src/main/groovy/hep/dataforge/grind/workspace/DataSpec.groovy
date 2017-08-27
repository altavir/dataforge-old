package hep.dataforge.grind.workspace

import hep.dataforge.data.Data
import hep.dataforge.grind.Grind
import hep.dataforge.grind.GrindMetaBuilder
import hep.dataforge.meta.Meta
import hep.dataforge.names.Named
import hep.dataforge.workspace.Workspace

import java.util.function.Supplier

/**
 * A specification to builder workspace data
 */
class DataSpec {
    private  final Workspace.Builder builder

    DataSpec(Workspace.Builder builder) {
        this.builder = builder
    }

    def files(String place, String path, @DelegatesTo(GrindMetaBuilder) Closure fileMeta) {
        builder.loadFileData(place, path, Grind.buildMeta(fileMeta))
    }

    def files(String place, String path) {
        builder.loadFileData(place, path)
    }

    /**
     * Put a static resource as data
     * @param place
     * @param path
     * @return
     */
    def resource(String place, String path) {
        URI uri = URI.create(path)
        builder.loadData(place, Data.buildStatic(uri))
    }

    def load(Map values = [:], String nodeName = "",
             @DelegatesTo(GrindMetaBuilder) Closure cl = null) {
        loadFromMeta(Grind.buildMeta(values, nodeName, cl))
    }

    def loadFromMeta(Meta meta) {
        //TODO remove control values from meta
        builder.loadData(
                meta.getString("as", ""),
                meta.getString("loader"),
                meta
        )
    }

    /**
     * Add a dynamic data
     * @param name
     * @param meta
     * @param type
     * @param cl
     */
    def <R> void item(String name, Meta meta = Meta.empty(), Class<R> type = Object, Supplier<R> cl) {
        builder.loadData(name, Data.generate(type, meta, cl))
    }

    /**
     * Create a static data item in the workspace
     * @param name
     * @param object
     * @return
     */
    def item(String name, Object object, Meta meta = Meta.empty()) {
        if (meta.isEmpty()) {
            builder.loadData(name, Data.buildStatic(object));
        } else {
            builder.loadData(name, Data.buildStatic(object, meta));
        }
    }

    /**
     * Load static data from map
     * @param items
     * @return
     */
    def items(Map<String, ?> items) {
        items.each { key, value ->
            item(key, Data.buildStatic(value))
        }
    }

    /**
     *        Load static data from collection of Named objects
     */
    def items(Collection<? extends Named> something) {
        something.each {
            item(it.name, Data.buildStatic(it))
        }
    }

    def item(Named object) {
        item(object.name, Data.buildStatic(object));
    }

    //TODO extend data specification
}
