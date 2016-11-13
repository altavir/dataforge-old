package hep.dataforge.maths.groovy.tables

/**
 * Created by darksnake on 13-Nov-16.
 */
class TableRow implements GRow {
    private GTable table;
    private int index;

    @Override
    Object getAt(String key) {
        return null
    }

    @Override
    Object getAt(int index) {
        return null
    }

    @Override
    Map<String, Object> asMap() {
        return null
    }

    @Override
    Iterator<Object> iterator() {
        return null
    }
}
