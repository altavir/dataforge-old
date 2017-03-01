/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.io.IOUtils;
import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;

import javax.json.*;
import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;

/**
 * Reader for JSON meta
 *
 * @author Alexander Nozik
 */
public class JSONMetaReader implements MetaStreamReader {
    Charset charset = IOUtils.UTF8_CHARSET;

    @Override
    public MetaStreamReader withCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public MetaBuilder read(InputStream stream, long length) throws IOException, ParseException {
        if (length == 0) {
            return new MetaBuilder("");
        } else if (length > 0) {
            byte[] buffer = new byte[(int) length];
            stream.read(buffer);
            return fromString(new String(buffer, getCharset()));
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int braceCounter = 0;

            int nextByte;
            boolean stopFlag = false;
            while (!stopFlag && stream.available() > 0) {
                nextByte = stream.read();

                //The first symbol is required to be '{'
                if (nextByte == '{') {
                    braceCounter++;
                } else if (nextByte == '}') {
                    braceCounter--;
                }
                baos.write(nextByte);
                if (braceCounter == 0) {
                    stopFlag = true;
                }
            }

            ByteArrayInputStream ins = new ByteArrayInputStream(baos.toByteArray());

            return toMeta(Json.createReader(new InputStreamReader(ins, getCharset())).readObject());
        }
    }

    public MetaBuilder toMeta(JsonObject source) throws ParseException {
        return toMeta(null, source);
    }

    public MetaBuilder fromString(String string) throws ParseException {
        return toMeta(Json.createReader(new StringReader(string)).readObject());
    }

    public MetaBuilder toMeta(String name, JsonObject source) throws ParseException {
        MetaBuilder builder = new MetaBuilder(name);
        for (Map.Entry<String, JsonValue> entry : source.entrySet()) {
            String key = entry.getKey();
            JsonValue value = entry.getValue();
            putJsonValue(builder, key, value);
        }
        return builder;
    }

    private void putJsonValue(MetaBuilder builder, String key, JsonValue value) throws ParseException {
        switch (value.getValueType()) {
            case OBJECT:
                builder.putNode(toMeta(key, (JsonObject) value));
                break;
            case ARRAY:
                JsonArray array = (JsonArray) value;
                for (int i = 0; i < array.size(); i++) {
                    putJsonValue(builder, key, array.get(i));
                }
                break;
            case FALSE:
                builder.putValue(key, Value.of(false));
                break;
            case TRUE:
                builder.putValue(key, Value.of(true));
                break;
            case STRING:
                builder.putValue(key, normalizeString((JsonString) value));
                break;
            case NUMBER:
                builder.putValue(key, ((JsonNumber) value).bigDecimalValue());
                break;
            case NULL:
                builder.putValue(key, Value.getNull());
                break;
        }
    }

    private String normalizeString(JsonString value) {
        return value.getString();
    }

}
