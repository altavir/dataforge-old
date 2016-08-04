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
package hep.dataforge.stat.fit;

import hep.dataforge.stat.models.Model;
import hep.dataforge.io.FittingIOUtils;
import static hep.dataforge.io.FittingIOUtils.printParamSet;
import hep.dataforge.maths.NamedMatrix;
import hep.dataforge.tables.Table;
import java.io.PrintWriter;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import static org.apache.commons.math3.util.MathArrays.ebeMultiply;
import hep.dataforge.stat.parametric.ParametricValue;

/**
 * This class combine the information required to fit data. The key elements are
 Table, Model and initial ParamSet. Additionally, one can provide
 covariance matrix, prior probability, fit history etc. To simplify
 construction of FitState use FitStateBuilder
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
//TODO добавить параметры по-умолчанию в модель
public class FitState extends FitSource {

    public static Builder builder() {
        return new Builder();
    }

    /**
     *
     */
    protected final NamedMatrix covariance;

    /**
     *
     */
    protected final IntervalEstimate interval;

    /**
     *
     */
    protected final ParamSet pars;

    public FitState(Table dataSet, Model model, ParamSet pars) {
        super(dataSet, model, null);
        this.pars = pars;
        this.covariance = null;
        this.interval = null;
    }

    /**
     * <p>
     * Constructor for FitState.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     * @param dataSet a {@link hep.dataforge.tables.Table} object.
     * @param model a {@link hep.dataforge.stat.models.Model} object.
     * @param pars a {@link hep.dataforge.stat.fit.ParamSet} object.
     * @param covariance a {@link hep.dataforge.maths.NamedMatrix} object.
     * @param interval a {@link hep.dataforge.stat.fit.IntervalEstimate}
     * object.
     * @param prior a {@link hep.dataforge.stat.parametric.ParametricValue} object.
     */
    public FitState(Table dataSet, Model model, ParamSet pars,
            NamedMatrix covariance, IntervalEstimate interval, ParametricValue prior) {
        super(dataSet, model, prior);
        this.covariance = covariance;
        this.interval = interval;
        this.pars = pars;
    }

    /**
     * clone constructor
     *
     * @param state a {@link hep.dataforge.stat.fit.FitState} object.
     */
    protected FitState(FitState state) {
        super(state.getDataSet(), state.getModel(), state.getPrior());
        this.covariance = state.covariance;
        this.pars = state.pars;
        this.interval = state.interval;
    }

    /**
     * Creates new FitState object based on this one and returns its Builder.
     *
     * @return a {@link hep.dataforge.fitting.FitState.Builder} object.
     */
    public Builder edit() {
        return new Builder(this);
    }

    /**
     * <p>
     * getChi2.</p>
     *
     * @return a double.
     */
    public double getChi2() {
        return getChi2(pars);
    }

    /**
     * <p>
     * getCorrelationMatrix.</p>
     *
     * @return a {@link hep.dataforge.maths.NamedMatrix} object.
     */
    public NamedMatrix getCorrelationMatrix() {
        if (covariance == null || pars == null) {
            return null;
        }
        NamedMatrix res = covariance.copy();
        String[] names = covariance.namesAsArray();

        for (String str1 : names) {
            for (String str2 : names) {
                double value = res.getElement(str1, str2) / pars.getError(str1) / pars.getError(str2);
                res.setElement(str1, str2, value);
            }
        }
        return res;
    }

    /**
     * Возвращается всегда полная матрица, включающая даже параметры, которые не
     * фитировались. Для параметров, для которых нет матрицы в явном виде
     * возвращаются только диоганальные элементы.
     *
     * @return the covariance
     */
    public NamedMatrix getCovariance() {
        double[] sigmas = this.pars.getParErrors().getArray();
        sigmas = ebeMultiply(sigmas, sigmas);
        RealMatrix baseMatrix = new DiagonalMatrix(sigmas);
        NamedMatrix result = new NamedMatrix(baseMatrix, this.pars.namesAsArray());
        if (this.covariance != null) {
            result.setValuesFrom(this.covariance);
        }
        return result;
    }

    /**
     * <p>
     * getIntervalEstimate.</p>
     *
     * @return a {@link hep.dataforge.stat.fit.IntervalEstimate} object.
     */
    public IntervalEstimate getIntervalEstimate() {
        return this.interval;
    }

    /**
     * <p>
     * getParameters.</p>
     *
     * @return the pars
     */
    public ParamSet getParameters() {
        return pars;
    }

    /**
     * <p>
     * print.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     */
    public void print(PrintWriter out) {
        out.println("***FITTING RESULT***");
        this.printAllValues(out);
        if (covariance != null) {
            out.println();
            out.println("Correlation marix:");
            FittingIOUtils.printNamedMatrix(out, getCorrelationMatrix());
        }
        if (this.interval != null) {
            this.interval.print(out);
        }
        out.println();
        double chi2 = getChi2();
        out.printf("Chi squared: %g%n", chi2);
        out.println();
        out.flush();
    }

    /**
     * <p>
     * printAllValues.</p>
     *
     * @param out a {@link java.io.PrintWriter} object.
     */
    protected void printAllValues(PrintWriter out) {
        out.println();
        out.println("All function parameters are: ");
        printParamSet(out, this.pars);
    }

    private void printCovariance(PrintWriter out) {
        if (getCovariance() != null) {
            out.println();
            out.printf("%n***COVARIANCE***%n");

            FittingIOUtils.printNamedMatrix(out, getCovariance());

        }
    }

    /**
     *
     */
    public static class Builder {

        NamedMatrix covariance;

        /**
         *
         */
        protected Table dataSet;

        /**
         *
         */
        protected IntervalEstimate interval;

        /**
         *
         */
        protected Model model;

        /**
         *
         */
        protected ParamSet pars;
        ParametricValue prior;

        /**
         *
         * @param state
         */
        public Builder(FitState state) {
            this.covariance = state.covariance;
            this.dataSet = state.dataSet;
            this.interval = state.interval;
            this.model = state.model;
            this.pars = state.pars;
            this.prior = state.prior;
        }

        /**
         *
         */
        public Builder() {

        }

        /**
         * @param dataSet the dataSet to set
         * @return
         */
        public Builder setDataSet(Table dataSet) {
            this.dataSet = dataSet;
            return this;
        }

        /**
         * @param model the model to set
         * @return
         */
        public Builder setModel(Model model) {
            this.model = model;
            return this;
        }

        /**
         * @param pars the pars to set
         * @return
         */
        public Builder setPars(ParamSet pars) {
            this.pars = pars;
            return this;
        }

        /**
         * <p>
         * Setter for the field <code>covariance</code>.</p>
         *
         * @param cov a {@link hep.dataforge.maths.NamedMatrix} object.
         * @param updateErrors a boolean.
         * @return
         */
        public Builder setCovariance(NamedMatrix cov, boolean updateErrors) {
            covariance = cov;
            if (updateErrors) {
                for (String name : cov.names()) {
                    double value = cov.getElement(name, name);
                    if (value > 0) {
                        pars.setParError(name, Math.sqrt(value));
                    } else {
                        throw new IllegalArgumentException("The argument is not valid covariance");
                    }
                }
            }
            return this;
        }

        /**
         *
         * @param priorDistribution
         * @return
         */
        public Builder setPrior(ParametricValue priorDistribution) {
            prior = priorDistribution;
            return this;
        }

        /**
         *
         * @param intervalEstimate
         * @return
         */
        public Builder setInterval(IntervalEstimate intervalEstimate) {
            this.interval = intervalEstimate;
            return this;
        }

        /**
         *
         * @return
         */
        public FitState build() {
            if (dataSet == null || model == null || pars == null) {
                throw new IllegalStateException("Can't build FitState, data, model and starting parameters must be provided.");
            }
            return new FitState(dataSet, model, pars, covariance, interval, prior);
        }

    }

}
