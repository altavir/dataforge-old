package hep.dataforge.grind.actions

import hep.dataforge.context.Context
import hep.dataforge.io.history.Chronicle
import hep.dataforge.meta.Meta

/**
 * An environment object in which action could be run
 */
class ActionEnv {
    final Context context;
    final String name;
    final Meta meta;
    final Chronicle log;

    ActionEnv(Context context, String name, Meta meta, Chronicle log) {
        this.context = context
        this.name = name
        this.meta = meta
        this.log = log
    }

    def <T, R> R execute(T input, Closure<R> closure) {
        Closure<R> rehydrated = closure.rehydrate(this, null, null);
        rehydrated.setResolveStrategy(Closure.DELEGATE_ONLY);
        return rehydrated.call(input);
    }
}
