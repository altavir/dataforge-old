/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import hep.dataforge.providers.Provider;
import hep.dataforge.values.MapValueProvider;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import hep.dataforge.values.ValueType;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.UnaryOperator;

import static hep.dataforge.meta.MetaUtils.transformValue;

/**
 * @author Alexander Nozik
 */
public class Template implements Annotated, UnaryOperator<Meta> {

    /**
     * Template itself
     */
    private final Meta template;

    /**
     * Default values
     */
    private final Meta def;

    public Template(Meta template) {
        this.template = template;
        this.def = Meta.empty();
    }

    public Template(Meta template, Meta def) {
        this.template = template;
        this.def = def;
    }

    /**
     * Build a Meta using given template.
     *
     * @param template
     * @return
     */
    public static MetaBuilder compileTemplate(Meta template, Meta data) {
        return new Template(template).compile(data);
    }

    public static MetaBuilder compileTemplate(Meta template, Map<String, Object> data) {
        return new Template(template).compile(new MapValueProvider(data), null);
    }

    @Override
    public Meta meta() {
        return template;
    }

    /**
     * Compile template using given meta and value providers.
     *
     * @param valueProvider
     * @param metaProvider
     * @return
     */
    public MetaBuilder compile(ValueProvider valueProvider, MetaProvider metaProvider) {
        MetaBuilder res = new MetaBuilder(meta());
        MetaUtils.nodeStream(res).forEach(pair -> {
            MetaBuilder node = (MetaBuilder) pair.getValue();
            if (node.hasValue("@include")) {
                String includePath = pair.getValue().getString("@include");
                if (metaProvider != null && metaProvider.hasMeta(includePath)) {
                    MetaBuilder parent = node.getParent();
                    parent.replaceChildNode(node, metaProvider.getMeta(includePath));
                } else if (def.hasMeta(includePath)) {
                    MetaBuilder parent = node.getParent();
                    parent.replaceChildNode(node, def.getMeta(includePath));
                } else {
                    LoggerFactory.getLogger(MetaUtils.class)
                            .warn("Can't compile template meta node with name {} not provided", includePath);
                }
            }
        });

        MetaUtils.valueStream(res).forEach(pair -> {
            Value val = pair.getValue();
            if (val.valueType().equals(ValueType.STRING) && val.stringValue().contains("$")) {
                res.setValue(pair.getKey(), transformValue(val, valueProvider, def));
            }
        });
        return res;
    }

    public MetaBuilder compile(Provider provider) {
        return compile(ValueProvider.buildFrom(provider), MetaProvider.buildFrom(provider));
    }

    @Override
    public MetaBuilder apply(Meta data) {
        return compile(data, data);
    }

}
