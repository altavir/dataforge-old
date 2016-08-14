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

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;

import java.util.function.Predicate;

import static hep.dataforge.tables.Filtering.buildConditionSet;

/**
 * Table transformation action
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@TypedActionDef(name = "transformTable", inputType = Table.class, outputType = Table.class, info = "Filter dataset with given filtering rules")
@NodeDef(name = "filters", required = true, info = "The filtering condition.", target = "method::hep.dataforge.tables.Filtering.buildConditionSet")
public class TransformTableAction extends OneToOneAction<Table, Table> {

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    protected Table execute(String name, Laminate meta, Table input) {
        Predicate<DataPoint> filterSet = buildFilter(meta);

        Table res;
        if (filterSet != null) {
            res = input.filter(filterSet);
        } else {
            res = input;
        }
        if (res.size() == 0) {
            throw new ContentException("The resulting DataSet is empty");
        }
        return res;
    }

    private Predicate<DataPoint> buildFilter(Meta meta) {
        Predicate<DataPoint> res = null;
        if (meta.hasNode("filter")) {
            for (Meta filter : meta.getNodes("filter")) {
                Predicate<DataPoint> predicate = buildConditionSet(filter);
                if (res == null) {
                    res = predicate;
                } else {
                    res = res.and(predicate);
                }
            }
            return res;
        } else {
            return null;
        }
    }
}
