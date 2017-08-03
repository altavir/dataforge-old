package hep.dataforge.grind.extensions

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MutableMetaNode

/**
 * A set of dynamic initializers for groovy features. Must be called explicitly at the start of the program.
 */
class ExtensionInitializer {

    static def initMeta(){
        Meta.metaClass.getProperty = {String name ->
            delegate.getMetaOrEmpty(name)
        }

        MutableMetaNode.metaClass.setProperty = { String name, Object value ->
            if (value instanceof Meta) {
                delegate.setNode(name, (Meta) value)
            } else if (value instanceof Collection) {
                delegate.setNode(name, (Collection<? extends Meta>) value)
            } else if (value.getClass().isArray()) {
                delegate.setNode(name, (Meta[]) value)
            } else {
                throw new RuntimeException("Can't convert ${value.getClass()} to Meta")
            }
        }
    }
}
