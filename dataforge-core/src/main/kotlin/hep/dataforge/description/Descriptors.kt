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

import hep.dataforge.context.Global
import hep.dataforge.exceptions.NameNotFoundException
import hep.dataforge.io.MetaFileReader
import hep.dataforge.listAnnotations
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.providers.Path
import hep.dataforge.utils.Misc
import hep.dataforge.values.ValueFactory
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.reflect.AnnotatedElement
import java.net.URISyntaxException
import java.nio.file.Paths
import java.text.ParseException
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

object Descriptors {

    private val descriptorCache = Misc.getLRUCache<String, NodeDescriptor>(1000)

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
                builder.setValue(vd.name, vd.default)
            }
        }

        descriptor.childrenDescriptors().values.forEach { nd: NodeDescriptor ->
            if (nd.hasDefault()) {
                builder.setNode(nd.name, nd.default)
            } else {
                val defaultNode = buildDefaultNode(nd)
                if (!defaultNode.isEmpty) {
                    builder.setNode(defaultNode)
                }
            }
        }
        return builder
    }

    private fun buildMetaFromResource(name: String, resource: String): MetaBuilder {
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
    private fun buildMetaFromFile(name: String, file: java.nio.file.Path): MetaBuilder {
        return MetaFileReader.read(file).builder.rename(name)
    }

    /**
     * Find a class or method designated by NodeDef `target` value
     *
     * @param path
     * @return
     */
    private fun findAnnotatedElement(path: Path): KAnnotatedElement? {
        try {
            when {
                path.target.isEmpty() || path.target == "class" -> return Class.forName(path.name.toString()).kotlin
                path.target == "method" -> {
                    val className = path.name.cutLast().toString()
                    val methodName = path.name.last.toString()
                    val dClass = Class.forName(className).kotlin
                    val res = dClass.memberFunctions.find { it.name == methodName }
                    if (res == null) {
                        LoggerFactory.getLogger(Descriptors::class.java).error("Annotated method not found by given path: $path")
                    }
                    return res

                }
                path.target == "property" -> {
                    val className = path.name.cutLast().toString()
                    val methodName = path.name.last.toString()
                    val dClass = Class.forName(className).kotlin
                    val res = dClass.memberProperties.find { it.name == methodName }
                    if (res == null) {
                        LoggerFactory.getLogger(Descriptors::class.java).error("Annotated property not found by given path: $path")
                    }
                    return res
                }
                else -> {
                    LoggerFactory.getLogger(Descriptors::class.java).error("Unknown target for descriptor finder: " + path.target)
                    return null
                }
            }
        } catch (ex: ClassNotFoundException) {
            LoggerFactory.getLogger(Descriptors::class.java).error("Class not fond by given path: $path", ex)
            return null
        }
    }

    fun forDef(def: NodeDef): NodeDescriptor? {
        val element = when {
            def.type == Any::class -> if(def.descriptor.isEmpty()) {
                null
            } else{
                findAnnotatedElement(Path.of(def.descriptor))
            }
            else -> def.type
        }
        return element?.let {
            describe(def.key, it).apply {
                info = def.info
                multiple = def.multiple
                required = def.required
                this.tags = def.tags.toList()
            }.build()
        }
    }

    private fun describe(name: String, element: KAnnotatedElement): DescriptorBuilder {
        //TODO use [Descriptor] annotation
        val builder = DescriptorBuilder(name)

        element.listAnnotations<NodeDef>(true)
                .stream()
                .filter { it -> !it.key.startsWith("@") }
                .forEach { nodeDef ->
                    builder.node(nodeDef)
                }

        //Filtering hidden values
        element.listAnnotations<ValueDef>(true)
                .stream()
                .filter { it -> !it.key.startsWith("@") }
                .forEach { valueDef ->
                    builder.value(ValueDescriptor.build(valueDef))
                }

        element.findAnnotation<Description>()?.let {
            builder.info = it.value
        }

        if (element is KProperty<*>) {
            LoggerFactory.getLogger(Descriptors::class.java).warn("Property node descriptor not implemented")
        }

        if (element is KClass<*>) {
            element.declaredMemberProperties.forEach { property ->
                try {
                    property.findAnnotation<ValueProperty>()?.let {
                        val propertyName = if (it.name.isEmpty()) {
                            property.name
                        } else {
                            it.name
                        }
                        builder.value(
                                name = propertyName,
                                info = property.description,
                                multiple = it.multiple,
                                defaultValue = ValueFactory.parse(it.def),
                                required = it.def.isEmpty(),
                                allowedValues = if (it.enumeration == Any::class) {
                                    emptyList()
                                } else {
                                    it.enumeration.java.enumConstants.map { it.toString() }
                                },
                                types = it.type.toList()
                        )
                    }

                    property.findAnnotation<NodeProperty>()?.let {
                        val nodeName = if (it.name.isEmpty()) property.name else it.name
                        builder.node(describe(nodeName, property).build())
                    }
                } catch (ex: Exception) {
                    LoggerFactory.getLogger(Descriptors::class.java).warn("Failed to construct descriptor from property {}", property.name)
                }
            }
        }


        return builder
    }

    private fun describe(name: String, element: AnnotatedElement): NodeDescriptor {
        //TODO use [Descriptor] annotation
        val builder = DescriptorBuilder(name)

        element.listAnnotations(NodeDef::class.java, true)
                .stream()
                .filter { it -> !it.key.startsWith("@") }
                .forEach { nodeDef ->
                    builder.node(nodeDef)
                }

        //Filtering hidden values
        element.listAnnotations(ValueDef::class.java, true)
                .stream()
                .filter { it -> !it.key.startsWith("@") }
                .forEach { valueDef ->
                    builder.value(ValueDescriptor.build(valueDef))
                }

        return builder.build()
    }

    private val KAnnotatedElement.description: String
        get() = findAnnotation<Description>()?.value ?: ""

//    /**
//     * Build value descriptor based on property and its delegate
//     */
//    private fun buildValueDescriptor(property: KProperty1<*, *>): ValueDescriptor {
//        val def = property.findAnnotation<ValueDef>()
//
//        val type = property.returnType.jvmErasure
//        val valueTypes = when {
//            type.isSubclassOf(Number::class) -> listOf(ValueType.NUMBER)
//            type.isSubclassOf(String::class) -> listOf(ValueType.STRING)
//            type.isSubclassOf(Boolean::class) -> listOf(ValueType.BOOLEAN)
//            type.isSubclassOf(Instant::class) -> listOf(ValueType.TIME)
//            type.isSubclassOf(LocalDateTime::class) -> listOf(ValueType.TIME)
//            else -> emptyList()
//        }
//
//        if (def == null) {
//            val name = property.name
//
//            return ValueDescriptor.build(
//                    name = name,
//                    required = true,
//                    multiple = type is Collection<*>,
//                    types = valueTypes,
//                    info = property.description
//            )
//        } else {
//
//        }


//        val name = def?.key ?: property.name
//        val type = property.returnType.jvmErasure
//        val valueTypes = when {
//            type.isSubclassOf(Number::class) -> listOf(ValueType.NUMBER)
//            type.isSubclassOf(String::class) -> listOf(ValueType.STRING)
//            type.isSubclassOf(Boolean::class) -> listOf(ValueType.BOOLEAN)
//            type.isSubclassOf(Instant::class) -> listOf(ValueType.TIME)
//            type.isSubclassOf(LocalDateTime::class) -> listOf(ValueType.TIME)
//            else -> emptyList()
//        }
//
//        val allowedValues: List<Any> = if (delegate is EnumValueDelegate<*>) {
//            delegate.type.java.enumConstants.map { type.cast(it) }
//        } else {
//            emptyList()
//        }
//
//        val descriptor = ValueDescriptor.build(
//                name = name,
//                info = property.description,
//                multiple = type.isSubclassOf(List::class),
//                required = def?.def == null,
//                types = valueTypes,
//                defaultValue = delegate.defaultValue,
//                allowedValues = allowedValues
//        )
//
//        /**
//         * Use value annotation if it is present
//         */
//        val annotation = property.findAnnotation<ValueDef>()
//
//        return if (annotation != null) {
//            ValueDescriptor.merge(descriptor, ValueDescriptor.build(annotation))
//        } else {
//            descriptor
//        }
//    }
//
//    /**
//     * Descriptor resolution order:
//     * 1. Delegate fields + [Description] annotation
//     * 2. Annotation description
//     * 3. External descriptor
//     */
//    private fun buildNodeDescriptor(property: KProperty1<*, *>, def: NodeDef?): NodeDescriptor {
//        val builder = DescriptorBuilder(delegate.name ?: property.name).apply {
//            info = property.description
//
//            //Use property annotation to describe node
//
//            TODO("Won't work")
//            property.annotations.filterIsInstance<NodeDef>()
//                    .filter { it -> !it.key.startsWith("@") }
//                    .forEach { nodeDef -> node(nodeDef) }
//
//            //Filtering hidden values
//            property.annotations.filterIsInstance<ValueDef>()
//                    .filter { it -> !it.key.startsWith("@") }
//                    .forEach { valueDef ->
//                        value(ValueDescriptor.build(valueDef))
//                    }
//
//            //default = delegate.def
//        }
//        property.findAnnotation<Descriptor>()?.let { builder.update(forName(it.value)) }
//
//        return builder.build()
//    }

//    /**
//     * Build descriptor for given instance
//     */
//    @JvmStatic
//    fun <T : Any> forObject(target: T): NodeDescriptor {
//        val builder = builder(target::class)
//
//        @Suppress("UNCHECKED_CAST")
//        val type: KClass<T> = target::class as KClass<T>
//
//        type.listAnnotations<Description>().firstOrNull()?.let {
//            builder.info = it.value
//        }
//
//        type.memberProperties.forEach { property ->
//            if (property.isAccessible) {
//                val delegate = property.getDelegate(target)
//                when (delegate) {
//                    is ValueDelegate<*> -> {
//                        builder.value(buildValueDescriptor(property, delegate))
//                    }
//                    is NodeDelegate<*> -> {
//                        builder.node(buildNodeDescriptor(property, delegate))
//                    }
//                    is NodeListDelegate<*> -> {
//                        builder.node(buildNodeDescriptor(property, delegate))
//                    }
//                }
//            }
//        }
//
//
//        return builder.build()
//    }

    /**
     * Build a descriptor for given Class or Method using Java annotations or restore it from cache if it was already used recently
     *
     * @param element
     * @return
     */
    @JvmStatic
    fun forType(name: String, element: KAnnotatedElement): NodeDescriptor {
        return descriptorCache.getOrPut(element.toString()) { describe(name, element).build() }
    }

    @JvmStatic
    fun forType(name: String, element: AnnotatedElement): NodeDescriptor {
        return descriptorCache.getOrPut(element.toString()) { describe(name, element) }
    }

    @JvmStatic
    fun forName(name: String, string: String): NodeDescriptor {
        return descriptorCache.getOrPut(string) {
            try {
                val path = Path.of(string)
                when (path.target) {
                    "", "class", "method", "property" -> {
                        val target = findAnnotatedElement(path)
                                ?: throw RuntimeException("Target element $path not found")
                        forType(name, target)
                    }
                    "file" -> return NodeDescriptor(MetaFileReader.read(Global.getFile(path.name.toString()).absolutePath).builder.setValue("name", name))
                    "resource" -> NodeDescriptor(buildMetaFromResource("node", path.name.toString()).builder.setValue("name", name))
                    else -> throw NameNotFoundException("Cant create descriptor from given target", string)
                }
            } catch (ex: Exception) {
                LoggerFactory.getLogger(Descriptors::class.java).error("Failed to build descriptor", ex)
                NodeDescriptor(Meta.empty());
            }
        }
    }

}