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
import hep.dataforge.goals.Goal;
import hep.dataforge.goals.PipeGoal;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeReader;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import hep.dataforge.workspace.FileReference;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static final String META_DIRECTORY = "@meta";

    /**
     * Combine two data elements of different type into single data
     */
    public static <R, S1, S2> Data<R> combine(Data<S1> data1, Data<S2> data2,
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
        return new Data<R>(combineGoal, type, dataNode.getMeta());
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
        return new Data<R>(goal, target, data.getMeta());
    }

    public static <T, R> NamedData<R> transform(NamedData<T> data, Class<R> target, Function<T, R> transformation) {
        Goal<R> goal = new PipeGoal<T, R>(data.getGoal(), transformation);
        return new NamedData<R>(data.getName(), goal, target, data.getMeta());
    }

    public static <T, R> Data<R> transform(Data<T> data, Class<R> target, Executor executor, Function<T, R> transformation) {
        Goal<R> goal = new PipeGoal<T, R>(data.getGoal(), executor, transformation);
        return new Data<R>(goal, target, data.getMeta());
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

    /**
     * Read an object from a file using given transformation. Capture a file meta from default directory. Override meta is placed above file meta.
     *
     * @param file
     * @param override
     * @param type
     * @param reader
     * @param <T>
     * @return
     */
    @NotNull
    public static <T> Data<T> readFile(FileReference file, Meta override, Class<T> type, Function<Binary, T> reader) {
        Path filePath = file.getAbsolutePath();
        if (!Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException(filePath.toString() + " is not existing file");
        }
        Binary binary = file.getBinary();
        Path metaFileDirectory = filePath.resolveSibling(META_DIRECTORY);
        Meta fileMeta = MetaFileReader.resolve(metaFileDirectory, filePath.getFileName().toString()).orElse(Meta.empty());
        Laminate meta = new Laminate(fileMeta, override);
        return Data.generate(type, meta, () -> reader.apply(binary));
    }

    /**
     * Read file as Binary Data.
     *
     * @param file
     * @param override
     * @return
     */
    @NotNull
    public static Data<Binary> readFile(FileReference file, Meta override) {
        return readFile(file, override, Binary.class, it -> it);
    }

//    public static <T> DataNode<T> readDirectory(Path directoryPath, Meta override, Class<T> type, Function<Binary, T> reader) {
//        if (!Files.isDirectory(directoryPath)) {
//            throw new IllegalArgumentException(directoryPath.toString() + " is not existing directory");
//        }
//        Meta nodeMeta = MetaFileReader.resolve(directoryPath, META_DIRECTORY).orElse(Meta.empty());
//        DataTree.Builder<T> builder = DataTree.builder(type).setMeta(nodeMeta).setName(directoryPath.getFileName().toString());
//        try {
//            Files.list(directoryPath).filter(it -> !it.getFileName().toString().startsWith("@")).forEach(file -> {
//                if (Files.isRegularFile(file)) {
//                    builder.putData(file.getFileName().toString(), readFile(file, Meta.empty(), type, reader));
//                } else {
//                    builder.putNode(readDirectory(file, Meta.empty(), type, reader));
//                }
//            });
//        } catch (IOException e) {
//            throw new RuntimeException("Can't list files in " + directoryPath.toString());
//        }
//        return builder.build();
//    }

    /**
     * Transform envelope file into data using given transformation. The meta of the data consists of 3 layers:
     * <ol>
     * <li>override - dynamic meta from method argument)</li>
     * <li>captured - captured from @meta directory</li>
     * <li>own - envelope owm meta</li>
     * </ol>
     *
     * @param filePath
     * @param override
     * @param type
     * @param reader   a bifunction taking the binary itself and combined meta as arguments and returning
     * @param <T>
     * @return
     */
    public static <T> Data<T> readEnvelope(Path filePath, Meta override, Class<T> type, BiFunction<Binary, Meta, T> reader) {
        try {
            Envelope envelope = EnvelopeReader.readFile(filePath);
            Binary binary = envelope.getData();
            Path metaFileDirectory = filePath.resolveSibling(META_DIRECTORY);
            Meta fileMeta = MetaFileReader.resolve(metaFileDirectory, filePath.getFileName().toString()).orElse(Meta.empty());
            Laminate meta = new Laminate(fileMeta, override, envelope.getMeta());
            return Data.generate(type, meta, () -> reader.apply(binary, meta));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + filePath.toString() + " as an envelope", e);
        }
    }
}
