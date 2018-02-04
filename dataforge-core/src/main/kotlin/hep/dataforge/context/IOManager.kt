/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hep.dataforge.context

import hep.dataforge.data.binary.Binary
import hep.dataforge.data.binary.StreamBinary
import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.io.IOUtils
import hep.dataforge.io.display.Output
import hep.dataforge.kodex.buildMeta
import hep.dataforge.kodex.optional
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.providers.Provides
import hep.dataforge.workspace.FileReference
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 *
 *
 * IOManager interface.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
abstract class IOManager(meta: Meta) : BasicPlugin(meta) {


    /**
     * Get primary output for this context
     * @return
     */
    abstract val output: Output

    /**
     * Get secondary output for this context
     * @param stage
     * @param name
     * @return
     */
    @ValueDefs(
            ValueDef(name = "stage", def = "", info = "Fully qualified name of the output stage"),
            ValueDef(name = "name", required = true, info = "Fully qualified name of the output inside the stage if it is present"),
            ValueDef(name = "type", def = DEFAULT_OUTPUT_TYPE, info = "Type of the output container")
    )
    abstract fun output(meta: Meta): Output


    /**
     * Helper method to access output
     */
    fun output(name: Name, stage: Name = Name.empty()): Output {
        val meta = buildMeta("output") {
            "stage" to stage.toUnescaped()
            "name" to name.toUnescaped()
            "type" to DEFAULT_OUTPUT_TYPE
        }
        return output(meta)
    }

    /**
     * Return the root directory for this IOManager. By convention, Context
     * should not have access outside root directory to prevent System damage.
     *
     * @return a [java.io.File] object.
     */
    val rootDir: Path
        get() {
            val rootDir = meta.getString(
                    ROOT_DIRECTORY_CONTEXT_KEY,
                    context.getString(ROOT_DIRECTORY_CONTEXT_KEY, System.getProperty("user.home"))
            )
            val root = Paths.get(rootDir)
            try {
                Files.createDirectories(root)
            } catch (e: IOException) {
                throw RuntimeException(context.name + ": Failed to create root directory " + root, e)
            }

            return root
        }

    /**
     * The working directory for output and temporary files. Is always inside root directory
     *
     * @return
     */
    val workDir: Path
        get() {
            val workDirName = context.getString(WORK_DIRECTORY_CONTEXT_KEY, ".dataforge")
            val work = rootDir.resolve(workDirName)
            try {
                Files.createDirectories(work)
            } catch (e: IOException) {
                throw RuntimeException(context.name + ": Failed to create work directory " + work, e)
            }

            return work
        }

    /**
     * Get the default directory for file data. By default uses context root directory
     * @return
     */
    val dataDir: Path
        get() = if (context.hasValue(DATA_DIRECTORY_CONTEXT_KEY)) {
            IOUtils.resolvePath(context.getString(DATA_DIRECTORY_CONTEXT_KEY))
        } else {
            rootDir
        }

    /**
     * The directory for temporary files. This directory could be cleaned up any
     * moment. Is always inside root directory.
     *
     * @return
     */
    val tmpDir: Path
        get() {
            val tmpDir = context.getString(TEMP_DIRECTORY_CONTEXT_KEY, ".dataforge/.temp")
            val tmp = rootDir.resolve(tmpDir)
            try {
                Files.createDirectories(tmp)
            } catch (e: IOException) {
                throw RuntimeException(context.name + ": Failed to create tmp directory " + tmp, e)
            }

            return tmp
        }


    fun getDataFile(path: String): FileReference {
        return FileReference.openDataFile(context, path)
    }

    /**
     * Get a file where `path` is relative to root directory or absolute.
     * @param path a [java.lang.String] object.
     * @return a [java.io.File] object.
     */
    fun getFile(path: String): FileReference {
        return FileReference.Companion.openFile(context, path)
    }

//    /**
//     * Provide file or resource as a binary. The path must be provided in DataForge notation (using ".").
//     * It is automatically converted to system path.
//     *
//     *
//     * Absolute paths could not be provided that way.
//     *
//     * @param name
//     * @return
//     */
//    @Provides(BINARY_TARGET)
//    fun optBinary(name: String): Optional<Binary> {
//
//        if (resource != null) {
//            return Optional.of(StreamBinary {
//                try {
//                    return@Optional.of resource !!. openStream ()
//                } catch (e: IOException) {
//                    throw RuntimeException("Failed to open resource stream", e)
//                }
//            })
//        } else {
//            return@Optional.of optFile name.map<Binary>({ FileBinary(it) }
//        }
//    }

    /**
     * Get the context based classpath resource
     */
    @Provides(RESOURCE_TARGET)
    fun optResource(name: String): Optional<Binary> {
        val path = name.replace(".", File.pathSeparator)
        val resource = context.classLoader.getResource(path)
        return resource?.let { StreamBinary { it.openStream() } }.optional
    }

    companion object {
        //const val BINARY_TARGET = "bin"
        const val RESOURCE_TARGET = "resource"
        //const val FILE_TARGET = "file"
        //    String RESOURCE_TARGET = "resource";

        const val LOGGER_APPENDER_NAME = "df.io"

        const val ROOT_DIRECTORY_CONTEXT_KEY = "rootDir"
        const val WORK_DIRECTORY_CONTEXT_KEY = "workDir"
        const val DATA_DIRECTORY_CONTEXT_KEY = "dataDir"
        const val TEMP_DIRECTORY_CONTEXT_KEY = "tempDir"

        const val DEFAULT_OUTPUT_TYPE = "dataforge/output"
    }

}
