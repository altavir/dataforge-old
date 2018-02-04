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

package dataforge.workspace

import hep.dataforge.cache.CachePlugin
import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.workspace.BasicWorkspace
import hep.dataforge.workspace.Workspace
import hep.dataforge.workspace.tasks.PipeTask
import hep.dataforge.workspace.tasks.TaskModel
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class WorkspaceTest {

    @Test(timeout = 900)
    @Throws(Exception::class)
    fun testExecution() {
        val res = wsp.runTask("test2", Meta.empty())
        res.computeAll()
        assertEquals(6, res.getCheckedData("data_1", Number::class.java).get().toLong())
        assertEquals(8, res.getCheckedData("data_2", Number::class.java).get().toLong())
        assertEquals(10, res.getCheckedData("data_3", Number::class.java).get().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testCaching() {
        counter.set(0)
        val res1 = wsp.runTask("test2", Meta.empty()).computeAll()
        val res2 = wsp.runTask("test2", Meta.empty()).computeAll()
        assertEquals(6, counter.get().toLong())
        assertEquals(
                8,
                wsp.runTask(
                        "test2",
                        MetaBuilder().putValue("a", 0)).getCheckedData("data_2", Number::class.java
                ).get().toLong()
        )
    }

    companion object {

        val context = Global.getContext("TEST")

        private val counter = AtomicInteger()

        val task1 = object : PipeTask<Number, Number>("test1", Number::class.java, Number::class.java) {
            override fun buildModel(model: TaskModel.Builder, meta: Meta) {
                model.data("*")
            }

            override fun result(context: Context, name: String, input: Number, meta: Laminate): Number {
                try {
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }

                counter.incrementAndGet()
                return input.toInt() + meta.getInt("a", 2)
            }
        }

        val task2 = object : PipeTask<Number, Number>("test2", Number::class.java, Number::class.java) {
            override fun buildModel(model: TaskModel.Builder, meta: Meta) {
                model.dependsOn("test1", meta)
            }

            override fun result(context: Context, name: String, input: Number, meta: Laminate): Number {
                try {
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }

                counter.incrementAndGet()
                return input.toInt() * meta.getInt("b", 2)
            }
        }

        private val wsp: Workspace = BasicWorkspace.Builder()
                .setContext(context)
                .staticData("data_1", 1)
                .staticData("data_2", 2)
                .staticData("data_3", 3)
                .task(task1)
                .task(task2)
                .build()


        @BeforeClass
        fun start() {
            context.load(
                    CachePlugin::class.java,
                    MetaBuilder().setValue("fileCache.enabled", false)
            )
        }
    }
}