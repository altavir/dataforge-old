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

package hep.dataforge.control.devices

import hep.dataforge.control.connections.DeviceConnection
import hep.dataforge.meta.Meta
import hep.dataforge.values.Value
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch

/**
 * Aggregator and transformer for device meta states like measurement results
 */
class Aggregator<out R>(buffer: Int = 100,val key: String = Sensor.MEASUREMENT_RESULT_STATE, private val converter: (Meta) -> R) : DeviceConnection(), DeviceListener {

    private val channel = Channel<R>(buffer)

    val receiver: ReceiveChannel<R> = channel;

    val result: R
        get() = converter.invoke(device.getMetaState(Sensor.MEASUREMENT_RESULT_STATE))


    override fun notifyStateChanged(device: Device, name: String, state: Value) {
        //Do nothing
    }

    override fun notifyMetaStateChanged(device: Device, name: String, state: Meta) {
        if (name == key) {
            launch {
                channel.send(converter.invoke(state))
            }
        }
    }

}