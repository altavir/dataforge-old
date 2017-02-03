/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Meta;

import java.util.function.BiPredicate;

/**
 * @author Alexander Nozik
 */
public class DataFilter {

    private static final BiPredicate TRUTH = (key, it) -> true;

    private BiPredicate<String, DataNode> nodeCondition = TRUTH;
    private BiPredicate<String, Data> dataCondition = TRUTH;

    public static String applyMask(String pattern) {
        return pattern.replace(".", "\\.").replace("?", ".").replace("*", ".*?");
    }

    public boolean acceptNode(String nodeName, DataNode node) {
        return this.nodeCondition.test(nodeName, node);
    }

    public boolean acceptData(String dataName, Data data) {
        return this.dataCondition.test(dataName, data);
    }

    public <T> DataNode<T> filter(DataNode<T> node) {
        DataTree.Builder<T> builder = DataTree.builder(node.type());
        node.dataStream(true).forEach(d -> {
            if (acceptData(d.getName(), d)) {
                builder.putData(d);
            }
        });
        return builder.build();
    }

    public final void includeData(BiPredicate<String, Data> dataCondition) {
        if (this.dataCondition == TRUTH) {
            this.dataCondition = dataCondition;
        } else {
            this.dataCondition = this.dataCondition.or(dataCondition);
        }
    }

    public final void includeData(String namePattern, Class type) {
        Class limitingType;
        if (type == null) {
            limitingType = Object.class;
        } else {
            limitingType = type;
        }
        BiPredicate<String, Data> predicate = ((name, data)
                -> name.matches(namePattern) && limitingType.isAssignableFrom(data.type()));
        includeData(predicate);
    }

    public final void excludeData(BiPredicate<String, Data> dataCondition) {
        this.dataCondition = this.dataCondition.and(dataCondition.negate());
    }

    public final void excludeData(String namePattern) {
        excludeData((name, data) -> name.matches(namePattern));
    }

    public final void includeNode(String namePattern, Class type) {

        Class limitingType;
        if (type == null) {
            limitingType = Object.class;
        } else {
            limitingType = type;
        }
        BiPredicate<String, DataNode> predicate = ((name, data)
                -> name.matches(namePattern) && limitingType.isAssignableFrom(data.type()));
        includeNode(predicate);
    }

    public final void includeNode(BiPredicate<String, DataNode> nodeCondition) {
        if (this.nodeCondition == TRUTH) {
            this.nodeCondition = nodeCondition;
        } else {
            this.nodeCondition = this.nodeCondition.or(nodeCondition);
        }
    }

    public final void excludeNode(BiPredicate<String, DataNode> nodeCondition) {
        this.nodeCondition = this.nodeCondition.and(nodeCondition.negate());
    }

    public final void excludeNode(String namePattern) {
        excludeNode((name, node) -> name.matches(namePattern));
    }

    private String getPattern(Meta node) {
        if (node.hasValue("mask")) {
            return applyMask(node.getString("mask"));
        } else if (node.hasValue("pattern")) {
            return node.getString("pattern");
        } else {
            return ".*";
        }
    }

    public DataFilter configure(Meta meta) {
        if (meta.hasMeta("include")) {
            meta.getMetaList("include").forEach(include -> {
                String namePattern = getPattern(include);
                Class type = Object.class;
                if (include.hasValue("type")) {
                    try {
                        type = Class.forName(include.getString("type"));
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException("type not found", ex);
                    }
                }
                if (include.getBoolean("includeData", true)) {
                    includeData(namePattern, type);
                }
                if (include.getBoolean("includeNodes", true)) {
                    includeNode(namePattern, type);
                }
            });
        }

        if (meta.hasMeta("exclude")) {
            meta.getMetaList("exclude").forEach(exclude -> {
                String namePattern = getPattern(exclude);

                if (exclude.getBoolean("excludeData", true)) {
                    excludeData(namePattern);
                }
                if (exclude.getBoolean("excludeNodes", true)) {
                    excludeNode(namePattern);
                }
            });
        }
        return this;
    }

}
