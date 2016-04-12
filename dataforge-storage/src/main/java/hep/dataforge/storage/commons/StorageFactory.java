/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.utils.MetaFactory;
import java.util.ServiceLoader;

/**
 *
 * @author Alexander Nozik
 */
public class StorageFactory {

    private static final ServiceLoader<StorageType> loader = ServiceLoader.load(StorageType.class);
    private static final String DEFAULT_STORAGE_TYPE = "file";

    private static MetaFactory<Storage> getStorageFactory(String type) {
        for (StorageType st : loader) {
            if (st.type().equalsIgnoreCase(type)) {
                return st;
            }
        }
        return null;
    }

    public static Storage buildStorage(Context context, Meta annotation) {
        String type = annotation.getString("type", DEFAULT_STORAGE_TYPE);
        if (context == null) {
            return getStorageFactory(type).build(GlobalContext.instance(), annotation);
        } else {
            return getStorageFactory(type).build(context, annotation);
        }
    }
}
