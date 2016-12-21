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
import hep.dataforge.meta.Meta;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Created by darksnake on 06-Sep-16.
 */
public class DataUtils {
    /**
     * Combine two data elements of different type into single data
     */
    public static <R, S1, S2> Data<R> combine(Class<R> type, Data<? extends S1> data1, Data<? extends S2> data2,
                                              Meta meta,
                                              BiFunction<S1, S2, R> transform) {
        Goal<R> combineGoal = new AbstractGoal<R>() {
            @Override
            protected R compute() throws Exception {
                return transform.apply(data1.get(), data2.get());
            }

            @Override
            public Stream<Goal> dependencies() {
                return Stream.of(data1.getGoal(), data2.getGoal());
            }
        };
        return new Data<R>(combineGoal, type, meta);
    }
}
