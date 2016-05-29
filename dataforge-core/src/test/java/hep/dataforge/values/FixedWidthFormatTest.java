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
package hep.dataforge.values;

import java.time.Instant;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Alexander Nozik
 */
public class FixedWidthFormatTest {

    public FixedWidthFormatTest() {
    }

    @Test
    public void testPi() {
        System.out.println("Print Pi with the length of 6");
        ValueFormatter shortFormat = new FixedWidthFormat(6);
        Value val = Value.of(Math.PI);
        System.out.println(shortFormat.format(val));
        assertTrue(shortFormat.format(val).length() == 6);
    }
    
    @Test
    public void testExpPi() {
        System.out.println("Print Pi*10^-5 with the length of 6");
        ValueFormatter shortFormat = new FixedWidthFormat(6);
        Value val = Value.of(Math.PI*1e-5);
        System.out.println(shortFormat.format(val));
        assertTrue(shortFormat.format(val).length() == 6);
    }    
    
    @Test
    public void testInt() {
        System.out.println("Print 34");
        ValueFormatter shortFormat = new FixedWidthFormat(6);
        Value val = Value.of(34);
        System.out.println(shortFormat.format(val));
        assertTrue(shortFormat.format(val).length() == 6);
    }      

    /**
     * Test of allowed method, of class FixedWidthFormat.
     */
    @Test
    public void testAllowed() {
        ValueFormatter format = new FixedWidthFormat(6);
        assertTrue(!format.allowed(Value.of(Instant.now())));
    }

}
