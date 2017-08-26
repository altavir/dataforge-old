package hep.dataforge.grind

import hep.dataforge.context.Global
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataSet
import hep.dataforge.grind.actions.GrindPipe
import spock.lang.Specification

/**
 * Created by darksnake on 07-Aug-16.
 */
class GrindPipeTest extends Specification {
    def "One to One action"() {
        given:
        def action = new GrindPipe<>({ Math.pow(input, meta.getDouble("pow")) });
        def data = DataSet.builder().putStatic("9", 81).putStatic("3", 9).build();
        when:
        DataNode res = action.run(Global.instance(), data, new GrindMetaBuilder().meta(pow: 0.5))
        then:
        res.dataStream().forEach { it.getName().toDouble() == it.get() }
    }
}
