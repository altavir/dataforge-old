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
package hep.dataforge.storage.jcr;

import hep.dataforge.data.DataPoint;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.values.Value;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.exceptions.JCRStorageException;
import hep.dataforge.storage.api.PushResult;
import static hep.dataforge.storage.loader.TimeSeriesProvider.TIMESTAMP;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;


public class JCRTimeSeriesLoader extends JCRLoader{
    
    private static final String LAST_UPDATE_PROPERTY = "lastupdate";

    public JCRTimeSeriesLoader(JCRStorage server, Node dataSetNode) {
        super(server, dataSetNode);
    }

    public JCRTimeSeriesLoader(JCRStorage server, String parentPath, String name) throws JCRStorageException {
        super(server, parentPath, name);
    }

    @Override
    protected String getDataPointNodeType() {
        return "df:tdp";
    }

    @Override
    protected String getDataSetNodeType() {
        return "df:tdata";
    }


    @Override
    public PushResult push(DataPoint dp) {
        PushResult pr =  tryPush();
        if(!pr.isSuccsess()){
            return pr;
        }        
        
        //Проверяем, что точка содержит информацию о времени
        if(!dp.names().contains(TIMESTAMP)){
            return new PushNodeResult(new IllegalArgumentException("DataPoint does not contain time parameter"));
        }
        PushResult res = super.push(dp);
        if(res.isSuccsess()){
            try {
                dataSetNode.setProperty(LAST_UPDATE_PROPERTY, time2long(dp.getValue(TIMESTAMP).timeValue()));
            } catch (NameNotFoundException | RepositoryException ex) {
                return new PushNodeResult(ex);
            }
        }
        return res;
    }

    private long time2long(Instant time){
        return time.toEpochMilli();
    }
    
    private Instant long2time(long time){
        return Instant.ofEpochMilli(time);
    }
    
    @Override
    protected DataPoint pull(Node node) throws RepositoryException {
        PropertyIterator iterator = node.getProperties();
        Map<String, Value> map = new HashMap<>();
        for (int i = 0; i < iterator.getSize(); i++) {
            Property p = iterator.nextProperty();
            if (!p.getName().startsWith("jcr")) {
                String pName = p.getName();
                if(pName.equals(TIMESTAMP)){
                    map.put(TIMESTAMP, Value.of(long2time(p.getValue().getLong())));
                } else {
                    map.put(pName, getDFValue(p.getValue()));
                }
            }
        }
        MapDataPoint dp = new MapDataPoint(map);

        return dp;
    }    
    
//    @Override
//    public DataPoint get(Instant time) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public DataPoint getLast() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public List<DataPoint> updateFrom(Instant from) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    
}
