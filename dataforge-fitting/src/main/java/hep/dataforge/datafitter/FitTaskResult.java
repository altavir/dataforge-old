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
package hep.dataforge.datafitter;

import static hep.dataforge.io.FittingIOUtils.printParamSet;
import static hep.dataforge.io.PrintNamed.printNamedMatrix;
import hep.dataforge.maths.NamedMatrix;
import java.io.PrintWriter;

/**
 * <p>
 * FitTaskResult class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class FitTaskResult extends FitState {
    
    public static FitTask emptyTask(String name){
        return new FitTask(name);
    }

    /**
     * <p>
     * Constructor for FitTaskResult.</p>
     *
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param allPars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @param lastTask a {@link hep.dataforge.datafitter.FitTask} object.
     * @return a {@link hep.dataforge.datafitter.FitTaskResult} object.
     */
    public static FitTaskResult buildResult(FitState state, FitTask lastTask, ParamSet allPars) {
        return new FitTaskResult(state.edit().setPars(allPars).build(), lastTask);
    }

    /**
     * <p>
     * Constructor for FitTaskResult.</p>
     *
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param allPars a {@link hep.dataforge.datafitter.ParamSet} object.
     * @param covariance a {@link hep.dataforge.maths.NamedMatrix} object.
     * @param lastTask a {@link hep.dataforge.datafitter.FitTask} object.
     * @return a {@link hep.dataforge.datafitter.FitTaskResult} object.
     */
    public static FitTaskResult buildResult(FitState state, FitTask lastTask, ParamSet allPars, NamedMatrix covariance) {
        return new FitTaskResult(state.edit().setPars(allPars).setCovariance(covariance, true).build(), lastTask);
    }

    /**
     * <p>
     * Constructor for FitTaskResult.</p>
     *
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param covariance a {@link hep.dataforge.maths.NamedMatrix} object.
     * @param lastTask a {@link hep.dataforge.datafitter.FitTask} object.
     * @return a {@link hep.dataforge.datafitter.FitTaskResult} object.
     */
    public static FitTaskResult buildResult(FitState state, FitTask lastTask, NamedMatrix covariance) {
        return new FitTaskResult(state.edit().setCovariance(covariance, true).build(), lastTask);
    }
    private boolean isValid = true;
    private final FitTask lastTask;

    /**
     * <p>
     * Constructor for FitTaskResult.</p>
     *
     * @param state a {@link hep.dataforge.datafitter.FitState} object.
     * @param lastTask a {@link hep.dataforge.datafitter.FitTask} object.
     */
    public FitTaskResult(FitState state, FitTask lastTask) {
        super(state);
        this.lastTask = lastTask;
    }

    /**
     * <p>
     * getFreePars.</p>
     *
     * @return the fitPars
     */
    public String[] getFreePars() {
        String[] res = lastTask.getFreePars();
        if (res == null || res.length == 0) {
            return getModel().namesAsArray();
        } else {
            return res;
        }
    }

    /**
     * <p>
     * isValid.</p>
     *
     * @return the isValid
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * <p>
     * setValid.</p>
     *
     * @param isValid the isValid to set
     */
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * <p>
     * ndf.</p>
     *
     * @return a int.
     */
    public int ndf() {
        return this.getDataSize() - this.getFreePars().length;
    }

    /** {@inheritDoc} */
    @Override
    public void print(PrintWriter out) {
        out.println("***FITTING RESULT***");
        this.printAllValues(out);
        this.printFitParsValues(out);
        if (covariance != null) {
            out.println();
            out.println("Corellation marix:");
            printNamedMatrix(out, getCorrelationMatrix());
        }
        if (this.getIntervalEstimate() != null) {
            this.getIntervalEstimate().print(out);
        }
        out.println();
        double chi2 = getChi2();
        out.printf("Chi squared over degrees of freedom: %g/%d = %g", chi2, this.ndf(), chi2 / this.ndf());
        out.println();
        out.flush();
    }

    private void printFitParsValues(PrintWriter out) {
        out.println();
        out.println("The best fit values are: ");
        printParamSet(out, getParameters().getSubSet(getFreePars()));
    }

}
