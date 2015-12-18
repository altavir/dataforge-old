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

/**
 * <p>CustomMergeRule class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class CustomMergeRule extends MergeRule {
    
    private ListMergeRule<Value> valueMerger;
    private ListMergeRule<Meta> elementMerger;

    /**
     * <p>Constructor for CustomMergeRule.</p>
     *
     * @param itemMerger a {@link hep.dataforge.meta.ListMergeRule} object.
     * @param elementMerger a {@link hep.dataforge.meta.ListMergeRule} object.
     */
    public CustomMergeRule(ListMergeRule<Value> itemMerger, ListMergeRule<Meta> elementMerger) {
        this.valueMerger = itemMerger;
        this.elementMerger = elementMerger;
    }
    
    /** {@inheritDoc} */
    @Override
    protected String mergeName(String mainName, String secondName) {
        return mainName;
    }

    /** {@inheritDoc} */
    @Override
    protected ListMergeRule<Value> valuesMerger() {
        return valueMerger;
    }

    /** {@inheritDoc} */
    @Override
    protected ListMergeRule<Meta> elementsMerger() {
        return elementMerger;
    }
    
}
