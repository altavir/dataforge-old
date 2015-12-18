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
 * Класс описывает ограничения на допустимые типы значений, а также процедуру
 * отображения значения в форматированную строку для записи в консоль или текстовый файл
 *
 * @author Alexander Nozik
 */
public interface ValueFormat {

    boolean allowed(Value val);

    String format(Value val);    
    /**
     * Вспомогательный метод для создания заголовка
     * @param str
     * @return 
     */
    default String formatString(String str){
        return format(Value.of(str));
    }
    
}
