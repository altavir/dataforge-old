/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.tables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Alexander Nozik
 */
public class SimplePointSource implements PointSource {

    private final TableFormat format;
    private final List<DataPoint> points;

    public SimplePointSource(TableFormat format, List<DataPoint> points) {
        this.format = format;
        this.points = new ArrayList<>(points);
    }

    public SimplePointSource(TableFormat format) {
        this.format = format;
        this.points = new ArrayList<>();
    }

    public SimplePointSource(String... names) {
        this.format = TableFormat.forNames(names);
        this.points = new ArrayList<>();
    }

    @Override
    public TableFormat getFormat() {
        return format;
    }

    @Override
    public Iterator<DataPoint> iterator() {
        return points.iterator();
    }

    public void addRow(DataPoint p) {
        this.points.add(p);
    }

}
