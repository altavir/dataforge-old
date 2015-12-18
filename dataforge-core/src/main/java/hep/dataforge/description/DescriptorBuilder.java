/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.description;

import hep.dataforge.meta.Annotated;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.values.Value;
import java.util.List;

/**
 * Helper class to build descriptors
 * @author Alexander Nozik
 */
public class DescriptorBuilder implements Annotated {

    private final MetaBuilder builder = new MetaBuilder("node");

    public DescriptorBuilder(String name) {
        builder.setValue("name", name);
    }

    public DescriptorBuilder() {
    }
    
    

//    public DescriptorBuilder setName(String name) {
//        builder.setValue("name", name);
//        return this;
//    }

    public DescriptorBuilder setRequired(boolean required) {
        builder.setValue("required", required);
        return this;
    }

    public DescriptorBuilder setMultiple(boolean multiple) {
        builder.setValue("multiple", multiple);
        return this;
    }

    public DescriptorBuilder setDefault(Meta... defaultNode) {
        builder.setNode("default", defaultNode);
        return this;
    }

    public DescriptorBuilder setInfo(String info) {
        builder.setValue("info", info);
        return this;
    }

    public DescriptorBuilder addNode(Meta childDescriptor) {
        builder.putNode(childDescriptor);
        return this;
    }

    public DescriptorBuilder addNode(DescriptorBuilder childBuilder) {
        DescriptorBuilder.this.addNode(childBuilder.builder);
        return this;
    }

    public DescriptorBuilder addNode(NodeDescriptor childDescriptor) {
        DescriptorBuilder.this.addNode(childDescriptor.meta());
        return this;
    }

    public DescriptorBuilder addValue(String name, String type, String info) {
        MetaBuilder valueBuilder = new MetaBuilder("value")
                .setValue("name", name)
                .setValue("type", type)
                .setValue("info", info);
        builder.putNode(valueBuilder);
        return this;
    }    
    
    public DescriptorBuilder addValue(String name, String type, String info, Object defaultValue) {
        MetaBuilder valueBuilder = new MetaBuilder("value")
                .setValue("name", name)
                .setValue("type", type)
                .setValue("info", info)
                .setValue("default", defaultValue);
        builder.putNode(valueBuilder);
        return this;
    }

    public DescriptorBuilder addValue(String name, boolean required,
            boolean multiple, String info, Value defaultValue, List<String> types, Value... allowedValues) {
        MetaBuilder valueBuilder = new MetaBuilder("value")
                .setValue("name", name)
                .setValue("type", types)
                .setValue("required", required)
                .setValue("multiple", multiple)
                .setValue("info", info)
                .setValue("default", defaultValue)
                .setValue("allowedValues", allowedValues);
        builder.putNode(valueBuilder);
        return this;
    }

    public NodeDescriptor build() {
        return new NodeDescriptor(builder.build());
    }

    @Override
    public MetaBuilder meta() {
        return builder;
    }

}
