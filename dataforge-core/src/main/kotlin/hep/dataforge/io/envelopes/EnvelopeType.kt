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
package hep.dataforge.io.envelopes

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.io.IOUtils
import hep.dataforge.nullable
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.READ

/**
 * Envelope io format description
 *
 * @author Alexander Nozik
 */
interface EnvelopeType {

    val code: Int

    val name: String

    val reader: EnvelopeReader
        get() = getReader(emptyMap())

    val writer: EnvelopeWriter
        get() = getWriter(emptyMap())

    fun description(): String

    /**
     * Get reader with properties override
     *
     * @param properties
     * @return
     */
    fun getReader(properties: Map<String, String>): EnvelopeReader

    /**
     * Get writer with properties override
     *
     * @param properties
     * @return
     */
    fun getWriter(properties: Map<String, String>): EnvelopeWriter

    companion object {


        /**
         * Infer envelope type from file reading only first line (ignoring empty and sha-bang)
         *
         * @param path
         * @return
         */
        fun infer(path: Path): EnvelopeType? {
            return try {
                Files.newInputStream(path, READ).use { stream ->
                    IOUtils.nextLine(stream, "ASCII") { line ->
                        //skip shabang
                        line.isEmpty() || line.startsWith("#!") && !line.endsWith("#!")
                    }.nullable?.let {  header ->
                        when {
                        //TODO use templates from appropriate types
                            header.startsWith("#!") -> error("Legacy dataforge tags are not supported")
                            header.startsWith("#~DFTL") -> TaglessEnvelopeType.INSTANCE
                            header.startsWith("#~") -> DefaultEnvelopeType.INSTANCE
                            else -> null
                        }
                    }
                }
            } catch (ex: Exception) {
                LoggerFactory.getLogger(EnvelopeType::class.java).warn("Could not infer envelope type of file {} due to exception: {}", path, ex)
                null
            }

        }

        fun resolve(code: Int, context: Context = Global): EnvelopeType? {
            synchronized(context) {
                return context.findService(EnvelopeType::class.java) { it -> it.code == code }
            }
        }

        fun resolve(name: String, context: Context = Global): EnvelopeType? {
            synchronized(context) {
                return context.findService(EnvelopeType::class.java){ it -> it.name == name }
            }
        }
    }
}
