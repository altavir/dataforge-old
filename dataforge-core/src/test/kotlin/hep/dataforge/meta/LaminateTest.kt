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

package hep.dataforge.meta

import org.junit.Assert.assertEquals
import org.junit.Test

class LaminateTest {

    internal var meta: Meta = MetaBuilder("test")
            .putNode(MetaBuilder("child").putValue("a", 22))

    internal var laminate = Laminate(meta, meta)

    @Test
    fun testToString() {
        println(laminate.toString())
        assertEquals(3, laminate.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size.toLong())
    }

}