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
package hep.dataforge.storage.commons;

import hep.dataforge.data.DataPoint;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

/**
 * Провайдер для временных серий произвольного вида. Каждая точка в обязательном
 * порядке должна содежрать time-значение TIMESTAMP. Подгрузка точек может быть
 * ленивой.
 *
 * @author Darksnake
 */
public interface TimeSeriesProvider extends Iterator<DataPoint>{

    public static String TIMESTAMP = "timestamp";

//    /**
//     * Выборка из равномерно по времени рабросанных точек. При отсутствии нужной
//     * точки в исходном наборе данных, берется ближайшая точка, время которой
//     * меньше заданного
//     *
//     * @param from
//     * @param to
//     * @param step
//     * @return
//     */
//    DataSet getUniformTimeSeries(Instant from, Instant to, Duration step);

    /**
     * Возвращает точку с штампом временеи ближайшим (снизу) к заданному
     * @param time
     * @return 
     */
    DataPoint get(Instant time);
    
    /**
     * Точка с наибольшим временем
     * @return 
     */
    DataPoint getLast();
    
   
//    /**
//     * Точка с наименьшим временем
//     * @return 
//     */
//    DataPoint getFirst();
    
    /**
     * The collection of data points with timestamp greater then from. Empty collections allowed
     * @param from
     * @return 
     */
    List<DataPoint> updateFrom(Instant from);
}
