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

import hep.dataforge.exceptions.NotDefinedException;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.WrongTargetException;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.storage.commons.MessageFactory;
import hep.dataforge.storage.commons.StorageMessageUtils;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.PointListener;
import hep.dataforge.values.Value;

import java.util.*;
import java.util.stream.Collectors;

import static hep.dataforge.storage.commons.StorageMessageUtils.*;
import static hep.dataforge.tables.DataPoint.buildFromMeta;

/**
 * @author Alexander Nozik
 */
public abstract class AbstractPointLoader extends AbstractLoader implements PointLoader {

    protected final Set<PointListener> listeners = new HashSet<>();
    private HashMap<String, ValueIndex<DataPoint>> indexMap = new HashMap<>();

    public AbstractPointLoader(Storage storage, String name, Meta meta) {
        super(storage, name, meta);
    }

    @Override
    public void push(Collection<DataPoint> dps) throws StorageException {
        for (DataPoint dp : dps) {
            push(dp);
        }
    }

    @Override
    public synchronized ValueIndex<DataPoint> getIndex(String name) {
        return indexMap.computeIfAbsent(name, this::buildIndex);
    }

    protected abstract ValueIndex<DataPoint> buildIndex(String name);

    /**
     * Push point and notify all listeners
     *
     * @param dp
     * @throws StorageException
     */
    @Override
    public void push(DataPoint dp) throws StorageException {
        //Notifying the listener
        listeners.forEach((l) -> {
            l.accept(dp);
        });
        pushPoint(dp);
    }

    /**
     * push procedure implementation
     *
     * @param dp
     * @throws StorageException
     */
    protected abstract void pushPoint(DataPoint dp) throws StorageException;

    @Override
    public Envelope respond(Envelope message) {
        try {
            if (!getValidator().isValid(message)) {
                return StorageMessageUtils.exceptionResponse(message, new WrongTargetException());
            }
            Meta messageMeta = message.meta();
            String operation = messageMeta.getString(ACTION_KEY);
            switch (operation) {
                case PUSH_OPERATION:
                    if (!messageMeta.hasMeta("data")) {
                        //TODO реализовать бинарную передачу данных
                        throw new StorageException("No data in the push data command");
                    }

                    Meta data = messageMeta.getMeta("data");
                    for (DataPoint dp : buildFromMeta(data)) {
                        this.push(dp);
                    }

                    return confirmationResponse(message);

                case PULL_OPERATION:
                    List<DataPoint> points = new ArrayList<>();
                    if (messageMeta.hasMeta(QUERY_ELEMENT)) {
                        points = getIndex().query(messageMeta.getMeta(QUERY_ELEMENT)).collect(Collectors.toList());
                    } else if (messageMeta.hasValue("value")) {
                        String valueName = messageMeta.getString("valueName", "");
                        points = messageMeta.getValue("value").listValue().stream()
                                .map(val -> getIndex(valueName).pullOne(val))
                                .filter(Optional::isPresent).map(Optional::get)
                                .collect(Collectors.toList());
                    } else if (messageMeta.hasMeta("range")) {
                        String valueName = messageMeta.getString("valueName", "");
                        for (Meta rangeAn : messageMeta.getMetaList("range")) {
                            Value from = rangeAn.getValue("from", Value.getNull());
                            Value to = rangeAn.getValue("to", Value.getNull());
//                            int maxItems = rangeAn.getInt("maxItems", Integer.MAX_VALUE);
                            points = this.getIndex(valueName).pull(from, to)
                                    .collect(Collectors.toList());
                        }
                    }

                    MetaBuilder dataAn = new MetaBuilder("data");
                    for (DataPoint dp : points) {
                        dataAn.putNode(dp.toMeta());
                    }
                    return new MessageFactory().okResponseBase(message, true, false)
                            .putMetaNode(dataAn)
                            .putMetaValue("data.size", points.size())
                            .build();

                default:
                    throw new NotDefinedException(operation);
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
