package hep.dataforge.grind

import hep.dataforge.context.Global

/**
 * Created by darksnake on 05-Nov-16.
 */
println "DataForge grind shell"
try {
    new GrindShell().launch()
} catch (Exception ex) {
    ex.printStackTrace();
} finally {
    Global.instance().close();
}
println "grind shell closed"