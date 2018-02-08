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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta

import hep.dataforge.description.Described
import hep.dataforge.description.NodeDescriptor
import hep.dataforge.utils.Optionals
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import java.util.*

/**
 * The base class for `Meta` objects with immutable meta which also
 * implements ValueProvider and Described interfaces
 *
 * @author Alexander Nozik
 */
open class MetaHolder(private val meta: Meta) : Metoid, Described, ValueProvider {

    @Transient
    private var descriptor: NodeDescriptor = super.getDescriptor()

    /**
     * Return meta of this object. If it is null, than return default meta from
     * `getDefaultMeta()` method
     *
     * @return
     */
    override fun getMeta(): Meta = meta

    /**
     * Get descriptor for contetn of this metaholder
     *
     * @return
     */
    override fun getDescriptor(): NodeDescriptor {
        return descriptor
    }

    /**
     * Reserved method to set override descriptor later
     *
     * @param descriptor
     */
    protected fun setDescriptor(descriptor: NodeDescriptor) {
        this.descriptor = descriptor
    }

    /**
     * If this object's meta provides given value, return it, otherwise, use
     * descriptor
     *
     * @param name
     * @return
     */
    override fun optValue(name: String): Optional<Value> {
        return Optionals
                .either(getMeta().optValue(name))
                .or { getDescriptor().optValueDescriptor(name).map { it.defaultValue() } }
                .opt()
    }

    /**
     * true if this object's meta or description contains the value
     *
     * @param name
     * @return
     */
    override fun hasValue(name: String): Boolean {
        return getMeta().hasValue(name) || getDescriptor().hasDefaultForValue(name)
    }

}