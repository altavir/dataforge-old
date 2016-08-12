/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.description;

import hep.dataforge.names.Named;
import org.junit.Test;

/**
 *
 * @author Alexander Nozik
 */
public class TextDescriptorFormatterTest {
    
    public TextDescriptorFormatterTest() {
    }

//    /**
//     * Test of showDescription method, of class TextDescriptorFormatter.
//     */
//    @Test
//    public void testPrintDescription() {
//        System.onComplete.println("showDescription");
//        Descriptor descriptor = null;
//        TextDescriptorFormatter instance = null;
//        instance.showDescription(descriptor);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of showShortDescription method, of class TextDescriptorFormatter.
     * @throws java.lang.Exception
     */
    @Test
    public void testPrintShortDescription() throws Exception {
        System.out.println("printShortDescription");
        NodeDescriptor descriptor = DescriptorUtils.buildDescriptor(TestContent.class);
        TextDescriptorFormatter formatter = new TextDescriptorFormatter(System.out);
        formatter.showShortDescription(descriptor);

    }
    
    
    @ValueDef(name = "firstpar", required = true, type = "NUMBER", info = "The description for my first parameter")
    @ValueDef(name = "secondpar", multiple = true, info = "The description for my second parameter")
    @ValueDef(name = "thirdpar", def = "test", info = "The description for my third parameter")    
    public static class TestContent implements Named{

        @Override
        public String getName() {
            return "myTestName";
        }
    }
    
}
