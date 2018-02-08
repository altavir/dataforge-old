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

package dataforge.names

import hep.dataforge.names.Name
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class NameTest {
    @Test
    fun testNameFromString() {
        val name = Name.of("first.second[28].third\\.andahalf")
        assertEquals(3, name.length.toLong())
        Assert.assertEquals("third.andahalf", name.last.toUnescaped())
    }

    @Test
    fun testReconstruction() {
        val name = Name.join(Name.of("first.second"), Name.ofSingle("name.with.dot"), Name.ofSingle("end[22]"))
        val str = name.toString()
        val reconstructed = Name.of(str)
        assertEquals(name, reconstructed)
        assertEquals("name.with.dot", reconstructed.tokens[2].toUnescaped())
    }

    @Test
    fun testJoin() {
        val name = Name.join("first", "second", "", "another")
        assertEquals(3, name.length.toLong())
    }

}