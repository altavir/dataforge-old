/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.context.Context;
import hep.dataforge.data.DataFactory;
import hep.dataforge.data.DataTree;
import hep.dataforge.data.StaticData;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;

/**
 *
 * @author Alexander Nozik
 */
public class StorageDataFactory extends DataFactory {

    @Override
    protected void buildChildren(Context context, DataTree.Builder builder, Meta meta) {
        Storage storage = StorageManager.buildFrom(context).buildStorage(meta);
        StorageUtils.loaderStream(storage).forEach(pair -> builder.putData(pair.getKey(), new StaticData<>(pair.getValue())));
    }

}
