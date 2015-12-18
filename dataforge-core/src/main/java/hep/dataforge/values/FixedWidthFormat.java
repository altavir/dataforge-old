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

/**
 *
 * @author Alexander Nozik
 */
public class FixedWidthFormat extends ColumnFormat {
    private final int width;
    private final ValueType type;

    
    public FixedWidthFormat(int width) {
        if (width < 1) {
            throw new IllegalArgumentException("FixedWidthFormat must have positive width");
        }
        this.width = width;
        type = ValueType.STRING;
    }

    public FixedWidthFormat(int width, ValueType type) {
        if (width < 1) {
            throw new IllegalArgumentException("FixedWidthFormat must have positive width");
        }
        this.width = width;
        this.type = type;
    }

    @Override
    public boolean allowed(Value val) {
        switch (val.valueType()) {
            case BOOLEAN:
                return width >= 5;
            case NULL:
                return true;
            case NUMBER:
                return true;
            case STRING:
                return val.stringValue().length() <= width;
            case TIME:
                return width > 11;//FIXME проверить 
            default:
                return false;
        }
    }

    @Override
    public ValueType primaryType() {
        return type;
    }

    @Override
    public int getMaxWidth() {
        return width;
    }

}
