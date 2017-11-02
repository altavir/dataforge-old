package hep.dataforge.names;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NameTest {
    @Test
    public void testNameFromString() {
        Name name = Name.of("first.second[28].third\\.andahalf");
        assertEquals(3, name.getLength());
        assertEquals("third.andahalf", ((NameToken) name.getLast()).toUnescaped());
    }

    @Test
    public void testReconstruction() {
        Name name = Name.join(Name.of("first.second"), Name.ofSingle("name.with.dot"), Name.ofSingle("end[22]"));
        String str = name.toString();
        Name reconstructed = Name.of(str);
        assertEquals(name, reconstructed);
        assertEquals("name.with.dot", reconstructed.getTokens().get(2).toUnescaped());
    }

    @Test
    public void testJoin() {
        Name name = Name.join("first", "second", "", "another");
        assertEquals(3, name.getLength());
    }

}