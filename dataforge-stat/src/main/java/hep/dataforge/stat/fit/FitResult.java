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

import hep.dataforge.io.FittingIOUtils;
import hep.dataforge.maths.NamedMatrix;

import java.io.PrintWriter;

import static hep.dataforge.io.FittingIOUtils.printParamSet;

/**
 * An extension of FitState containing information about last fit stage
 * @author Alexander Nozik
 */
public class FitResult extends FitState {
    
    private final FitStage lastTask;
    private boolean isValid = true;

    public FitResult(FitState state, FitStage lastTask) {
        super(state);
        this.lastTask = lastTask;
    }

    public static FitStage emptyTask(String name){
        return new FitStage(name);
    }

    public static FitResult buildResult(FitState state, FitStage lastTask, ParamSet allPars) {
        return new FitResult(state.edit().setPars(allPars).build(), lastTask);
    }

    public static FitResult buildResult(FitState state, FitStage lastTask, ParamSet allPars, NamedMatrix covariance) {
        return new FitResult(state.edit().setPars(allPars).setCovariance(covariance, true).build(), lastTask);
    }

    public static FitResult buildResult(FitState state, FitStage lastTask, NamedMatrix covariance) {
        return new FitResult(state.edit().setCovariance(covariance, true).build(), lastTask);
    }

    public String[] getFreePars() {
        String[] res = lastTask.getFreePars();
        if (res == null || res.length == 0) {
            return getModel().namesAsArray();
        } else {
            return res;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public int ndf() {
        return this.getDataSize() - this.getFreePars().length;
    }

    /** {@inheritDoc} */
    @Override
    public void print(PrintWriter out) {
        out.println("***FITTING RESULT***");
        this.printAllValues(out);
        this.printFitParsValues(out);
        if (hasCovariance()) {
            out.println();
            out.println("Corellation marix:");
            FittingIOUtils.printNamedMatrix(out, getCorrelationMatrix());
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
