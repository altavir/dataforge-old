package hep.dataforge.maths.groovy.extensions

import hep.dataforge.maths.groovy.linear.LinearUtils
import org.apache.commons.math3.linear.RealVector
import spock.lang.Specification

/**
 * Created by darksnake on 06-Nov-16.
 */
class RealVectorExtensionTest extends Specification {
    def "Map"() {
        given:
        RealVector vec1 = LinearUtils.vector([1, 2]);
        RealVector vec2 = LinearUtils.vector([1.5, 1]);

        when:
        def res = (vec1 + vec2*2).transform{
            it**2d
        }

        then:
        [16,16] == res.toArray()
    }
}
