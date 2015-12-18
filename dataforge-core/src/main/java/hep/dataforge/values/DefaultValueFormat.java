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
 * ValueFormat for specific ValueType. If type is string than any format is
 * allowed.
 *
 * @author Alexander Nozik
 */
public class DefaultValueFormat extends ColumnFormat {
    
    ValueType type;
    
    public DefaultValueFormat(ValueType type) {
        this.type = type;
    }
    
    @Override
    public boolean allowed(Value val) {
        return (type == ValueType.STRING || val.valueType() == type) && val.stringValue().length() <= getDefaultWidth(type);
    }
    
//    @Override
//    public String format(Value val) {
//        return formatString(val.stringValue());
//    }
    
    @Override
    public ValueType primaryType() {
        return type;
    }
    
//    @Override
//    public String formatString(String str) {
//        int spaces = getDefaultWidth(type) - str.length();
//        for (int i = 0; i < spaces; i++) {
//            str += " ";
//        }
//        return str;
//    }
    
    /**
     * Get pre-defined default width for given value type
     * @param type
     * @return 
     */
    public static int getDefaultWidth(ValueType type) {
        switch (type) {
            case NUMBER:
                return 8;
            case BOOLEAN:
                return 6;
            case STRING:
                return 15;
            case TIME:
                return 20;
            case NULL:
                return 6;
            default:
                throw new AssertionError(type.name());
        }
    }

    @Override
    public int getMaxWidth() {
        return getDefaultWidth(primaryType());
    }
}
