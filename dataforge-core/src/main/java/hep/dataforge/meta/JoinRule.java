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
package hep.dataforge.meta;

import hep.dataforge.values.Value;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>JoinRule class.</p>
 *
 * @author darksnake
 * @version $Id: $Id
 */
public class JoinRule extends MergeRule {

    /** {@inheritDoc} */
    @Override
    protected ListMergeRule<Meta> elementsMerger() {
        return (String name, List<? extends Meta> first, List<? extends Meta> second) -> {
            List<Meta> list = new ArrayList<>();
            list.addAll(first);
            list.addAll(second);
            return list;
        };
    }

    /** {@inheritDoc} */
    @Override
    protected String mergeName(String mainName, String secondName) {
        return mainName;
    }

    /** {@inheritDoc} */
    @Override
    protected ListMergeRule<Value> valuesMerger() {
        return (String name, List<? extends Value> first, List<? extends Value> second) -> {
            List<Value> list = new ArrayList<>();
            list.addAll(first);
            list.addAll(second);
            return list;

        };
    }
}
