/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.io.XMLMetaReader;
import java.io.IOException;
import java.text.ParseException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Nozik
 */
public class TemplateTest {
    
    public TemplateTest() {
    }

    /**
     * Test of compileTemplate method, of class MetaUtils.
     */
    @Test
    public void testCompileTemplate() throws IOException, ParseException {
        System.out.println("compileTemplate");
        Meta template = new XMLMetaReader().read(getClass().getResourceAsStream("/meta/template.xml"));
        Meta data = new XMLMetaReader().read(getClass().getResourceAsStream("/meta/templateData.xml"));
        Meta result = Template.compileTemplate(template, data);
        assertEquals(result.getString("someNode.myValue"), "crocodile");
        assertEquals(result.getString("someNode.subNode[0].ggg"), "81.5");
        assertEquals(result.getString("someNode.subNode[1].subNode.subNodeValue"), "ccc");
    }
    
}
