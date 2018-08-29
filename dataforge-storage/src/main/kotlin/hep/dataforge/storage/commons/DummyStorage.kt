package hep.dataforge.storage.commons

import hep.dataforge.context.Context
import hep.dataforge.meta.Meta
import hep.dataforge.storage.api.Loader
import hep.dataforge.storage.api.Storage

class DummyStorage(context: Context) : AbstractStorage(context,null,"dummy", Meta.empty()) {
    override fun createLoader(loaderName: String, loaderConfiguration: Meta): Loader {
        error("Not available for dummy")
    }

    override fun createShelf(shelfConfiguration: Meta, shelfName: String): Storage {
        error("Not available for dummy")
    }

}