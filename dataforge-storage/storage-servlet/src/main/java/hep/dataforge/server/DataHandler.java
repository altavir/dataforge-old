package hep.dataforge.server;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.tables.ColumnFormat;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.Table;
import hep.dataforge.tables.TableFormat;
import hep.dataforge.utils.DateTimeUtils;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static hep.dataforge.storage.api.ValueIndex.LIMIT_KEY;

/**
 * Created by darksnake on 04-Apr-17.
 */
public abstract class DataHandler implements Handler {


    private JsonArrayBuilder makeCols(TableFormat format) {
        JsonArrayBuilder res = Json.createArrayBuilder();
        for (String valueName : format.names()) {
            ColumnFormat cf = format.getColumnFormat(valueName);
            String type;
            switch (cf.getPrimaryType()) {
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
                    .add("label", cf.getTitle())
                    .add("type", type));
        }
        return res;
    }

    private JsonArrayBuilder makeRows(TableFormat format, Iterable<DataPoint> data) {
        JsonArrayBuilder rows = Json.createArrayBuilder();
        data.forEach(it -> {
            rows.add(makeRow(format, it));
        });
        return rows;
    }

    private String formatTime(Instant time) {
        OffsetDateTime off = time.atOffset(ZoneOffset.UTC);
        return String.format("Date(%d,%d,%d,%d,%d,%d)", off.getYear(), off.getMonthValue(),
                off.getDayOfMonth(), off.getHour(), off.getMinute(), off.getSecond());
    }

    private JsonObjectBuilder makeRow(TableFormat format, DataPoint point) {
        JsonArrayBuilder values = Json.createArrayBuilder();
        for (String valueName : format.names()) {
            Value value = point.getValue(valueName);
            switch (value.valueType()) {
                case TIME:

                    values.add(Json.createObjectBuilder().add("v", formatTime(value.timeValue()))
                            .add("f", value.stringValue()));
                    break;
                case NUMBER:
                    values.add(Json.createObjectBuilder().add("v", value.doubleValue())
                            .add("f", value.stringValue()));
                default:
                    values.add(Json.createObjectBuilder().add("v", value.stringValue())
                            .add("f", value.stringValue()));
            }

        }
        return Json.createObjectBuilder()
                .add("c", values);
    }

    private Meta buildQuery(String tq, Map<String, String> params) {
//        Query q = QueryBuilder.getInstance().parseQuery(tq);
        MetaBuilder builder = new MetaBuilder("query");
        builder.update(params);
        if (!params.containsKey(LIMIT_KEY)) {
            builder.setValue(LIMIT_KEY, 500);
        }
        return builder.build();
    }

    private Map<String, String> getRequestParams(String tqx) {
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

    protected JsonObjectBuilder makeTable(TableFormat format, Iterable<DataPoint> data) {
        JsonObjectBuilder res = Json.createObjectBuilder();

        res.add("cols", makeCols(format));
        res.add("rows", makeRows(format, data));
        return res;
    }

    protected JsonObjectBuilder makeTable(Table source) {
        return makeTable(source.getFormat(), source);
    }

    protected abstract Table getData(Meta query);

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

        Logger logger = LoggerFactory.getLogger("POINT_LOADER");
        Instant start = DateTimeUtils.now();

        Table data = getData(buildQuery(tq, params));
        logger.info("Table built in {}", Duration.between(start, DateTimeUtils.now()));

        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("reqId", rqid)
                .add("table", makeTable(data))
                .build();

        logger.info("Response built in {}", Duration.between(start, DateTimeUtils.now()));

        ctx.render(wrapResponse(response, responseHandler));
    }

    private String wrapResponse(JsonObject response, String responseHandler) {
        StringWriter writer = new StringWriter();
        Json.createWriter(writer).writeObject(response);

        return String.format("%s(%s)", responseHandler, writer.toString());
    }
}
