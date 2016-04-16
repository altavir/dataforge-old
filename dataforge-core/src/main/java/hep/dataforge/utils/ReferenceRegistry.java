/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.utils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A registry of listener references. References could be weak to allow GC to
 * finilize referenced objects.
 *
 *
 *
 * @author Alexander Nozik
 */
public class ReferenceRegistry<T> extends AbstractCollection<T> {

    private final Set<Reference<T>> weakRegistry = new HashSet<>();
    /**
     * Used only to store strongreferences
     */
    private final Set<T> strongRegistry = new HashSet<>();

    /**
     * Listeners could be added either as strong references or weak references.
     *
     *
     * @param obj
     */
    public synchronized boolean add(T obj, boolean isStrong) {
        if (isStrong) {
            strongRegistry.add(obj);
        }
        return weakRegistry.add(new WeakReference<>(obj));
    }

    /**
     * Add weak reference to registry
     * <p>
     * <strong>WARNING:</strong> registered listeners are stored as weak
     * references meaning they will be cleaned if they are not declared
     * somewhere else.
     * </p>
     *
     * @param obj
     */
    @Override
    public boolean add(T obj) {
        return add(obj, false);
    }

    @Override
    public synchronized boolean remove(Object obj) {
        strongRegistry.remove(obj);
        Reference<T> reference = weakRegistry.stream().filter(it -> obj.equals(it.get())).findFirst().orElse(null);

        if (reference != null) {
            return weakRegistry.remove(reference);
        } else {
            return false;
        }
    }

    /**
     * Clean up all null entries from weak registry
     */
    private synchronized void cleanUp() {
        weakRegistry.removeIf(ref -> ref.get() == null);
    }

    @Override
    public synchronized Iterator<T> iterator() {
        cleanUp();
        //FIXME concurrency problem here?
        final Iterator<Reference<T>> referenceIterator = weakRegistry.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return referenceIterator.hasNext();
            }

            @Override
            public T next() {
                return referenceIterator.next().get();
            }
        };
    }

    @Override
    public int size() {
        return weakRegistry.size();
    }

    public Optional<T> findFirst(Predicate<T> predicate) {
        return this.weakRegistry.stream()
                .map(ref -> ref.get())
                .filter((t) -> t != null && predicate.test(t))
                .findFirst();
    }

    public List<T> findAll(Predicate<T> predicate) {
        return this.weakRegistry.stream()
                .map(ref -> ref.get())
                .filter((t) -> t != null && predicate.test(t))
                .collect(Collectors.toList());
    }
}
