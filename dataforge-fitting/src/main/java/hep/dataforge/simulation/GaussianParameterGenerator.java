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
package hep.dataforge.simulation;

import hep.dataforge.datafitter.ParamSet;
import hep.dataforge.maths.NamedDoubleArray;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.maths.NamedMatrix;
import hep.dataforge.names.Names;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

/**
 * Random Gaussian vector generator
 *
 * @author darksnake
 */
public class GaussianParameterGenerator implements ParameterGenerator {

    private final MultivariateNormalDistribution distribution;
    private final Names names;

    public GaussianParameterGenerator(MultivariateNormalDistribution distribution, Names names) {
        this.distribution = distribution;
        this.names = names;
    }

    /**
     * Create new Gaussian generator. The name set is taken from means and must
     * be present in the covariance
     *
     * @param means
     * @param covariance
     */
    public GaussianParameterGenerator(NamedDoubleSet means, NamedMatrix covariance) {
        if (!covariance.names().contains(means.names())) {
            throw new IllegalArgumentException("Covariance names must include average values names");
        }

        this.names = means.names();
        distribution = new MultivariateNormalDistribution(means.getValues(),
                covariance.getNamedSubMatrix(names.asArray()).getMatrix().getData());
    }

    /**
     * Create new Gaussian generator from given parameter set and covariance.
     * The covariance is optional. If not present, than errors are taken from
     * parameter set.
     *
     * @param means
     * @param covariance
     */
    public GaussianParameterGenerator(ParamSet means, NamedMatrix covariance) {
        NamedMatrix matrix = NamedMatrix.diagonal(means.getParErrors());
        if (covariance != null) {
            matrix.setValuesFrom(covariance);
        }

        this.names = means.names();
        distribution = new MultivariateNormalDistribution(means.getValues(), matrix.getMatrix().getData());
    }

    @Override
    public NamedDoubleSet generate() {
        double[] vector = distribution.sample();
        return new NamedDoubleArray(names, vector);
    }

}
