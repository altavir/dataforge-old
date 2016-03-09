/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.collectors;

import hep.dataforge.points.MapPoint;
import hep.dataforge.points.PointListener;
import hep.dataforge.values.Value;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An averaging DataPoint collector that starts timer on first put operation and
 * forces collection when timer expires. If there are few Values with same time
 * during this period, they are averaged.
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public class RegularPointCollector implements ValueCollector {

    private Instant startTime;
    private final Map<String, List<Value>> values = new ConcurrentHashMap<>();
    /**
     * The names that must be in the dataPoint
     */
    private List<String> names = new ArrayList<>();
    private Timer timer;
    private final PointListener consumer;
    private final Duration duration;

    public RegularPointCollector(PointListener consumer, Duration duration) {
        this.consumer = consumer;
        this.duration = duration;
    }

    public RegularPointCollector(PointListener consumer, Duration duration, Collection<String> names) {
        this(consumer, duration);
        this.names = new ArrayList<>(names);
    }

    public RegularPointCollector(PointListener consumer, Duration duration, String... names) {
        this(consumer, duration);
        this.names = Arrays.asList(names);
        //TODO add wait for all names option
    }

    @Override
    public void collect() {
        collect(Instant.now());
    }

    public synchronized void collect(Instant time) {
        MapPoint point = new MapPoint();

        Instant average = Instant.ofEpochMilli((time.toEpochMilli() + startTime.toEpochMilli()) / 2);

        point.putValue("timestamp", average);

        for (Map.Entry<String, List<Value>> entry : values.entrySet()) {
            point.putValue(entry.getKey(), entry.getValue().stream().mapToDouble((v) -> v.doubleValue()).sum() / entry.getValue().size());
        }

        // filling all missing values with nulls
        for (String name : names) {
            if (!point.hasValue(name)) {
                point.putValue(name, Value.getNull());
            }
        }

        startTime = null;
        values.clear();
        consumer.accept(point);
    }

    @Override
    public synchronized void put(String name, Value value) {
        if (startTime == null) {
            startTime = Instant.now();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    collect();
                }
            }, duration.toMillis());
        }

        if (!values.containsKey(name)) {
            values.put(name, new ArrayList<>());
        }
        values.get(name).add(value);
    }

    public void cancel() {
        if (timer != null && startTime != null) {
            timer.cancel();
        }
    }

}
