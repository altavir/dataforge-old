package hep.dataforge.tables;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

/**
 * Created by darksnake on 14-Apr-17.
 */
public interface NavigablePointSource extends PointSource{
    Values getRow(int i);

    default Value get(String name, int index) throws NameNotFoundException {
        return getRow(index).getValue(name);
    }

    default double getDouble(String name, int index){
        return get(name,index).doubleValue();
    }

    /**
     * Number of rows in the table
     *
     * @return
     */
    int size();
}
