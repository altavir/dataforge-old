/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.exceptions.PathSyntaxException;
import hep.dataforge.values.ValueProvider;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import javafx.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utilities to work with meta
 *
 * @author Alexander Nozik
 */
public class MetaUtils {

    /**
     * Find all nodes with given path that satisfy given condition. Return empty
     * list if no such nodes are found.
     *
     * @param root
     * @param path
     * @param condition
     * @return
     */
    public static List<Meta> findNodes(Meta root, String path, Predicate<Meta> condition) {
        if (!root.hasNode(path)) {
            return Collections.emptyList();
        } else {
            return root.getNodes(path).stream()
                    .filter(condition)
                    .collect(Collectors.<Meta>toList());
        }
    }

    /**
     * Return the first node with given path that satisfies condition. Null if
     * no such nodes are found.
     *
     * @param root
     * @param path
     * @param condition
     * @return
     */
    public static Meta findNode(Meta root, String path, Predicate<Meta> condition) {
        List<? extends Meta> list = findNodes(root, path, condition);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * Find node by given key-value pair
     *
     * @param root
     * @param path
     * @param key
     * @param value
     * @return
     */
    public static Meta findNodeByValue(Meta root, String path, String key, Object value) {
        return findNode(root, path, (m) -> m.hasValue(key) && m.getValue(key).equals(Value.of(value)));
    }

    /**
     * The transformation which should be performed on each value before it is
     * returned to user. Basically is used to ensure automatic substitutions. If
     * the reference not found in the current annotation scope than the value is
     * returned as-is.
     * <p>
     * the notation for template is the following: {@code ${path|def}} where
     * {@code path} is the path for value in the context and {@code def} is the
     * default value.
     * </p>
     *
     * @param val
     * @param contexts a list of contexts to draw value from
     * @return
     */
    public static Value transformValue(Value val, ValueProvider... contexts) {
        if (contexts.length == 0) {
            return val;
        }
        if (val.valueType().equals(ValueType.STRING) && val.stringValue().contains("$")) {
            String valStr = val.stringValue();
//            Matcher matcher = Pattern.compile("\\$\\{(?<sub>.*)\\}").matcher(valStr);
            Matcher matcher = Pattern.compile("\\$\\{(?<sub>[^|]*)(?:\\|(?<def>.*))?\\}").matcher(valStr);
            while (matcher.find()) {
                String group = matcher.group();
                String sub = matcher.group("sub");
                String replacement = matcher.group("def");
                for (ValueProvider context : contexts) {
                    if (context != null && context.hasValue(sub)) {
                        replacement = context.getString(sub);
                        break;
                    }
                }
                if (replacement != null) {
                    valStr = valStr.replace(group, replacement);
                }
            }
            return Value.of(valStr);
        } else {
            return val;
        }
    }

    /**
     * Apply query to node list
     *
     * @param <T>
     * @param nodeList
     * @param query
     * @return
     */
    public static <T extends MetaNode> List<T> applyQuery(List<T> nodeList, String query) {
        //TODO make queries more complicated
        int num;
        try {
            num = Integer.parseInt(query);
        } catch (NumberFormatException ex) {
            throw new PathSyntaxException("The query ([]) syntax for annotation must contain only integer numbers");
        }
        if (num < 0 || num >= nodeList.size()) {
            throw new NamingException("No list element with given index");
        }
        return Collections.singletonList(nodeList.get(num));
    }

    /**
     * A stream containing pairs
     *
     * @param prefix
     * @return
     */
    private static Stream<Pair<String, Meta>> nodeStream(String prefix, Meta node) {
        return Stream.concat(Stream.of(new Pair<>(prefix, node)),
                node.getNodeNames().stream().flatMap(nodeName -> {
                    List<? extends Meta> metaList = node.getNodes(nodeName);
                    String nodePrefix;
                    if (prefix == null || prefix.isEmpty()) {
                        nodePrefix = nodeName;
                    } else {
                        nodePrefix = prefix + "." + nodeName;
                    }
                    if (metaList.size() == 1) {
                        return nodeStream(nodePrefix, metaList.get(0));
                    } else {
                        return IntStream.range(0, metaList.size()).boxed()
                                .flatMap(i -> {
                                    String subPrefix = String.format("%s[%d]", nodePrefix, i);
                                    Meta subNode = metaList.get(i);
                                    return nodeStream(subPrefix, subNode);
                                });
                    }
                })
        );
    }

    public static Stream<Pair<String, Meta>> nodeStream(Meta node) {
        return nodeStream("", node);
    }

    public static Stream<Pair<String, Value>> valueStream(Meta node) {
        return nodeStream(node).flatMap((Pair<String, Meta> entry) -> {
            String key = entry.getKey();
            Meta childMeta = entry.getValue();
            return childMeta.getValueNames().stream()
                    .map((String valueName) -> {
                        String prefix;
                        if (key.isEmpty()) {
                            prefix = "";
                        } else {
                            prefix = key + ".";
                        }
                        return new Pair<>(prefix + valueName, childMeta.getValue(valueName));
                    });
        });
    }

}
