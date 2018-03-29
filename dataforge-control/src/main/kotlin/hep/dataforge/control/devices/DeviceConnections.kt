/*
 * Copyright  2018 Alexander Nozik.
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

import hep.dataforge.meta.Meta
import hep.dataforge.values.Value
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlin.coroutines.experimental.CoroutineContext

/**
 * A listener that
 */
class SensorResultListener(capacity: Int = 50, private val coroutineContext: CoroutineContext): DeviceListener{
    val results = Channel<Meta>(capacity)
    val messages = Channel<String>(capacity)

    override fun notifyStateChanged(device: Device, name: String, state: Value) {
        async(coroutineContext) {
            when (name) {
                Sensor.MEASUREMENT_MESSAGE_STATE -> messages.send(state.stringValue())
            }
        }
    }

    override fun notifyMetaStateChanged(device: Device, name: String, state: Meta) {
        async(coroutineContext) {
            when (name) {
                Sensor.MEASUREMENT_RESULT_STATE -> results.send(state)
            }
        }
    }
}