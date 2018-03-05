/*
 * Copyright  2017 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.devices

import hep.dataforge.context.Context
import hep.dataforge.control.devices.Sensor.Companion.MEASUREMENT_META_STATE
import hep.dataforge.control.devices.Sensor.Companion.MEASUREMENT_RESULT_STATE
import hep.dataforge.control.devices.Sensor.Companion.MEASUREMENT_STATE_STATE
import hep.dataforge.control.devices.Sensor.Companion.MEASURING_STATE
import hep.dataforge.description.NodeDef
import hep.dataforge.description.ValueDef
import hep.dataforge.exceptions.ControlException
import hep.dataforge.meta.Meta
import hep.dataforge.states.MetaStateDef
import hep.dataforge.states.MetaStateDefs
import hep.dataforge.states.StateDef
import hep.dataforge.states.StateDefs
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import java.time.Duration
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * A device with which could perform one type of one-time or regular measurements
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "resultBuffer", type = [ValueType.NUMBER], def = "100", info = "The size of the buffer for results of measurements")
@StateDefs(
        StateDef(value = ValueDef(name = MEASURING_STATE, type = [ValueType.BOOLEAN], info = "Shows if this sensor is actively measuring"), writable = true),
        StateDef(ValueDef(name = MEASUREMENT_STATE_STATE, enumeration = Sensor.Companion.MeasurementState::class, info = "Shows if this sensor is actively measuring"))
)
@MetaStateDefs(
        MetaStateDef(value = NodeDef(name = MEASUREMENT_META_STATE, info = "Configuration of current measurement."), writable = true),
        MetaStateDef(NodeDef(name = MEASUREMENT_RESULT_STATE, info = "The result of the last measurement in Meta form"))
)
abstract class Sensor(context: Context, meta: Meta) : AbstractDevice(context, meta) {

    private val measurements: MutableMap<Meta, Future<*>> = HashMap()

    /**
     * The result of last measurement
     */
    val result: Meta by metaState(MEASUREMENT_RESULT_STATE)

    /**
     * Current measurement configuration
     */
    var measurement by metaState(MEASUREMENT_META_STATE)

    /**
     * true if measurement in pro
     */
    var measuring by booleanState(MEASURING_STATE)

    /**
     * Current state of the measurement
     */
    val measurementState by stringState(MEASUREMENT_STATE_STATE)

    override fun shutdown() {
        stopAllMeasurements()
        super.shutdown()
    }

    /**
     * update result
     */
    protected fun notifyResult(result: Meta) {
        updateLogicalMetaState(MEASUREMENT_RESULT_STATE, result)
    }

    /**
     * Notify measurement state changed
     */
    protected fun notifyMeasurementState(state: MeasurementState) {
        updateLogicalState(MEASUREMENT_STATE_STATE, state.name)
        when (state) {
            MeasurementState.STOPPED -> updateLogicalState(MEASURING_STATE, false)
            MeasurementState.IN_PROGRESS -> updateLogicalState(MEASURING_STATE, true)
            MeasurementState.WAITING -> updateLogicalState(MEASURING_STATE, true)
            else -> {
            }
        }
    }

    @Throws(ControlException::class)
    override fun requestStateChange(stateName: String, value: Value) {
        when (stateName) {
            MEASURING_STATE -> {
                val meta = optMetaState(MEASUREMENT_META_STATE).orElse(Meta.empty())
                if (value.booleanValue()) {
                    startMeasurement(measurement, meta)
                } else {
                    stopMeasurement(meta)
                }
            }
            else -> super.requestStateChange(stateName, value)
        }
    }

    @Throws(ControlException::class)
    override fun requestMetaStateChange(stateName: String, meta: Meta) {
        when (stateName) {
            MEASUREMENT_META_STATE -> {
                val oldMeta = optMetaState(MEASUREMENT_META_STATE).orElse(null)
                startMeasurement(oldMeta, meta)
            }
            else -> super.requestMetaStateChange(stateName, meta)
        }
    }


    /**
     * Set active measurement using given meta
     * @param oldMeta Meta of previous active measurement. If null no measurement was set
     * @param newMeta Meta of new measurement. If null, then clear measurement
     * @return actual meta for new measurement
     */
    protected abstract fun startMeasurement(oldMeta: Meta, newMeta: Meta)

    /**
     * stop measurement with given meta
     */
    protected open fun stopMeasurement(meta: Meta) {
        measurements[meta]?.cancel(false)
    }

    protected fun stopCurrentMeasurement(){
        stopMeasurement(measurement)
    }

    /**
     * Stop all active measurements
     */
    protected open fun stopAllMeasurements() {
        measurements.values.forEach {
            if (!it.isDone) {
                it.cancel(false)
            }
        }
    }


    protected fun startMeasurement(action: () -> Meta) {
        synchronized(measurement) {
            val future = executor.submit {
                notifyMeasurementState(MeasurementState.IN_PROGRESS)
                val res = action.invoke()
                notifyResult(res)
                notifyMeasurementState(MeasurementState.STOPPED)
            }
            measurements.put(measurement, future)
        }
    }

    protected fun scheduleMeasurement(delay: Duration, action: () -> Meta) {
        synchronized(measurement) {
            notifyMeasurementState(MeasurementState.WAITING)
            val future = executor.schedule({
                notifyMeasurementState(MeasurementState.IN_PROGRESS)
                val res = action.invoke()
                notifyResult(res)
                notifyMeasurementState(MeasurementState.STOPPED)
            }, delay.toMillis(), TimeUnit.MILLISECONDS)
            measurements.put(measurement, future)
        }
    }

    protected fun startRegularMeasurement(interval: Duration, action: () -> Meta) {
        synchronized(measurement) {
            notifyMeasurementState(MeasurementState.WAITING)
            val future = executor.scheduleWithFixedDelay({
                notifyMeasurementState(MeasurementState.IN_PROGRESS)
                val res = action.invoke()
                notifyResult(res)
                notifyMeasurementState(MeasurementState.STOPPED)
            }, 0, interval.toMillis(), TimeUnit.MILLISECONDS)
            measurements.put(measurement, future)
        }
    }

    companion object {
        const val MEASURING_STATE = "measurement.active"
        const val MEASUREMENT_STATE_STATE = "measurement.state"
        const val MEASUREMENT_META_STATE = "measurement.meta"
        const val MEASUREMENT_RESULT_STATE = "measurement.result"
        const val MEASUREMENT_ERROR_STATE = "measurement.error"
        const val MEASUREMENT_MESSAGE_STATE = "measurement.message"
        const val MEASUREMENT_PROGRESS_STATE = "measurement.progress"

        enum class MeasurementState {
            NOT_STARTED, // initial state, not started
            IN_PROGRESS, // in progress
            WAITING, // waiting on scheduler
            STOPPED // stopped
        }

    }

}
