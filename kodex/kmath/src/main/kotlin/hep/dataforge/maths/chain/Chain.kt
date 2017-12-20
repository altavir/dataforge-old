/*
 * Copyright  2017 Alexander Nozik.
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

package hep.dataforge.maths.chain

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.channels.sendBlocking

/**
 * A not-necessary-Markov chain of some type
 * @param S - the state of the chain
 * @param R - the chain element type
 */
interface Chain<out R> {
    /**
     * Last value of the chain
     */
    val value: R

    /**
     * Generate next value, changin state if needed
     */
    suspend fun next(): R

    /**
     * Transform chain to a lazy sequence
     */
    fun asChannel(): ReceiveChannel<R>{
        return produce {
            while(true) {
                sendBlocking(next())
            }
        }
    }
}

/**
 * A stateless Markov chain
 */
class MarkovChain<R : Any>(private val seed: () -> R, private val gen: suspend (R) -> R) : Chain<R> {

    constructor(seed: R, gen: suspend (R) -> R) : this({ seed }, gen)

    private var currentValue: R? = null

    override val value: R
        get() = currentValue ?: seed()

    suspend override fun next(): R {
        synchronized(this) {
            currentValue = gen(value)
            return value
        }
    }

}

/**
 * A chain with possibly mutable state. The state must not be changed outside the chain. Two chins should never share yhe state
 */
class StatefulChain<S, R : Any>(val state: S, private val seed: S.() -> R, private val gen: suspend S.(R) -> R) : Chain<R> {
    constructor(state: S, seed: R, gen: suspend S.(R) -> R) : this(state, { seed }, gen)

    private var currentValue: R? = null

    override val value: R
        get() = currentValue ?: state.seed()

    suspend override fun next(): R {
        synchronized(this) {
            currentValue = gen(state, value)
            return value
        }
    }
}