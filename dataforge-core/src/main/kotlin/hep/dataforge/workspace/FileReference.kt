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
import hep.dataforge.workspace.FileReference.Companion.FileReferenceType.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest


/**
 * A reference to file with content not managed by DataForge
 */
class FileReference(private val _context: Context, val path: String, val type: FileReferenceType = WORK) : ContextAware {

    override fun getContext(): Context = _context

    /**
     * Absolute path for this reference
     */
    val absolutePath: Path = when (type) {
        SYS -> Paths.get(path)
        DATA -> context.io.dataDirectory.resolve(path)
        WORK -> context.io.workDirectory.resolve(path)
        TMP -> context.io.tmpDirectory.resolve(path)
    }.toAbsolutePath()

    /**
     * Get binary references by this file reference
     */
    val binary: Binary
        get() = FileBinary(absolutePath)

    /**
     * A flag showing that internal modification of reference content is allowed
     */
    val mutable: Boolean = type == WORK || type == TMP

    /**
     * Checksum of the file
     */
    val md5 = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(absolutePath))

    companion object {
        enum class FileReferenceType {
            SYS, // absolute system path, content immutable
            DATA, // a reference in data directory, content immutable
            WORK, // a reference in work directory, mutable
            TMP // A temporary file reference, mutable
        }



    }
}