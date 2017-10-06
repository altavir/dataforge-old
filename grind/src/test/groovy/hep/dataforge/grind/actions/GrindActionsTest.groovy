package hep.dataforge.grind.actions

import hep.dataforge.context.Global
import hep.dataforge.data.DataNode
import hep.dataforge.data.DataSet
import hep.dataforge.grind.GrindMetaBuilder
import hep.dataforge.meta.Meta
import spock.lang.Specification

/**
 * Created by darksnake on 07-Aug-16.
 */
class GrindActionsTest extends Specification {
    def pipe() {
        given:
        def action = GrindPipe.build("pow") {
            name = "res_" + name
            result { Double input ->
                Math.pow(input, meta.getDouble("pow"))
            }
        }
        def data = DataSet.builder()
                .putStatic("9", 81)
                .putStatic("3", 9)
                .build();
        when:
        DataNode res = action.run(Global.instance(), data, new GrindMetaBuilder().meta(pow: 0.5))
        then:
        res["res_9"].get() == 9
    }

    def join() {
        given:
        def action = GrindJoin.build("sum") {
            result { Map<String, Integer> map ->
                map.values().sum()
            }
        }
        def data = DataSet.builder()
        data.with { builder ->
            (1..10).each {
                builder.putData("data_$it", it, Meta.empty())
            }
        }
        data = data.build();
        when:
        DataNode res = action.run(Global.instance(), data, new GrindMetaBuilder().meta(resName: "sum"))
        then:
        res["sum"].get() == 55
    }
}
