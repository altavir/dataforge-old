/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.servlet;

import hep.dataforge.meta.Meta;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.Table;

/**
 * A handler to evaluate Google visualization library requests to point loaders
 * <p> https://developers.google.com/chart/interactive/docs/dev/implementing_data_source </p>
 * <p> https://developers.google.com/chart/interactive/docs/reference </p>
 *
 * @author Alexander Nozik
 */
public class PointLoaderDataHandler extends DataHandler {

//    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());

    private final PointLoader loader;

    public PointLoaderDataHandler(PointLoader loader) {
        this.loader = loader;
    }

    @Override
    protected Table getData(Meta query) {
        ValueIndex<DataPoint> index;

        //use custom index if needed
        if (query.hasValue("index")) {
            index = loader.getIndex(query.getString("index", ""));
        } else {
            //use loader default one otherwise
            index = loader.getIndex();
        }
        try {
            return new ListTable(loader.getFormat(), index.query(query));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
