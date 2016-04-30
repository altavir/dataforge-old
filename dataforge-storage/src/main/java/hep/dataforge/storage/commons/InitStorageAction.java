/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.actions.GeneratorAction;
import hep.dataforge.context.Context;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.io.reports.Reportable;
import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.Storage;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import javafx.util.Pair;

/**
 * An action to initialize a storage
 *
 * @author Alexander Nozik
 */
@TypedActionDef(name = "initStorage", outputType = Storage.class, info = "Build a storage")
public class InitStorageAction extends GeneratorAction<Storage> {

    @Override
    protected Map<String, Pair<Meta, Supplier<Storage>>> generate(Context context, Meta meta, Reportable log) {
        return Collections.singletonMap("", new Pair<>(meta, () -> StorageManager.buildFrom(context).buildStorage(meta)));
    }

}
