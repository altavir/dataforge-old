package hep.dataforge.grind;

import groovy.lang.GroovyClassLoader;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.workspace.Workspace;

import java.io.File;

/**
 * Created by darksnake on 12-Aug-16.
 */
public class JavaGrindLauncher {
    private static GroovyClassLoader loader = new GroovyClassLoader();

    public static Workspace buildWorkspace(File file) {
        try {
            return (Workspace) loader.loadClass("hep.dataforge.grind.GrindLauncher")
                    .getMethod("buildWorkspace", File.class)
                    .invoke(null, file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Workspace buildWorkspace(File file, String overrideClassName) {
        try {
            Class overrideClass = loader.loadClass(overrideClassName);
            return (Workspace) loader.loadClass("hep.dataforge.grind.GrindLauncher")
                    .getMethod("buildWorkspace", File.class, Class.class)
                    .invoke(null, file, overrideClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MetaBuilder buildMeta(String input) {
        try {
            Class utils = loader.loadClass("hep.dataforge.grind.GrindUtils");
            return (MetaBuilder) utils.getMethod("buildMeta", String.class).invoke(null, input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
