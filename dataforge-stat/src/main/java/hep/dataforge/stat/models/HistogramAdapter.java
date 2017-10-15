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
package hep.dataforge.stat.models;

import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.tables.AxisValuesAdapter;
import hep.dataforge.tables.ValuesAdapter;
import hep.dataforge.values.Values;

/**
 * TODO сделать адаптер для гистограммы с фиксированными бинами
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class HistogramAdapter extends AxisValuesAdapter {

    public static final String BIN_BEGIN_NAME = "binBegin";
    public static final String BIN_END_NAME = "binEnd";
    public static final String BIN_CENTER_NAME = "binCenter";
    public static final String COUNT_NAME = "count";

    public HistogramAdapter() {
    }
    
    public HistogramAdapter(String binBeginName, String binEndName, String countName) {
        super(new MetaBuilder(ValuesAdapter.DATA_ADAPTER_KEY)
                .putValue(BIN_BEGIN_NAME, binBeginName)
                .putValue(BIN_END_NAME, binEndName)
                .putValue(COUNT_NAME, countName)
                .build());
    }


    public double getBinBegin(Values point) {
        return this.getComponent(point, BIN_BEGIN_NAME).doubleValue();
    }

    public double getBinEnd(Values point) {
        return this.getComponent(point, BIN_END_NAME).doubleValue();
    }

    public long getCount(Values point) {
        return this.getComponent(point, COUNT_NAME).longValue();
    }

    public double getBinSize(Values point) {
        return getBinEnd(point) - getBinBegin(point);
    }

    public double getBinCenter(Values point) {
        return (getBinEnd(point) + getBinBegin(point)) / 2;
    }
}
