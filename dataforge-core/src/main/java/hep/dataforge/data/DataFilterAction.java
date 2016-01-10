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

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.context.Context;
import static hep.dataforge.data.DataFiltering.buildConditionSet;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import java.util.function.Predicate;

/**
 * действие для фильтрации {@code ListDataSet}
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@TypedActionDef(name = "filterData", inputType = DataSet.class, outputType = DataSet.class, description = "Filter dataset with given filtering rules")
@NodeDef(name = "filters", required = true, info = "The filtering condition.", target = "method::hep.dataforge.data.DataFiltering.buildConditionSet")
public class DataFilterAction extends OneToOneAction<DataSet, DataSet> {

    /**
     * <p>
     * Constructor for DataFilterAction.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     */
    public DataFilterAction(Context context, Meta annotation) {
        super(context, annotation);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    protected DataSet execute(Logable log, Meta meta, DataSet input) {
        Predicate<DataPoint> filterSet = buildFilter(meta);

        DataSet res;
        if (filterSet != null) {
            res = input.filter(filterSet);
        } else {
            res = input;
        }
        if (res.size() == 0) {
            throw new ContentException("The resulting DataSet is empty");
        }
        res.setMeta(input.meta());
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
