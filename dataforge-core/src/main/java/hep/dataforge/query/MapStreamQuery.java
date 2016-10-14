/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.query;

import javafx.util.Pair;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A specific type of query to work with key-value pairs
 *
 * @author Alexander Nozik
 * @param <T>
 */
@Deprecated
public class MapStreamQuery<K, V> extends StreamQuery<Pair<K, V>> {

    /**
     * Create query from map entries instead of pairs
     * @param <K>
     * @param <V>
     * @param sup
     * @return 
     */
    public static <K, V> MapStreamQuery<K, V> forMap(Supplier<Stream<Map.Entry<K, V>>> sup) {
        return new MapStreamQuery<>(() -> sup.get().map((entry -> new Pair<>(entry.getKey(), entry.getValue()))));
    }

    public MapStreamQuery(Supplier<Stream<Pair<K, V>>> sup) {
        super(sup);
    }

    protected MapStreamQuery(Supplier<Stream<Pair<K, V>>> sup,
            Predicate<Pair<K, V>> condition,
            Function<Stream<Pair<K, V>>, Stream<Pair<K, V>>> transformation) {
        super(sup, condition, transformation);
    }

    @Override
    protected MapStreamQuery<K, V> buildQuery(Supplier<Stream<Pair<K, V>>> sup,
            Predicate<Pair<K, V>> condition, Function<Stream<Pair<K, V>>, Stream<Pair<K, V>>> transformation) {
        return new MapStreamQuery<>(sup, condition, transformation);
    }

    @Override
    public MapStreamQuery<K, V> transform(Function<Stream<Pair<K, V>>, Stream<Pair<K, V>>> function) {
        return (MapStreamQuery<K, V>) super.transform(function);
    }

    @Override
    public MapStreamQuery<K, V> filter(Predicate<Pair<K, V>> predicate) {
        return (MapStreamQuery<K, V>) super.filter(predicate);
    }

    public MapStreamQuery<K, V> filterByKey(Predicate<K> keyPredicate, Predicate<V> valuePredicate) {
        return filter((Pair<K, V> entry) -> keyPredicate.test(entry.getKey()) && valuePredicate.test(entry.getValue()));
    }

    public MapStreamQuery<K, V> filterByKey(Predicate<K> predicate) {
        return filter((Pair<K, V> entry) -> predicate.test(entry.getKey()));
    }

    public MapStreamQuery<K, V> filterByValue(Predicate<V> predicate) {
        return filter((Pair<K, V> entry) -> predicate.test(entry.getValue()));
    }

    public Stream<V> makeValues() {
        return makeStream().map(entry -> entry.getValue());
    }

}
