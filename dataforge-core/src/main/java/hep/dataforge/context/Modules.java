package hep.dataforge.context;

import hep.dataforge.io.XMLMetaReader;
import hep.dataforge.meta.Meta;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * An utility class to work with dataforge modules. Each module has a name, a version, dependencies and a list of exported entities.
 * Created by darksnake on 05-May-17.
 */
public class Modules {
    public static Stream<Meta> getModuleMeta(ClassLoader classLoader) throws IOException {
        XMLMetaReader reader = new XMLMetaReader();
        return Collections.list(classLoader.getResources("dataforge/module.xml")).stream()
                .map(url -> {
                    try {
                        return reader.read(url.openStream());
                    } catch (IOException | ParseException e) {
                        throw new RuntimeException("Can't read module info", e);
                    }
                });
    }
}
