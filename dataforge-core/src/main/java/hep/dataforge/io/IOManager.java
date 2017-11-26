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
package hep.dataforge.io;

import hep.dataforge.context.Plugin;
import hep.dataforge.data.binary.Binary;
import hep.dataforge.data.binary.FileBinary;
import hep.dataforge.data.binary.StreamBinary;
import hep.dataforge.io.history.Record;
import hep.dataforge.io.markup.MarkupRenderer;
import hep.dataforge.io.markup.SimpleMarkupRenderer;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import hep.dataforge.providers.Provides;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * <p>
 * IOManager interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface IOManager extends Plugin {
    String BINARY_TARGET = "bin";
//    String RESOURCE_TARGET = "resource";

    String LOGGER_APPENDER_NAME = "df.io";

    String ROOT_DIRECTORY_CONTEXT_KEY = "rootDir";
    String WORK_DIRECTORY_CONTEXT_KEY = "workDir";
    String TEMP_DIRECTORY_CONTEXT_KEY = "tempDir";

    String DEFAULT_OUTPUT_TYPE = "dataforge/output";

    /**
     * Output stream for specific stage and specific name. All parameters could
     * be null. In this case default values are used.
     *
     * @param stage
     * @param name
     * @return
     */
    OutputStream out(Name stage, Name name, String type);

    /**
     * Custom output builder using given configuration
     *
     * @param outConfig
     * @return
     */
    default OutputStream out(Meta outConfig) {
        return out(
                Name.of(outConfig.getString("stage", "")),
                Name.of(outConfig.getString("name", "")),
                DEFAULT_OUTPUT_TYPE
        );
    }

    default OutputStream out(String stage, String name, String type) {
        return out(Name.of(stage), Name.of(name), type);
    }

    default OutputStream out(String stage, String name) {
        return out(stage, name, DEFAULT_OUTPUT_TYPE);
    }

    default MarkupRenderer getMarkupRenderer() {
        return new SimpleMarkupRenderer(out());
    }

    /**
     * Open a markup renderer for this IOManager
     *
     * @param stage
     * @param name
     * @return
     */
    default MarkupRenderer getMarkupRenderer(@Nullable String stage, @Nullable String name) {
        return new SimpleMarkupRenderer(out(stage, name));
    }

    /**
     * The default outputStream for this IOManager. Should not be used for any
     * sensitive data or results
     *
     * @return
     */
    OutputStream out();

    /**
     * User input Stream
     *
     * @return a {@link java.io.InputStream} object.
     */
    InputStream in();

    /**
     * Inputstream built by custom path
     *
     * @param path
     * @return
     */
    InputStream in(String path);

    /**
     * Get a file where {@code path} is relative to root directory or absolute.
     *
     * @param path a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    default Path getFile(String path) {
        return optFile(path).orElseThrow(() -> new RuntimeException("File " + path + " not found in the context"));
    }

    /**
     * Provide a file
     *
     * @param path
     * @return
     */
    Optional<Path> optFile(String path);

    /**
     * Provide file or resource as a binary. The path must be provided in DataForge notation (using ".").
     * It is automatically converted to system path.
     * <p>
     * Absolute paths could not be provided that way.
     *
     * @param name
     * @return
     */
    @Provides(BINARY_TARGET)
    default Optional<Binary> optBinary(String name) {
        String path = name.replace(".", "/");
        URL resource = getContext().getClassLoader().getResource(path);
        if (resource != null) {
            return Optional.of(new StreamBinary(() -> {
                try {
                    return resource.openStream();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to open resource stream", e);
                }
            }));
        } else {
            return optFile(name).map(FileBinary::new);
        }
    }

    default Optional<Binary> optResource(String path) {
        return Optional.ofNullable(getContext().getClassLoader().getResource(path))
                .map(resource -> new StreamBinary(() -> {
                            try {
                                return resource.openStream();
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to open resource stream", e);
                            }
                        })
                );
    }

    /**
     * Return the root directory for this IOManager. By convention, Context
     * should not have access outside root directory to prevent System damage.
     *
     * @return a {@link java.io.File} object.
     */
    Path getRootDirectory();

    /**
     * The working directory for output and temporary files. Is always inside root directory
     *
     * @return
     */
    default Path getWorkDirectory() {
        String workDirName = getContext().getString(WORK_DIRECTORY_CONTEXT_KEY, ".dataforge");
        Path work = getRootDirectory().resolve(workDirName);
        try {
            Files.createDirectories(work);
        } catch (IOException e) {
            throw new RuntimeException(getContext().getName() + ": Failed to create work directory " + work, e);
        }
        return work;
    }

    /**
     * The directory for temporary files. This directory could be cleaned up any
     * moment. Is always inside root directory.
     *
     * @return
     */
    default Path getTmpDirectory() {
        String tmpDir = getContext().getString(TEMP_DIRECTORY_CONTEXT_KEY, ".dataforge/.temp");
        Path tmp = getRootDirectory().resolve(tmpDir);
        try {
            Files.createDirectories(tmp);
        } catch (IOException e) {
            throw new RuntimeException(getContext().getName() + ": Failed to create tmp directory " + tmp, e);
        }
        return tmp;
    }

    default Consumer<Record> getLogEntryHandler() {
        return (Record t) -> {
            try {
                out().write((t.toString() + "\n").getBytes());
                out().flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

}
