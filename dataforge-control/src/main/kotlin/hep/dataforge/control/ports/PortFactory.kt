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
package hep.dataforge.control.ports

import hep.dataforge.description.ValueDef
import hep.dataforge.description.ValueDefs
import hep.dataforge.exceptions.ControlException
import hep.dataforge.kodex.set
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.utils.MetaFactory
import java.util.*

/**
 *
 * @author [Alexander Nozik](mailto:altavir@gmail.com)
 */
object PortFactory : MetaFactory<Port> {

    private val portMap = HashMap<Meta, Port>()


    @ValueDefs(
            ValueDef(name = "type", def = "tcp", info = "The type of the port"),
            ValueDef(name = "address", required = true, info = "The specific designation of this port according to type"),
            ValueDef(name = "type", def = "tcp", info = "The type of the port")
    )
    override fun build(meta: Meta): Port {
        val protocol = meta.getString("type", "tcp")
        val port = when (protocol) {
            "com" -> ComPort(meta)
            "tcp" -> TcpPort(meta);
            "virtual" -> buildVirtualPort(meta)
            else -> throw ControlException("Unknown protocol")
        }
        return portMap.getOrPut(port.meta) { port }
    }

    private fun buildVirtualPort(meta: Meta): Port {
        val className = meta.getString("class")
        val theClass = Class.forName(className)
        return theClass.getDeclaredConstructor(Meta::class.java).newInstance(meta) as Port
    }

    /**
     * Create new port or reuse existing one if it is already created
     * @param portName
     * @return
     * @throws ControlException
     */
    fun build(portName: String): Port {
        return build(nameToMeta(portName))
    }

    private fun nameToMeta(portName: String): Meta {
        val builder = MetaBuilder("port")
                .setValue("name", portName)

        when {
            portName.contains("::") -> {
                val split = portName.split("::".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                builder["type"] = split[0]
                builder["address"] = split[1]
            }
            portName.contains(".") -> {
                builder["type"] = "tcp"
                builder["ip"] = portName
            }
            else -> {
                builder["type"] = "com"
                builder["address"] = portName
            }
        }
        return builder.build();
    }
}
