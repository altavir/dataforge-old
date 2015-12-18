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
package hep.dataforge.control.tasks;

import hep.dataforge.data.DataPoint;
import hep.dataforge.exceptions.MeasurementException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Alexander Nozik
 */
public class MeasurementTask implements Signal<DataPoint> {

    @Override
    public DataPoint get(Duration duration) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getTag() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Instant getTime() throws MeasurementException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean cancel(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDone() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataPoint get() throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataPoint get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
//    
//    private final String timeName = "timestamp";
//    
//    private TimeCaliculationMethod method;
//    
//    private Map<String, ControlTask<Value>> measurements;
//    
//    private String tag;
//    
//    @Override
//    public boolean cancel(boolean interrupt) {
//        measurements.values().stream().forEach((m) -> {
//            m.cancel(interrupt);
//        });
//        return true;
//    }
//    
//    @Override
//    public DataPoint get(Duration timeout){
//        waitUntillReady(timeout);
//        return getNow();
//    }
//    
//    @Override
//    public boolean isCancelled() {
//        boolean res = false;
//        for (ControlTask m : measurements.values()) {
//            res = res || m.isCancelled();
//        }
//        return res;
//    }
//
//    /**
//     * Флаг показывает закончно ли измерение
//     *
//     * @return
//     */
//    @Override
//    public boolean isDone() {
//        boolean res = true;
//        for (ControlTask mes : measurements.values()) {
//            res = res && mes.isDone();
//        }
//        return res;
//    }
//    
//    @Override
//    public String getTag() {
//        return tag;
//    }
//
//    /**
//     * Значение, которое измерялось
//     *
//     * @return
//     * @throws MeasurementNotReadyException если не ready()
//     */
//    Value value(String name) throws MeasurementException {
//        if (name.equals(timeName)) {
//            return Value.of(getTime());
//        }
//        if (measurements.containsKey(name)) {
//            return measurements.get(name).getNow();
//        } else {
//            throw new NameNotFoundException(name);
//        }
//    }
//    
//    @Override
//    public DataPoint get(){
//        if (isDone()) {
//            Map<String, Value> pointMap = new HashMap<>(measurements.size());
//            for (Map.Entry<String, ControlTask<Value>> entrySet : measurements.entrySet()) {
//                pointMap.put(timeName, Value.of(getTime()));
//                String key = entrySet.getKey();
//                ControlTask<Value> value = entrySet.getValue();
//                pointMap.put(key, value.getNow());
//                
//            }
//            return new MapDataPoint(pointMap);
//        } else {
//            throw new MeasurementNotReadyException();
//        }
//    }
//
//    /**
//     * Время на момент измерения. Берется или среднее время между измерениями,
//     * или время последнего измерения в зависимости от настроек
//     *
//     * @return
//     * @throws MeasurementNotReadyException если не ready()
//     */
//    @Override
//    public Instant getTime() throws MeasurementException {
//        if (isDone()) {
//            Instant earliest = Instant.now();
//            Instant latest = Instant.EPOCH;
//            for (ControlTask mes : measurements.values()) {
//                Instant mestime = mes.getTime();
//                if (earliest == null) {
//                    earliest = mestime;
//                    latest = mestime;
//                } else {
//                    if (mestime.isBefore(earliest)) {
//                        earliest = mestime;
//                    }
//                    if (mestime.isAfter(latest)) {
//                        latest = mestime;
//                    }
//                }
//            }
//            switch (method) {
//                case EARLIEST:
//                    return earliest;
//                case LATEST:
//                    return latest;
//                case AVERAGE:
//                    return Instant.ofEpochMilli((earliest.toEpochMilli() + latest.toEpochMilli()) / 2);
//                default:
//                    throw new Error();
//            }
//        } else {
//            throw new MeasurementNotReadyException();
//        }
//    }
//
//    /**
//     * Блокирует вызвавший поток до тех пор, пока измерение не будет закончено
//     * или не пройдт timeout
//     *
//     * @param timeout
//     * @throws hep.dataforge.exceptions.MeasurementException
//     */
//    private void waitUntillReady(Duration timeout) throws MeasurementException {
//        Instant start = Instant.now();
//        for (ControlTask m : measurements.values()) {
//            if (Duration.between(start, Instant.now()).compareTo(timeout) > 0) {
//                throw new MeasurementTimeoutException();
//            } else {
//                m.waitUntillReady(timeout);
//            }
//        }
//    }
//    
//    public enum TimeCaliculationMethod {
//        
//        EARLIEST, LATEST, AVERAGE
//    }
//    
//    
}
