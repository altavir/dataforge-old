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
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Name;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

/**
 * An adapter to correctly interpret DataPoint into X-Y plot or model. Could have multiple Y-s.
 * By default point keys are constructed as {@code <axis name>.<field key>}
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@NodeDef(name = "x", info = "x axis mapping", from = "method::hep.dataforge.tables.AxisValuesAdapter.getAxisMeta")
@NodeDef(name = "y", multiple = true, info = "y axis mapping", from = "method::hep.dataforge.tables.AxisValuesAdapter.getAxisMeta")
public class XYAdapter extends AxisValuesAdapter {

    public static final String X_AXIS = "x";
    public static final String Y_AXIS = "y";

    public static final String X_VALUE_KEY = Name.joinString(X_AXIS, VALUE_KEY);
    public static final String X_ERROR_KEY = Name.joinString(X_AXIS, ERROR_KEY);
    public static final String Y_VALUE_KEY = Name.joinString(Y_AXIS, VALUE_KEY);
    public static final String Y_ERROR_KEY = Name.joinString(Y_AXIS, ERROR_KEY);


    public static final XYAdapter DEFAULT_ADAPTER = new XYAdapter();

    /**
     * Convert any adapter to XY adapter using meta conversion. If input adapter already is XY adapter,
     * then it is returned as is (since it is immutable)
     *
     * @param adapter
     * @return
     */
    public static XYAdapter from(ValuesAdapter adapter) {
        if (adapter instanceof XYAdapter) {
            return (XYAdapter) adapter;
        } else {
            return new XYAdapter(adapter.getMeta());
        }
    }

    private static MetaBuilder buildAdapterMeta(String xName, String xErrName, String yName, String yErrName) {
        return new MetaBuilder(ValuesAdapter.ADAPTER_KEY)
                .setValue(X_VALUE_KEY, xName)
                .setValue(Y_VALUE_KEY, yName)
                .setValue(X_ERROR_KEY, xErrName)
                .setValue(Y_ERROR_KEY, yErrName);
    }

    public XYAdapter() {
        this(Meta.buildEmpty(ADAPTER_KEY));
    }

    public XYAdapter(Meta meta) {
        super(meta);
    }

    private String getYAxisName(int num) {
        if (num == 0) {
            return Y_AXIS;
        } else {
            return Y_AXIS + "[" + num + "]";
        }
    }

    public XYAdapter(String xName, String xErrName, String yName, String yErrName) {
        this(buildAdapterMeta(xName, xErrName, yName, yErrName));
    }

    public XYAdapter(String xName, String yName, String yErrName) {
        this(xName, null, yName, yErrName);
    }

    public XYAdapter(String xName, String... yName) {
        this(new MetaBuilder(ValuesAdapter.ADAPTER_KEY)
                .setValue(X_VALUE_KEY, xName)
                .setValue(Y_VALUE_KEY, yName));
    }

    public Values buildXYDataPoint(double x, double y, double yErr) {
        return ValueMap.of(new String[]{nameFor(X_VALUE_KEY), nameFor(Y_VALUE_KEY), nameFor(Y_ERROR_KEY)},
                x, y, yErr);
    }

    public Values buildXYDataPoint(double x, double y) {
        return ValueMap.of(new String[]{nameFor(X_VALUE_KEY), nameFor(Y_VALUE_KEY)},
                x, y);
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
        return Math.max(getMeta().getMetaList(Y_AXIS).size(), 1);
    }

    public String getYTitle(int i) {
        return getMeta().getMetaList(Y_AXIS).get(i).getString("title", nameFor(VALUE_KEY));
    }

    public final Value getX(Values point) {
        return getValue(point, X_AXIS);
    }

    public final Value getXerr(Values point) {
        return getError(point, X_AXIS);
    }

    /**
     * Get y value for first y axis
     *
     * @param point
     * @return
     */
    public Value getY(Values point) {
        return getY(point, 0);
    }

    /**
     * get y error for first y axis
     *
     * @param point
     * @return
     */
    public Value getYerr(Values point) {
        return getYerr(point, 0);
    }

    /**
     * get y value for any y axis.
     *
     * @param point
     * @param index
     * @return
     */
    public Value getY(Values point, int index) {
        return getValue(point, getYAxisName(index));
    }

    public final Value getYerr(Values point, int index) {
        return getError(point, getYAxisName(index));
    }

    /**
     * Upper bound on y value
     *
     * @param point
     * @return
     */
    public final double getYUpper(Values point) {
        return getUpperBound(point, Y_AXIS);
    }

    /**
     * Lower bound on y value
     *
     * @param point
     * @return
     */
    public final double getYLower(Values point) {
        return getLowerBound(point, Y_AXIS);
    }

    /**
     * Upper bound on x value
     *
     * @param point
     * @return
     */
    public final double getXUpper(Values point) {
        return getUpperBound(point, X_AXIS);
    }

    /**
     * Lower bound on x value
     *
     * @param point
     * @return
     */
    public final double getXLower(Values point) {
        return getLowerBound(point, X_AXIS);
    }

    public final double getYUpper(Values point, int index) {
        return getUpperBound(point, yAxis(index));
    }

    public final double getYLower(Values point, int index) {
        return getLowerBound(point, yAxis(index));
    }

    public boolean providesXError(Values point) {
        return point.hasValue(nameFor(X_ERROR_KEY));
    }

    public boolean providesYError(Values point) {
        return point.hasValue(nameFor(Y_ERROR_KEY));
    }

    /**
     * Return a default TableFormat corresponding to this adapter
     * TODO move to utils
     *
     * @return
     */
    public TableFormat getFormat() {
        TableFormatBuilder builder = new TableFormatBuilder()
                .addNumber(nameFor(X_VALUE_KEY), X_VALUE_KEY);

        if (getMeta().hasValue(X_ERROR_KEY)) {
            builder.addNumber(nameFor(X_ERROR_KEY), X_ERROR_KEY);
        }

        getMeta().getMetaList(Y_AXIS).forEach(axisMeta -> {
            builder.addNumber(axisMeta.getString(VALUE_KEY), Y_VALUE_KEY);
            axisMeta.optString(ERROR_KEY).ifPresent(errKey -> builder.addNumber(errKey, Y_ERROR_KEY));
        });

        return builder.build();
    }
}
