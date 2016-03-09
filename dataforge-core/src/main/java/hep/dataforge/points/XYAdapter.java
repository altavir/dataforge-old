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
package hep.dataforge.points;

import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;

/**
 * Интерпретатор полей DataPoint
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@ValueDef(name = "x", def = "x", info = "X value name")
@ValueDef(name = "y", def = "y", info = "Y value name")
@ValueDef(name = "xErr", def = "xErr", info = "X error value name")
@ValueDef(name = "yErr", def = "yErr", info = "Y error value name")
public class XYAdapter extends AbstractPointAdapter {

    public static final String X_NAME = "x";
    public static final String Y_NAME = "y";
    public static final String X_ERR_NAME = "xErr";
    public static final String Y_ERR_NAME = "yErr";

    public XYAdapter() {
    }

    public XYAdapter(Meta meta) {
        super(meta);
    }

    public XYAdapter(String xName, String xErrName, String yName, String yErrName) {
        super(new MetaBuilder(PointAdapter.DATA_ADAPTER_ANNOTATION_NAME)
                .putValue(X_NAME, xName)
                .putValue(Y_NAME, yName)
                .putValue(X_ERR_NAME, xErrName)
                .putValue(Y_ERR_NAME, yErrName)
                .build()
        );
    }

    public XYAdapter(String xName, String yName, String yErrName) {
        this(xName, null, yName, yErrName);
    }

    public XYAdapter(String xName, String yName) {
        this(xName, null, yName, null);
    }

    public DataPoint buildXYDataPoint(double x, double y, double yErr) {
        return new MapPoint(new String[]{getValueName(X_NAME), getValueName(Y_NAME),getValueName(Y_ERR_NAME)},
                x, y, yErr);
    }

    public Value getX(DataPoint point) {
        return getFrom(point, X_NAME);
    }

    public Value getXerr(DataPoint point) {
        return getFrom(point, X_ERR_NAME, 0d);
    }

    public Value getY(DataPoint point) {
        return getFrom(point, Y_NAME);
    }

    public Value getYerr(DataPoint point) throws NameNotFoundException {
        return getFrom(point, Y_ERR_NAME, 0d);
    }

    /**
     * Upper 1-sigma bound on y value
     *
     * @param point
     * @return
     */
    public double getYUpper(DataPoint point) {
        return point.getDouble(getValueName("yUp"), getY(point).doubleValue() + getYerr(point).doubleValue());
    }

    /**
     * Lower 1-sigma bound on y value
     *
     * @param point
     * @return
     */
    public double getYLower(DataPoint point) {
        return point.getDouble(getValueName("yLo"), getY(point).doubleValue() - getYerr(point).doubleValue());
    }

    /**
     * {@inheritDoc}
     *
     * @param point
     * @return
     */
    public double getWeight(DataPoint point) throws NameNotFoundException {
        if (point.names().contains(WEIGHT)) {
            return point.getDouble(WEIGHT);
        } else {
            double r = getYerr(point).doubleValue();
            return 1 / (r * r);
        }
    }

    public boolean providesXError(DataPoint point) {
        return point.hasValue(getValueName(X_ERR_NAME));
    }

    public boolean providesYError(DataPoint point) {
        return point.hasValue(getValueName(Y_ERR_NAME));
    }

    public DataPoint mapTo(DataPoint point, String xName, String yName, String xErrName, String yErrName) {
        MapPoint res = new MapPoint();
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
        return mapTo(point, X_NAME, Y_NAME, X_ERR_NAME, Y_ERR_NAME);
    }

    /**
     * Return a default Format corresponding to this adapter
     * @return 
     */
    public Format getFormat(){
        return new FormatBuilder()
                .addNumber(X_NAME)
                .addNumber(Y_NAME)
                .build();
    }

}
