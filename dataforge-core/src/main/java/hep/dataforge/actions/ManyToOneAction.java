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

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MergeRule;
import hep.dataforge.content.Content;
import hep.dataforge.content.GroupBuilder;
import hep.dataforge.content.NamedGroup;
import hep.dataforge.context.Context;
import hep.dataforge.dependencies.Dependency;
import hep.dataforge.dependencies.DependencySet;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Laminate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Abstract ManyToOneAction class.</p>
 *
 * FIXME bad architecture
 * @author Alexander Nozik
 * @param <T>
 * @param <R>
 * @version $Id: $Id
 */
public abstract class ManyToOneAction<T extends Content, R extends Content> extends GenericAction<T, R> {

    /**
     * <p>
     * Constructor for ManyToOneAction.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     */
    public ManyToOneAction(Context context, String name, Meta annotation) {
        super(context, name, annotation);
    }

    /**
     * <p>
     * Constructor for ManyToOneAction.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     */
    public ManyToOneAction(Context context, Meta annotation) {
        super(context, annotation);
    }

    /**
     * {@inheritDoc}
     *
     * @param groupAnnotation
     * @return
     */
    @Override
    protected List<Dependency<R>> execute(Logable log, Meta groupAnnotation, DependencySet<T> input) {
        List<T> inputList = StreamSupport.stream(input.spliterator(), false).<T>map((i)->i.get()).collect(Collectors.toList());
        List<NamedGroup<T>> groups = buildGroups(readMeta(groupAnnotation),inputList);
        List<Dependency<R>> res;
        if (isParallelExecutionAllowed()) {
            res = groups.parallelStream()
                    .<Dependency<R>>map((group) -> wrap(log, groupAnnotation, runGroup(log, group)))
                    .collect(Collectors.toList());
        } else {
            res = groups.stream()
                    .<Dependency<R>>map((group) -> wrap(log, groupAnnotation, runGroup(log, group)))
                    .collect(Collectors.toList());
        }
        return res;
    }

    /**
     * <p>
     * runGroup.</p>
     *
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @param input a {@link hep.dataforge.content.NamedGroup} object.
     * @return a R object.
     */
    public R runGroup(Logable log, NamedGroup<T> input) {
        beforeGroup(log, input);
        Meta individualMeta = readMeta(input.meta());
        R res = execute(log, individualMeta, input);
        afterGroup(log, res);
        res.configure(MergeRule.getDefault().merge(res.meta(), input.meta()));
        return res;
    }

    /**
     * <p>
     * buildGroups.</p>
     *
     * @param meta a {@link hep.dataforge.description.MetaReader}
     * object.
     * @param input a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    protected List<NamedGroup<T>> buildGroups(Meta meta, List<T> input) {
        return GroupBuilder.byAnnotation(meta.getNode("grouping")).group(input);
    }

    /**
     * Calculate the action result for single input content
     *
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @param reader a {@link hep.dataforge.description.MetaReader}
     * object.
     * @param input a T object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     * @return a R object.
     */
    protected abstract R execute(Logable log, Meta meta, NamedGroup<T> input);

    /**
     * Выполняется один раз перед основным действием независимо от того,
     * распаралелено действие или нет
     *
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @param input a {@link hep.dataforge.content.Content} object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     */
    protected void beforeGroup(Logable log, NamedGroup<T> input) throws ContentException {

    }

    /**
     * Выполняется один раз после основного действия независимо от того,
     * распаралелено действие или нет
     *
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @param output a {@link hep.dataforge.content.Content} object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     */
    protected void afterGroup(Logable log, R output) throws ContentException {

    }

}
