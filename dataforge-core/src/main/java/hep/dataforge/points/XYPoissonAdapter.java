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
package hep.dataforge.points;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;

/**
 * Specialized adapter for poissonian distributed values
 * @author darksnake
 */
public class XYPoissonAdapter extends XYAdapter {

    public XYPoissonAdapter(Meta adapterAnnotation) {
        super(adapterAnnotation);
    }

    public XYPoissonAdapter(String xName, String yName) {
        super(xName, yName);
    }

    @Override
    public boolean providesYError(DataPoint point) {
        return super.providesYError(point) || getY(point).doubleValue()>0;
    }

    @Override
    public Value getYerr(DataPoint point) throws NameNotFoundException {
        if(super.providesYError(point)){
            return super.getYerr(point);
        } else {
            double y = getY(point).doubleValue();
            if(y>0){
                return Value.of(Math.sqrt(y));
            }
        }
        return super.getYerr(point);
    }
    
    
    
}
