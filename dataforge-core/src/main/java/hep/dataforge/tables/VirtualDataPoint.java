/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.tables;

import hep.dataforge.names.Names;
import hep.dataforge.values.Value;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A DataPoint that uses another data point or another object as a source but does not copy data
 * itself
 *
 * @author Alexander Nozik
 */
public class VirtualDataPoint<S> implements DataPoint {

    private final S source;
    private final BiFunction<String, S, Value> transformation;
    private final Names names;

    public VirtualDataPoint(S source, BiFunction<String, S, Value> transformation, String... names) {
        this.source = source;
        this.transformation = transformation;
        this.names = Names.of(names);
    }

    @Override
    public Optional<Value> optValue(String name) {
        if (hasValue(name)) {
            return Optional.ofNullable(transformation.apply(name, source));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Names names() {
        return names;
    }


}
