package hep.dataforge.grind.workspace

import hep.dataforge.grind.actions.GrindJoin
import hep.dataforge.grind.actions.GrindPipe
import hep.dataforge.workspace.tasks.TaskBuilder

/**
 * A groovy specification wrapping {@link TaskBuilder} class
 *
 */
class TaskSpec extends TaskBuilder {

    def join(Map params = [:], String name = "@dynamic", @DelegatesTo(GrindJoin.JoinGroupBuilder) Closure action) {
        doLast(GrindJoin.build(params, name, action));
    }

    def action(Map params = [:], String name = "@dynamic", @DelegatesTo(GrindPipe.PipeBuilder) Closure action) {
        doLast(GrindPipe.build(params, name, action))
    }

}
