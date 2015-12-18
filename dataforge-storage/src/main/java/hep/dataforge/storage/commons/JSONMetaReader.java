/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Reader for JSON meta
 *
 * @author Alexander Nozik
 */
public class JSONMetaReader implements MetaStreamReader {

    @Override
    public MetaBuilder read(InputStream stream, long length, Charset encoding) throws IOException, ParseException {
        if (length == 0) {
            return new MetaBuilder("");
        } else if (length > 0) {
            byte[] buffer = new byte[(int) length];
            stream.read(buffer);
            return fromString(new String(buffer, encoding));
        } else {
//            if(!stream.markSupported()){
//                LoggerFactory.getLogger(getClass())
//                        .warn("Trying to infere annotation on not buffered stream. Following data could be corrupted.");
//            }
//            stream.mark(2048);
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

            return toMeta(Json.createReader(new InputStreamReader(ins, encoding)).readObject());
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
        for (Map.Entry<String, JsonValue> entrySet : source.entrySet()) {
            String key = entrySet.getKey();
            JsonValue value = entrySet.getValue();
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
                builder.putValue(key, ((JsonString) value).getString());
                break;
            case NUMBER:
                builder.putValue(key, ((JsonNumber) value).bigDecimalValue());
                break;
            case NULL:
                builder.putValue(key, Value.getNull());
                break;
        }
    }

    @Override
    public boolean acceptsFile(File file) {
        return file.toString().toLowerCase().endsWith(".json");
    }
}
