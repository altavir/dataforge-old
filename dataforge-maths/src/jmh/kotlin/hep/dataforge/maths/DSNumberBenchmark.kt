package hep.dataforge.maths

import hep.dataforge.maths.expressions.DSNumberContext
import hep.dataforge.maths.expressions.autoDiff
import hep.dataforge.maths.extensions.exp
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Warmup
import kotlin.math.PI
import kotlin.math.sqrt


@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 10)
open class DSNumberBenchmark{

    @Benchmark
    fun benchmarkNumberContext() {
        val x = 0
        val context = DSNumberContext(1, "amp", "pos", "sigma")

        val gauss = with(context) {
            val amp = variable("amp", 1)
            val pos = variable("pos", 0)
            val sigma = variable("sigma", 1)
            amp / (sigma * sqrt(2 * PI)) * exp(-pow(pos - x, 2) / pow(sigma, 2) / 2)
        }

        gauss.toDouble()
        gauss.deriv("pos")
    }

    @Benchmark
    fun benchmarkAD() {
        val x = 0
        val gauss = autoDiff(1, "amp", "pos", "sigma") {
            val amp = variable("amp", 1)
            val pos = variable("pos", 0)
            val sigma = variable("sigma", 1)
            amp / (sigma * sqrt(2 * PI)) * exp(-(pos - x).pow(2) / sigma.pow(2) / 2)
        }

        gauss.toDouble()
        gauss.deriv("pos")
    }
}

