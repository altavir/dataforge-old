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

import hep.dataforge.content.Named;
import hep.dataforge.context.Context;
import hep.dataforge.dependencies.Dependency;
import hep.dataforge.dependencies.DependencySet;
import hep.dataforge.dependencies.SimpleDataDependency;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.log.Log;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.MergeRule;
import hep.dataforge.meta.Meta;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A template to build actions that reflect strictly one to one content
 * transformations
 *
 * @author Alexander Nozik
 * @param <T>
 * @param <R>
 * @version $Id: $Id
 */
public abstract class OneToOneAction<T extends Named, R extends Named> extends GenericAction<T, R> {

    public OneToOneAction(Context context, String name, Meta annotation) {
        super(context, name, annotation);
    }

    public OneToOneAction(Context context, Meta annotation) {
        super(context, annotation);
    }

    @Override
    protected List<Dependency<R>> execute(Logable packLog, Meta packAnnotation, DependencySet<T> input) {
        List<Dependency<R>> res;
        if (isParallelExecutionAllowed()) {
            res = StreamSupport.stream(input.spliterator(), true)
                    .<Dependency<R>>map((d) -> runOne(packLog, packAnnotation, d))
                    .collect(Collectors.toList());
        } else {
            res = StreamSupport.stream(input.spliterator(), false)
                    .<Dependency<R>>map((d) -> runOne(packLog, packAnnotation, d))
                    .collect(Collectors.toList());
        }
        return res;
    }

    public R runOne(T input){
        return runOne(getContext(), null, new SimpleDataDependency<>(input)).get();
    }
    
    
    /**
     * Run action on single dependency
     *
     * @param packLog
     * @param packAnnotation a {@link hep.dataforge.meta.Meta}
     * object.
     * @param input a T object.
     * @return a R object.
     */
    public Dependency<R> runOne(Logable packLog, Meta packAnnotation, Dependency<T> input) {
        beforeSingle(packLog, input);
        //building annotation for a single run or using existing content meta
        Meta singleMeta = null;
        if (input.keys().contains(ACTION_DEPENDENCY_META_KEY)) {
            singleMeta = input.<Meta>get(ACTION_DEPENDENCY_META_KEY);
        } else if (input.type().isAssignableFrom(Annotated.class)) {
            singleMeta = ((Annotated) input.get()).meta();
        } else {
            singleMeta = input.meta();
        }

        //building log for input if it does not exist
        Logable singleLog;
        if (input.keys().contains(ACTION_DEPENDENCY_LOG_KEY)) {
            singleLog = input.<Logable>get(ACTION_DEPENDENCY_LOG_KEY);
        } else {
            singleLog = new Log(input.getName(), packLog);
        }

        R res = execute(new Log(this.getName(), singleLog), readMeta(singleMeta, packAnnotation), input.get());

        Meta resultMeta = singleMeta;
        if (res instanceof Annotated) {
            if (singleMeta != null) {
                resultMeta = MergeRule.getDefault().merge(((Annotated) res).meta(), singleMeta);
            } else {
                resultMeta = ((Annotated) res).meta();
            }
        }

        Dependency<R> resDep = wrap(singleLog, resultMeta, res);
        afterSingle(packLog, resDep);
//        res.annotate(MergeRule.getDefault().merge(res.meta(), input.meta()));
        return resDep;
    }

    /**
     * Calculate the action result for single input content TODO replace input
     * by Dependency
     *
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @param reader a {@link hep.dataforge.description.MetaReader}
     * object.
     * @param input a T object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     * @return a R object.
     */
    protected abstract R execute(Logable log, Meta meta, T input);

    /**
     * Выполняется один раз перед основным действием независимо от того,
     * распаралелено действие или нет
     *
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @param input a {@link hep.dataforge.content.Content} object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     */
    protected void beforeSingle(Logable log, Dependency<T> input) throws ContentException {
        log.log("Starting action '{}' on content with name '{}'", getName(), input.getName());
    }

    /**
     * Выполняется один раз после основного действия независимо от того,
     * распаралелено действие или нет
     *
     * @param log a {@link hep.dataforge.io.log.Logable} object.
     * @param output a {@link hep.dataforge.content.Content} object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     */
    protected void afterSingle(Logable log, Dependency<R> output) throws ContentException {
        log.log("Action '{}' is finished with result named '{}'", getName(), output.getName());
    }

}
