package hep.dataforge.grind

import hep.dataforge.context.GlobalContext

/**
 * Created by darksnake on 27-Oct-16.
 */

println "DataForge grind shell"
try {
    new GrindShell().withTerminal().launch()
} catch (Exception ex) {
    ex.printStackTrace();
} finally {
    GlobalContext.instance().close();
}
println "grind shell closed"