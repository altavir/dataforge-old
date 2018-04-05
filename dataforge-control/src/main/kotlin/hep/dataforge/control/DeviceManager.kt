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

package hep.dataforge.control

import hep.dataforge.connections.Connection
import hep.dataforge.context.*
import hep.dataforge.control.devices.Device
import hep.dataforge.control.devices.DeviceFactory
import hep.dataforge.control.devices.DeviceHub
import hep.dataforge.exceptions.ControlException
import hep.dataforge.exceptions.EnvelopeTargetNotFoundException
import hep.dataforge.io.envelopes.Envelope
import hep.dataforge.io.messages.Dispatcher
import hep.dataforge.io.messages.Responder
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import java.util.*
import java.util.stream.Stream

/**
 * A plugin for creating and using different devices
 * Created by darksnake on 11-Oct-16.
 */
@PluginDef(name = "devices", info = "Management plugin for devices an their interaction")
class DeviceManager : BasicPlugin(), Dispatcher, DeviceHub {

    /**
     * Registered devices
     */
    private val devices = HashMap<Name, Device>()

    override val deviceNames: Stream<Name>
        get() = devices.entries.stream().flatMap { entry ->
            if (entry.value is DeviceHub) {
                (entry.value as DeviceHub).deviceNames.map { it -> entry.key.append(it) }
            } else {
                Stream.of<Name>(entry.key)
            }
        }


    fun add(device: Device) {
        val name = Name.ofSingle(device.name)
        if (devices.containsKey(name)) {
            logger.warn("Replacing existing device in device manager!")
            remove(name)
        }
        devices[name] = device
    }

    fun remove(name: Name) {
        Optional.ofNullable(this.devices.remove(name)).ifPresent { it ->
            try {
                it.shutdown()
            } catch (e: ControlException) {
                logger.error("Failed to stop the device: " + it.name, e)
            }
        }
    }


    fun buildDevice(deviceMeta: Meta): Device {
        val factory = context
                .optService(DeviceFactory::class.java) { it.type == ControlUtils.getDeviceType(deviceMeta) }
                .orElseThrow { RuntimeException("Can't find factory for given device type") }
        val device = factory.build(context, deviceMeta)

        deviceMeta.getMetaList("connection").forEach { connectionMeta -> device.connectionHelper.connect(context, connectionMeta) }

        add(device)
        return device
    }

    @Throws(EnvelopeTargetNotFoundException::class)
    override fun getResponder(targetInfo: Meta): Responder {
        throw UnsupportedOperationException()
    }

    override fun getResponder(envelope: Envelope): Responder {
        throw UnsupportedOperationException()
    }

    override fun optDevice(name: Name): Optional<Device> {
        return when {
            name.isEmpty -> throw IllegalArgumentException("Can't provide a device with zero name")
            name.length == 1 -> Optional.ofNullable(devices[name])
            else -> Optional.ofNullable(devices[name.first]).flatMap { hub ->
                if (hub is DeviceHub) {
                    (hub as DeviceHub).optDevice(name.cutFirst())
                } else {
                    Optional.empty()
                }
            }
        }
    }

    /**
     * Get the stream of top level devices
     *
     * @return
     */
    fun getDevices(): Stream<Device> {
        return devices.values.stream()
    }

    override fun detach() {
        devices.values.forEach { it ->
            try {
                it.shutdown()
            } catch (e: ControlException) {
                logger.error("Failed to stop the device: " + it.name, e)
            }
        }
        super.detach()
    }

    override fun connectAll(connection: Connection, vararg roles: String) {
        this.devices.values.forEach { device -> device.connect(connection, *roles) }
    }

    override fun connectAll(context: Context, meta: Meta) {
        this.devices.values.forEach { device -> device.connectionHelper.connect(context, meta) }
    }

    class Factory : PluginFactory() {

        override val type: Class<out Plugin>
            get() = DeviceManager::class.java

        override fun build(meta: Meta): Plugin {
            return DeviceManager()
        }
    }
}
