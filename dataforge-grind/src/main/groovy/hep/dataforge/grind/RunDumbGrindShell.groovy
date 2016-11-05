package hep.dataforge.grind

import hep.dataforge.context.GlobalContext

/**
 * Created by darksnake on 05-Nov-16.
 */
println "DataForge grind shell"
try {
    new GrindShell().launch()
} catch (Exception ex) {
    ex.printStackTrace();
} finally {
    GlobalContext.instance().close();
}
println "grind shell closed"