/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace.identity;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Alexander Nozik
 */
public class CombinedIdentityTest {

    public CombinedIdentityTest() {
    }

    @Test
    public void testStrings() {
        System.out.println("test string identity");
        assertEquals(new ValueIdentity("myString"), new ValueIdentity("myString"));
    }

    /**
     * Test of equals method, of class CombinedIdentity.
     */
    @Test
    public void testEquals() {
        System.out.println("test combination");
        Meta myMeta = new MetaBuilder("meta").putNode(new MetaBuilder("child").putValue("childValue", 18)).putValue("value", true);

        Identity one = new MetaIdentity(myMeta).and("myString");
        Identity two = new ValueIdentity("myString").and(new MetaIdentity(new MetaBuilder(myMeta)));

        assertEquals(one, two);
    }

}
