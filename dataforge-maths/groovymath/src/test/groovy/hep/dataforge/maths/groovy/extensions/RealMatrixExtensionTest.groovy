package hep.dataforge.maths.groovy.extensions

import hep.dataforge.maths.groovy.linear.LinearUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector
import spock.lang.Specification

/**
 * Created by darksnake on 01-Jul-16.
 */
class RealMatrixExtensionTest extends Specification {
    def "matrix extension"(){
        given:
            RealMatrix mat = LinearUtils.matrix([[0, -0.5], [-0.5, 0]]);
            RealVector vec = LinearUtils.vector([1,1]);
        when:
            def res = vec*(mat + 1)*vec;
        then:
            res == 1;

    }
}
