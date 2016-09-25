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
package hep.dataforge.tables;

import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Name;
import hep.dataforge.values.Value;

/**
 * An adapter to correctly interpret DataPoint into X-Y plot or model. could have multiple Y-s
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@NodeDef(name = "x", info = "x axis mapping", target = "method::hep.dataforge.tables.AbstractPointAdapter.getAxisMeta")
@NodeDef(name = "y", info = "y axis mapping", target = "method::hep.dataforge.tables.AbstractPointAdapter.getAxisMeta")
public class XYAdapter extends AbstractPointAdapter {

    public static final String X_AXIS = "x";
    public static final String Y_AXIS = "y";

    public static final String X_VALUE_KEY = Name.joinString(X_AXIS, VALUE_KEY);
    public static final String X_ERROR_KEY = Name.joinString(X_AXIS, ERROR_KEY);
    public static final String Y_VALUE_KEY = Name.joinString(Y_AXIS, VALUE_KEY);
    public static final String Y_ERROR_KEY = Name.joinString(Y_AXIS, ERROR_KEY);


    public static final XYAdapter DEFAULT_ADAPTER = new XYAdapter();

    public static final MetaBuilder buildAdapterMeta(String xName, String xErrName, String yName, String yErrName) {
        return new MetaBuilder(PointAdapter.DATA_ADAPTER_KEY)
                .putValue(X_VALUE_KEY, xName)
                .putValue(Y_VALUE_KEY, yName)
                .putValue(X_ERROR_KEY, xErrName)
                .putValue(Y_ERROR_KEY, yErrName);
    }

    protected XYAdapter() {
    }

    public XYAdapter(Meta meta) {
        super(meta);
    }

    public XYAdapter(String xName, String xErrName, String yName, String yErrName) {
        super(buildAdapterMeta(xName, xErrName, yName, yErrName));
    }

    public XYAdapter(String xName, String yName, String yErrName) {
        this(xName, null, yName, yErrName);
    }

    public XYAdapter(String xName, String yName) {
        this(xName, null, yName, null);
    }


    public DataPoint buildXYDataPoint(double x, double y, double yErr) {
        return new MapPoint(new String[]{getValueName(X_VALUE_KEY), getValueName(Y_VALUE_KEY), getValueName(Y_ERROR_KEY)},
                x, y, yErr);
    }

    private String yAxis(int index) {
        if (getYCount() == 1) {
            return Y_AXIS;
        } else {
            return Y_AXIS + "[" + index + "]";
        }
    }

    /**
     * The number of y axis explicitly defined. If no y axis defined returns 1.
     *
     * @return
     */
    public int getYCount() {
        if (meta().hasNode(Y_AXIS)) {
            return this.meta().getNodes(Y_AXIS).size();
        } else return 1;
    }

    public String getYTitle(int i) {
        return meta().getNodes(Y_AXIS).get(i).getString("axisTitle", getValueName(VALUE_KEY));
    }

    public Value getX(DataPoint point) {
        return getValue(point, X_AXIS);
    }

    public Value getXerr(DataPoint point) {
        return getError(point, X_AXIS);
    }

    /**
     * Get y value for first y axis
     *
     * @param point
     * @return
     */
    public Value getY(DataPoint point) {
        return getValue(point, Y_AXIS);
    }

    /**
     * get y error for first y axis
     *
     * @param point
     * @return
     */
    public Value getYerr(DataPoint point) {
        return getError(point, Y_AXIS);
    }

    /**
     * get y value for any y axis.
     *
     * @param point
     * @param index
     * @return
     */
    public Value getY(DataPoint point, int index) {
        return getValue(point, yAxis(index));
    }

    public Value getYerr(DataPoint point, int index) {
        return getError(point, yAxis(index));
    }

    /**
     * Upper bound on y value
     *
     * @param point
     * @return
     */
    public double getYUpper(DataPoint point) {
        return getUpperBound(point, Y_AXIS);
    }

    /**
     * Lower bound on y value
     *
     * @param point
     * @return
     */
    public double getYLower(DataPoint point) {
        return getLowerBound(point, Y_AXIS);
    }

    /**
     * Upper bound on x value
     *
     * @param point
     * @return
     */
    public double getXUpper(DataPoint point) {
        return getUpperBound(point, X_AXIS);
    }

    /**
     * Lower bound on x value
     *
     * @param point
     * @return
     */
    public double getXLower(DataPoint point) {
        return getLowerBound(point, X_AXIS);
    }

    public double getYUpper(DataPoint point, int index) {
        return getUpperBound(point, yAxis(index));
    }

    public double getYLower(DataPoint point, int index) {
        return getLowerBound(point, yAxis(index));
    }

    public boolean providesXError(DataPoint point) {
        return point.hasValue(getValueName(X_ERROR_KEY));
    }

    public boolean providesYError(DataPoint point) {
        return point.hasValue(getValueName(Y_ERROR_KEY));
    }

    /**
     * Return a default TableFormat corresponding to this adapter
     *
     * @return
     */
    public TableFormat getFormat() {
        //FIXME wrong table format here
        //TODO move to utils
        return new TableFormatBuilder()
                .addNumber(getValueName(X_VALUE_KEY))
                .addNumber(getValueName(Y_VALUE_KEY))
                .build();
    }

}
