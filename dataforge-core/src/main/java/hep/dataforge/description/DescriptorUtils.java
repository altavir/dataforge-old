/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.MergeRule;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.providers.Path;
import hep.dataforge.utils.Misc;
import hep.dataforge.values.Value;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

        descriptor.childrenDescriptors().values().stream().forEach((NodeDescriptor nd) -> {
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
        File file = new File(DescriptorUtils.class.getClassLoader().getResource(resource).getFile());
        try {
            return buildMetaFromFile(name, file);
        } catch (IOException ex) {
            throw new RuntimeException("Can't read resource file for descriptor", ex);
        } catch (ParseException ex) {
            throw new RuntimeException("Can't parse resource file for descriptor", ex);
        }
    }

    public static MetaBuilder buildMetaFromFile(String name, File file) throws IOException, ParseException {
        return MetaFileReader.read(name, file);
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
    public static NodeDescriptor buildDescriptor(AnnotatedElement element) {
        return descriptorCache.computeIfAbsent(element, e -> new NodeDescriptor(buildDescriptorMeta(e)));
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

        for (NodeDef nodeDef : listAnnotations(element, NodeDef.class, true)) {
            //TODO replace by map to avoid multiple node parsing
            boolean exists = res.hasMeta("node") && res.getMetaList("node").stream()
                    .anyMatch(mb -> mb.getString("name").equals(nodeDef.name()));
            //avoiding dublicate nodes
            if (!exists) {
                MetaBuilder nodeMeta = new MetaBuilder("node")
                        .putValue("name", nodeDef.name())
                        .putValue("info", nodeDef.info())
                        .putValue("required", nodeDef.required())
                        .putValue("multiple", nodeDef.multiple())
                        .putValue("tags", nodeDef.tags());

                // Either target or resource is used
                if (!nodeDef.target().isEmpty()) {
                    AnnotatedElement target = findAnnotatedElement(nodeDef.target());
                    if (target != null) {
                        nodeMeta = MergeRule.replace(nodeMeta, buildDescriptorMeta(target));
                    }
                } else if (!nodeDef.resource().isEmpty()) {
                    nodeMeta = MergeRule.replace(nodeMeta, buildMetaFromResource("node", nodeDef.resource()));
                }

                res.putNode(nodeMeta);
            }
        }

        //FIXME forbid non-unique values and Nodes
        for (ValueDef valueDef : listAnnotations(element, ValueDef.class, true)) {
            boolean exists = res.hasMeta("value") && res.getMetaList("value").stream()
                    .anyMatch(mb -> mb.getString("name").equals(valueDef.name()));
            if (!exists) {
                res.putNode(ValueDescriptor.build(valueDef).meta());
            }
        }

        return res;
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
    public static Value extractValue(String name, Annotated obj) {
        return extractValue(name, obj.meta(), buildDescriptor(obj));
    }

    /**
     * Find a class or method designated by NodeDef {@code target} value
     *
     * @param path
     * @return
     */
    public static AnnotatedElement findAnnotatedElement(String path) {
        Path segment = Path.of(path);
        if (segment.target().isEmpty() || segment.target().equals("class")) {
            try {
                return Class.forName(segment.name().toString());
            } catch (ClassNotFoundException ex) {
                LoggerFactory.getLogger(DescriptorUtils.class).error("Class not found by given path: " + path, ex);
                return null;
            }
        } else if (segment.target().equals("method")) {
            try {
                String className = segment.name().cutLast().toString();
                String methodName = segment.name().getLast().toString();

                Class dClass = Class.forName(className);

                for (Method method : dClass.getDeclaredMethods()) {
                    // Проверяем что это метод, у которого есть аннотация
                    if (method.getName().equals(methodName)
                            && (method.isAnnotationPresent(ValueDef.class) || method.isAnnotationPresent(NodeDef.class)
                            || method.isAnnotationPresent(ValuesDefs.class) || method.isAnnotationPresent(NodeDefs.class))) {
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
            LoggerFactory.getLogger(DescriptorUtils.class).error("Unknown target for descriptor finder: " + segment.target());
            return null;
        }
    }

    public static <T extends Annotation> List<T> listAnnotations(AnnotatedElement source, Class<T> type, boolean searchSuper) {
        List<T> res = new ArrayList<>();
        if (source instanceof Class) {
            T[] array = source.getDeclaredAnnotationsByType(type);
            res.addAll(Arrays.asList(array));
            if (searchSuper) {
                Class sourceClass = (Class) source;
                Class superClass = sourceClass.getSuperclass();
                if (superClass != null) {
                    res.addAll(listAnnotations(superClass, type, false));
                }
                for (Class cl : sourceClass.getInterfaces()) {
                    res.addAll(listAnnotations(cl, type, false));
                }
            }
        } else {
            T[] array = source.getAnnotationsByType(type);
            res.addAll(Arrays.asList(array));
        }
        return res;
    }

}
