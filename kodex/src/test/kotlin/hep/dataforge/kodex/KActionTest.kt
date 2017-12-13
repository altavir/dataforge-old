package hep.dataforge.kodex

import hep.dataforge.data.DataSet
import hep.dataforge.meta.Meta
import hep.dataforge.workspace.BasicWorkspace
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.experimental.time.delay
import org.junit.Test
import java.time.Duration

class KActionTest {
    val data: DataSet<String> = DataSet.builder(String::class.java).apply {
        (1..10).forEach {
            putData("$it", "this is my data $it", buildMeta { "index" to it });
        }
    }.build()

    val pipe = KPipe("testPipe", String::class.java, String::class.java) {
        name = "newName_${meta["index"]}"
        if (meta["index"].intValue() % 2 == 0) {
            meta.putValue("odd", true);
        }
        result {
            println("performing action on $name")
            delay(Duration.ofMillis(400));
            it + ": stage1";
        }
    }

    @Test
    fun testPipe() {
        println("test pipe")
        val res = pipe.run(global, data, Meta.empty());
        val datum = res.optData("newName_4").get()
        val value = datum.goal.get()
        assertTrue(datum.meta["odd"].booleanValue())
        assertEquals("this is my data 4: stage1", value)
    }

    @Test
    fun testPipeTask() {
        println("test pipe task")

        val testTask = task("test") {
            model {
                data("*")
            }
            action(pipe)
        }



        global.setValue("cache.enabled", false)
        val workspace = BasicWorkspace.builder()
                .data("test", data)
                .task(testTask)
                .target("test", Meta.empty())
                .setContext(global)
                .build()

        val res = workspace.runTask("test")

        val datum = res.optData("newName_4").get()
        val value = datum.goal.get()
        assertTrue(datum.meta["odd"].booleanValue())
        assertEquals("this is my data 4: stage1", value)

    }
}