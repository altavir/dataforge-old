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
package hep.dataforge.io.envelopes;

import hep.dataforge.io.envelopes.PropertyLib;
import java.nio.charset.Charset;

/**
 *
 * @author Alexander Nozik
 */
public class CharsetLibrary extends PropertyLib<Charset> {
    public static final Charset ASCII_CHARSET = Charset.forName("Cp1252");
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");     
    public static final short UTF8_ENCODING = 0;
    public static final short ACII_ENCODING = 1;    
    
    private static final CharsetLibrary instance = new CharsetLibrary();
    
    
    public static CharsetLibrary instance(){
        return instance;
    }


    @Override
    public Charset get(String key) {
        return Charset.forName(key);
    }
    
    @Override
    public Charset getDefault(){
        return UTF8_CHARSET;
    }

    private CharsetLibrary() {
        putComposite(UTF8_ENCODING, "UTF-8", UTF8_CHARSET);
        putComposite(ACII_ENCODING, "ACII", ASCII_CHARSET);
    }
}
