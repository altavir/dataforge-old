/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriterFactory;

/**
 * A converter from Meta object to JSON character stream
 * @author Alexander Nozik
 */
public class JSONMetaWriter implements MetaStreamWriter {

    @Override
    public void write(OutputStream stream, Meta meta, Charset charset) {
        if(charset == null){
            charset = Charset.forName("UTF-8");
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.json.stream.JsonGenerator.prettyPrinting", true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        writerFactory.createWriter(stream, charset)
                .write(fromMeta(meta));
    }

    public JsonObject fromMeta(Meta an) {
        JsonObjectBuilder res = Json.createObjectBuilder();
        // записываем все значения
        for (String key : an.getValueNames()) {
            List<Value> item = an.getValue(key).listValue();
            if (item.size() == 1) {
                res.add(key, item.get(0).stringValue());
            } else {
                JsonArrayBuilder array = Json.createArrayBuilder();
                for (Value val : item) {
                    array.add(val.stringValue());
                }
                res.add(key, array);
            }
        }
        // рекурсивно записываем все аннотации
        for (String key : an.getNodeNames()) {
            List<? extends Meta> item = an.getNodes(key);
            if (item.size() == 1) {
                res.add(key, fromMeta(item.get(0)));
            } else {
                JsonArrayBuilder array = Json.createArrayBuilder();
                for (Meta anval : item) {
                    array.add(fromMeta(anval));
                }
                res.add(key, array);
            }
        }
        return res.build();
    }
}
