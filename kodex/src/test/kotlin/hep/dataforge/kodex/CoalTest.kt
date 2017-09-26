package hep.dataforge.kodex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoalTest {
    val firstLevel = (1..10).map { index ->
        generate {
            Thread.sleep(100)
            println("this is coal $index")
            "this is coal $index"
        }
    }
    val secondLevel = firstLevel.map {
        it.pipe {
            Thread.sleep(200)
            val res = it + ":Level 2"
            println(res)
            res
        }
    }
    val thirdLevel = secondLevel.map {
       it.pipe {
            Thread.sleep(300)
            val res = it.replace("Level 2", "Level 3")
            println(res)
            res
        }
    }
    val joinGoal = thirdLevel.join { Pair("joining ${it.size} elements", 10) }

    @Test
    fun testSingle() {
        assertEquals(firstLevel [3].get(), "this is coal 4")
    }

    @Test
    fun testDep() {
        assertTrue(secondLevel [3].get().endsWith("Level 2"))
    }

    @Test
    fun testJoin() {
        val (_, num) = joinGoal.get();
        assertEquals(num, 10);

    }
}