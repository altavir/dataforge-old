package hep.dataforge.server.storage


import hep.dataforge.io.JSONMetaWriter
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.buildMeta
import hep.dataforge.storage.api.ValueIndex.Companion.LIMIT_KEY
import hep.dataforge.tables.Table
import hep.dataforge.tables.TableFormat
import hep.dataforge.utils.DateTimeUtils
import hep.dataforge.values.ValueType
import hep.dataforge.values.Values
import org.slf4j.LoggerFactory
import ratpack.handling.Context
import ratpack.handling.Handler
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

/**
 * The handler that returns data in Google Visualisation table format
 * Created by darksnake on 04-Apr-17.
 */
abstract class GoogleDataHandler : Handler {
    private fun makeCols(format: TableFormat): List<MetaBuilder> {
        return format.names.map { valueName ->
            val cf = format.getColumn(valueName)
            buildMeta("column") {
                "id" to valueName
                "label" to cf.title
                "type" to when (cf.primaryType) {
                    ValueType.NUMBER -> "number"
                    ValueType.BOOLEAN -> "boolean"
                    ValueType.TIME -> "datetime"
                    else -> "string"
                }
            }
        }
    }

    private fun makeRows(format: TableFormat, data: Iterable<Values>): List<MetaBuilder> {
        return data.map { values ->
            buildMeta("row") {
                format.names.forEach {
                    val value = values.getValue(it)
                    "c" to {
                        "f" to value.string
                        when (value.type) {
                            ValueType.TIME -> "v" to formatTime(value.time)
                            ValueType.NUMBER -> "v" to value.double
                            else -> "v" to value.string
                        }
                    }
                }
            }
        }
    }

    private fun formatTime(time: Instant): String {
        val off = time.atOffset(ZoneOffset.UTC)
        return String.format("Date(%d,%d,%d,%d,%d,%d)", off.year, off.monthValue,
                off.dayOfMonth, off.hour, off.minute, off.second)
    }

    private fun buildQuery(tq: String, params: Map<String, String>): Meta {
        //        Query q = QueryBuilder.getInstance().parseQuery(tq);
        val builder = MetaBuilder("query")
        builder.update(params)
        if (!params.containsKey(LIMIT_KEY)) {
            builder.setValue(LIMIT_KEY, 500)
        }
        return builder.build()
    }

    private fun getRequestParams(tqx: String): Map<String, String> {
        val map = HashMap<String, String>()
        for (part in tqx.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val keyValue = part.split(":".toRegex(), 2).toTypedArray()
            if (keyValue.size == 2) {
                map[keyValue[0]] = keyValue[1]
            } else {
                LoggerFactory.getLogger(javaClass).error("Can't parse request")
            }
        }
        return map
    }

    private fun toJson(format: TableFormat, data: Iterable<Values>): MetaBuilder {
        return buildMeta {
            "cols" to makeCols(format)
            "rows" to makeRows(format, data)
        }
    }

    private fun toJson(source: Table): MetaBuilder {
        return toJson(source.format, source)
    }

    protected abstract fun getData(query: Meta): Table

    @Throws(Exception::class)
    override fun handle(ctx: Context) {
        val tq = ctx.request.queryParams["tq"]!!
        val tqx = ctx.request.queryParams["tqx"]!!
        val params = getRequestParams(tqx)

        val reqId = params["reqId"]
        val out = params.getOrDefault("onComplete", "json")
        val responseHandler = params.getOrDefault("responseHandler", "google.visualization.Query.setResponse")
        if (out != "json") {
            //render error
        }

        ctx.response.contentType("text/json")

        val logger = LoggerFactory.getLogger("POINT_LOADER")
        val start = DateTimeUtils.now()

        val data = getData(buildQuery(tq, params))
        logger.info("Table built in {}", Duration.between(start, DateTimeUtils.now()))

        val response = buildMeta {
            "status" to "ok"
            "reqId" to reqId
            "table" to toJson(data)
        }

        logger.info("Response built in {}", Duration.between(start, DateTimeUtils.now()))

        ctx.render(wrapResponse(response, responseHandler))
    }

    private fun wrapResponse(response: MetaBuilder, responseHandler: String): String {
        return String.format("%s(%s)", responseHandler, JSONMetaWriter.writeString(response))
    }
}
