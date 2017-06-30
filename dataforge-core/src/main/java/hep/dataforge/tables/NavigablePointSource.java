package hep.dataforge.tables;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

/**
 * Created by darksnake on 14-Apr-17.
 */
public interface NavigablePointSource extends PointSource{
    Values getPoint(int i);

    default Value getValue(int index, String name) throws NameNotFoundException {
        return getPoint(index).getValue(name);
    }

    /**
     * Number of rows in the table
     *
     * @return
     */
    int size();
}
