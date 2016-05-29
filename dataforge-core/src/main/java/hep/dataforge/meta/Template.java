/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.meta;

import static hep.dataforge.meta.MetaUtils.transformValue;
import hep.dataforge.navigation.Provider;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueProvider;
import hep.dataforge.values.ValueType;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Nozik
 */
public class Template implements Annotated {

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
    public Meta compile(ValueProvider valueProvider, MetaProvider metaProvider) {
        MetaBuilder res = new MetaBuilder(meta());
        res.nodeStream().forEach(pair -> {
            MetaBuilder node = pair.getValue();
            if (node.hasValue("@include")) {
                String includePath = pair.getValue().getString("@include");
                if (metaProvider.hasMeta(includePath)) {
                    MetaBuilder parent = node.getParent();
                    parent.replaceChildNode(node, metaProvider.getMeta(includePath));
                } else if (def.hasNode(includePath)) {
                    MetaBuilder parent = node.getParent();
                    parent.replaceChildNode(node, def.getNode(includePath));
                } else {
                    LoggerFactory.getLogger(MetaUtils.class)
                            .warn("Can't compile template meta node with name {} not provided", includePath);
                }
            }
        });

        res.valueStream().forEach(pair -> {
            Value val = pair.getValue();
            if (val.valueType().equals(ValueType.STRING) && val.stringValue().contains("$")) {
                res.setValue(pair.getKey(), transformValue(val, valueProvider, def));
            }
        });
        return res;
    }

    public Meta compile(Provider provider) {
        return compile(ValueProvider.buildFrom(provider), MetaProvider.buildFrom(provider));
    }

    public Meta compile(Meta data) {
        return compile(data, data);
    }

    /**
     * Build a Meta using given template.
     *
     * @param meta
     * @param valueProvider
     * @param metaProvider
     * @return
     */
    public static Meta compileTemplate(Meta meta, Meta data) {
        return new Template(meta).compile(data);
    }

}
