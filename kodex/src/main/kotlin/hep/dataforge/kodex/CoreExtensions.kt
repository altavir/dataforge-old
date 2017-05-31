package hep.dataforge.kodex

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MutableMetaNode
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import java.time.Instant

/**
 * Core DataForge classes extensions
 * Created by darksnake on 26-Apr-17.
 */

operator fun Value.plus(other: Value): Value {
    return when (this.valueType()!!) {
        ValueType.NUMBER -> Value.of(this.numberValue() + other.numberValue());
        ValueType.STRING -> Value.of(this.stringValue() + other.stringValue());
        ValueType.TIME -> Value.of(Instant.ofEpochMilli(this.timeValue().toEpochMilli() + other.timeValue().toEpochMilli()))
        ValueType.BOOLEAN -> Value.of(this.booleanValue() || other.booleanValue());
        ValueType.NULL -> other;
    }
}


operator fun Value.plus(other: Any): Value {
    return this + Value.of(other);
}

operator fun Meta.get(path: String): Value {
    return this.getValue(path);
}

operator fun MutableMetaNode<*>.set(path: String, value: Value):MutableMetaNode<*> {
    return this.setValue(path, value);
}

operator fun MutableMetaNode<*>.plus(meta: Meta): MutableMetaNode<*>{
    return this.putNode(meta);
}
