/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes

import hep.dataforge.context.Global
import hep.dataforge.io.MetaStreamReader
import hep.dataforge.io.MetaStreamWriter
import hep.dataforge.io.envelopes.Envelope.Companion.META_TYPE_KEY
import java.util.*
import java.util.function.Predicate
import java.util.stream.StreamSupport

/**
 *
 * @author Alexander Nozik
 */
interface MetaType {


    val codes: List<Short>

    val name: String

    val reader: MetaStreamReader

    val writer: MetaStreamWriter

    /**
     * A file name filter for meta encoded in this format
     * @return
     */
    fun fileNameFilter(): Predicate<String>

    companion object {

        val loader: ServiceLoader<MetaType> = ServiceLoader.load(MetaType::class.java)

        /**
         * Resolve a meta type code and return null if code could not be resolved
         * @param code
         * @return
         */
        fun resolve(code: Short): MetaType? {
            //TODO add caching here?
            synchronized(Global) {
                return StreamSupport.stream(loader.spliterator(), false)
                        .filter { it -> it.codes.contains(code) }.findFirst().orElse(null)
            }
        }

        /**
         * Resolve a meta type and return null if it could not be resolved
         * @param name
         * @return
         */
        fun resolve(name: String): MetaType? {
            synchronized(Global) {
                return StreamSupport.stream(loader.spliterator(), false)
                        .filter { it -> it.name.equals(name, ignoreCase = true) }.findFirst().orElse(null)
            }
        }

        fun resolve(properties: Map<String, String>): MetaType {
            return properties[META_TYPE_KEY]?.let { MetaType.resolve(it) } ?: XMLMetaType
        }
    }
}
