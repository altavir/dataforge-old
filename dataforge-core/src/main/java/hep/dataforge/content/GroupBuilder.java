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
package hep.dataforge.content;

import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.meta.Meta;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Laminate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class to build groups of content with annotation defined rules
 *
 * @author Alexander Nozik
 */
public class GroupBuilder {

    /**
     * Create grouping rule that creates groups for different values of value field with name {@code tag}
     * @param tag
     * @param defaultTagValue
     * @return 
     */
    public static GroupRule byValue(final String tag, String defaultTagValue) {
        return new GroupRule() {
            @Override
            public <T extends Content> List<NamedGroup<T>> group(Iterable<T> content) {
                Map<String, List<T>> map = new HashMap<>();
                for (T c : content) {
                    String tagValue = DescriptorUtils.buildDefaultNode(c.getDescriptor())
                            .getString(tag, defaultTagValue);
                    if (!map.containsKey(tagValue)) {
                        map.put(tagValue, new ArrayList<>());
                    }
                    map.get(tagValue).add(c);
                }
                List<NamedGroup<T>> res = new ArrayList<>();
                for (Map.Entry<String, List<T>> entry : map.entrySet()) {
                    res.add(new ContentList(entry.getKey(), entry.getValue()));
                }
                return res;
            }
        };
    }
    
    @ValueDef(name = "byValue", required = true, info = "The name of annotation value by which grouping should be made")
    @ValueDef(name = "defaultValue", def = "default", info = "Default value which should be used for content "
            + "in which the grouping value is not presented")
    public static GroupRule byAnnotation(Meta groupingAnnotation){
        return byValue(groupingAnnotation.getString("byValue"), groupingAnnotation.getString("defaultValue", "default"));
    }

    public interface GroupRule {

        <T extends Content> List<NamedGroup<T>> group(Iterable<T> content);
        
        default <T extends Content> List<NamedGroup<T>> group(NamedGroup<T> content){
            return group(content.asList());
        }
    }
}
