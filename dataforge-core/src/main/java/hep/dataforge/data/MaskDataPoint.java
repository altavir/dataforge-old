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
package hep.dataforge.data;

import hep.dataforge.values.Value;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.Names;
import java.util.Map;

/**
 * <p>MaskDataPoint class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class MaskDataPoint implements DataPoint {

    private final Map<String, String> nameMap;
    private final DataPoint source;
    private final Names names;

    /**
     * <p>Constructor for MaskDataPoint.</p>
     *
     * @param source a {@link hep.dataforge.data.DataPoint} object.
     * @param nameMap a {@link java.util.Map} object.
     */
    public MaskDataPoint(DataPoint source, Map<String, String> nameMap) {
        this.source = source;
        this.nameMap = nameMap;
        names = Names.of(nameMap.keySet());
    }

    /** {@inheritDoc}
     * @return  */
    @Override
    public MaskDataPoint copy(){
        return new MaskDataPoint(source.copy(), nameMap);
    }

        /** {@inheritDoc}
     * @return  */
    @Override
    public int getDimension() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasValue(String path) {
        return nameMap.containsKey(path);
    }

    /** {@inheritDoc}
     * @return  */
    @Override
    public Names names() {
        return names;
    }


    /** {@inheritDoc} */
    @Override
    public Value getValue(String name) throws NameNotFoundException {
        return source.getValue(nameMap.get(name));
    }

}
