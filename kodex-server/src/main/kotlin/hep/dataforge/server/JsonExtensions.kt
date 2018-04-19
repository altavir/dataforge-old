package hep.dataforge.server

import hep.dataforge.meta.Meta
import hep.dataforge.values.Values


suspend fun jsonObject(builder: suspend JsonObjectBuilder.() -> Unit): JsonObjectBuilder {
    val res = Json.createObjectBuilder()
    builder.invoke(res);
    return res;
}

fun jsonObject(): JsonObjectBuilder {
    return Json.createObjectBuilder()
}

suspend fun Meta.asJson(): JsonObject {
    return JSONMetaWriter.metaToJson(this);
}

fun Values.asJson(): JsonObject {
    val builder = jsonObject();
    for (name in names) {
        builder.add(name, getString(name))
    }
    return builder.build();
}

suspend fun jsonArray(builder: suspend JsonArrayBuilder.() -> Unit): JsonArrayBuilder {
    val res = Json.createArrayBuilder()
    builder.invoke(res);
    return res;
}

fun jsonArray(): JsonArrayBuilder {
    return Json.createArrayBuilder()
}

suspend fun JsonValue.render(): String {
    return StringWriter().use { writer ->
        Json.createWriter(writer).write(this)
    }.toString()
}

suspend fun JsonObjectBuilder.render(): String {
    return StringWriter().use { writer ->
        Json.createWriter(writer).write(this.build())
    }.toString()
}

suspend fun JsonArrayBuilder.render(): String {
    return StringWriter().use { writer ->
        Json.createWriter(writer).write(this.build())
    }.toString()
}