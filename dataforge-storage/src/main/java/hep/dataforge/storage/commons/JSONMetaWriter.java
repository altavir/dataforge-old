/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.commons;

import hep.dataforge.io.IOUtils;
import hep.dataforge.io.MetaStreamWriter;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

import javax.json.*;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A converter from Meta object to JSON character stream
 *
 * @author Alexander Nozik
 */
public class JSONMetaWriter implements MetaStreamWriter {

    public static JSONMetaWriter instance = new JSONMetaWriter();

    private boolean prettify = true;
    private Charset charset = IOUtils.UTF8_CHARSET;

    public JSONMetaWriter() {
    }
    
    public JSONMetaWriter(boolean prettify) {
        this.prettify = prettify;
    }

    @Override
    public JSONMetaWriter withCharset(Charset charset) {
        JSONMetaWriter res = new JSONMetaWriter();
        res.charset = charset;
        return res;
    }

    @Override
    public void write(OutputStream stream, Meta meta) {
        Map<String, Object> properties = new HashMap<>();
        if (prettify) {
            properties.put("javax.json.stream.JsonGenerator.prettyPrinting", true);
        }
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        writerFactory.createWriter(stream, charset)
                .write(fromMeta(meta));
    }

    public JsonObject fromMeta(Meta meta) {
        JsonObjectBuilder res = Json.createObjectBuilder();
        // записываем все значения
        meta.getValueNames().forEach(key ->{
            List<Value> item = meta.getValue(key).listValue();
            if (item.size() == 1) {
                res.add(key, item.get(0).stringValue());
            } else {
                JsonArrayBuilder array = Json.createArrayBuilder();
                for (Value val : item) {
                    array.add(val.stringValue());
                }
                res.add(key, array);
            }
        });
        // write all meta nodes recursively
        meta.getNodeNames().forEach(key ->{
            List<? extends Meta> item = meta.getMetaList(key);
            if (item.size() == 1) {
                res.add(key, fromMeta(item.get(0)));
            } else {
                JsonArrayBuilder array = Json.createArrayBuilder();
                for (Meta anval : item) {
                    array.add(fromMeta(anval));
                }
                res.add(key, array);
            }
        });
        return res.build();
    }
}
