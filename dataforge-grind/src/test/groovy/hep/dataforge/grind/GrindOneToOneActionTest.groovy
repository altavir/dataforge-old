package hep.dataforge.grind

import hep.dataforge.data.DataNode
import hep.dataforge.data.DataSet
import hep.dataforge.meta.Meta
import spock.lang.Specification

/**
 * Created by darksnake on 07-Aug-16.
 */
class GrindOneToOneActionTest extends Specification {
    def "One to One action"() {
        given:
        def action = GrindOneToOneAction.build { Math.pow(input, meta.getDouble("pow")) };
        def data = DataSet.builder().putStatic("9", 81).putStatic("3", 9).build();
        when:
        DataNode res = action.run(data, new GrindMetaBuilder().meta(pow: 0.5))
        then:
        res.forEachData { key, value -> key.toDouble() == value.get() }
    }
}
