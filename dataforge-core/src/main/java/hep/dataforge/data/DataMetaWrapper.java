/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import java.util.concurrent.CompletableFuture;

/**
 * A container for unannotated data to include meta-data
 *
 * @author Alexander Nozik
 */
public class DataMetaWrapper<T> implements Data<T>, Annotated {

    private final Data<T> data;
    private final Meta meta;

    public DataMetaWrapper(Data<T> data, Meta meta) {
        this.data = data;
        this.meta = meta;
    }

    @Override
    public CompletableFuture<T> getInFuture() {
        return data.getInFuture();
    }

    @Override
    public Class<? super T> dataType() {
        return data.dataType();
    }

    @Override
    public Meta meta() {
        return meta;
    }

}
