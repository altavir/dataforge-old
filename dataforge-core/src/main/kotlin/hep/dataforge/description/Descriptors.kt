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

package hep.dataforge.description

import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.io.MetaFileReader
import hep.dataforge.kodex.listAnnotations
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.providers.Path
import hep.dataforge.utils.Misc
import hep.dataforge.values.Value
import hep.dataforge.values.ValueProvider
import hep.dataforge.values.ValueType
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.reflect.AnnotatedElement
import java.net.URISyntaxException
import java.nio.file.Paths
import java.text.ParseException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * A variation of ValueDef for value properties
 */
@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class PropertyDef(
        val name: String,
        val type: Array<ValueType> = [ValueType.STRING],
        val multiple: Boolean = false,
        val def: String = "",
        val info: String = "",
        val required: Boolean = false,
        val allowed: Array<String> = [],
        val enumeration: KClass<*> = Any::class,
        val tags: Array<String> = []
)

object Descriptors {

    private val descriptorCache = Misc.getLRUCache<AnnotatedElement, NodeDescriptor>(500)

    fun getDelegatedValue(delegate: Meta, valueName: String?, thisRef: Any, property: KProperty<*>, def: Any?): Value {
        val name = valueName ?: property.name
        val propertyAnnotation = property.findAnnotation<PropertyDef>()
        return when {
            delegate.hasValue(name) -> delegate.getValue(name)
            propertyAnnotation != null -> Value.of(propertyAnnotation.def)
            def != null -> Value.of(def)
            else -> Descriptors.extractValue(name, delegate, Descriptors.buildDescriptor(thisRef))
        }
    }

    fun setDelegatedValue(delegate: MutableMetaNode<*>, valueName: String?, value: Value, thisRef: Any, property: KProperty<*>) {
        //TODO add check for allowed values
        delegate.setValue(valueName ?: property.name, value)
    }

    fun getDelegatedMeta(delegate: Meta, valueName: String?, thisRef: Any, property: KProperty<*>): Meta {
        val name = valueName ?: property.name
        return delegate.optMeta(name).orElseGet {
            if (thisRef is Described) {
                thisRef.descriptor.optChildDescriptor(name).map {
                    if (it.hasDefault()) {
                        it.defaultNode().first()
                    } else {
                        null
                    }
                }.orElse(Meta.empty())
            } else {
                Meta.empty()
            }
        }
    }

    fun setDelegatedMeta(delegate: MutableMetaNode<*>, valueName: String?, node: Meta, thisRef: Any, property: KProperty<*>) {
        delegate.setNode(valueName ?: property.name, node)
    }

    /**
     * Build Meta that contains all the default nodes and values from given node
     * descriptor
     *
     * @param descriptor
     * @return
     */
    @JvmStatic
    fun buildDefaultNode(descriptor: NodeDescriptor): Meta {
        val builder = MetaBuilder(descriptor.name)
        descriptor.valueDescriptors().values.stream().filter { vd -> vd.hasDefault() }.forEach { vd ->
            if (vd.hasDefault()) {
                builder.setValue(vd.name, vd.defaultValue())
            }
        }

        descriptor.childrenDescriptors().values.forEach { nd: NodeDescriptor ->
            if (nd.hasDefault()) {
                builder.setNode(nd.name, nd.defaultNode())
            } else {
                val defaultNode = buildDefaultNode(nd)
                if (!defaultNode.isEmpty) {
                    builder.setNode(defaultNode)
                }
            }
        }
        return builder
    }

    fun buildMetaFromResource(name: String, resource: String): MetaBuilder {
        try {
            val file = Paths.get(Descriptors::class.java.classLoader.getResource(resource)!!.toURI())
            return buildMetaFromFile(name, file)
        } catch (ex: IOException) {
            throw RuntimeException("Can't read resource file for descriptor", ex)
        } catch (ex: URISyntaxException) {
            throw RuntimeException("Can't read resource file for descriptor", ex)
        } catch (ex: ParseException) {
            throw RuntimeException("Can't parse resource file for descriptor", ex)
        }

    }

    @Throws(IOException::class, ParseException::class)
    fun buildMetaFromFile(name: String, file: java.nio.file.Path): MetaBuilder {
        return MetaFileReader.read(file).builder.rename(name)
    }

    @JvmStatic
    fun buildDescriptor(obj: Any): NodeDescriptor {
        return if (obj is Described) {
            obj.descriptor
        } else {
            buildDescriptor(obj.javaClass)
        }
    }

    /**
     * Build a descriptor for given Class or Method using Java annotations or restore it from cache if it was already used recently
     *
     * @param element
     * @return
     */
    @Synchronized
    @JvmStatic
    fun buildDescriptor(element: AnnotatedElement): NodeDescriptor {
        return descriptorCache.getOrPut(element) {
            NodeDescriptor(buildDescriptorMeta(element))
        }
    }

    @JvmStatic
    fun buildDescriptor(string: String): NodeDescriptor {
        try {
            val path = Path.of(string)
            return if (path.target.isEmpty() || "class" == path.target || "method" == path.target) {
                val target = findAnnotatedElement(path) ?: throw RuntimeException("Target element not found")
                buildDescriptor(target)
            } else if ("resource" == path.target) {
                NodeDescriptor(buildMetaFromResource("node", path.nameString()))
            } else {
                throw NameNotFoundException("Cant create descriptor from given target", string)
            }
        } catch (ex: Exception) {
            LoggerFactory.getLogger(Descriptors::class.java).error("Failed to build descriptor", ex)
            return NodeDescriptor(Meta.empty());
        }
    }

    fun buildDescriptor(name: String?, element: AnnotatedElement): NodeDescriptor {
        return if (name == null || name.isEmpty()) {
            buildDescriptor(element)
        } else {
            descriptorCache.computeIfAbsent(element) { e -> NodeDescriptor(buildDescriptorMeta(e).setValue("name", name)) }
        }
    }

    @JvmStatic
    fun buildDescriptorMeta(element: AnnotatedElement): MetaBuilder {
        // applying meta from resource file
        val res = if (element.isAnnotationPresent(DescriptorFileDef::class.java)) {
            val dfile = element.getAnnotation(DescriptorFileDef::class.java)
            buildMetaFromResource(dfile.name, dfile.resource)
        } else {
            MetaBuilder("")
        }

        listAnnotations(element, NodeDef::class.java, true)
                .stream()
                .filter({ it -> !it.name.startsWith("@") })
                .forEach { nodeDef ->
                    //TODO replace by map to avoid multiple node parsing
                    val exists = res.hasMeta("node") && res.getMetaList("node").stream()
                            .anyMatch { mb -> mb.getString("name") == nodeDef.name }
                    //warning on duplicate nodes
                    if (exists) {
                        LoggerFactory.getLogger(Descriptors::class.java).trace("Ignoring duplicate node with name {} in descriptor", nodeDef.name)
                    } else {

                        var nodeMeta = MetaBuilder("node")
                                .putValue("name", nodeDef.name)
                                .putValue("info", nodeDef.info)
                                .putValue("required", nodeDef.required)
                                .putValue("multiple", nodeDef.multiple)
                                .putValue("tags", nodeDef.tags)

                        // If descriptor target is present, use it
                        if (!nodeDef.from.isEmpty()) {
                            val descriptor = buildDescriptor(nodeDef.from)
                            nodeMeta = MergeRule.replace(nodeMeta, descriptor.meta)
                        }
                        putDescription(res, nodeMeta)
                    }

                }

        //Filtering hidden values
        listAnnotations(element, ValueDef::class.java, true)
                .stream()
                .filter({ it -> !it.name.startsWith("@") })
                .forEach { valueDef ->
                    val exists = res.hasMeta("value") && res.getMetaList("value").stream()
                            .anyMatch { mb -> mb.getString("name") == valueDef.name }
                    if (exists) {
                        LoggerFactory.getLogger(Descriptors::class.java).trace("Ignoring duplicate value with name {} in descriptor", valueDef.name)
                    } else {
                        putDescription(res, ValueDescriptor.build(valueDef).meta)
                    }
                }

        return res
    }

    /**
     * Put a node or value description inside existing meta builder creating intermediate nodes
     *
     * @param builder
     * @param meta
     */
    private fun putDescription(builder: MetaBuilder, meta: Meta) {
        var nodeName = Name.of(meta.getString("name"))
        var currentNode = builder
        while (nodeName.length > 1) {
            val childName = nodeName.first.toString()
            val finalCurrentNode = currentNode
            currentNode = finalCurrentNode.getMetaList("node").stream()
                    .filter { node -> node.getString("name") == childName }
                    .findFirst()
                    .orElseGet {
                        val newChild = MetaBuilder("node").setValue("name", childName)
                        finalCurrentNode.attachNode(newChild)
                        newChild
                    }
            nodeName = nodeName.cutFirst()
        }

        val childName = nodeName.toString()
        val finalCurrentNode = currentNode
        currentNode.getMetaList(meta.name).stream()
                .filter { node -> node.getString("name") == childName }
                .findFirst()
                .orElseGet {
                    val newChild = MetaBuilder(meta.name).setValue("name", childName)
                    finalCurrentNode.attachNode(newChild)
                    newChild
                }.update(meta).setValue("name", childName)
    }


    /**
     * Get the value using descriptor as a default
     *
     * @param provider
     * @param descriptor
     * @return
     */
    fun extractValue(name: String, provider: ValueProvider, descriptor: NodeDescriptor): Value {
        return provider.optValue(name).orElseGet { buildDefaultNode(descriptor).getValue(name) }
    }

    /**
     * Extract value using class descriptor
     *
     * @param name
     * @param obj
     * @return
     */
    fun extractValue(name: String, obj: Metoid): Value {
        return extractValue(name, obj.meta, buildDescriptor(obj))
    }

    /**
     * Find a class or method designated by NodeDef `target` value
     *
     * @param path
     * @return
     */
    private fun findAnnotatedElement(path: Path): AnnotatedElement? {
        if (path.target.isEmpty() || path.target == "class") {
            return try {
                Class.forName(path.name.toString())
            } catch (ex: ClassNotFoundException) {
                LoggerFactory.getLogger(Descriptors::class.java).error("Class not found by given path: " + path, ex)
                null
            }

        } else if (path.target == "method") {
            try {
                val className = path.name.cutLast().toString()
                val methodName = path.name.last.toString()

                val dClass = Class.forName(className)

                dClass.declaredMethods
                        .filter {
                            // Проверяем что это метод, у которого есть аннотация
                            it.name == methodName && (it.isAnnotationPresent(ValueDef::class.java) || it.isAnnotationPresent(NodeDef::class.java)
                                    || it.isAnnotationPresent(ValueDefs::class.java) || it.isAnnotationPresent(NodeDefs::class.java))
                        }
                        .forEach { return it }
                LoggerFactory.getLogger(Descriptors::class.java).error("Annotated method not found by given path: " + path)
                return null
            } catch (ex: ClassNotFoundException) {
                LoggerFactory.getLogger(Descriptors::class.java).error("Class not found by given path: " + path, ex)
                return null
            }

        } else {
            LoggerFactory.getLogger(Descriptors::class.java).error("Unknown target for descriptor finder: " + path.target)
            return null
        }
    }


}
