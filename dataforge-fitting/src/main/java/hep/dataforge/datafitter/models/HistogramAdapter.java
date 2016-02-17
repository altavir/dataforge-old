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

import hep.dataforge.data.AbstractDataAdapter;
import hep.dataforge.data.DataAdapter;
import hep.dataforge.data.DataPoint;
import hep.dataforge.meta.MetaBuilder;

/**
 * TODO сделать адаптер для гистограммы с фиксированными бинами
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class HistogramAdapter extends AbstractDataAdapter {

    public static final String BIN_BEGIN_NAME = "binBegin";
    public static final String BIN_END_NAME = "binEnd";
    public static final String BIN_CENTER_NAME = "binCenter";
    public static final String COUNT_NAME = "count";

    public HistogramAdapter() {
    }
    
    public HistogramAdapter(String binBeginName, String binEndName, String countName) {
        super(new MetaBuilder(DataAdapter.DATA_ADAPTER_ANNOTATION_NAME)
                .putValue(BIN_BEGIN_NAME, binBeginName)
                .putValue(BIN_END_NAME, binEndName)
                .putValue(COUNT_NAME, countName)
                .build());
    }

    public double getWeight(DataPoint point) {
        if (point.names().contains(WEIGHT)) {
            return point.getDouble(WEIGHT);
        } else {
            return 1 / getCount(point);
        }
    }

    public double getBinBegin(DataPoint point) {
        return this.getFrom(point, BIN_BEGIN_NAME).doubleValue();
    }

    public double getBinEnd(DataPoint point) {
        return this.getFrom(point, BIN_END_NAME).doubleValue();
    }

    public long getCount(DataPoint point) {
        return this.getFrom(point, COUNT_NAME).longValue();
    }

    public double getBinSize(DataPoint point) {
        return getBinEnd(point) - getBinBegin(point);
    }

    public double getBinCenter(DataPoint point) {
        return (getBinEnd(point) + getBinBegin(point)) / 2;
    }
}
