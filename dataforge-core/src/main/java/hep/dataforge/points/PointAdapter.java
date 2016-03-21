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

import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

/**
 * An adapter to interpret datapoint
 * @author Alexander Nozik
 */
public interface PointAdapter extends Annotated {
    
    public static final String WEIGHT = "@weight";    

    public static final String DATA_ADAPTER_ANNOTATION_NAME = "adapter";

//    public double getWeight(DataPoint point);

    /**
     * Аннотация, описывающая данный тип адаптера
     * @return 
     */
    @Override
    Meta meta();

//    /**
//     * минимальный набор имен в DataPoint
//     * @return 
//     */
//    Names getNames();    
    
    /**
     * Get a value with specific designation from given DataPoint
     * @param point
     * @param component
     * @return 
     */
    Value getFrom(DataPoint point, String component);
    
//    default public ListPointSet buildEmptyDataSet(String name) {
//        ListPointSet res = new ListPointSet(DataFormat.forNames(0, getNames()));
//        res.configure(new MetaBuilder(name).putNode(meta()).build());
//        return res;
//    }
}