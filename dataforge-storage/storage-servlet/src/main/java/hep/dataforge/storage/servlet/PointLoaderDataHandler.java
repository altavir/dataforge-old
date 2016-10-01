/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.servlet;

import com.google.visualization.datasource.base.InvalidQueryException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.TableFormat;
import hep.dataforge.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static hep.dataforge.storage.api.ValueIndex.LIMIT_KEY;

/**
 * A handler to evaluate Google visualization library requests to point loaders
 * <p https://developers.google.com/chart/interactive/docs/dev/implementing_data_source />
 * <p https://developers.google.com/chart/interactive/docs/reference />
 *
 * @author Alexander Nozik
 */
public class PointLoaderDataHandler implements Handler {

//    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());

    private final PointLoader loader;

    public PointLoaderDataHandler(PointLoader loader) {
        this.loader = loader;
    }

    public static JsonObjectBuilder makeTable(ValueIndex index, TableFormat format, Meta query) throws StorageException {
        JsonObjectBuilder res = Json.createObjectBuilder();

        res.add("cols", makeCols(format));
        res.add("rows", makeRows(format, index.query(query)));
        return res;
    }

    private static JsonArrayBuilder makeCols(TableFormat format) {
        JsonArrayBuilder res = Json.createArrayBuilder();
        for (String valueName : format.names()) {
            String type;
            switch (format.getType(valueName)) {
                case NUMBER:
                    type = "number";
                    break;
                case BOOLEAN:
                    type = "boolean";
                    break;
                case TIME:
                    type = "datetime";
                    break;
                default:
                    type = "string";
            }
            res.add(Json.createObjectBuilder()
                    .add("id", valueName)
                    .add("label", format.getTitle(valueName))
                    .add("type", type));
        }
        return res;
    }

    private static JsonArrayBuilder makeRows(TableFormat format, Iterable<Supplier<DataPoint>> data) {
        JsonArrayBuilder rows = Json.createArrayBuilder();
        data.forEach(it -> {
            rows.add(makeRow(format, it.get()));
        });
        return rows;
    }

    private static JsonObjectBuilder makeRow(TableFormat format, DataPoint point) {
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (String valueName : format.names()) {
            Value value = point.getValue(valueName);
            switch (value.valueType()) {
                case TIME:
                    values.add(Json.createObjectBuilder().add("v", "Date(" + value.longValue() + ")")
                            .add("f", value.stringValue()));
                    break;
                default:
                    values.add(Json.createObjectBuilder().add("v", value.stringValue())
                            .add("f", value.stringValue()));
            }

        }
        return Json.createObjectBuilder()
                .add("c", values);
    }

    private static Meta buildQuery(String tq, Map<String, String> params) throws InvalidQueryException {
//        Query q = QueryBuilder.getInstance().parseQuery(tq);
        MetaBuilder builder = new MetaBuilder("query");
        builder.update(params);
        if (!params.containsKey(LIMIT_KEY)) {
            builder.setValue(LIMIT_KEY, 500);
        }
        return builder.build();
    }

    private static Map<String, String> getRequestParams(String tqx) {
        Map<String, String> map = new HashMap<>();
        for (String part : tqx.split(";")) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            } else {
                LoggerFactory.getLogger("PointLoaderVisualizationHandler").error("Can't parse request");
            }
        }
        return map;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String tq = ctx.getRequest().getQueryParams().get("tq");
        String tqx = ctx.getRequest().getQueryParams().get("tqx");
        Map<String, String> params = getRequestParams(tqx);

        String rqid = params.get("reqId");
        String out = params.getOrDefault("onComplete", "json");
        String responseHandler = params.getOrDefault("responseHandler", "google.visualization.Query.setResponse");
        if (!out.equals("json")) {
            //render error
        }

        ctx.getResponse().contentType("text/json");

        ValueIndex<DataPoint> index;

        Meta query = buildQuery(tq, params);

        Instant start = Instant.now();

        //use custom index if needed
        if (query.hasValue("index")) {
            index = loader.getIndex(query.getString("index", ""));
        } else {
            //use loader default one otherwise
            index = loader.getIndex();
        }

        Logger logger = LoggerFactory.getLogger("POINT_LOADER");
        Duration indexLoadTime = Duration.between(start,Instant.now());
        logger.info("Index file loaded in {}", indexLoadTime);


        start = Instant.now();
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("reqId", rqid)
                .add("table", makeTable(index, loader.getFormat(), buildQuery(tq, params)))
                .build();

        Duration tableBuildTime = Duration.between(start, Instant.now());
        logger.info("Table built in {}", tableBuildTime);

        ctx.render(wrapResponse(response, responseHandler));
    }

    private String wrapResponse(JsonObject response, String responseHandler) {
        StringWriter writer = new StringWriter();
        Json.createWriter(writer).writeObject(response);

        return String.format("%s(%s)", responseHandler, writer.toString());
    }
}
