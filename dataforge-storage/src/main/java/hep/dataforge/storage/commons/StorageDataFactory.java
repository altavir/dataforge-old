/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.context.Context;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataFactory;
import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;
import javafx.util.Pair;

import java.util.stream.Stream;

/**
 * @author Alexander Nozik
 */
public class StorageDataFactory extends DataFactory<Loader> {

    public StorageDataFactory() {
        super(Loader.class);
    }

    @Override
    public String getName() {
        return "storage";
    }

    @Override
    protected void fill(DataTree.Builder<Loader> builder, Context context, Meta meta) {
        //FIXME this process takes long time for large storages. Need to wrap it in process
        Storage storage = StorageManager.buildFrom(context).buildStorage(meta);
        Stream<Pair<String, Loader>> stream = StorageUtils.loaderStream(storage);
        stream.forEach(pair -> {
            builder.putData(pair.getKey(), Data.buildStatic(pair.getValue(), Meta.empty()));
        });
    }

}
