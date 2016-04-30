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
package hep.dataforge.fitting;

import static hep.dataforge.context.GlobalContext.out;
import hep.dataforge.datafitter.FitManager;
import hep.dataforge.datafitter.FitSource;
import hep.dataforge.datafitter.FitState;
import hep.dataforge.datafitter.Hessian;
import hep.dataforge.datafitter.ParamSet;
import hep.dataforge.datafitter.models.XYModel;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.functions.ParametricFunction;
import hep.dataforge.io.PrintNamed;
import hep.dataforge.maths.GridCalculator;
import hep.dataforge.maths.MatrixOperations;
import hep.dataforge.maths.NamedDoubleArray;
import hep.dataforge.maths.NamedDoubleSet;
import hep.dataforge.maths.NamedMatrix;
import hep.dataforge.names.AbstractNamedSet;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.ListTable;
import hep.dataforge.tables.Table;
import hep.dataforge.tables.XYAdapter;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 *
 * @author Darksnake
 */
public class GaussianSpectrum extends AbstractNamedSet implements ParametricFunction {

    private static final String[] list = {"w", "pos", "amp"};

    public static FitState fit(Table data, ParamSet pars, String engine) {
        FitManager fm = new FitManager();
        XYModel model = new XYModel("gaussian", new GaussianSpectrum());
        FitState state = new FitState(data, model, pars);

        return fm.runTask(state, engine, "run", "pos");
    }

    public static void printInvHessian(Table data, ParamSet pars) {
        XYModel model = new XYModel("gaussian", new GaussianSpectrum());
        FitSource fs = new FitSource(data, model);
        NamedMatrix h = Hessian.getHessian(fs.getLogLike(), pars, pars.namesAsArray());
        NamedMatrix hInv = new NamedMatrix(MatrixOperations.inverse(h.getMatrix()), pars.namesAsArray());
        PrintNamed.printNamedMatrix(out(), hInv);
    }
    private final RandomGenerator rnd;

    public GaussianSpectrum() {

        super(list);
        rnd = new JDKRandomGenerator();
    }

    @Override
    public double derivValue(String parName, double x, NamedDoubleSet set) {
        double pos = set.getValue("pos");
        double w = set.getValue("w");
        double dif = x - pos;
        switch (parName) {
            case "pos":
                return this.value(x, set) * dif / w / w;
            case "w":
                return value(x, set) / w * (dif * dif / w / w - 1);
            case "amp":
                return value(x, set) / set.getValue("amp");
            default:
                throw new NameNotFoundException(parName);
        }

    }

    @Override
    public boolean providesDeriv(String name) {
        return this.names().contains(name);
    }

    public Table sample(double pos, double w, double amp, double a, double b, int number) {
        XYAdapter factory = new XYAdapter();
        ListTable.Builder data = new ListTable.Builder();
        double[] v = new double[3];
        v[0] = w;
        v[1] = pos;
        v[2] = amp;
        NamedDoubleArray vector = new NamedDoubleArray(list, v);
        double[] grid = GridCalculator.getUniformUnivariateGrid(a, b, number);
        for (double d : grid) {
            double value = this.value(d, vector);
            double error = Math.sqrt(value);
            double randValue = Math.max(0, rnd.nextGaussian() * error + value);
            DataPoint p = factory.buildXYDataPoint(d, randValue, Math.max(Math.sqrt(randValue), 1d));
            data.addRow(p);
        }
        return data.build();
    }

    @Override
    public double value(double x, NamedDoubleSet set) {
        double pos = set.getValue("pos");
        double w = set.getValue("w");
        double amp = set.getValue("amp");
        double dif = x - pos;
        return amp * 1 / Math.sqrt(2 * Math.PI) / w * Math.exp(-dif * dif / 2 / w / w);
    }

}
