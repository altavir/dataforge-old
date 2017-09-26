package hep.dataforge.kodex

import hep.dataforge.context.Global
import hep.dataforge.data.DataSet
import hep.dataforge.meta.Meta
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.experimental.time.delay
import org.junit.Test
import java.time.Duration

class KActionTest {
    val data = DataSet.builder(String::class.java).apply {
        (1..10).forEach {
            putData("$it", "this is my data $it", Meta.empty());
        }
    }.build()

    @Test
    fun testPipe() {
        val action = KPipe("test1", String::class.java,String::class.java){
            delay(Duration.ofMillis(200));
            return@KPipe it + ": stage1";
        }

        val res = action.run(Global.instance(),data, Meta.empty()).computeAll();
        assertEquals("this is my data 4: stage1",res.optData("4").get().get())
    }

}