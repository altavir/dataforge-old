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
package hep.dataforge.actions;

import hep.dataforge.content.ContentList;
import hep.dataforge.content.NamedGroup;
import hep.dataforge.dependencies.Data;
import hep.dataforge.dependencies.DataNode;
import hep.dataforge.dependencies.DataSet;
import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.util.Pair;

/**
 * The class to build groups of content with annotation defined rules
 *
 * @author Alexander Nozik
 */
public class GroupBuilder {

    /**
     * Create grouping rule that creates groups for different values of value
     * field with name {@code tag}
     *
     * @param tag
     * @param defaultTagValue
     * @return
     */
    public static GroupRule byValue(final String tag, String defaultTagValue) {
        return new GroupRule() {
            @Override
            public <T> Map<String, DataNode<T>> group(DataNode<T> input) {
                Map<String, DataSet.Builder<T>> map = new HashMap<>();

                input.stream().forEach((Pair<String, Data<? extends T>> entry) -> {
                    String tagValue = DescriptorUtils.buildDefaultNode(DescriptorUtils.buildDescriptor(entry.getValue()))
                            .getString(tag, defaultTagValue);
                    if (!map.containsKey(tagValue)) {
                        DataSet.Builder<T> builder = DataSet.builder();
                        builder.setName(tagValue);
                        //PENDING share meta here?
                        map.put(tagValue, builder);
                    }
                    map.get(tagValue).putData(entry.getKey(), entry.getValue());
                });

                return map.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry-> entry.getValue().build()));
            }
        };
    }

    @ValueDef(name = "byValue", required = true, info = "The name of annotation value by which grouping should be made")
    @ValueDef(name = "defaultValue", def = "default", info = "Default value which should be used for content "
            + "in which the grouping value is not presented")
    public static GroupRule byAnnotation(Meta groupingAnnotation) {
        return byValue(groupingAnnotation.getString("byValue"), groupingAnnotation.getString("defaultValue", "default"));
    }

    public interface GroupRule {
        <T> Map<String, DataNode<T>> group(DataNode<T> input);
    }
}
