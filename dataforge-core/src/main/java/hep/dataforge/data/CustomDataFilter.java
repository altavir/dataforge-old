/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.description.NodeDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.SimpleMetaMorph;
import hep.dataforge.values.ValueType;

import java.util.function.BiPredicate;

/**
 * A meta-based data filter
 *
 * @author Alexander Nozik
 */
@NodeDef(name = "include",
        info = "Define inclusion rule for data and/or dataNode. If not inclusion rule is present, everything is included by default.",
        from = "method::hep.dataforge.data.CustomDataFilter.applyMeta")
@NodeDef(name = "exclude",
        info = "Define exclusion rule for data and/or dataNode. Exclusion rules are allied only to included items.",
        from = "method::hep.dataforge.data.CustomDataFilter.applyMeta")
public class CustomDataFilter extends SimpleMetaMorph implements DataFilter {

    private BiPredicate<String, DataNode> nodeCondition;
    private BiPredicate<String, Data> dataCondition;

    private static String applyMask(String pattern) {
        return pattern.replace(".", "\\.").replace("?", ".").replace("*", ".*?");
    }

    public CustomDataFilter(Meta meta) {
        super(meta);
        applyMeta(meta);
    }

    public boolean acceptNode(String nodeName, DataNode node) {
        return this.nodeCondition == null || this.nodeCondition.test(nodeName, node);
    }

    public boolean acceptData(String dataName, Data data) {
        return this.dataCondition == null || this.dataCondition.test(dataName, data);
    }

    @Override
    public <T> DataNode<T> filter(DataNode<T> node) {
        DataSet.Builder<T> builder = DataSet.builder(node.type());
        node.dataStream(true).forEach(d -> {
            if (acceptData(d.getName(), d)) {
                builder.putData(d);
            }
        });
        return builder.build();
    }

    private void includeData(BiPredicate<String, Data> dataCondition) {
        if (this.dataCondition == null) {
            this.dataCondition = dataCondition;
        } else {
            this.dataCondition = this.dataCondition.or(dataCondition);
        }
    }

    private void includeData(String namePattern, Class<?> type) {
        Class<?> limitingType;
        if (type == null) {
            limitingType = Object.class;
        } else {
            limitingType = type;
        }
        BiPredicate<String, Data> predicate = ((name, data)
                -> name.matches(namePattern) && limitingType.isAssignableFrom(data.type()));
        includeData(predicate);
    }

    private void excludeData(BiPredicate<String, Data> dataCondition) {
        if (this.dataCondition != null) {
            this.dataCondition = this.dataCondition.and(dataCondition.negate());
        }
    }

    private void excludeData(String namePattern) {
        excludeData((name, data) -> name.matches(namePattern));
    }

    private void includeNode(String namePattern, Class<?> type) {

        Class<?> limitingType;
        if (type == null) {
            limitingType = Object.class;
        } else {
            limitingType = type;
        }
        BiPredicate<String, DataNode> predicate = ((name, data)
                -> name.matches(namePattern) && limitingType.isAssignableFrom(data.type()));
        includeNode(predicate);
    }

    private void includeNode(BiPredicate<String, DataNode> nodeCondition) {
        if (this.nodeCondition == null) {
            this.nodeCondition = nodeCondition;
        } else {
            this.nodeCondition = this.nodeCondition.or(nodeCondition);
        }
    }

    private void excludeNode(BiPredicate<String, DataNode> nodeCondition) {
        if (this.nodeCondition != null) {
            this.nodeCondition = this.nodeCondition.and(nodeCondition.negate());
        }
    }

    private void excludeNode(String namePattern) {
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

    @ValueDef(name = "mask", info = "Add rule using glob mask")
    @ValueDef(name = "pattern", info = "Add rule rule using regex pattern")
    @ValueDef(name = "forData", type = ValueType.BOOLEAN, def = "true", info = "Apply this rule to individual data")
    @ValueDef(name = "forNodes", type = ValueType.BOOLEAN, def = "true", info = "Apply this rule to data nodes")
    private void applyMeta(Meta meta) {
        if (meta.hasMeta("include")) {
            meta.getMetaList("include").forEach(include -> {
                String namePattern = getPattern(include);
                Class<?> type = Object.class;
                if (include.hasValue("type")) {
                    try {
                        type = Class.forName(include.getString("type"));
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException("type not found", ex);
                    }
                }
                if (include.getBoolean("forData", true)) {
                    includeData(namePattern, type);
                }
                if (include.getBoolean("forNodes", true)) {
                    includeNode(namePattern, type);
                }
            });
        }

        if (meta.hasMeta("exclude")) {
            meta.getMetaList("exclude").forEach(exclude -> {
                String namePattern = getPattern(exclude);

                if (exclude.getBoolean("forData", true)) {
                    excludeData(namePattern);
                }
                if (exclude.getBoolean("forNodes", true)) {
                    excludeNode(namePattern);
                }
            });
        }
    }
}
