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
package hep.dataforge.io;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.functions.NamedFunction;
import hep.dataforge.maths.NamedVector;
import static hep.dataforge.names.NamedUtils.combineNames;
import hep.dataforge.names.Names;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.MapPoint;
import hep.dataforge.tables.PointSource;
import hep.dataforge.tables.Table;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Формирование и печать наборов данных
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class OutputData {

    /**
     * <p>getNamedFunctionData.</p>
     *
     * @param func a {@link hep.dataforge.functions.NamedFunction} object.
     * @param points a {@link java.util.List} object.
     * @return a {@link hep.dataforge.tables.ListTable} object.
     */
    public static Table getNamedFunctionData(NamedFunction func, List<NamedVector> points) {
        final String[] format = combineNames(func.namesAsArray(), "value");
        ListTable.Builder res = new ListTable.Builder(format);
        Double[] values = new Double[func.getDimension() + 1];
        for (NamedVector point : points) {
            for (int j = 0; j < func.getDimension(); j++) {
                values[j] = point.getVector().getEntry(j);
            }
            values[values.length - 1] = func.value(point);
            DataPoint dp = new MapPoint(format, values);
            res.addRow(dp);
        }
        return res.build();
    }

    /**
     * <p>getUnivariateFunctionData.</p>
     *
     * @param func a {@link org.apache.commons.math3.analysis.UnivariateFunction} object.
     * @param points an array of double.
     * @return a {@link hep.dataforge.tables.ListTable} object.
     */
    public static Table getUnivariateFunctionData(UnivariateFunction func, double[] points) {
        final String[] format = {"point", "value"};
        ListTable.Builder res = new ListTable.Builder(format);
        Double[] values = new Double[2];
        
        for (int i = 0; i < format.length; i++) {
            values[0] = points[i];
            values[1] = func.value(points[i]);
            DataPoint dp = new MapPoint(format, values);
            res.addRow(dp);
        }
        return res.build();
    }

    /**
     * <p>printDataSet.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     * @param outputFormat a {@link java.lang.String} object.
     * @param data a {@link hep.dataforge.tables.Table} object.
     * @param head a {@link java.lang.String} object.
     */
    public static void printDataSet(PrintWriter out, PointSource data, String head, String... outputFormat) {
        Names format = data.getFormat().names();

        if (outputFormat.length > 0) {
            if (!data.getFormat().names().contains(outputFormat)) {
                throw new NameNotFoundException();
            }
            format = Names.of(outputFormat);
        }

        if (head != null) {
            out.println(head);
        }
        out.println();
        
        String[] names = format.asArray();
        for (int i = 0; i < format.getDimension(); i++) {
            out.printf("%-8s\t",names[i]);
        }
        out.println();
        out.println();
        for (DataPoint dataPoint : data) {
            for (int i = 0; i < format.getDimension(); i++) {
                out.printf("%8.8g\t", dataPoint.getValue(names[i]));
            }
//            if(printTags){
//                Set<String> tags = dataPoint.getTags();
//                for (String tag : tags) {
//                    out.printf("%s\t", tag);
//                }
//            }
            out.printf("\b%n");
        }

    }
}
