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
package hep.dataforge.plots.fx;

import hep.dataforge.utils.DateTimeUtils;

import java.time.Instant;

/**
 *
 * @author Alexander Nozik
 */
public class FXTimeAxis extends FXObjectAxis<Instant> {

    public FXTimeAxis(Instant lower, Instant upper) {
        super(lower, upper);
    }

    public FXTimeAxis() {
        super(DateTimeUtils.now(), DateTimeUtils.now().plusSeconds(60));
    }

    @Override
    protected String getTickMarkLabel(Instant value) {
        return value.toString();
    }

    @Override
    public double toNumericValue(Instant value) {
        return value.toEpochMilli();
    }

    @Override
    public Instant toRealValue(double value) {
        return Instant.ofEpochMilli((long) value);
    }

}
