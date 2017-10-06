package hep.dataforge.kodex

import hep.dataforge.context.BasicPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginDef
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlin.coroutines.experimental.CoroutineContext

@PluginDef(group = "hep.dataforge", name = "kodex", info = "Kodex coroutine context and other useful things")
class KodexPlugin : BasicPlugin() {
    var dispatcher: CoroutineContext = CommonPool

    override fun attach(context: Context) {
        super.attach(context)
        context.logger.debug("Switching KODEX coroutine dispatcher to context executor")
        dispatcher = context.parallelExecutor().asCoroutineDispatcher()
    }

    override fun detach() {
        super.detach()
        dispatcher = CommonPool
    }
}