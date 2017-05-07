/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.context;

import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The plugin resolver that searches classpath for Plugin services and loads the
 * best one
 *
 * @author Alexander Nozik
 */
public class ClassPathPluginRepository extends AbstractPluginRepository {

    private final ServiceLoader<Plugin> loader;


    public ClassPathPluginRepository(Context context) {
        ClassLoader cl = context.getClassLoader();
        loader = ServiceLoader.load(Plugin.class, cl);
    }

    @Override
    protected Stream<Plugin> stream() {
        return StreamSupport.stream(loader.spliterator(), false);
    }
}
