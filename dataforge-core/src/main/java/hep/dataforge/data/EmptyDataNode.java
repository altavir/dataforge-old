/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Meta;

import java.util.Optional;
import java.util.stream.Stream;


public class EmptyDataNode<T> implements DataNode<T> {

    private final String name;
    private final Class<T> type;

    public EmptyDataNode(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Optional<Data<T>> optData(String name) {
        return Optional.empty();
    }

    @Override
    public Stream<NamedData<? extends T>> dataStream(boolean recursive) {
        return Stream.empty();
    }

    @Override
    public Stream<DataNode<? extends T>> nodeStream(boolean recusive) {
        return Stream.empty();
    }


    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Optional<DataNode<T>> optNode(String nodeName) {
        return Optional.empty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Meta getMeta() {
        return Meta.empty();
    }

}
