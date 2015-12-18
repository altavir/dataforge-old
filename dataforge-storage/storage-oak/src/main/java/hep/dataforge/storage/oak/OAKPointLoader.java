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
package hep.dataforge.storage.oak;

import hep.dataforge.annotations.Annotation;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.DataSet;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.StorageQueryException;
import hep.dataforge.storage.api.Query;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.common.AbstractPointLoader;
import hep.dataforge.values.Value;
import java.util.Iterator;
import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.spi.state.ChildNodeEntry;
import org.apache.jackrabbit.oak.spi.state.NodeState;

/**
 *
 * @author Alexander Nozik
 */
public class OAKPointLoader extends AbstractPointLoader implements Iterable<DataPoint> {

    public static final String POINT_NODE_NAME = "point";
    public static final String INDEX_NODE_NAME = "@index";

    NodeState node;

    public OAKPointLoader(Storage storage, String name, Annotation annotation) {
        super(storage, name, annotation);
    }

    @Override
    public boolean isEmpty() {
        return !node.hasChildNode(POINT_NODE_NAME);
    }

    @Override
    public DataSet asDataSet() throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataPoint pull(Value value) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataSet pull(Value from, Value to, int maxItems) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void push(DataPoint dp) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataSet pull(Query query) throws StorageQueryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Iterator<DataPoint> iterator() {
        return new Iterator<DataPoint>() {
            Iterator<? extends ChildNodeEntry> it = node.getChildNodeEntries().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public DataPoint next() {
                ChildNodeEntry state = it.next();
                if (state.getName().startsWith(POINT_NODE_NAME)) {
                    return readDataPoint(state.getNodeState());
                } else {
                    return null;
                }
            }
        };
    }

    private DataPoint readDataPoint(NodeState node){
        MapDataPoint point = new MapDataPoint();
        for (PropertyState pr : node.getProperties()) {
            point.putValue(pr.getName(), OAKUtils.valueOf(pr));
        }
        return point;
    }

}
