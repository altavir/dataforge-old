package hep.dataforge.grind.extensions

import hep.dataforge.values.Value
import spock.lang.Specification

/**
 * Created by darksnake on 05-Aug-16.
 */
class ValueExtensionTest extends Specification {

    def "Type conversion"() {
        given:
        Value val = Value.of(22.5);
        expect:
        val as Double == 22.5
        val as String == "22.5"
    }

    def "Equality"() {
        given:
        Value val = Value.of(22.5);
        expect:
        val == 22.5
    }
}
