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
import java.util.Collection;
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
     * @param main a {@link hep.dataforge.meta.Meta} object.
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
        MetaBuilder builder = new MetaBuilder(mergeName(main.getName(), second.getName()));

        Collection<String> secondValues = second.getValueNames();
        Collection<String> mainValues = main.getValueNames();

        // Сначала копирум все значения второй аннотации, которых нет в первой
        for (String name : secondValues) {
            if (!mainValues.contains(name)) {
                builder = writeValue(builder, name, second.getValue(name));
            }
        }
        // Копируем все значения первой аннотации, объединяя их по необходимости
        for (String name : mainValues) {
            if (!secondValues.contains(name)) {
                builder = writeValue(builder, name, main.getValue(name));
            } else {
                List<Value> item = new ArrayList<>(valuesMerger()
                        .merge(name, main.getValue(name).listValue(), second.getValue(name).listValue()));
                builder = writeValue(builder, name, Value.of(item));
            }
        }

        // То же самое для элементов
        Collection<String> secondElements = second.getNodeNames();
        Collection<String> mainElements = main.getNodeNames();

        // Сначала копирум все значения второй аннотации, которых нет в первой
        for (String name : secondElements) {
            if (!mainElements.contains(name)) {
                builder = writeElement(builder, name, second.getNodes(name));
            }
        }

        // Копируем все значения первой аннотации, объединяя их по необходимости
        for (String name : mainElements) {
            if (!secondElements.contains(name)) {
                builder = writeElement(builder, name, main.getNodes(name));
            } else {
                List<? extends Meta> item = elementsMerger().merge(name, main.getNodes(name), second.getNodes(name));
                builder = writeElement(builder, name, item);
            }
        }

        return builder;
    }

    protected abstract String mergeName(String mainName, String secondName);

    protected abstract ListMergeRule<Value> valuesMerger();

    protected abstract ListMergeRule<Meta> elementsMerger();

    protected MetaBuilder writeValue(MetaBuilder builder, String name, Value item) {
        return builder.setValue(name, item);
    }

    protected MetaBuilder writeElement(MetaBuilder builder, String name, List<? extends Meta> item) {
        return builder.setNode(name, item);
    }
}
