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

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;
import hep.dataforge.utils.MetaMorph;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;

/**
 * An adapter to interpret datapoint
 * @author Alexander Nozik
 */
public interface ValuesAdapter extends Metoid, MetaMorph {
    
    String ADAPTER_KEY = "@adapter";

    /**
     * Аннотация, описывающая данный тип адаптера
     * @return 
     */
    @Override
    Meta meta();

    /**
     * Get a value with specific designation from given DataPoint
     * @param point
     * @param component
     * @return 
     */
    Value getComponent(Values point, String component);
}
