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
import hep.dataforge.kodex.listAnnotations
import hep.dataforge.meta.*
import hep.dataforge.providers.Path
import hep.dataforge.utils.Misc
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.reflect.AnnotatedElement
import java.net.URISyntaxException
import java.nio.file.Paths
import java.text.ParseException
import java.time.Instant
import java.time.LocalDateTime
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

object Descriptors {

    private val descriptorCache = Misc.getLRUCache<String, NodeDescriptor>(500)

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
    fun buildMetaFromFile(name: String, file: java.nio.file.Path): MetaBuilder {
        return MetaFileReader.read(file).builder.rename(name)
    }


    /**
     * Build a descriptor for given Class or Method using Java annotations or restore it from cache if it was already used recently
     *
     * @param element
     * @return
     */
    @JvmStatic
    fun buildDescriptor(element: AnnotatedElement): NodeDescriptor {
        return builder(element).build()
    }

    @JvmStatic
    fun getDescriptor(string: String): NodeDescriptor {
        return descriptorCache.getOrPut(string) {
            try {
                val path = Path.of(string)
                when (path.target) {
                    "", "class", "method", "property" -> {
                        val target = findAnnotatedElement(path)
                                ?: throw RuntimeException("Target element $path not found")
                        buildDescriptor(target)
                    }
                    "file" -> return NodeDescriptor(MetaFileReader.read(Global.getFile(path.name.toString()).absolutePath))
                    "resource" -> NodeDescriptor(buildMetaFromResource("node", path.name.toString()))
                    else -> throw NameNotFoundException("Cant create descriptor from given target", string)
                }
            } catch (ex: Exception) {
                LoggerFactory.getLogger(Descriptors::class.java).error("Failed to build descriptor", ex)
                NodeDescriptor(Meta.empty());
            }
        }
    }

    /**
     * Get the value using descriptor as a default
     *
     * @param provider
     * @param descriptor
     * @return
     */
    fun extractValue(name: String, descriptor: NodeDescriptor): Value {
        return buildDefaultNode(descriptor).getValue(name)
    }

    /**
     * Find a class or method designated by NodeDef `target` value
     *
     * @param path
     * @return
     */
    private fun findAnnotatedElement(path: Path): KAnnotatedElement? {
        try {
            if (path.target.isEmpty() || path.target == "class") {
                return Class.forName(path.name.toString()).kotlin

            } else if (path.target == "method") {

                val className = path.name.cutLast().toString()
                val methodName = path.name.last.toString()
                val dClass = Class.forName(className).kotlin
                val res = dClass.memberFunctions.find { it.name == methodName }
                if (res == null) {
                    LoggerFactory.getLogger(Descriptors::class.java).error("Annotated method not found by given path: $path")
                }
                return res

            } else if (path.target == "property") {
                val className = path.name.cutLast().toString()
                val methodName = path.name.last.toString()
                val dClass = Class.forName(className).kotlin
                val res = dClass.memberProperties.find { it.name == methodName }
                if (res == null) {
                    LoggerFactory.getLogger(Descriptors::class.java).error("Annotated property not found by given path: $path")
                }
                return res
            } else {
                LoggerFactory.getLogger(Descriptors::class.java).error("Unknown target for descriptor finder: " + path.target)
                return null
            }
        } catch (ex: ClassNotFoundException) {
            LoggerFactory.getLogger(Descriptors::class.java).error("Class not fond by given path: $path", ex)
            return null
        }
    }

    /*--------------------*/

    fun builder(element: AnnotatedElement): DescriptorBuilder {
        //TODO use [Descriptor] annotation
        val builder = DescriptorBuilder("meta")

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

        return builder
    }

    private val KAnnotatedElement.description: String
        get() = findAnnotation<Description>()?.value ?: ""

    /**
     * Build value descriptor based on property and its delegate
     */
    private fun buildValueDescriptor(property: KProperty1<*, *>, delegate: ValueDelegate<*>): ValueDescriptor {
        val name = delegate.name ?: property.name
        val type = property.returnType.jvmErasure
        val valueTypes = when {
            type.isSubclassOf(Number::class) -> listOf(ValueType.NUMBER)
            type.isSubclassOf(String::class) -> listOf(ValueType.STRING)
            type.isSubclassOf(Boolean::class) -> listOf(ValueType.BOOLEAN)
            type.isSubclassOf(Instant::class) -> listOf(ValueType.TIME)
            type.isSubclassOf(LocalDateTime::class) -> listOf(ValueType.TIME)
            else -> emptyList()
        }

        val allowedValues: List<Any> = if (delegate is EnumValueDelegate<*>) {
            delegate.type.java.enumConstants.map { type.cast(it) }
        } else {
            emptyList()
        }

        val descriptor = ValueDescriptor.build(
                name = name,
                info = property.description,
                multiple = type.isSubclassOf(List::class),
                required = delegate.def == null,
                types = valueTypes,
                defaultValue = delegate.defaultValue,
                allowedValues = allowedValues
        )

        /**
         * Use value annotation if it is present
         */
        val annotation = property.findAnnotation<ValueDef>()

        return if (annotation != null) {
            ValueDescriptor.merge(descriptor, ValueDescriptor.build(annotation))
        } else {
            descriptor
        }
    }

    /**
     * Descriptor resolution order:
     * 1. Delegate fields + [Description] annotation
     * 2. Annotation description
     * 3. External descriptor
     */
    private fun buildNodeDescriptor(property: KProperty1<*, *>, delegate: MetaDelegate): NodeDescriptor {
        val builder = DescriptorBuilder(delegate.name ?: property.name).apply {
            info = property.description

            //Use property annotation to describe node
            property.annotations.filterIsInstance<NodeDef>()
                    .filter { it -> !it.key.startsWith("@") }
                    .forEach { nodeDef -> node(nodeDef) }

            //Filtering hidden values
            property.annotations.filterIsInstance<ValueDef>()
                    .filter { it -> !it.key.startsWith("@") }
                    .forEach { valueDef ->
                        value(ValueDescriptor.build(valueDef))
                    }

            //default = delegate.def
        }
        property.findAnnotation<Descriptor>()?.let { builder.update(getDescriptor(it.value)) }

        return builder.build()
    }

    /**
     * Build descriptor for
     */
    fun <T : Any> buildDescriptor(target: T): NodeDescriptor {
        val builder = builder(target::class.java)

        @Suppress("UNCHECKED_CAST")
        val type: KClass<T> = target::class as KClass<T>

        type.listAnnotations<Description>().firstOrNull()?.let {
            builder.info = it.value
        }

        type.memberProperties.forEach { property ->
            val delegate = property.getDelegate(target)
            when (delegate) {
                is ValueDelegate<*> -> {
                    builder.value(buildValueDescriptor(property, delegate))
                }
                is NodeDelegate<*> -> {
                    builder.node(buildNodeDescriptor(property, delegate))
                }
                is NodeListDelegate<*> -> {
                    builder.node(buildNodeDescriptor(property, delegate))
                }
            }
        }


        return builder.build()
    }

}