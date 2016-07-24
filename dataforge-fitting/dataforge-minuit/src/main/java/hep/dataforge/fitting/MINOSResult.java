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

import hep.dataforge.fitting.IntervalEstimate;
import hep.dataforge.names.AbstractNamedSet;
import hep.dataforge.names.Names;
import java.io.PrintWriter;

/**
 * Контейнер для несимметричных оценок и доверительных интервалов
 *
 * @author Darksnake
 * @version $Id: $Id
 */
public class MINOSResult extends AbstractNamedSet implements IntervalEstimate {

    private double[] errl;
    private double[] errp;

    /**
     * <p>Constructor for MINOSResult.</p>
     *
     * @param named a {@link hep.dataforge.names.Names} object.
     */
    public MINOSResult(Names named) {
        super(named);
    }

    /**
     * <p>Constructor for MINOSResult.</p>
     *
     * @param list an array of {@link java.lang.String} objects.
     */
    public MINOSResult(String[] list) {
        super(list);
    }

    /** {@inheritDoc} */
    @Override
    public void print(PrintWriter out) {
        if ((this.errl != null) || (this.errp != null)) {
            out.println();
            out.println("Assymetrical errors:");
            out.println();
            out.println("Name\tLower\tUpper");
            for (int i = 0; i < this.getDimension(); i++) {
                out.print(this.names().getName(i));
                out.print("\t");
                if (this.errl != null) {
                    out.print(this.errl[i]);
                } else {
                    out.print("---");
                }
                out.print("\t");
                if (this.errp != null) {
                    out.print(this.errp[i]);
                } else {
                    out.print("---");
                }
                out.println();
            }
        }
    }

    /**
     * <p>setAssimetricalErrors.</p>
     *
     * @param errl an array of double.
     * @param errp the errp to set
     */
    public void setAssimetricalErrors(double[] errl, double[] errp) {
        this.errp = errp.clone();
        this.errl = errl.clone();
    }
}
