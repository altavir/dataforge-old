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

package hep.dataforge.workspace

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.data.binary.Binary
import hep.dataforge.data.binary.FileBinary
import hep.dataforge.names.Name
import hep.dataforge.workspace.FileReference.FileReferenceScope.*
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption


/**
 * A reference to file with content not managed by DataForge
 */
class FileReference private constructor(private val _context: Context, val path: Path, val scope: FileReferenceScope = WORK) : ContextAware {

    override fun getContext(): Context = _context

    /**
     * Absolute path for this reference
     */
    val absolutePath: Path = when (scope) {
        SYS -> path
        DATA -> context.io.dataDir.resolve(path)
        WORK -> context.io.workDir.resolve(path)
        TMP -> context.io.tmpDir.resolve(path)
    }.toAbsolutePath()

    /**
     * Get binary references by this file reference
     */
    val binary: Binary?
        get() {
            return if(exists) {
                FileBinary(absolutePath)
            } else{
                null
            }
        }

    /**
     * A flag showing that internal modification of reference content is allowed
     */
    val mutable: Boolean = scope == WORK || scope == TMP

    val exists: Boolean = Files.exists(absolutePath)


    private fun prepareWrite() {
        if (!mutable) {
            throw RuntimeException("Trying to write to immutable file reference")
        }
        absolutePath.parent.apply {
            if (!Files.exists(this)) {
                Files.createDirectories(this)
            }
        }
    }

    /**
     * Write and replace content of the file
     */
    fun write(content: ByteArray) {
        prepareWrite()
        Files.write(absolutePath, content, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
    }

    fun append(content: ByteArray) {
        prepareWrite()
        Files.write(absolutePath, content, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }


    /**
     * Output stream for this file reference
     */
    val output: OutputStream
        get() {
            prepareWrite()
            return Files.newOutputStream(absolutePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        }


//    /**
//     * Checksum of the file
//     */
//    val md5 = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(absolutePath))


    enum class FileReferenceScope {
        SYS, // absolute system path, content immutable
        DATA, // a reference in data directory, content immutable
        WORK, // a reference in work directory, mutable
        TMP // A temporary file reference, mutable
    }

    companion object {

        /**
         * Provide a reference to a new file in tmp directory with unique ID.
         */
        fun newTmpFile(context: Context, prefix: String, suffix: String): FileReference {
            val path = Files.createTempFile(context.io.tmpDir, prefix, suffix)
            return FileReference(context, path, TMP)
        }

        /**
         * Create a reference for a file in a work directory. Filr itself is not created
         */
        fun newWorkFile(context: Context, fileName: String, extension: String, path: Name = Name.EMPTY): FileReference {
            val dir = if (path.isEmpty) {
                context.io.workDir
            } else {
                val relativeDir = path.tokens.joinToString(File.pathSeparator) { it.toString() }
                context.io.workDir.resolve(relativeDir)
            }

            val file = dir.resolve("$fileName.$extension")
            return FileReference(context, file, WORK)
        }

        /**
         * Create a reference using data scope file using path
         */
        fun openDataFile(context: Context, path: Path): FileReference {
            return FileReference(context, path, DATA)
        }

        fun openDataFile(context: Context, name: String): FileReference {
            val path = context.io.dataDir.resolve(name)
            return FileReference(context, path, DATA)
        }

        /**
         * Create a reference to the system scope file using path
         */
        fun openFile(context: Context, path: Path): FileReference {
            return FileReference(context, path, SYS)
        }

        /**
         * Create a reference to the system scope file using string
         */
        fun openFile(context: Context, path: String): FileReference {
            return FileReference(context, Paths.get(path), SYS)
        }

    }
}