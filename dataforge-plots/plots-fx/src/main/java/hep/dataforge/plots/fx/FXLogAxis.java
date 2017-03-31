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
package hep.dataforge.plots.fx;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * taken from http://blog.dooapp.com/logarithmic-scale-strikes-back-in-javafx-20
 *
 */
public class FXLogAxis extends FXObjectAxis<Number> {

    private static final NumberFormat format;

    static {
        format = NumberFormat.getInstance();
        format.setMaximumIntegerDigits(6);
        format.setMinimumIntegerDigits(1);
    }

    public FXLogAxis(Number lower, Number upper) {
        super(lower, upper);
    }

    
    

//    /**
//     * Now we have to implement all abstract methods of the ValueAxis class. The
//     * first one, calculateMinorTickMarks is used to get the list of minor tick
//     * marks position that you want to display on the axis. You could find my
//     * definition below. It's based on the number of minor tick and the
//     * logarithmic formula.
//     *
//     * @return
//     */
//    @Override
//    protected List<Number> calculateMinorTickMarks() {
//        Range range = getRange();
//        List<Number> minorTickMarksPositions = new ArrayList<>();
//        if (range != null) {
//
//            Number lowerBound = range.getFirst();
//            Number upperBound = range.getSecond();
//            double logUpperBound = Math.log10(upperBound.doubleValue());
//            double logLowerBound = Math.log10(lowerBound.doubleValue());
//
//            int minorTickMarkCount = getMinorTickCount();
//
//            for (double i = logLowerBound; i <= logUpperBound; i += 1) {
//                for (double j = 0; j <= 10; j += (1. / minorTickMarkCount)) {
//                    double value = j * Math.pow(10, i);
//                    minorTickMarksPositions.add(value);
//                }
//            }
//        }
//        return minorTickMarksPositions;
//    }

    /**
     * Then, the calculateTickValues method is used to calculate a list of all
     * the data values for each tick mark in range, represented by the second
     * parameter. The formula is the same than previously but here we want to
     * display one tick each power of 10.
     *
     * @param length
     * @param range
     * @return
     */
    @Override
    protected List<Number> calculateTickValues(double length, Object range) {
        Range<Number> theRange = (Range) range;
        List<Number> tickPositions = new ArrayList<>();
        if (range != null) {
            Number lowerBound = theRange.getLower();
            Number upperBound = theRange.getUpper();
            double logLowerBound = Math.log10(lowerBound.doubleValue());
            double logUpperBound = Math.log10(upperBound.doubleValue());
//            System.out.println("lower bound is: " + lowerBound.doubleValue());

            for (double i = logLowerBound; i <= logUpperBound; i += 1) {
                for (double j = 1; j <= 10; j++) {
                    double value = (j * Math.pow(10, i));
                    tickPositions.add(value);
                }
            }
        }
        return tickPositions;
    }

    /**
     * The getTickMarkLabel is only used to convert the number value to a string
     * that will be displayed under the tickMark. Here I choose to use a number
     * formatter.
     *
     * @param value
     * @return
     */
    @Override
    protected String getTickMarkLabel(Number value) {
        return format.format(value);
    }

    @Override
    public double toNumericValue(Number value) {
        return Math.log10(value.doubleValue());
    }

    @Override
    public Number toRealValue(double value) {
        return Math.pow(10, value);
    }
}
