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

package hep.dataforge.storage

import hep.dataforge.tables.MetaTableFormat
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class TableLoaderTest {
    @Test
    fun testReadWrite() {
        val path = Files.createTempDirectory("dataforge-test").resolve("table.df")
        val format = MetaTableFormat.forNames("a", "b", "c")
        val loader = runBlocking { TableLoaderType.create(null, path, format) }
        val writer = loader.mutable()
        runBlocking {
            writer.append(1, 2, 3)
            writer.append(2, 3, 4)
        }
        assertEquals(3, loader[1]?.get("b"))
    }
}