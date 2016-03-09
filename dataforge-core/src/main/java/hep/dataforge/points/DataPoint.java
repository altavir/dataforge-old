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

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import hep.dataforge.names.NameSet;

/**
 * Интерфейс для определения абстрактной точки с произвольной размерностью.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface DataPoint extends NameSet, ValueProvider {

    public static Meta toMeta(DataPoint point) {
        MetaBuilder builder = new MetaBuilder("point");
        for (String name : point.namesAsArray()) {
            builder.putValue(name, point.getValue(name));
        }
        return builder.build();
    }

    public static List<DataPoint> fromMeta(Meta annotation) {
        List<DataPoint> res = new ArrayList<>();
        for (Meta pointAn : annotation.getNodes("point")) {
            Map<String, Value> map = new HashMap<>();
            for (String key : pointAn.getValueNames()) {
                map.put(key, pointAn.getValue(key));
            }
            res.add(new MapPoint(map));
        }
        return res;
    }

    /**
     * {@inheritDoc}
     *
     * Значение числового поля, может быть целым или числом с плавающей точкой.
     * При операциях с объектами типа Number наблюдается потеря
     * производительности, поэтому все вычисления желательно проводить в
     * примитивах.
     *
     * Имя в принципе может быть составным. Эту возможность можно использовать
     * для построения деревьев a la root
     */
    @Override
    Value getValue(String name) throws NameNotFoundException;

//    @Override
//    default double getDouble(String name) throws NameNotFoundException{
//        return getValue(name).doubleValue();
//    }
    /**
     * Метод для удобной фильтрации по булевым тэгам
     *
     * @param name
     * @return
     */
    default boolean hasTag(String name) {
        if (!names().contains(name)) {
            return false;
        } else {
            return getValue(name).booleanValue();
        }
    }

    DataPoint copy();

}
