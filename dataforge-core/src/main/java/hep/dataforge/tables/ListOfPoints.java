package hep.dataforge.tables;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.MetaMorph;
import hep.dataforge.values.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by darksnake on 18-Apr-17.
 */
public class ListOfPoints implements MetaMorph, NavigablePointSource {
    protected final ArrayList<DataPoint> data = new ArrayList<>();

    public ListOfPoints() {
    }

    public ListOfPoints(Iterable<DataPoint> points) {
        if (points != null) {
            addRows(points);
        }
    }

    public ListOfPoints(Stream<DataPoint> points) {
        if (points != null) {
            addRows(points.collect(Collectors.toList()));
        }
    }

    /**
     * Если formatter == null, то могут быть любые точки
     *
     * @param e a {@link DataPoint} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    protected void addRow(DataPoint e) throws NamingException {
        this.data.add(e);
    }

    protected void addRows(Iterable<? extends DataPoint> points) {
        for (DataPoint dp : points) {
            addRow(dp);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param i
     * @return
     */
    public DataPoint getPoint(int i) {
        return data.get(i);
    }

    /**
     * {@inheritDoc}
     */
    public Value getValue(int index, String name) throws NameNotFoundException {
        return this.data.get(index).getValue(name);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<DataPoint> iterator() {
        return data.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return data.size();
    }

    /**
     * Clear all data in the Table. Does not affect annotation.
     */
    public void clear() {
        this.data.clear();
    }

    @Override
    public Meta toMeta() {
        MetaBuilder dataNode = new MetaBuilder("data");
        forEach(dp -> dataNode.putNode("point", dp.toMeta()));
        return dataNode;
    }

    @Override
    public void fromMeta(Meta meta) {
        data.addAll(DataPoint.buildFromMeta(meta));
    }
}
