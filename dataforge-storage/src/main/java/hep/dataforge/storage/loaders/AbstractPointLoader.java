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
package hep.dataforge.storage.loaders;

import hep.dataforge.points.DataPoint;
import static hep.dataforge.points.DataPoint.fromMeta;
import hep.dataforge.points.PointListener;
import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.WrongTargetException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.commons.MessageFactory;
import hep.dataforge.storage.commons.StorageMessageUtils;
import static hep.dataforge.storage.commons.StorageMessageUtils.ACTION_KEY;
import static hep.dataforge.storage.commons.StorageMessageUtils.PULL_OPERATION;
import static hep.dataforge.storage.commons.StorageMessageUtils.PUSH_OPERATION;
import static hep.dataforge.storage.commons.StorageMessageUtils.QUERY_ELEMENT;
import static hep.dataforge.storage.commons.StorageMessageUtils.confirmationResponse;
import hep.dataforge.values.Value;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Alexander Nozik
 */
public abstract class AbstractPointLoader extends AbstractLoader implements PointLoader {

    protected final Set<PointListener> listeners = new HashSet<>();

    public AbstractPointLoader(Storage storage, String name, Meta annotation) {
        super(storage, name, annotation);
    }

    public AbstractPointLoader(Storage storage, String name) {
        super(storage, name);
    }

    @Override
    public void push(Collection<DataPoint> dps) throws StorageException {
        for (DataPoint dp : dps) {
            push(dp);
        }
    }

    /**
     * Push point and notify all listeners
     * @param dp
     * @throws StorageException 
     */
    @Override
    public void push(DataPoint dp) throws StorageException {
        //Notifying the listener
        listeners.stream().forEach((l) -> {
            l.accept(dp);
        });
        pushPoint(dp);
    }

    /**
     * push procedure implementation
     * @param dp
     * @throws StorageException
     */
    protected abstract void pushPoint(DataPoint dp) throws StorageException;

    @Override
    public Envelope respond(Envelope message) {
        try {
            if(!acceptEnvelope(message)){
                return StorageMessageUtils.exceptionResponse(message, new WrongTargetException());
            }
            Meta messageMeta = message.meta();
            String operation = messageMeta.getString(ACTION_KEY);
            switch (operation) {
                case PUSH_OPERATION:
                    if (!messageMeta.hasNode("data")) {
                        //TODO реализовать бинарную передачу данных
                        throw new StorageException("No data in the push data command");
                    }

                    Meta data = messageMeta.getNode("data");
                    for (DataPoint dp : fromMeta(data)) {
                        this.push(dp);
                    }

                    return confirmationResponse(message);

                case PULL_OPERATION:
                    MetaBuilder dataAn = new MetaBuilder("data");
                    if (messageMeta.hasNode(QUERY_ELEMENT)) {
                        //TODO implement query building
                        throw new UnsupportedOperationException("Not implemented");
                    } else if (messageMeta.hasValue("value")) {
                        String valueName = messageMeta.getString("valueName", "");
                        for (Value value : messageMeta.getValue("value").listValue()) {
                            DataPoint point = this.getIndex(valueName).pullOne(value);
                            dataAn.putNode(DataPoint.toMeta(point));
                        }
                    } else if (messageMeta.hasNode("range")) {
                        String valueName = messageMeta.getString("valueName", "");                        
                        for (Meta rangeAn : messageMeta.getNodes("range")) {
                            Value from = rangeAn.getValue("from", Value.getNull());
                            Value to = rangeAn.getValue("to", Value.getNull());
                            int maxItems = rangeAn.getInt("maxItems", Integer.MAX_VALUE);
                            for (DataPoint point : this.getIndex(valueName).pull(from, to, maxItems)) {
                                dataAn.putNode(DataPoint.toMeta(point));
                            }
                            //PENDING add annotation from resulting DataSet?
                        }
                    }
                    return new MessageFactory().okResponseBase(message, true, false)
                            .putMetaNode(dataAn)
                            .build();

                default:
                    throw new NotDefinedException("Unknown operation");
            }

        } catch (StorageException | UnsupportedOperationException | NotDefinedException ex) {
            return StorageMessageUtils.exceptionResponse(message, ex);
        }
    }

    @Override
    public String getType() {
        return POINT_LOADER_TYPE;
    }

    @Override
    public void addPointListener(PointListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removePointListener(PointListener listener) {
        this.listeners.remove(listener);
    }

}
