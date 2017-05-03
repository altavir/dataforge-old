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
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.tables.ListOfPoints;
import hep.dataforge.tables.NavigablePointSource;
import hep.dataforge.utils.MetaMorph;
import hep.dataforge.utils.SimpleMetaMorph;

import java.io.PrintWriter;
import java.util.Optional;

import static hep.dataforge.io.FittingIOUtils.printParamSet;

/**
 * A metamorph representation of fit result
 *
 * @author Alexander Nozik
 */
public class FitResult extends SimpleMetaMorph {

    private transient FitState state = null;

    public static FitResult build(FitState state, boolean valid, String... freeParameters) {
        FitResult res = new FitResult();
        res.state = state;
        MetaBuilder builder = new MetaBuilder("fitResult")
                .setNode("data", new ListOfPoints(state.getPoints()).toMeta())//setting data
                .setValue("dataSize", state.getDataSize())
                .setNode("params", state.getParameters().toMeta())
                .setValue("chi2", state.getChi2())
                .setValue("isValid", valid);

        //TODO add residuals to data
        if (freeParameters.length == 0) {
            builder.setValue("freePars", freeParameters);
        } else {
            builder.setValue("freePars", state.getParameters().namesAsArray());
        }

        //FIXME add covariance
//        if (state.hasCovariance()) {
//            builder.setNode("covariance", state.getCovariance().toMeta());
//        }

        //FIXME add interval estimate

        //setting model
        if (state.getModel() instanceof MetaMorph) {
            builder.setNode("model", ((MetaMorph) state.getModel()).toMeta());
        }


        res.setMeta(builder.build());
        return res;
    }

    public static FitResult build(FitState state, String... freeParameters) {
        return build(state, true, freeParameters);
    }

    public ParamSet getParameters() {
        return MetaMorph.morph(ParamSet.class, meta().getMeta("params"));
    }

    public String[] getFreePars() {
        return meta().getStringArray("freePars");
    }

    public boolean isValid() {
        return meta().getBoolean("isValid", true);
    }


    public int ndf() {
        return this.getDataSize() - this.getFreePars().length;
    }

    private int getDataSize() {
        return meta().getInt("dataSize");
    }

    public double normedChi2() {
        return getChi2() / ndf();
    }

    private double getChi2() {
        return meta().getDouble("chi2");
    }

    public Optional<FitState> getState() {
        return Optional.ofNullable(state);
    }

    public NavigablePointSource getData(){
        return MetaMorph.morph(ListOfPoints.class,meta().getMeta("data"));
    }

    /**
     * TODO replace by Markup
     *
     * @param out
     */
    @Deprecated
    public void printState(PrintWriter out) {
        out.println("***FITTING RESULT***");
        this.printAllValues(out);
        this.printFitParsValues(out);

        getState().ifPresent(state -> {
            if (state.hasCovariance()) {
                out.println();
                out.println("Correlation matrix:");
                FittingIOUtils.printNamedMatrix(out, state.getCorrelationMatrix());
            }

            state.getIntervalEstimate().ifPresent(
                    intervalEstimate -> intervalEstimate.print(out)
            );

        });

        out.println();
        double chi2 = getChi2();
        out.printf("Chi squared over degrees of freedom: %g/%d = %g", chi2, this.ndf(), chi2 / this.ndf());
        out.println();
        out.flush();
    }

    @Deprecated
    private void printAllValues(PrintWriter out) {
        out.println();
        out.println("All function parameters are: ");
        printParamSet(out, getParameters());
    }

    @Deprecated
    public void printCovariance(PrintWriter out) {
        getState().ifPresent(state -> {
            if (state.getCovariance() != null) {
                out.println();
                out.printf("%n***COVARIANCE***%n");

                FittingIOUtils.printNamedMatrix(out, state.getCovariance());

            }

        });
    }

    @Deprecated
    private void printFitParsValues(PrintWriter out) {
        out.println();
        out.println("The best fit values are: ");
        printParamSet(out, getParameters().getSubSet(getFreePars()));
    }
}
