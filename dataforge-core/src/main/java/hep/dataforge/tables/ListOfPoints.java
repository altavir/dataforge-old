package hep.dataforge.tables;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.NamingException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.utils.MetaMorph;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by darksnake on 18-Apr-17.
 */
public class ListOfPoints implements MetaMorph, NavigablePointSource {

    public static List<Values> buildFromMeta(Meta annotation) {
        List<Values> res = new ArrayList<>();
        for (Meta pointMeta : annotation.getMetaList("point")) {
            Map<String, Value> map = new HashMap<>();
            pointMeta.getValueNames().forEach(key -> {
                map.put(key, pointMeta.getValue(key));
            });
            res.add(new ValueMap(map));
        }
        return res;
    }

    protected final ArrayList<Values> data = new ArrayList<>();

    public ListOfPoints() {
    }

    public ListOfPoints(Iterable<Values> points) {
        if (points != null) {
            addRows(points);
        }
    }

    public ListOfPoints(Stream<Values> points) {
        if (points != null) {
            addRows(points.collect(Collectors.toList()));
        }
    }

    /**
     * Если formatter == null, то могут быть любые точки
     *
     * @param e
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    protected void addRow(Values e) throws NamingException {
        this.data.add(e);
    }

    protected void addRows(Iterable<? extends Values> points) {
        for (Values dp : points) {
            addRow(dp);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param i
     * @return
     */
    public Values getRow(int i) {
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
    public Iterator<Values> iterator() {
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
        data.addAll(buildFromMeta(meta));
    }
}
