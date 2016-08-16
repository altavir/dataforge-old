package hep.dataforge.grind;

import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.workspace.Workspace;

import java.io.File;

/**
 * Created by darksnake on 12-Aug-16.
 */
public class JavaGrindLauncher {
    public static Workspace buildWorkspace(File file) {
        return new GrindLauncher().from(file).buildWorkspace();
    }


    public static Workspace buildWorkspace(File file, Class<? extends WorkspaceSpec> specClass) {
        return new GrindLauncher().from(file).with(specClass).buildWorkspace();
    }

    public static MetaBuilder buildMeta(String input) {
        return GrindUtils.buildMeta(input);
    }
}
