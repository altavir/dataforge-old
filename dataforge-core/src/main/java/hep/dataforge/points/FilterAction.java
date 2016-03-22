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

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.context.Context;
import hep.dataforge.description.NodeDef;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Meta;
import static hep.dataforge.points.Filtering.buildConditionSet;
import java.util.function.Predicate;

/**
 * действие для фильтрации {@code ListPointSet}
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@TypedActionDef(name = "filterData", inputType = PointSet.class, outputType = PointSet.class, description = "Filter dataset with given filtering rules")
@NodeDef(name = "filters", required = true, info = "The filtering condition.", target = "method::hep.dataforge.points.Filtering.buildConditionSet")
public class FilterAction extends OneToOneAction<PointSet, PointSet> {

    /**
     * <p>
     * Constructor for DataFilterAction.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     */
    public FilterAction(Context context, Meta annotation) {
        super(context, annotation);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    protected PointSet execute(Logable log, String name, Meta meta, PointSet input) {
        Predicate<DataPoint> filterSet = buildFilter(meta);

        PointSet res;
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
