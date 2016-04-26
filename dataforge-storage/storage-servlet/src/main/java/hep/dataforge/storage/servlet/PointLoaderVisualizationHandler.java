/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.servlet;

import hep.dataforge.meta.Meta;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.TableFormat;
import hep.dataforge.storage.api.PointLoader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * A handler to evaluate Google visualization library requests to point loaders
 * <p https://developers.google.com/chart/interactive/docs/dev/implementing_data_source />
 * <p https://developers.google.com/chart/interactive/docs/reference />
 *
 * @author Alexander Nozik
 */
public class PointLoaderVisualizationHandler implements Handler {

    private final PointLoader loader;

    public PointLoaderVisualizationHandler(PointLoader loader) {
        this.loader = loader;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String tq = ctx.getRequest().getQueryParams().get("tq");
        String tqx = ctx.getRequest().getQueryParams().get("tqx");
        Map<String, String> params = getRequestParams(tqx);

        String rqid = params.get("reqId");
        String out = params.getOrDefault("out", "json");
        String responseHandler = params.getOrDefault("responseHandler", "google.visualization.Query.setResponse");
        if (!out.equals("json")) {
            //render error
        }

        ctx.getResponse().contentType("text/json");

        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("reqId", rqid)
                .add("table", makeTable(loader, buildQuery(tq)))
                .build();

        ctx.render(wrapResponse(response, responseHandler));
    }

    private String wrapResponse(JsonObject response, String responseHandler) {
        StringWriter writer = new StringWriter();
        Json.createWriter(writer).writeObject(response);

        return String.format("%s(%s)", responseHandler, writer.toString());
    }

    public static JsonObjectBuilder makeTable(PointLoader loader, Meta query) {
        JsonObjectBuilder res = Json.createObjectBuilder();
        res.add("cols", makeCols(loader.getFormat()));
        res.add("rows", makeRows(loader.getFormat(), loader));
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
                    type = "date";
                    break;
                default:
                    type = "string";
            }
            res.add(Json.createObjectBuilder()
                    .add("id", valueName)
                    .add("label", format.getTitle(valueName))
                    .add("type", "string"));
        }
        return res;
    }

    private static JsonArrayBuilder makeRows(TableFormat format, Iterable<DataPoint> data) {
        JsonArrayBuilder rows = Json.createArrayBuilder();
        for (DataPoint p : data) {
            rows.add(makeRow(format, p));
        }
        return rows;
    }

    private static JsonObjectBuilder makeRow(TableFormat format, DataPoint point) {
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (String valueName : format.names()) {
            values.add(Json.createObjectBuilder().add("v", point.getString(valueName)));
        }
        return Json.createObjectBuilder()
                .add("c", values);
    }

    private static Meta buildQuery(String tq) {
        return null;
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
}
