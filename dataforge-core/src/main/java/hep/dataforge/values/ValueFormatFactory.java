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

import hep.dataforge.meta.Meta;

/**
 * <p>
 * ValueFormatFactory class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class ValueFormatFactory {

    public static final ValueFormatter EMPTY_FORMAT = new ValueFormatter() {
        @Override
        public boolean allowed(Value val) {
            return true;
        }

        @Override
        public String format(Value val) {
            return val.stringValue();
        }
    };

    public static ValueFormatter forValue(Value value) {
        return forType(value.valueType());
    }

    public static ValueFormatter forType(ValueType type) {
        return forType(type, 0);
    }

    public static ValueFormatter forType(ValueType type, int maxWidth) {
        if (maxWidth > DefaultValueFormat.getDefaultWidth(type)) {
            switch (type) {
                case NUMBER:
                    return new FixedWidthFormat(maxWidth, ValueType.NUMBER);
                case TIME:
                    return new FixedWidthFormat(maxWidth, ValueType.TIME);
                default:
                    return new FixedWidthFormat(maxWidth);
            }
        } else {
            return new DefaultValueFormat(type);
        }
    }

    public static ValueFormatter fixedWidth(int width) {
        return new FixedWidthFormat(width);
    }

    public static ValueFormatter build(Meta a) {
        if (a == null || a.isEmpty()) {
            return EMPTY_FORMAT;
        } else if (a.hasValue("width")) {
            return new FixedWidthFormat(a.getInt("width"));
        } else if (a.hasValue("type")){
            return forType(ValueType.valueOf(a.getString("type")));
        } else{
            return EMPTY_FORMAT;
        }
    }

}
