/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A registry of listener references. References could be weak to allow GC to
 * finilize referenced objects.
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
     * Listeners could be added either as strong references or weak references. Thread safe
     *
     * @param obj
     */
    public boolean add(T obj, boolean isStrong) {
        synchronized (strongRegistry) {
            if (isStrong) {
                strongRegistry.add(obj);
            }
        }
        synchronized (weakRegistry) {
            return weakRegistry.add(new WeakReference<>(obj));
        }
    }

    /**
     * Add a strong reference to registry
     *
     * @param obj
     */
    @Override
    public boolean add(T obj) {
        return add(obj, true);
    }

    @Override
    public boolean remove(Object obj) {
        synchronized (strongRegistry) {
            strongRegistry.remove(obj);
        }
        synchronized (weakRegistry) {
            Reference<T> reference = weakRegistry.stream().filter(it -> obj.equals(it.get())).findFirst().orElse(null);

            return reference != null && weakRegistry.remove(reference);
        }
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        boolean res = true;
        synchronized (strongRegistry) {
            res = strongRegistry.removeIf(filter);
        }
        synchronized (weakRegistry) {
            res = res && weakRegistry.removeIf(it -> filter.test(it.get()));
        }
        return res;
    }

    @Override
    public void clear() {
        synchronized (strongRegistry) {
            strongRegistry.clear();
        }
        synchronized (weakRegistry) {
            weakRegistry.clear();
        }
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return weakRegistry.stream().map(Reference::get).filter(Objects::nonNull).iterator();
    }


    @Override
    public int size() {
        return weakRegistry.size();
    }

    public Optional<T> findFirst(Predicate<T> predicate) {
        return this.weakRegistry.stream()
                .map(Reference::get)
                .filter((t) -> t != null && predicate.test(t))
                .findFirst();
    }

    public List<T> findAll(Predicate<T> predicate) {
        return this.weakRegistry.stream()
                .map(Reference::get)
                .filter((t) -> t != null && predicate.test(t))
                .collect(Collectors.toList());
    }
}
