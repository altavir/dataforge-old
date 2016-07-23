/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.context.Context;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataFactory;
import hep.dataforge.data.DataFilter;
import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;
import java.util.stream.Stream;
import javafx.util.Pair;

/**
 *
 * @author Alexander Nozik
 */
public class StorageDataFactory extends DataFactory<Loader> {

    public StorageDataFactory() {
        super(Loader.class);
    }

    @Override
    protected void buildChildren(Context context, DataTree.Builder<Loader> builder, DataFilter filter, Meta meta) {
        //FIXME this process takes long time for large storages. Need to wrap it in process
        Storage storage = StorageManager.buildFrom(context).buildStorage(meta);
        Stream<Pair<String, Loader>> stream = StorageUtils.loaderStream(storage);
        stream.forEach(pair -> {
            //TODO add meta nodes from input meta
            Data<Loader> datum = Data.buildStatic(pair.getValue(), Meta.empty());
            if (filter.acceptData(pair.getKey(), datum)) {
                builder.putData(pair.getKey(), datum);
            }
        });
    }

}
