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

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.exceptions.NameNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author darksnake
 */
public class AnnotationTest {

    Meta testAnnotation;

    public AnnotationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
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

    @After
    public void tearDown() {
    }

    @Test
    public void testSubst() {
        System.out.println("Value substitution via AnnotationReader");
        assertEquals("otherValue", testAnnotation.getString("some"));
    }

    @Test
    public void testPath() {
        System.out.println("Path search");
        assertEquals("childValue", testAnnotation.getString("child.childValue"));
        assertEquals("grandChildValue", testAnnotation.getString("child.grandChild.grandChildValue"));
        assertEquals("otherGrandChildValue", testAnnotation.getString("child[1].grandChild.grandChildValue"));
    }
    
    @Test(expected = NameNotFoundException.class)
    public void testWrongPath() {
        System.out.println("Missing path search");
        assertEquals("otherGrandChildValue", testAnnotation.getString("child[2].grandChild.grandChildValue"));
    }
}
