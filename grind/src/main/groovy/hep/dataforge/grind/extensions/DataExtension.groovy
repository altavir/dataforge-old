package hep.dataforge.grind.extensions

import groovy.transform.CompileStatic
import hep.dataforge.tables.Table
import hep.dataforge.values.Values

@CompileStatic
class DataExtension {
    //table extension
    static Values getAt(final Table self, int index){
        return self.getRow(index);
    }

    static Object getAt(final Table self, String name, int index){
        return self.get(name,index).value();
    }
}
