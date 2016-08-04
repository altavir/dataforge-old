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
package hep.dataforge.stat.likelihood;

import hep.dataforge.stat.likelihood.Marginalizer;
import hep.dataforge.stat.likelihood.ScaleableNamedFunction;
import hep.dataforge.stat.likelihood.NamedGaussianPDFLog;
import hep.dataforge.maths.NamedVector;
import hep.dataforge.maths.NamedMatrix;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Alexander Nozik
 */
public class MarginalizerTest {

    static final String[] nameList = {"par1", "par2", "par3"};

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
    Marginalizer instance;
    ScaleableNamedFunction testFunc;

    /**
     *
     */
    public MarginalizerTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        double[] d = {1d, 2d, 0.5d};
        RealMatrix mat = new DiagonalMatrix(d);

        NamedMatrix cov = new NamedMatrix(mat, nameList);
        testFunc = new NamedGaussianPDFLog(cov);
        ArrayRealVector vector = new ArrayRealVector(cov.getDimension());
        NamedVector zero = new NamedVector(nameList, vector);
        RandomGenerator generator = new JDKRandomGenerator();
        generator.setSeed(54321);
        instance = new Marginalizer(cov, testFunc, zero,generator);
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of getMarginalValue method, of class Marginalizer.
     */
    @Test
    public void testGetMarginalValue() {
        System.out.println("getMarginalValue");
        int maxCalls = 1000;
        double expResult = 1 / (2 * Math.PI)/sqrt(2d);
        double result = instance.getMarginalValue(maxCalls, "par2");
        assertEquals(expResult, result, 0.01);
        System.out.printf("The expected value is %g, the test result is %g%n", expResult, result);
        System.out.printf("On %d calls the relative discrepancy is %g%n", maxCalls, abs(result - expResult) / result);
    }

    /**
     * Test of getNorm method, of class Marginalizer.
     */
    @Test
    public void testGetNorm() {
        System.out.println("getNorm");
        int maxCalls = 10000;
        double expResult = 1.0;
        double result = instance.getNorm(maxCalls);
        assertEquals(expResult, result, 0.05);
        System.out.printf("The expected value is %g, the test result is %g%n", expResult, result);
        System.out.printf("On %d calls the relative discrepancy is %g%n", maxCalls, abs(result - expResult) / result);
    }
}