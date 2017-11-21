/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.MergeRule;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Name;
import hep.dataforge.providers.Path;
import hep.dataforge.utils.Misc;
import hep.dataforge.values.Value;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

/**
 * Tools to work with descriptors
 *
 * @author Alexander Nozik
 */
public class DescriptorUtils {

    private static final Map<AnnotatedElement, NodeDescriptor> descriptorCache = Misc.getLRUCache(500);

    /**
     * Build Meta that contains all the default nodes and values from given node
     * descriptor
     *
     * @param descriptor
     * @return
     */
    public static Meta buildDefaultNode(NodeDescriptor descriptor) {
        MetaBuilder builder = new MetaBuilder(descriptor.getName());
        descriptor.valueDescriptors().values().stream().filter((vd) -> (vd.hasDefault())).forEach((vd) -> {
            if (vd.hasDefault()) {
                builder.setValue(vd.getName(), vd.defaultValue());
            }
        });

        descriptor.childrenDescriptors().values().forEach((NodeDescriptor nd) -> {
            if (nd.hasDefault()) {
                builder.setNode(nd.getName(), nd.defaultNode());
            } else {
                Meta defaultNode = buildDefaultNode(nd);
                if (!defaultNode.isEmpty()) {
                    builder.setNode(defaultNode);
                }
            }
        });
        return builder;
    }

    public static MetaBuilder buildMetaFromResource(String name, String resource) {
        try {
            java.nio.file.Path file = Paths.get(DescriptorUtils.class.getClassLoader().getResource(resource).toURI());
            return buildMetaFromFile(name, file);
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException("Can't read resource file for descriptor", ex);
        } catch (ParseException ex) {
            throw new RuntimeException("Can't parse resource file for descriptor", ex);
        }
    }

    public static MetaBuilder buildMetaFromFile(String name, java.nio.file.Path file) throws IOException, ParseException {
        return MetaFileReader.read(file).getBuilder().rename(name);
    }

    public static NodeDescriptor buildDescriptor(Object obj) {
        if (obj instanceof Described) {
            return ((Described) obj).getDescriptor();
        } else {
            return buildDescriptor(obj.getClass());
        }
    }

    /**
     * Build a descriptor for given Class or Method using Java annotations or restore it from cache if it was already used recently
     *
     * @param element
     * @return
     */
    public static synchronized NodeDescriptor buildDescriptor(AnnotatedElement element) {
        if (descriptorCache.containsKey(element)) {
            return descriptorCache.get(element);
        } else {
            NodeDescriptor descriptor = new NodeDescriptor(buildDescriptorMeta(element));
            descriptorCache.put(element, descriptor);
            return descriptor;
        }
    }

    public static NodeDescriptor buildDescriptor(String string) {
        Path path = Path.of(string);
        if (path.getTarget().isEmpty() || "class".equals(path.getTarget()) || "method".equals(path.getTarget())) {
            return buildDescriptor(findAnnotatedElement(path));
        } else if ("resource".equals(path.getTarget())) {
            return new NodeDescriptor(buildMetaFromResource("node", path.nameString()));
        } else {
            throw new NameNotFoundException("Cant create descriptor from given target", string);
        }

    }

    public static NodeDescriptor buildDescriptor(String name, AnnotatedElement element) {
        if (name == null || name.isEmpty()) {
            return buildDescriptor(element);
        } else {
            return descriptorCache.computeIfAbsent(element, e -> new NodeDescriptor(buildDescriptorMeta(e).setValue("name", name)));
        }
    }

    public static MetaBuilder buildDescriptorMeta(AnnotatedElement element) {
        MetaBuilder res;
        // applying meta from resource file
        if (element.isAnnotationPresent(DescriptorFileDef.class)) {
            DescriptorFileDef dfile = element.getAnnotation(DescriptorFileDef.class);
            res = buildMetaFromResource(dfile.name(), dfile.resource());
        } else {
            res = new MetaBuilder("");
        }

        listAnnotations(element, NodeDef.class, true)
                .stream()
                .filter(it -> !it.name().startsWith("@"))
                .forEach(nodeDef -> {
                            //TODO replace by map to avoid multiple node parsing
                            boolean exists = res.hasMeta("node") && res.getMetaList("node").stream()
                                    .anyMatch(mb -> mb.getString("name").equals(nodeDef.name()));
                            //warning on duplicate nodes
                            if (exists) {
                                LoggerFactory.getLogger(DescriptorUtils.class).trace("Ignoring duplicate node with name {} in descriptor", nodeDef.name());
                            } else {

                                MetaBuilder nodeMeta = new MetaBuilder("node")
                                        .putValue("name", nodeDef.name())
                                        .putValue("info", nodeDef.info())
                                        .putValue("required", nodeDef.required())
                                        .putValue("multiple", nodeDef.multiple())
                                        .putValue("tags", nodeDef.tags());

                                // If descriptor target is present, use it
                                if (!nodeDef.from().isEmpty()) {
                                    NodeDescriptor descriptor = buildDescriptor(nodeDef.from());
                                    nodeMeta = MergeRule.replace(nodeMeta, descriptor.getMeta());
                                }
//
//                                if (!nodeDef.target().isEmpty()) {
//                                    AnnotatedElement target = findAnnotatedElement(Path.of(nodeDef.target()));
//                                    if (target != null) {
//                                        nodeMeta = MergeRule.replace(nodeMeta, buildDescriptorMeta(target));
//                                    }
//                                } else if (!nodeDef.resource().isEmpty()) {
//                                    nodeMeta = MergeRule.replace(nodeMeta, buildMetaFromResource("node", nodeDef.resource()));
//                                }

                                putDescription(res, nodeMeta);
                            }

                        }
                );

        //Filtering hidden values
        listAnnotations(element, ValueDef.class, true)
                .stream()
                .filter(it -> !it.name().startsWith("@"))
                .forEach(valueDef -> {
                            boolean exists = res.hasMeta("value") && res.getMetaList("value").stream()
                                    .anyMatch(mb -> mb.getString("name").equals(valueDef.name()));
                            if (exists) {
                                LoggerFactory.getLogger(DescriptorUtils.class).trace("Ignoring duplicate value with name {} in descriptor", valueDef.name());
                            } else {
                                putDescription(res, ValueDescriptor.build(valueDef).getMeta());
                            }
                        }
                );

        return res;
    }

    /**
     * Put a node or value description inside existing meta builder creating intermediate nodes
     *
     * @param builder
     * @param meta
     */
    private static void putDescription(MetaBuilder builder, Meta meta) {
        Name nodeName = Name.of(meta.getString("name"));
        MetaBuilder currentNode = builder;
        while (nodeName.getLength() > 1) {
            String childName = nodeName.getFirst().toString();
            MetaBuilder finalCurrentNode = currentNode;
            currentNode = finalCurrentNode.getMetaList("node").stream()
                    .filter(node -> Objects.equals(node.getString("name"), childName))
                    .findFirst()
                    .orElseGet(() -> {
                        MetaBuilder newChild = new MetaBuilder("node").setValue("name", childName);
                        finalCurrentNode.attachNode(newChild);
                        return newChild;
                    });
            nodeName = nodeName.cutFirst();
        }

        String childName = nodeName.toString();
        MetaBuilder finalCurrentNode = currentNode;
        currentNode.getMetaList(meta.getName()).stream()
                .filter(node -> Objects.equals(node.getString("name"), childName))
                .findFirst()
                .orElseGet(() -> {
                    MetaBuilder newChild = new MetaBuilder(meta.getName()).setValue("name", childName);
                    finalCurrentNode.attachNode(newChild);
                    return newChild;
                }).update(meta).setValue("name", childName);
    }


    /**
     * Get the value using descriptor as a default
     *
     * @param meta
     * @param descriptor
     * @return
     */
    public static Value extractValue(String name, Meta meta, NodeDescriptor descriptor) {
        if (meta.hasValue(name)) {
            return meta.getValue(name);
        } else {
            return buildDefaultNode(descriptor).getValue(name);
        }
    }

    /**
     * Extract value using class descriptor
     *
     * @param name
     * @param obj
     * @return
     */
    public static Value extractValue(String name, Metoid obj) {
        return extractValue(name, obj.getMeta(), buildDescriptor(obj));
    }

    /**
     * Find a class or method designated by NodeDef {@code target} value
     *
     * @param path
     * @return
     */
    public static AnnotatedElement findAnnotatedElement(Path path) {
        if (path.getTarget().isEmpty() || path.getTarget().equals("class")) {
            try {
                return Class.forName(path.getName().toString());
            } catch (ClassNotFoundException ex) {
                LoggerFactory.getLogger(DescriptorUtils.class).error("Class not found by given path: " + path, ex);
                return null;
            }
        } else if (path.getTarget().equals("method")) {
            try {
                String className = path.getName().cutLast().toString();
                String methodName = path.getName().getLast().toString();

                Class dClass = Class.forName(className);

                for (Method method : dClass.getDeclaredMethods()) {
                    // Проверяем что это метод, у которого есть аннотация
                    if (method.getName().equals(methodName)
                            && (method.isAnnotationPresent(ValueDef.class) || method.isAnnotationPresent(NodeDef.class)
                            || method.isAnnotationPresent(ValueDefs.class) || method.isAnnotationPresent(NodeDefs.class))) {
                        return method;
                    }
                }
                LoggerFactory.getLogger(DescriptorUtils.class).error("Annotated method not found by given path: " + path);
                return null;
            } catch (ClassNotFoundException ex) {
                LoggerFactory.getLogger(DescriptorUtils.class).error("Class not found by given path: " + path, ex);
                return null;
            }
        } else {
            LoggerFactory.getLogger(DescriptorUtils.class).error("Unknown target for descriptor finder: " + path.getTarget());
            return null;
        }
    }

    //TODO move to global utils
    public static <T extends Annotation> List<T> listAnnotations(AnnotatedElement source, Class<T> type, boolean searchSuper) {
        List<T> res = new ArrayList<>();
        if (source instanceof Class) {
            T[] array = source.getDeclaredAnnotationsByType(type);
            res.addAll(Arrays.asList(array));
            if (searchSuper) {
                Class sourceClass = (Class) source;
                Class superClass = sourceClass.getSuperclass();
                if (superClass != null) {
                    res.addAll(listAnnotations(superClass, type, true));
                }
                for (Class cl : sourceClass.getInterfaces()) {
                    res.addAll(listAnnotations(cl, type, true));
                }
            }
        } else {
            T[] array = source.getAnnotationsByType(type);
            res.addAll(Arrays.asList(array));
        }
        return res;
    }

}
