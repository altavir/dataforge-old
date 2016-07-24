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
package hep.dataforge.fitting.parametric;

import hep.dataforge.fitting.parametric.ParametricValue;
import hep.dataforge.fitting.ParamSet;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.providesValidDerivative;
import hep.dataforge.fitting.likelihood.NamedGaussianPDFLog;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.maths.NamedMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import static org.apache.commons.math3.util.MathArrays.ebeMultiply;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static org.junit.Assert.assertTrue;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;
import static hep.dataforge.fitting.parametric.DerivativeCalculator.calculateDerivative;

/**
 *
 * @author Alexander Nozik
 */
public class DerivativeCalculatorTest {
    
    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
    }
    
    /**
     *
     */
    @AfterClass
    public static void tearDownClass() {
    }
    
    /**
     *
     */
    public DerivativeCalculatorTest() {
        }
    
    /**
     *
     */
    @Before
    public void setUp() {
    }
    
    /**
     *
     */
    @After
    public void tearDown() {
    }


    /**
     * Test of providesValidDerivative method, of class DerivativeCalculator.
     */
    @Test
    public void testProvidesValidDerivative() {
        String[] names = {"par1","par2","par3"};
        double[] zero = {-2d,0.5d,0d};
        double[] sigmas = {1d,2d,0.5d};
        double[] diag = ebeMultiply(sigmas, sigmas);
        RealMatrix matrix = new DiagonalMatrix(diag);
        
        
        NamedMatrix cov = new NamedMatrix(matrix, names);
        NamedVector values = new NamedVector(names, zero);
        NamedVector errors = new NamedVector(names, sigmas);
        ParamSet set = new ParamSet(names);
        set.setParValues(values);
        set.setParErrors(errors);
        
        System.out.println("providesValidDerivative");
        ParametricValue function = new NamedGaussianPDFLog(cov);
        double tolerance = 1e-4;
        String parName = "par2";
        double realDerivative = function.derivValue(parName, set);
        double calculatedDerivative = calculateDerivative(function, set, parName);
        boolean result = providesValidDerivative(function, set, tolerance, parName);
        System.out.printf("True derivative value is %g, calculated derivativeValue is %g%n", 
                realDerivative,calculatedDerivative);
        assertTrue(result);

    }

}