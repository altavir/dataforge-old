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
package hep.dataforge.data;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Laminate;
import hep.dataforge.names.Names;
import hep.dataforge.values.Value;

/**
 * Интерпретатор полей DataPoint
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@ValueDef(name = "xName", def = "x", info = "X value name")
@ValueDef(name = "yName", def = "y", info = "Y value name")
@ValueDef(name = "xErrName", def = "xErr", info = "X error value name")
@ValueDef(name = "yErrName", def = "yErr", info = "Y error value name")
public class XYDataAdapter implements DataAdapter {

    protected String xErrName = "xErr";
    protected String xName = "x";
    protected String yErrName = "yErr";
    protected String yName = "y";

    /**
     * <p>
     * Constructor for XYDataAdapter.</p>
     */
    public XYDataAdapter() {
    }

    /**
     * <p>
     * Constructor for XYDataAdapter.</p>
     *
     * @param adapterAnnotation a {@link hep.dataforge.meta.Meta}
     * object.
     */
    public XYDataAdapter(Meta adapterAnnotation) {
        Meta meta =  new Laminate(adapterAnnotation).setDescriptor(DescriptorUtils.buildDescriptor(getClass()));
        this.xName = meta.getString("xName");
        this.yName = meta.getString("yName");
        this.xErrName = meta.getString("xErrName");
        this.yErrName = meta.getString("yErrName");
    }

    /**
     * <p>
     * Constructor for XYDataAdapter.</p>
     *
     * @param xName a {@link java.lang.String} object.
     * @param xErrName a {@link java.lang.String} object.
     * @param yName a {@link java.lang.String} object.
     * @param yErrName a {@link java.lang.String} object.
     */
    public XYDataAdapter(String xName, String xErrName, String yName, String yErrName) {
        this.xName = xName;
        this.yName = yName;
        this.xErrName = xErrName;
        this.yErrName = yErrName;
    }

    /**
     * <p>
     * Constructor for XYDataAdapter.</p>
     *
     * @param xName a {@link java.lang.String} object.
     * @param yName a {@link java.lang.String} object.
     * @param yErrName a {@link java.lang.String} object.
     */
    public XYDataAdapter(String xName, String yName, String yErrName) {
        this.xName = xName;
        this.yName = yName;
        this.yErrName = yErrName;
    }

    /**
     * <p>
     * Constructor for XYDataAdapter.</p>
     *
     * @param xName a {@link java.lang.String} object.
     * @param yName a {@link java.lang.String} object.
     */
    public XYDataAdapter(String xName, String yName) {
        this.xName = xName;
        this.yName = yName;
    }

    /**
     * <p>
     * buildXYDataPoint.</p>
     *
     * @param x a double.
     * @param y a double.
     * @param yErr a double.
     * @return a {@link hep.dataforge.data.DataPoint} object.
     */
    public DataPoint buildXYDataPoint(double x, double y, double yErr) {
        return new MapDataPoint(getNames().asArray(), x, y, yErr);
    }

    /**
     * <p>
     * getX.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a double.
     */
    public Value getX(DataPoint point) {
        return point.getValue(xName);
    }

    /**
     * <p>
     * getXerr.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a double.
     */
    public Value getXerr(DataPoint point) {
        if (point.names().contains(xErrName)) {
            return point.getValue(xErrName);
        } else {
            return Value.of(0);
        }
    }

    /**
     * <p>
     * getY.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a double.
     */
    public Value getY(DataPoint point) {
        return point.getValue(yName);
    }

    /**
     * <p>
     * getYerr.</p>
     *
     * @param point a {@link hep.dataforge.data.DataPoint} object.
     * @return a double.
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public Value getYerr(DataPoint point) throws NameNotFoundException {
        if (point.names().contains(yErrName)) {
            return point.getValue(yErrName);
        } else {
            return Value.of(0);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param point
     * @return
     */
    @Override
    public double getWeight(DataPoint point) throws NameNotFoundException {
        if (point.names().contains(WEIGHT)) {
            return point.getDouble(WEIGHT);
        } else {
            double r = getYerr(point).doubleValue();
            return 1 / (r * r);
        }
    }

    public boolean providesXError(DataPoint point) {
        return point.hasValue(xErrName);
    }

    public boolean providesYError(DataPoint point) {
        return point.hasValue(yErrName);
    }

    public DataPoint mapTo(DataPoint point, String xName, String yName, String xErrName, String yErrName) {
        MapDataPoint res = new MapDataPoint();
        res.putValue(xName, getX(point));
        res.putValue(yName, getY(point));
        if (providesXError(point)) {
            res.putValue(xErrName, getXerr(point));
        }
        if (providesYError(point)) {
            res.putValue(yErrName, getYerr(point));
        }
        return res;
    }

    public DataPoint mapToDefault(DataPoint point) {
        return mapTo(point, "x", "y", "xErr", "yErr");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Names getNames() {
        return Names.of(xName, yName, xErrName, yErrName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Meta buildAnnotation() {
        return new MetaBuilder(DataAdapter.DATA_ADAPTER_ANNOTATION_NAME)
                .putValue("xName", xName)
                .putValue("yName", yName)
                .putValue("xErrName", xErrName)
                .putValue("xErrName", yErrName)
                .build();
    }

}
