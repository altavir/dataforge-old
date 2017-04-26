package hep.dataforge.providers;

import hep.dataforge.exceptions.ChainPathNotSupportedException;
import hep.dataforge.exceptions.TargetNotProvidedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by darksnake on 25-Apr-17.
 */
public class Providers {
    /**
     * Provide using custom resolver.
     *
     * @param path
     * @param resolver
     * @return
     */
    public static Optional<?> provide(Path path, Function<String, Optional<?>> resolver) {
        Optional<?> opt = resolver.apply(path.nameString());
        if (path.hasTail()) {
            return opt.map(res -> {
                if (res instanceof Provider) {
                    Provider p = Provider.class.cast(res);
                    //using default chain target if needed
                    Path tail = path.tail();
                    if (tail.target().isEmpty()) {
                        tail = tail.setTarget(p.defaultChainTarget());
                    }
                    return p.provide(tail);
                } else {
                    throw new ChainPathNotSupportedException();
                }
            });
        } else {
            return opt;
        }
    }

    public static Optional<?> provide(Object provider, Path path) {
        return provide(path, str -> provideDirect(provider, path.target(), str));
    }


    public static Collection<String> listTargets(Object provider) {
        return findProviders(provider.getClass()).keySet();
    }

    @SuppressWarnings("unchecked")
    public static Collection<String> listContent(Object provider, String target) {
        return Stream.of(provider.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(ProvidesNames.class))
                .filter(method -> Objects.equals(method.getAnnotation(ProvidesNames.class).value(), target))
                .findFirst()
                .map(method -> {
                    try {
                        return (Collection<String>) method.invoke(provider);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("Failed to provide names by reflections");
                    }
                }).orElse(Collections.emptySet());
    }

    /**
     * Provide direct descendant without using chain path
     *
     * @param provider
     * @param target
     * @param name
     * @return
     */
    private static Optional<?> provideDirect(Object provider, String target, String name) {
        Map<String, Method> providers = findProviders(provider.getClass());

        // using default target if needed
        if (target.isEmpty() && provider instanceof Provider) {
            target = ((Provider) provider).defaultTarget();
        }

        if (!providers.containsKey(target)) {
            throw new TargetNotProvidedException(target);
        } else {
            Method method = providers.get(target);
            try {
                return Optional.class.cast(method.invoke(provider, name));
            } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
                throw new RuntimeException("Failed to provide by reflections. The method " + method.getName() + " is not a provider method");
            }
        }
    }

    private static Map<String, Method> findProviders(Class cl) {
        return Stream.of(cl.getMethods())
                .filter(method -> method.isAnnotationPresent(Provides.class))
                .collect(Collectors.toMap(method -> method.getAnnotation(Provides.class).value(), method -> method));
    }

}