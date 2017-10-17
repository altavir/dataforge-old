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
@NodeDef(name = "y", info = "y axis mapping", from = "method::hep.dataforge.tables.AxisValuesAdapter.getAxisMeta")
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
            return new XYAdapter(adapter.meta());
        }
    }

    private static MetaBuilder buildAdapterMeta(String xName, String xErrName, String yName, String yErrName) {
        return new MetaBuilder(ValuesAdapter.ADAPTER_KEY)
                .putValue(X_VALUE_KEY, xName)
                .putValue(Y_VALUE_KEY, yName)
                .putValue(X_ERROR_KEY, xErrName)
                .putValue(Y_ERROR_KEY, yErrName);
    }

    private String xValueName;
    private String[] yValuesNames;
    private String xErrorName;
    private String[] yErrorsNames;


    public XYAdapter() {
        this(Meta.buildEmpty(ADAPTER_KEY));
    }

    public XYAdapter(Meta meta) {
        super(meta);
        updateCache();
    }

    public XYAdapter(String xName, String xErrName, String yName, String yErrName) {
        this(buildAdapterMeta(xName, xErrName, yName, yErrName));
    }

    public XYAdapter(String xName, String yName, String yErrName) {
        this(xName, null, yName, yErrName);
    }

    public XYAdapter(String xName, String yName) {
        this(xName, null, yName, null);
    }

    private void updateCache() {
        xValueName = meta().getString(X_VALUE_KEY, X_AXIS);
        xErrorName = meta().getString(X_ERROR_KEY, X_ERROR_KEY);
        if (meta().hasMeta(Y_AXIS)) {
            yValuesNames = meta().getMetaList(Y_AXIS).stream().map(node -> node.getString(VALUE_KEY, Y_AXIS))
                    .toArray(String[]::new);
            yErrorsNames = meta().getMetaList(Y_AXIS).stream().map(node -> node.getString(ERROR_KEY, ERROR_KEY))
                    .toArray(String[]::new);
        } else {
            yValuesNames = new String[]{Y_AXIS};
            yErrorsNames = new String[]{Y_ERROR_KEY};
        }
    }

    public Values buildXYDataPoint(double x, double y, double yErr) {
        return ValueMap.of(new String[]{nameFor(X_VALUE_KEY), nameFor(Y_VALUE_KEY), nameFor(Y_ERROR_KEY)},
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
        return yValuesNames.length;
    }

    public String getYTitle(int i) {
        return meta().getMetaList(Y_AXIS).get(i).getString("axisTitle", nameFor(VALUE_KEY));
    }

    public Value getX(Values point) {
        return point.getValue(xValueName);
    }

    public Value getXerr(Values point) {
        return point.getValue(xErrorName, Value.NULL);
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
        return point.getValue(yValuesNames[index]);
    }

    public Value getYerr(Values point, int index) {
        return point.getValue(yErrorsNames[index]);
    }

    /**
     * Upper bound on y value
     *
     * @param point
     * @return
     */
    public double getYUpper(Values point) {
        return getUpperBound(point, Y_AXIS);
    }

    /**
     * Lower bound on y value
     *
     * @param point
     * @return
     */
    public double getYLower(Values point) {
        return getLowerBound(point, Y_AXIS);
    }

    /**
     * Upper bound on x value
     *
     * @param point
     * @return
     */
    public double getXUpper(Values point) {
        return getUpperBound(point, X_AXIS);
    }

    /**
     * Lower bound on x value
     *
     * @param point
     * @return
     */
    public double getXLower(Values point) {
        return getLowerBound(point, X_AXIS);
    }

    public double getYUpper(Values point, int index) {
        return getUpperBound(point, yAxis(index));
    }

    public double getYLower(Values point, int index) {
        return getLowerBound(point, yAxis(index));
    }

    public boolean providesXError(Values point) {
        return point.hasValue(xErrorName);
    }

    public boolean providesYError(Values point) {
        return point.hasValue(yErrorsNames[0]);
    }

    /**
     * Return a default TableFormat corresponding to this adapter
     * TODO move to utils
     *
     * @return
     */
    public TableFormat getFormat() {
        updateCache();
        TableFormatBuilder builder = new TableFormatBuilder()
                .addNumber(xValueName, X_VALUE_KEY);

        if (meta().hasValue(X_ERROR_KEY)) {
            builder.addNumber(xErrorName, X_ERROR_KEY);
        }

        for (String yValueKey : yValuesNames) {
            builder.addNumber(yValueKey, Y_VALUE_KEY);
        }

        if (meta().hasValue(Y_ERROR_KEY)) {
            for (String yErrorKey : yErrorsNames) {
                builder.addNumber(yErrorKey, Y_ERROR_KEY);
            }
        }

        return builder.build();
    }


    @Override
    public void fromMeta(Meta meta) {
        super.fromMeta(meta);
        updateCache();
    }
}
