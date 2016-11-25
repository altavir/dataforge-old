package hep.dataforge.groovymath.tables

/**
 *
 * Created by darksnake on 13-Nov-16.
 */
interface GRow extends Iterable<Object> {
    Object getAt(String key);

    Object getAt(int index);

    Map<String, Object> asMap();
}