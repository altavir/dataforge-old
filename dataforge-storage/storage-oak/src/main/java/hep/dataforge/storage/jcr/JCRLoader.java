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

import hep.dataforge.annotations.Annotation;
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.values.Value;
import hep.dataforge.exceptions.JCRStorageException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.PushResult;
import hep.dataforge.storage.api.Storage;
import static hep.dataforge.storage.api.Storage.DATA_PATH;
import hep.dataforge.storage.loader.AbstractDataLoader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.apache.jackrabbit.commons.JcrUtils;

/**
 * Позволяет осуществлять загрузку и выгрузку точек из репозитория //FIXME
 * скорость быстро падает при росте количества элементов. Сделать сигментацию?
 *
 * @author Darksnake
 */
public class JCRLoader extends AbstractDataLoader<DataPoint> {

    private static String DATAPOINT_NODE_NAME = "dp";
    static String DATAPOINT_TAGS = "df:tags";
    private static String SEGMENT_NODE_NAME = "segment";
    private static String SEGMENT_REFERENCE_PROPERTY = "cursegment";
    private static int maxSegmentSize = 1000;

    protected Node dataSetNode;
    protected Node segmentNode;

    public JCRLoader(Storage server, Node dataSetNode) {
        super(server);
        this.dataSetNode = dataSetNode;
    }

    /**
     *
     * @param server
     * @param parentPath - absolute path to dataSetNode element of the dataset.
     * Parent element must exist
     * @param name - name of the dataSet element
     * @throws JCRStorageException
     */
    public JCRLoader(JCRStorage server, String parentPath, String name) throws JCRStorageException {
        super(server);
        try {
            this.dataSetNode = buildDataSetNode(server.getSession(), DATA_PATH + parentPath, name);
            this.segmentNode = getSegmentNode();
        } catch (RepositoryException ex) {
            throw new JCRStorageException(ex);
        }
    }
    
    public static JCRLoader getDataSetLoader(JCRStorage server, Annotation config) {
        String path = config.getString("path", "/default");
        String name = config.getString("name");
        try {
            return new JCRLoader(server, path, name);
        } catch (JCRStorageException ex) {
            throw new RuntimeException(ex);
        }
    }    

    private Node getSegmentNode() throws RepositoryException {
        if (dataSetNode.hasProperty(SEGMENT_REFERENCE_PROPERTY)) {
            return dataSetNode.getProperty(SEGMENT_REFERENCE_PROPERTY).getNode();
        } else {
            return dataSetNode.addNode(SEGMENT_NODE_NAME);
        }
    }

    private int segmentSize() throws RepositoryException {
        if (segmentNode == null) {
            throw new RuntimeException("Can't find segment node");
        }
        return (int) segmentNode.getNodes().getSize();
    }



    private Node buildDataSetNode(Session session, String parentPath, String name) throws RepositoryException {
        Node node = JcrUtils.getOrCreateByPath(parentPath, "nt:unstructured", session);
        if (node.hasNode(name)) {
            return node.getNode(name);
        } else {
            return node.addNode(name, getDataSetNodeType());
        }
    }

    protected String getDataSetNodeType() {
        return "df:data";
    }

    protected Value getDFValue(javax.jcr.Value value) throws RepositoryException {
        switch (value.getType()) {
            case PropertyType.DOUBLE:
                return Value.of(value.getDouble());
            case PropertyType.DECIMAL:
                return Value.of(value.getDecimal());
            case PropertyType.LONG:
                return Value.of(value.getLong());
            default:
                return Value.of(value.getString());
        }
    }

    private javax.jcr.Value getJCRValue(DataPoint point, String name) throws RepositoryException {
        Value value = point.getValue(name);
        return getJCRValue(value);
    }

    protected javax.jcr.Value getJCRValue(Value value) throws RepositoryException {
        ValueFactory factory = dataSetNode.getSession().getValueFactory();
        switch (value.valueType()) {
            case NUMBER:
                Number num = value.numberValue();
                if (num instanceof Integer) {
                    return factory.createValue(num.intValue());
                } else if (num instanceof Long) {
                    return factory.createValue(num.longValue());
                } else if (num instanceof BigDecimal) {
                    return factory.createValue((BigDecimal)num);
                } else {
                    return factory.createValue(num.doubleValue());
                }
            case STRING:
                return factory.createValue(value.stringValue());
            case TIME:
                return factory.createValue(value.longValue());
            default:
                return factory.createValue(value.stringValue());
        }
    }

    /**
     * Возвращаем текущую ноду сегмента. Если она переполнена, создаем новую.
     *
     * @return
     * @throws RepositoryException
     */
    private Node getParentNode() throws RepositoryException {
        if (segmentSize() <= maxSegmentSize) {
            return segmentNode;
        } else {
            segmentNode = dataSetNode.addNode(SEGMENT_NODE_NAME);
            segmentNode.addMixin("mix:referenceable");
            System.out.printf("Segment with name '%s' is created%n", segmentNode.getName());
            dataSetNode.setProperty(SEGMENT_REFERENCE_PROPERTY, segmentNode);
            segmentNode.getSession().save();
            segmentNode.getSession().refresh(true);
            return segmentNode;
        }
    }

    protected String getPointNodePath(int number) {
        return String.format("%s[%d]", DATAPOINT_NODE_NAME, number);
    }

    @Override
    public Iterator<DataPoint> iterator() {
        try {
            final NodeIterator nodeIterator = getParentNode().getNodes(DATAPOINT_NODE_NAME);

            return new Iterator<DataPoint>() {

                @Override
                public boolean hasNext() {
                    return nodeIterator.hasNext();
                }

                @Override
                public DataPoint next() {
                    try {
                        return pull(nodeIterator.nextNode());
                    } catch (RepositoryException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            };

        } catch (RepositoryException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected DataPoint pull(Node node) throws RepositoryException {
        PropertyIterator iterator = node.getProperties();
        Map<String, Value> map = new HashMap<>();
        for (int i = 0; i < iterator.getSize(); i++) {
            Property p = iterator.nextProperty();
            if (!p.getName().startsWith("jcr")) {
                String pName = p.getName();
                map.put(pName, getDFValue(p.getValue()));
            }
        }
        MapDataPoint dp = new MapDataPoint(map);

        return dp;
    }

    /**
     * Вытягивает точку, используя запрос JCR - XPATH. На данном этапе
     * используется строгое соответствие ключа
     *
     * @param name
     * @param value
     * @return
     * @throws StorageException
     */
    @Override
    public DataPoint pullSingle(String name, Value value) throws StorageException {
        if (name == null) {
            throw new StorageException("The key field is not defined");
        }
        try {
            String qs = String.format("/jcr:root%s//element(*,%s)[@%s=%f]",
                    dataSetNode.getPath(), getDataPointNodeType(), name, value.doubleValue());

            List<DataPoint> res = pullXPath(qs);
            if (!res.isEmpty()) {
                return res.get(0);
            } else {
                //FIXME сделать логирование ошибки
                return null;
            }

        } catch (RepositoryException ex) {
            throw new JCRStorageException(ex);
        }
    }

    @Override
    public List<DataPoint> pullRange(String name, Value from, Value to) throws StorageException {
        try {
            if (name == null) {
                throw new StorageException("The key field is not defined");
            }
            String qs = String.format("/jcr:root%s//element(*,%s)[@%s>=%f and @%s<=%f]",
                    dataSetNode.getPath(), getDataPointNodeType(), name, from.doubleValue(), name, to.doubleValue());

            return pullXPath(qs);
        } catch (RepositoryException ex) {
            throw new JCRStorageException(ex);
        }
    }

    public List<DataPoint> pullXPath(String query) throws StorageException {
        try {
            QueryResult qr = execXPath(query);
            NodeIterator ni = qr.getNodes();
            List<DataPoint> res = new ArrayList<>((int) ni.getSize());
            while (ni.hasNext()) {
                res.add(pull(ni.nextNode()));
            }
            return res;
        } catch (RepositoryException ex) {
            throw new JCRStorageException(ex);
        }
    }

    private QueryResult execXPath(String qs) throws RepositoryException {
        Session session = dataSetNode.getSession();
        QueryManager qm = session.getWorkspace().getQueryManager();

        Query query = qm.createQuery(qs, "xpath");

        // Execute the query and get the results ...
        return query.execute();
    }

    /**
     * Не стоит злоупотреблять этим методом, он может быть весьма медленным
     *
     * @return
     */
    public long size() {
        try {
            String qs = String.format("/jcr:root%s//element(*,%s)",
                    dataSetNode.getPath(), getDataPointNodeType());

            QueryResult result = execXPath(qs);

            NodeIterator ni = result.getNodes();
            return ni.getSize();
        } catch (RepositoryException ex) {
            throw new Error(ex);
        }
    }

    protected String getDataPointNodeType() {
        return "df:dp";
    }

    @Override
    public PushResult push(DataPoint dp) {
        PushResult pr = tryPush();
        if (!pr.isSuccsess()) {
            return pr;
        }

        try {
            String nodeName = DATAPOINT_NODE_NAME;
            Node res = getParentNode().addNode(nodeName, getDataPointNodeType());
            for (String name : dp.names()) {
                javax.jcr.Value val = getJCRValue(dp, name);
                res.setProperty(name, val);
            }
            res.getSession().save();
            return new PushNodeResult(res);
        } catch (RepositoryException ex) {
            return new PushNodeResult(ex);
        }
    }

}
