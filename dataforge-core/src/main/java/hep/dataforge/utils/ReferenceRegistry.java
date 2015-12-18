/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.utils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A registry of listener references. References could be weak to allow GC to
 * finilize referenced objects.
 *
 *
 *
 * @author Alexander Nozik
 */
public class ReferenceRegistry<T> {

    private final Set<Reference<T>> weakRegistry = new HashSet<>();
    private final Set<T> strongRegistry = new HashSet<>();

    /**
     * Listeners could be added either as strong references or weak references.
     *
     *
     * @param obj
     */
    public synchronized void add(T obj, boolean isStrong) {
        if (isStrong) {
            strongRegistry.add(obj);
        } else {
            weakRegistry.add(new WeakReference<>(obj));
        }
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
    public void add(T obj) {
        add(obj, false);
    }

    public synchronized void remove(T obj) {
        Reference<T> reference
                = weakRegistry.stream()
                .filter(it -> obj.equals(it.get())).findFirst().orElse(null);
        if (reference != null) {
            weakRegistry.remove(reference);
        }
        strongRegistry.remove(obj);
    }

    /**
     * Perform given action on each of existing listeners
     *
     * @param action
     */
    public void forEach(Consumer<T> action) {
        weakRegistry.stream().filter(it -> it.get() != null).forEach(it -> action.accept(it.get()));
        strongRegistry.stream().filter(it -> it != null).forEach(it -> action.accept(it));
    }

}
