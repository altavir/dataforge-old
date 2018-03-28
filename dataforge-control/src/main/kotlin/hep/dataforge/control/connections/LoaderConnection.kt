/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections

import hep.dataforge.connections.Connection
import hep.dataforge.exceptions.StorageException
import hep.dataforge.storage.api.TableLoader
import hep.dataforge.tables.ValuesListener
import hep.dataforge.values.Values
import org.slf4j.LoggerFactory

/**
 *
 * @author Alexander Nozik
 */
class LoaderConnection(private val loader: TableLoader): Connection, ValuesListener {

    override fun accept(point: Values) {
        try {
            loader.push(point)
        } catch (ex: StorageException) {
            LoggerFactory.getLogger(javaClass).error("Error while pushing data", ex)
        }

    }

    override fun isOpen(): Boolean {
        return loader.isOpen
    }

    override fun open(`object`: Any) {

    }

    @Throws(Exception::class)
    override fun close() {
        loader.close()
    }

}
