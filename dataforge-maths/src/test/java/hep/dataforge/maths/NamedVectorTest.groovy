package hep.dataforge.maths

import hep.dataforge.names.Names
import org.apache.commons.math3.linear.ArrayRealVector
import spock.lang.Specification

/**
 * Created by darksnake on 14.06.2017.
 */
class NamedVectorTest extends Specification {
    def "MetaMorphing"() {
        given:
        def vector = new NamedVector(Names.of(["a", "b", "c"]), new ArrayRealVector([1d, 2d, 3d] as double[]));
        when:
        def meta = vector.toMeta();
        def newVector = MetaMorph.morphNode(NamedVector,meta);
        then:
        newVector.getDouble("b") == 2
    }

}
