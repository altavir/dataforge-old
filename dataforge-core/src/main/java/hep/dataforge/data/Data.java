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

import hep.dataforge.data.binary.Binary;
import hep.dataforge.goals.AbstractGoal;
import hep.dataforge.goals.GeneratorGoal;
import hep.dataforge.goals.Goal;
import hep.dataforge.goals.StaticGoal;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.Metoid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A piece of data which is basically calculated asynchronously
 *
 * @param <T>
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class Data<T> implements Metoid {

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Data<T> buildStatic(T content, Meta meta) {
        return new Data<T>(new StaticGoal<T>(content), (Class<T>) content.getClass(), meta);
    }

    @NotNull
    @Contract("null -> fail")
    public static <T> Data<T> buildStatic(T content) {
        if (content == null) {
            throw new RuntimeException("Can't create a data from null");
        }

        Meta meta = Meta.empty();
        if (content instanceof Metoid) {
            meta = ((Metoid) content).meta();
        }
        return buildStatic(content, meta);
    }

    @NotNull
    public static <T> Data<T> empty(Class<T> type, Meta meta) {
        Goal<T> emptyGoal = new StaticGoal<>(null);
        return new Data<>(emptyGoal, type, meta);
    }

    /**
     * Build data from envelope using given lazy binary transformation
     *
     * @param envelope
     * @param type
     * @param transform
     * @param <T>
     * @return
     */
    public static <T> Data<T> fromEnvelope(Envelope envelope, Class<T> type, Function<Binary, T> transform) {
        Goal<T> goal = new AbstractGoal<T>() {
            @Override
            protected T compute() throws Exception {
                return transform.apply(envelope.getData());
            }

            @Override
            public Stream<Goal<?>> dependencies() {
                return Stream.empty();
            }
        };
        return new Data<>(goal, type, envelope.meta());
    }

    public static <T> Data<T> generate(Class<T> type, Meta meta, Executor executor, Supplier<T> sup) {
        return new Data<T>(new GeneratorGoal<>(executor, sup), type, meta);
    }

    public static <T> Data<T> generate(Class<T> type, Meta meta, Supplier<T> sup) {
        return new Data<T>(new GeneratorGoal<>(sup), type, meta);
    }


    private final Goal<T> goal;
    private final Meta meta;
    private final Class<T> type;

    public Data(Goal<T> goal, Class<T> type, Meta meta) {
        this.goal = goal;
        this.meta = meta;
        this.type = type;
    }

    public Data(Goal<T> goal, Class<T> type) {
        this.goal = goal;
        this.meta = Meta.empty();
        this.type = type;
    }

    public Goal<T> getGoal() {
        return goal;
    }

    /**
     * Compute underlying goal and return sync result.
     *
     * @return
     */
    public T get() {
        goal.run();
        return goal.result().join();
    }

    /**
     * Asynchronous data handler. Computation could be canceled if needed
     *
     * @return
     */
    public CompletableFuture<T> getInFuture() {
        return goal.result();
    }

    /**
     * Data type. Should be defined before data is calculated.
     *
     * @return
     */
    public Class<T> type() {
        return type;
    }

    /**
     * @return false if goal is canceled or completed exceptionally
     */
    public boolean isValid() {
        return !getInFuture().isCancelled() && !getInFuture().isCompletedExceptionally();
    }

    @Override
    public Meta meta() {
        return meta;
    }

    /**
     * Upcast the data tupe
     *
     * @param type
     * @param <R>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <R> Data<R> cast(Class<R> type) {
        if (type.isAssignableFrom(this.type)) {
            return (Data<R>) this;
        } else {
            throw new IllegalArgumentException("Invalid type to upcast data");
        }
    }

}
