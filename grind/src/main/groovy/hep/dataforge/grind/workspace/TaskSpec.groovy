package hep.dataforge.grind.workspace

import hep.dataforge.grind.actions.GrindJoin
import hep.dataforge.grind.actions.GrindPipe
import hep.dataforge.workspace.tasks.TaskBuilder

/**
 * A groovy specification wrapping {@link TaskBuilder} class
 *
 */
class TaskSpec extends TaskBuilder {

    def join(Map params = [:], String name = "@dynamic", @DelegatesTo(GrindJoin.ManyToOneCallable) Closure action) {
        doLast(new GrindJoin(params, name, action));
    }

    def action(Map params = [:], String name = "@dynamic", @DelegatesTo(GrindPipe.OneToOneCallable) Closure action) {
        doLast(new GrindPipe(params, name, action))
    }

}
