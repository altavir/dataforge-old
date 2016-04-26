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
package hep.dataforge.tables;

import hep.dataforge.values.Value;
import java.util.Scanner;

/**
 * <p>
 SimpleParser class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class SimpleParser implements PointParser {

    private final String[] format;

    /**
     * <p>
     * Constructor for SimpleDataParser.</p>
     *
     * @param format an array of {@link java.lang.String} objects.
     */
    public SimpleParser(String[] format) {
        this.format = format;
    }

    /**
     * Создаем парсер по заголовной строке
     *
     * @param line a {@link java.lang.String} object.
     */
    public SimpleParser(String line) {
        this.format = line.trim().split("[^\\w']+");
    }
    
    public SimpleParser(TableFormat format){
        this.format = format.namesAsArray();
    }

    /**
     * {@inheritDoc}
     *
     * @param line
     */
    @Override
    public DataPoint parse(String line) {
        Scanner sc = new Scanner(line);

        Value[] values = new Value[format.length];
        for (int i = 0; i < format.length; i++) {
            if (sc.hasNextDouble()) {
                values[i] = Value.of(sc.nextDouble());
            } else if (sc.hasNextInt()) {
                values[i] = Value.of(sc.nextInt());
            } else {
                values[i] = Value.of(sc.next());
            }
        }

        DataPoint point = new MapPoint(format, values);

//            //Все, что после послднего значения считаем тэгами
//            while (sc.hasNext()) {
//                point.addTag(sc.next());
//            }
        return point;
    }

}
