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
package hep.dataforge.likelihood;

import hep.dataforge.datafitter.IntervalEstimate;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.math3.util.Pair;

/**
 * Bayesian combined confidence limit for one parameter
 * @author Alexander Nozik
 */
class BayesianConfidenceLimit implements IntervalEstimate{
    String[] freePars;
    private final HashMap<Double,Pair<Double,Double>> limits;
    String parName;

    BayesianConfidenceLimit(String parName, String[] freePars) {
        this.limits = new HashMap<>();
        this.parName = parName;
        this.freePars = freePars;
    }

    BayesianConfidenceLimit() {
        this.limits = new HashMap<>();
    }
    
    void add(Double cl, Pair<Double,Double> limit){
        this.limits.put(cl, limit);
    }
    
    /** {@inheritDoc} */
    @Override
    public void print(PrintWriter out) {
        if(this.parName!=null) {
            out.printf("Combined marginal likelihood confidence limits for paramteter \'%s\'.%n", parName);
        }
        if(this.freePars !=null) {
            out.printf("The marginalization parameters are:%n%s%n%n", Arrays.toString(freePars));
        }
        out.printf("%s\t%-8s\t%-8s%n","CL ","Lower","Upper");
        limits.entrySet().stream().forEach((entry) -> {
            out.printf("%2.2g%%\t%8.8g\t%8.8g%n", entry.getKey()*100,
                    entry.getValue().getFirst(),entry.getValue().getSecond());
        });
        out.println();
    }
    

    
}
