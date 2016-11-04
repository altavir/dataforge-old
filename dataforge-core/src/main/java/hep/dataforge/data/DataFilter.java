/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data;

import hep.dataforge.meta.Meta;

import java.util.function.BiPredicate;

/**
 *
 * @author Alexander Nozik
 */
public class DataFilter {

    private static final BiPredicate TRUTH = (key, it) -> true;

    private BiPredicate<String, DataNode> nodeCondition = TRUTH;
    private BiPredicate<String, Data> dataCondition = TRUTH;

    public boolean acceptNode(String nodeName, DataNode node) {
        return this.nodeCondition.test(nodeName, node);
    }

    public boolean acceptData(String dataName, Data data) {
        return this.dataCondition.test(dataName, data);
    }

    public final void includeData(BiPredicate<String, Data> dataCondition) {
        if (this.dataCondition == TRUTH) {
            this.dataCondition = dataCondition;
        } else {
            this.dataCondition = this.dataCondition.or(dataCondition);
        }
    }

    public final void includeData(String mask, Class type) {
        if (mask == null || mask.isEmpty()) {
            mask = "*";
        }
        Class limitingType;
        if (type == null) {
            limitingType = Object.class;
        } else {
            limitingType = type;
        }
        String compiledPattern = mask.replace(".", "\\.").replace("?", ".?").replace("*", ".*?");
        BiPredicate<String, Data> predicate = ((name, data)
                -> name.matches(compiledPattern) && limitingType.isAssignableFrom(data.type()));
        includeData(predicate);
    }

    public final void excludeData(BiPredicate<String, Data> dataCondition) {
        this.dataCondition = this.dataCondition.and(dataCondition.negate());
    }

    public final void excludeData(String namePattern) {
        String compiledPattern = namePattern.replace(".", "\\.").replace("?", ".?").replace("*", ".*?");
        excludeData((name, data) -> name.matches(compiledPattern));
    }

    public final void includeNode(String namePattern, Class type) {
        if (namePattern == null || namePattern.isEmpty()) {
            namePattern = "*";
        }
        Class limitingType;
        if (type == null) {
            limitingType = Object.class;
        } else {
            limitingType = type;
        }
        String compiledPattern = namePattern.replace(".", "\\.").replace("?", ".?").replace("*", ".*?");
        BiPredicate<String, DataNode> predicate = ((name, data)
                -> name.matches(compiledPattern) && limitingType.isAssignableFrom(data.type()));
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
        String compiledPattern = namePattern.replace(".", "\\.").replace("?", ".?").replace("*", ".*?");
        excludeNode((name, node) -> name.matches(compiledPattern));
    }

    public DataFilter configure(Meta meta) {
        if (meta.hasMeta("include")) {
            meta.getMetaList("include").forEach(include -> {
                String namePattern = include.getString("mask", "*");
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
                String namePattern = exclude.getString("mask", "*");

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
