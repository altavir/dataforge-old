/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.navigation.Path;
import java.util.Iterator;
import java.util.stream.Stream;
import javafx.util.Pair;


public class EmptyDataNode<T> implements DataNode<T> {
    
    private final String name;
    private final Class<T> type;

    public EmptyDataNode(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Data<? extends T> getData(String name) {
        throw new NameNotFoundException(name);
    }

    @Override
    public Stream<Pair<String, Data<? extends T>>> dataStream() {
        return Stream.empty();
    }

    @Override
    public Stream<Pair<String, DataNode<? extends T>>> nodeStream() {
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
    public int size() {
        return 0;
    }

    @Override
    public DataNode<? extends T> getNode(String nodeName) {
        return null;
    }

    @Override
    public Iterator<Data<? extends T>> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Meta meta() {
        return Meta.empty();
    }

    @Override
    public Object provide(Path path) {
        return null;
    }

    @Override
    public boolean provides(Path path) {
        return false;
    }
    
}
