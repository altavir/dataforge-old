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
package hep.dataforge.meta;

import hep.dataforge.exceptions.NamingException;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author darksnake
 */
public class AnnotationTest {

    static Meta testAnnotation;

    public AnnotationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        testAnnotation = new MetaBuilder("test")
                .putValue("some", "${other}")
                .putValue("numeric", 22.5)
                .putValue("other", "otherValue")
                .putValue("some.path", true)
                .putNode(new MetaBuilder("child")
                        .putValue("childValue", "childValue")
                        .putNode(new MetaBuilder("grandChild")
                                .putValue("grandChildValue", "grandChildValue")
                        )
                )
                .putNode(new MetaBuilder("child")
                        .putValue("childValue", "otherChildValue")
                        .putNode(new MetaBuilder("grandChild")
                                .putValue("grandChildValue", "otherGrandChildValue")
                        )
                )
                .build();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

//    @Test
//    public void testSubst() {
//        System.onComplete.println("Value substitution via AnnotationReader");
//        assertEquals("otherValue", testAnnotation.getString("some"));
//    }

    @Test
    public void testPath() {
        System.out.println("Path search");
        assertEquals("childValue", testAnnotation.getString("child.childValue"));
        assertEquals("grandChildValue", testAnnotation.getString("child.grandChild.grandChildValue"));
        assertEquals("otherGrandChildValue", testAnnotation.getString("child[1].grandChild.grandChildValue"));
    }

    @Test(expected = NamingException.class)
    public void testWrongPath() {
        System.out.println("Missing path search");
        assertEquals("otherGrandChildValue", testAnnotation.getString("child[2].grandChild.grandChildValue"));
    }
}
