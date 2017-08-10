package hep.dataforge.meta;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LaminateTest {

    Meta meta = new MetaBuilder("test")
            .putNode(new MetaBuilder("child").putValue("a", 22));

    Laminate laminate = new Laminate(meta, meta);

    @Test
    public void testToString() {
        System.out.println(laminate.toString());
        assertEquals(3, laminate.toString().split("\n").length);
    }

}