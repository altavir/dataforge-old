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
public class ClassPathPluginLoader extends AbstractPluginLoader {

    private final ServiceLoader<PluginFactory> loader;


    public ClassPathPluginLoader(Context context) {
        ClassLoader cl = context.getClassLoader();
        loader = ServiceLoader.load(PluginFactory.class, cl);
    }

    @Override
    protected Stream<PluginFactory> factories() {
        return StreamSupport.stream(loader.spliterator(), false);
    }
}
