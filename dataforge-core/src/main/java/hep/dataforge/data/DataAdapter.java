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

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Names;

/**
 * An adapter to interpret datapoint
 * @author Alexander Nozik
 */
public interface DataAdapter {
    
    public static final String WEIGHT = "@weight";    

    public static final String DATA_ADAPTER_ANNOTATION_NAME = "adapter";

    public double getWeight(DataPoint point);

    /**
     * Аннотация, описывающая данный тип адаптера
     * @return 
     */
    public Meta buildAnnotation();

    /**
     * минимальный набор имен в DataPoint
     * @return 
     */
    Names getNames();    
    
    default public ListDataSet buildEmptyDataSet(String name) {
        ListDataSet res = new ListDataSet(DataFormat.forNames(0, getNames()));
        res.configure(new MetaBuilder(name).putNode(buildAnnotation()).build());
        return res;
    }
}
