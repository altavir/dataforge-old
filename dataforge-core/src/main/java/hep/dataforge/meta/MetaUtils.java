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
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

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
            Matcher matcher = Pattern.compile("\\$\\{(?<sub>.*)\\}").matcher(valStr);
            while (matcher.find()) {
                String group = matcher.group();
                String sub = matcher.group("sub");
                for (ValueProvider context : contexts) {
                    if (context != null && context.hasValue(sub)) {
                        valStr = valStr.replace(group, context.getString(sub));
                        break;
                    }
                }
            }
            return Value.of(valStr);
        } else {
            return val;
        }
    }

    /**
     * Build a Meta using given template.
     *
     * @param meta
     * @param valueProvider
     * @param metaProvider
     * @return
     */
    public static Meta compileTemplate(Meta meta, ValueProvider valueProvider, MetaProvider metaProvider) {
        MetaBuilder res = new MetaBuilder(meta);
        res.nodeStream().forEach(pair -> {
            MetaBuilder node = pair.getValue();
            if (node.hasValue("@include")) {
                String includePath = pair.getValue().getString("@include");
                if (metaProvider.hasMeta(includePath)) {
                    MetaBuilder parent = node.getParent();
                    parent.replaceChildNode(node, metaProvider.getMeta(includePath));
                } else {
                    LoggerFactory.getLogger(MetaUtils.class).warn("Can't compile template meta node with name {} not provided", includePath);
                }
            }
        });

        res.valueStream().forEach(pair -> {
            Value val = pair.getValue();
            if (val.valueType().equals(ValueType.STRING) && val.stringValue().contains("$")) {
                res.setValue(pair.getKey(), transformValue(val, valueProvider));
            }
        });
        return res;
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

}
