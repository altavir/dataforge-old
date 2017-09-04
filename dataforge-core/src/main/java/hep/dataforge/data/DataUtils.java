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

import hep.dataforge.goals.AbstractGoal;
import hep.dataforge.goals.Goal;
import hep.dataforge.goals.PipeGoal;
import hep.dataforge.meta.Meta;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by darksnake on 06-Sep-16.
 */
public class DataUtils {
    /**
     * Combine two data elements of different type into single data
     */
    public static <R, S1, S2> Data<R> combine(Data<? extends S1> data1, Data<? extends S2> data2,
                                              Class<R> type,
                                              Meta meta,
                                              BiFunction<S1, S2, R> transform) {
        Goal<R> combineGoal = new AbstractGoal<R>() {
            @Override
            protected R compute() throws Exception {
                return transform.apply(data1.get(), data2.get());
            }

            @Override
            public Stream<Goal<?>> dependencies() {
                return Stream.of(data1.getGoal(), data2.getGoal());
            }
        };
        return new Data<R>(combineGoal, type, meta);
    }


    /**
     * Join a uniform list of elements into a single datum
     */
    public static <R, S> Data<R> join(Collection<Data<? extends S>> data,
                                      Class<R> type,
                                      Meta meta,
                                      Function<List<S>, R> transform) {
        Goal<R> combineGoal = new AbstractGoal<R>() {
            @Override
            protected R compute() throws Exception {
                return transform.apply(data.stream().map(Data::get).collect(Collectors.toList()));
            }

            @Override
            public Stream<Goal<?>> dependencies() {
                return data.stream().map(Data::getGoal);
            }
        };
        return new Data<R>(combineGoal, type, meta);
    }

    public static <R, S> Data<R> join(DataNode<S> dataNode,
                                      Class<R> type,
                                      Function<List<S>, R> transform) {
        Goal<R> combineGoal = new AbstractGoal<R>() {
            @Override
            protected R compute() throws Exception {
                return transform.apply(dataNode.dataStream()
                        .filter(Data::isValid)
                        .map(Data::get)
                        .collect(Collectors.toList())
                );
            }

            @Override
            public Stream<Goal<?>> dependencies() {
                return dataNode.dataStream().map(Data::getGoal);
            }
        };
        return new Data<R>(combineGoal, type, dataNode.meta());
    }

    /**
     * Apply lazy transformation of the data using default executor. The meta of the result is the same as meta of input
     *
     * @param target
     * @param transformation
     * @param <R>
     * @return
     */
    public static <T, R> Data<R> transform(Data<T> data, Class<R> target, Function<T, R> transformation) {
        Goal<R> goal = new PipeGoal<T, R>(data.getGoal(), transformation);
        return new Data<R>(goal, target, data.meta());
    }

    public static <T, R> NamedData<R> transform(NamedData<T> data, Class<R> target, Function<T, R> transformation) {
        Goal<R> goal = new PipeGoal<T, R>(data.getGoal(), transformation);
        return new NamedData<R>(data.getName(), goal, target, data.meta());
    }

    public static <T, R> Data<R> transform(Data<T> data, Class<R> target, Executor executor, Function<T, R> transformation) {
        Goal<R> goal = new PipeGoal<T, R>(data.getGoal(), executor, transformation);
        return new Data<R>(goal, target, data.meta());
    }

    /**
     * A node containing single data fragment
     *
     * @param nodeName
     * @param data
     * @param <T>
     * @return
     */
    public static <T> DataNode<T> singletonNode(String nodeName, Data<T> data) {
        return DataSet.builder(data.type()).putData(DataNode.DEFAULT_DATA_FRAGMENT_NAME, data).build();
    }

    public static <T> DataNode<T> singletonNode(String nodeName, T object) {
        return singletonNode(nodeName, Data.buildStatic(object));
    }
}
