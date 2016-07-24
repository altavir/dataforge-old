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
package hep.dataforge.fitting.models;

import hep.dataforge.fitting.ParamSet;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.maths.RandomUtils;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.Table;
import static java.lang.Double.isNaN;
import static java.lang.Math.sqrt;
import java.util.Iterator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import static java.lang.Double.isNaN;
import static java.lang.Double.isNaN;
import static java.lang.Double.isNaN;

/**
 * Генератор наборов данных для спектров. На входе требуется набор данных,
 * содержащих X-ы и время набора. Могут быть использованы реальные
 * экспериментальные наборы данных.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class HistogramGenerator implements Generator {

    static final double POISSON_BOUNDARY = 100;
    private GeneratorType genType = GeneratorType.POISSONIAN;
    private final RandomDataGenerator generator;
    private final ParamSet params;
    private final HistogramModel source;

    /**
     * <p>Constructor for HistogramGenerator.</p>
     *
     * @param rnd a {@link org.apache.commons.math3.random.RandomGenerator} object.
     * @param source a {@link hep.dataforge.fitting.models.HistogramModel} object.
     * @param params a {@link hep.dataforge.fitting.ParamSet} object.
     */
    public HistogramGenerator(RandomGenerator rnd, HistogramModel source, ParamSet params) {
        this.source = source;
        this.params = params;

        if (rnd != null) {
            this.generator = new RandomDataGenerator(rnd);
        } else {
            this.generator = new RandomDataGenerator(RandomUtils.getDefaultRandomGenerator());
        }

    }

    /** {@inheritDoc} */
    @Override
    public Table generateData(Iterable<DataPoint> config) {
        ListTable.Builder res = new ListTable.Builder(Histogram.names);
        for (Iterator<DataPoint> it = config.iterator(); it.hasNext();) {
            res.row(this.generateDataPoint(it.next()));

        }
        return res.build();
    }

    /** {@inheritDoc} */
    @Override
    public HistogramBin generateDataPoint(DataPoint configPoint) {
        double mu = this.getMu(configPoint);
        if (isNaN(mu) || (mu < 0)) {
            throw new IllegalStateException();
        }
        double y;
        switch (this.genType) {
            case GAUSSIAN:
                double sigma = sqrt(1 / mu);
                if ((sigma == 0) && (mu == 0)) {
                    y = 0;// Проверяем чтобы не было сингулярности
                } else {
                    y = generator.nextGaussian(mu, sigma);
                }
                if (y < 0) {
                    y = 0;//Проверяем, чтобы не было отрицательных значений
                }
                break;
            case POISSONIAN:

                if (mu < 0) {
                    throw new RuntimeException("Negative input parameter for poissonian generator.");
                }
                if (mu == 0) {
                    y = 0;
                    break;
                }
                if (mu < POISSON_BOUNDARY) {
                    y = generator.nextPoisson(mu);
                } else {
                    y = generator.nextGaussian(mu, sqrt(mu));
                }
                break;
            default:
                throw new Error("Enum listing failed!");
        }

        if (y > Long.MAX_VALUE) {
            throw new IllegalStateException("Long number overflow");
        }

        return new HistogramBin(configPoint.getValue("binBegin").doubleValue(), configPoint.getValue("binEnd").doubleValue(), (long) y);
    }

    /**
     * <p>generateUniformHistogram.</p>
     *
     * @param begin a double.
     * @param end a double.
     * @param binNumber a int.
     * @return a {@link hep.dataforge.fitting.models.Histogram} object.
     */
    public Table generateUniformHistogram(double begin, double end, int binNumber) {
        assert end > begin;
        ListTable.Builder res = new ListTable.Builder(Histogram.names);
        DataPoint bin;
        double step = (end - begin) / (binNumber);
        double a = begin;
        double b = begin + step;
        for (int i = 0; i < binNumber; i++) {
            bin = new HistogramBin(a, b, 0);
            bin = this.generateDataPoint(bin);
            res.row(bin);
            a = b;
            b += step;
        }
        return res.build();
    }

    /** {@inheritDoc} */
    @Override
    public String getGeneratorType() {
        return this.genType.name();
    }

    private double getMu(DataPoint point) throws NameNotFoundException {
        return source.value(point.getValue("binBegin").doubleValue(), point.getValue("binEnd").doubleValue(), params);
    }

    /**
     * <p>setGeneratorType.</p>
     *
     * @param type a {@link hep.dataforge.datafitter.models.HistogramGenerator.GeneratorType} object.
     */
    public void setGeneratorType(GeneratorType type) {
        this.genType = type;
    }

    /**
     *
     */
    public enum GeneratorType {

        /**
         *
         */
        POISSONIAN,

        /**
         *
         */
        GAUSSIAN
    }
}
