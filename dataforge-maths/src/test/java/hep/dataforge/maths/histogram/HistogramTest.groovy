package hep.dataforge.maths.histogram

import org.apache.commons.math3.random.JDKRandomGenerator
import spock.lang.Specification

import java.util.stream.DoubleStream

import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

/**
 * Created by darksnake on 02.07.2017.
 */
class HistogramTest extends Specification {

    def testUnivariate() {
        given:
        def histogram = new UnivariateHistogram(-5, 5, 0.1)
        def generator = new JDKRandomGenerator();
        when:
        histogram.fill(DoubleStream.generate { (generator.nextGaussian() - 0.5) * 2 }.limit(200000))
        then:
        def average = histogram.binStream()
                .mapToDouble{(it.getLowerBound(0) + it.getUpperBound(0)) / 2d * it.count}
                .average()
                .getAsDouble()
        expect average, closeTo(0,0.1)
    }
}
