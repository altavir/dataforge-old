package hep.dataforge.grind

import hep.dataforge.meta.MetaBuilder

/**
 * Created by darksnake on 04-Aug-16.
 */
class GrindUtils {

    public static MetaBuilder buildMeta(Closure cl){
        def metaSpec = new GrindMetaBuilder()
        def metaExec = cl.rehydrate(metaSpec, this, this);
        metaExec.resolveStrategy = Closure.DELEGATE_ONLY;
        return metaExec
    }

//    public static MetaBuilder buildMeta(String str){
//        GroovyShell shell = new GroovyShell();
//        Script script = shell.parse(str);
//        script.
//    }
}
