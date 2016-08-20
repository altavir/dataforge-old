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

import hep.dataforge.names.Name;
import hep.dataforge.values.Value;

import java.util.Collections;
import java.util.List;

/**
 * Правило объединения двух аннотаций
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class MergeRule {

    /**
     * Правило объединения по-умолчанию. Подразумевается простая замена всех
     * совподающих элементов.
     *
     * @return
     */
    public static MergeRule getDefault() {
        return new DefaultMergeRule();
    }

    /**
     * Возвращает правило объединения в котором элементы, входящие в список
     * объединяются, а остальные заменяются
     *
     * @param toJoin
     * @return
     */
    public static MergeRule getConfigured(String... toJoin) {
        return new ConfigurableMergeRule(toJoin);
    }

    /**
     * Выполняет объединение с заменой всех совподающих элементов
     *
     * @param main
     * @param second
     * @return
     */
    public static MetaBuilder replace(Meta main, Meta second) {
        return getDefault().merge(main, second);
    }

    /**
     * Выполняет объединение с объединением всех списков
     *
     * @param main   a {@link hep.dataforge.meta.Meta} object.
     * @param second a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    public static Meta join(Meta main, Meta second) {
        return new JoinRule().merge(main, second);
    }

    /**
     * Метод, объединяющий две аннотации. Порядок имеет значение. Первая
     * аннотация является основной, вторая запасной
     *
     * @param main
     * @param second
     * @return
     */
    public MetaBuilder merge(Meta main, Meta second) {
        return mergeInPlace(main, new MetaBuilder(second));
    }

    /**
     * Apply changes from main Meta to meta builder in place
     *
     * @param main
     * @param builder
     * @return
     */
    public MetaBuilder mergeInPlace(Meta main, MetaBuilder builder) {
        //MetaBuilder builder = new MetaBuilder(mergeName(main.getName(), second.getName()));
        builder.rename(mergeName(main.getName(), builder.getName()));

        // Overriding values
        for (String valueName : main.getValueNames()) {
            if (!builder.hasValue(valueName)) {
                builder = writeValue(builder, valueName, main.getValue(valueName));
            } else {
                builder = writeValue(builder, valueName, mergeValues(Name.join(builder.getFullName(), Name.of(valueName)),
                        main.getValue(valueName), builder.getValue(valueName)));
            }
        }

        // Overriding nodes
        for (String nodeName : main.getNodeNames()) {
            if (!builder.hasNode(nodeName)) {
                builder = writeElement(builder, nodeName, main.getNodes(nodeName));
            } else {
                List<? extends Meta> mainNodes = main.getNodes(nodeName);
                List<? extends Meta> secondNodes = builder.getNodes(nodeName);
                if (mainNodes.size() == 1 && secondNodes.size() == 1) {
                    writeElement(builder, nodeName, Collections.singletonList(merge(mainNodes.get(0), secondNodes.get(0))));
                } else {
                    //TODO apply smart merging rule for lists?
                    List<? extends Meta> item = mergeNodes(Name.join(builder.getFullName(),
                            Name.of(nodeName)), mainNodes, secondNodes);
                    builder = writeElement(builder, nodeName, item);
                }
            }
        }

        return builder;
    }

    protected abstract String mergeName(String mainName, String secondName);

    /**
     * @param valueName full name of the value relative to root
     * @param first
     * @param second
     * @return
     */
    protected abstract Value mergeValues(Name valueName, Value first, Value second);

    /**
     * @param nodeName       full name of the node relative to root
     * @param mainNodes
     * @param secondaryNodes
     * @return
     */
    protected abstract List<? extends Meta> mergeNodes(Name nodeName, List<? extends Meta> mainNodes, List<? extends Meta> secondaryNodes);

    protected MetaBuilder writeValue(MetaBuilder builder, String name, Value item) {
        return builder.setValue(name, item);
    }

    protected MetaBuilder writeElement(MetaBuilder builder, String name, List<? extends Meta> item) {
        return builder.setNode(name, item);
    }
}
