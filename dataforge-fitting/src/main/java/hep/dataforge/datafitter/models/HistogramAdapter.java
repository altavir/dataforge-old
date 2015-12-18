/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.datafitter.models;

import hep.dataforge.data.DataAdapter;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.data.DataPoint;
import hep.dataforge.names.Names;

/**
 * TODO сделать адаптер для гистограммы с фиксированными бинами
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class HistogramAdapter implements DataAdapter {

    private static final String ANNOTATION_BIN_BEGIN_NAME = "binBeginName";
    private static final String ANNOTATION_BIN_END_NAME = "binEndName";
    private static final String ANNOTATION_COUNT_NAME = "countName";

    private String binBeginName = "binBegin";
    private String binEndName = "binEnd";
    private String countName = "count";

    /**
     * <p>
     * Constructor for HistogramAdapter.</p>
     */
    public HistogramAdapter() {
    }

    /**
     * <p>
     * Constructor for HistogramAdapter.</p>
     *
     * @param binBeginName a {@link java.lang.String} object.
     * @param binEndName a {@link java.lang.String} object.
     * @param countName a {@link java.lang.String} object.
     */
    public HistogramAdapter(String binBeginName, String binEndName, String countName) {
        this.binBeginName = binBeginName;
        this.binEndName = binEndName;
        this.countName = countName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Meta buildAnnotation() {
        return new MetaBuilder(DataAdapter.DATA_ADAPTER_ANNOTATION_NAME)
                .putValue(ANNOTATION_BIN_BEGIN_NAME, binBeginName)
                .putValue(ANNOTATION_BIN_END_NAME, binEndName)
                .putValue(ANNOTATION_COUNT_NAME, countName)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Names getNames() {
        return Names.of(binBeginName, binEndName, countName);
    }

    /**
     * {@inheritDoc}
     *
     * @param point
     * @return
     */
    @Override
    public double getWeight(DataPoint point) {
        if (point.names().contains(WEIGHT)) {
            return point.getDouble(WEIGHT);
        } else {
            return 1 / getCount(point);
        }
    }

    /**
     * <p>
     * getBinBegin.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a double.
     */
    public double getBinBegin(DataPoint point) {
        return point.getValue(binBeginName).doubleValue();
    }

    /**
     * <p>
     * getBinEnd.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a double.
     */
    public double getBinEnd(DataPoint point) {
        return point.getValue(binEndName).doubleValue();
    }

    /**
     * <p>
     * getCount.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a long.
     */
    public long getCount(DataPoint point) {
        return point.getValue(countName).intValue();
    }

    /**
     * <p>
     * getBinSize.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a double.
     */
    public double getBinSize(DataPoint point) {
        return getBinEnd(point) - getBinBegin(point);
    }

    /**
     * <p>
     * getBinCenter.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a double.
     */
    public double getBinCenter(DataPoint point) {
        return (getBinEnd(point) + getBinBegin(point)) / 2;
    }

}
