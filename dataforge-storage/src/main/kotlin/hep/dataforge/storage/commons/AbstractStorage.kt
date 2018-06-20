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
package hep.dataforge.storage.commons

import hep.dataforge.connections.ConnectionHelper
import hep.dataforge.context.Context
import hep.dataforge.events.Event
import hep.dataforge.events.EventHandler
import hep.dataforge.exceptions.StorageException
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaHolder
import hep.dataforge.names.Name
import hep.dataforge.providers.Provides
import hep.dataforge.storage.api.Loader
import hep.dataforge.storage.api.Storage
import hep.dataforge.storage.api.Storage.Companion.LOADER_TARGET
import hep.dataforge.storage.api.Storage.Companion.STORAGE_TARGET
import hep.dataforge.storage.commons.StorageUtils.buildPath
import java.util.*

/**
 * Конфигурации загрузчиков хранятся в оперативной памяти. Те, что поставляются
 * вместе с сервером загружаются в конструкторе, остальные загружаются только на
 * время выполнения. Для того, чтобы сделать другой формат хранения, нужно
 * переопредлить методы `setupLoader` и `overrideLoader`
 *
 * @author Darksnake
 */
abstract class AbstractStorage : MetaHolder, Storage {

    protected val loaders: MutableMap<String, Loader> = HashMap()
    protected val shelves: MutableMap<String, Storage> = HashMap()
    override val name: String
    override val context: Context
    /**
     * @return the parent
     */
    override val parent: Storage?
    private val connectionHelper: ConnectionHelper

    override val isOpen: Boolean
        get() = true

    val isRoot: Boolean
        get() = parent == null


    protected constructor(parent: Storage, name: String, meta: Meta) : super(Laminate(meta, parent.meta)) {
        this.name = name.replace(".", "_")
        this.parent = parent
        context = parent.context
        connectionHelper = ConnectionHelper(this)
    }

    protected constructor(context: Context, meta: Meta) : super(meta) {
        this.name = meta.getString("name", "").replace(".", "_")
        this.context = context
        this.parent = null
        connectionHelper = ConnectionHelper(this)
    }

    override fun getConnectionHelper(): ConnectionHelper {
        return connectionHelper
    }

    /**
     * Initialize this storage.
     *
     * @throws hep.dataforge.exceptions.StorageException
     */
    @Throws(StorageException::class)
    override fun open() {

    }

    /**
     * Refresh the state of storage
     *
     * @throws StorageException
     */
    @Throws(StorageException::class)
    override fun refresh() {

    }

    /**
     * Close the storage
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun close() {
        logger.debug("Closing storage {}", fullName)
        for (shelf in shelves()) {
            shelf.close()
        }
        for (loader in loaders()) {
            loader.close()
        }
    }

    /**
     * Create shelf with given name and inherited configuration. By default is
     * equivalent of `buildShelf(shelfName, null)`
     *
     * @param shelfName
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun buildShelf(shelfName: String): Storage {
        return buildShelf(shelfName, Meta.empty())
    }

    @Throws(StorageException::class)
    override fun buildLoader(loaderName: String, loaderConfiguration: Meta): Loader {
        val name = Name.of(loaderName)
        if (name.length == 1) {
            val loader = createLoader(loaderName, loaderConfiguration)
            this.loaders[loaderName] = loader
            return loader
        } else {
            //delegate building to child storage
            return buildPath(this, name.cutLast()).buildLoader(name.last.toString(), loaderConfiguration)
        }
    }

    /**
     * Create a child loader but do not add it to loader list
     *
     * @param loaderName
     * @param loaderConfiguration
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    protected abstract fun createLoader(loaderName: String, loaderConfiguration: Meta): Loader


    @Throws(StorageException::class)
    override fun buildShelf(shelfName: String, shelfConfiguration: Meta): Storage {
        val name = Name.of(shelfName)
        if (name.length == 1) {
            val shelf = createShelf(shelfConfiguration, shelfName)
            this.shelves[shelfName] = shelf
            return shelf
        } else {
            //delegate building to child storage
            return buildPath(this, name.cutLast()).buildShelf(name.last.toString(), shelfConfiguration)
        }
    }

    /**
     * Create a direct child shelf but do not add it to shelf list
     *
     * @param shelfConfiguration
     * @param shelfName
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    protected abstract fun createShelf(shelfConfiguration: Meta, shelfName: String): Storage

    /**
     * update an annotation of loader using overriding annotation
     *
     * @param currentLoader
     * @param newAnnotation
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    protected fun overrideLoader(currentLoader: Loader, newAnnotation: Meta): Loader {
        return if (currentLoader.meta.equalsIgnoreName(newAnnotation)) {
            currentLoader
        } else {
            throw StorageException("Can't update loader with new annotation")
        }
    }


    @Provides(LOADER_TARGET)
    override fun optLoader(name: String): Optional<Loader> {
        return Optional.ofNullable(loaders[name])
    }


    @Provides(STORAGE_TARGET)
    override fun optShelf(name: String): Optional<Storage> {
        val shelfName = Name.of(name)
        return if (shelfName.length == 0) {
            Optional.of(this)
        } else if (shelfName.length == 1) {
            Optional.ofNullable(shelves[name])
        } else {
            optShelf(shelfName.first.toString()).flatMap { child -> child.optShelf(shelfName.cutFirst().toString()) }
        }
    }

    /**
     * map of direct descendants
     *
     * @return
     * @throws StorageException
     */
    @Throws(StorageException::class)
    override fun loaders(): Collection<Loader> {
        return Collections.unmodifiableCollection(loaders.values)
    }

    @Throws(StorageException::class)
    override fun shelves(): Collection<Storage> {
        return Collections.unmodifiableCollection(shelves.values)
    }

//    @NodeDef(key = "security", info = "Some information for  security manager")
//    override fun respond(message: Envelope): Envelope {
//        val responder = getResponder(message)
//        return if (responder == this) {
//            //TODO add security management here
//            //TODO implement
//            errorResponseBase("", UnsupportedOperationException("Not supported yet.")).build()
//        } else {
//            responder.respond(message)
//        }
//    }

    //    @Override
    //    public boolean acceptEnvelope(Envelope envelope) {
    //        if (envelope.meta().hasMeta(ENVELOPE_DESTINATION_NODE)) {
    //            Meta target = envelope.meta().getMeta(ENVELOPE_DESTINATION_NODE);
    //            String targetType = target.getString(TARGET_TYPE_KEY, STORAGE_TARGET_TYPE);
    //            if (targetType.equals(STORAGE_TARGET_TYPE)) {
    //                String targetName = target.getString(TARGET_NAME_KEY);
    //                return targetName.endsWith(getName());
    //            } else {
    //                return false;
    //            }
    //        } else {
    //            LoggerFactory.getLogger(getClass()).debug("Envelope does not have target. Acepting by default.");
    //            return true;
    //        }
    //    }
    //
    //    @Override
    //    public Meta destinationMeta() {
    //        return new MetaBuilder(ENVELOPE_DESTINATION_NODE)
    //                .putValue(TARGET_TYPE_KEY, STORAGE_TARGET_TYPE)
    //                .putValue(TARGET_NAME_KEY, getName())
    //                .builder();
    //    }

//    @NodeDef(key = DEFAULT_META_NAME, info = "A meta for sotrage or loader creation. Only used if 'allowCreate' is true.")
//    override fun getResponder(targetInfo: Meta): Responder {
//        val targetType = targetInfo.getString(TARGET_TYPE_KEY, Storage.LOADER_TARGET)
//        val targetName = targetInfo.getString(TARGET_NAME_KEY, "")
//        val allowCreate = targetInfo.getBoolean("allowCreate", true)
//        val addMeta = targetInfo.getMeta(DEFAULT_META_NAME, Meta.empty())
//        try {
//            when (targetType) {
//                Storage.STORAGE_TARGET -> return if (targetName.isEmpty()) {
//                    this
//                } else {
//                    optShelf(targetName).orElseGet {
//                        if (allowCreate) {
//                            //TODO add some path parsing cutting first segment if it is the same as this storage name
//                            optShelf(targetName).orElseGet { buildShelf(targetName, addMeta) }
//                        } else {
//                            throw EnvelopeTargetNotFoundException(targetType, targetName, targetInfo)
//                        }
//                    }
//                }
//                Storage.LOADER_TARGET -> return optLoader(targetName).orElseGet {
//                    if (allowCreate) {
//                        optLoader(targetName).orElseGet { buildLoader(targetName, addMeta) }
//                    } else {
//                        throw EnvelopeTargetNotFoundException(targetType, targetName, targetInfo)
//                    }
//                }
//                else -> throw EnvelopeTargetNotFoundException(targetType, targetName, targetInfo)
//            }
//        } catch (ex: StorageException) {
//            throw EnvelopeTargetNotFoundException(targetType, targetName, targetInfo)
//        }
//
//    }

    /**
     * Notify all connections which can handle events
     *
     * @param event
     */
    protected fun dispatchEvent(event: Event) {
        forEachConnection(EventHandler::class.java) { eventHandler -> eventHandler.pushEvent(event) }
    }

}
