/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.meta;

import hep.dataforge.exceptions.AnnotationBuilderException;
import hep.dataforge.navigation.ValueProvider;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A convenient builder to construct immutable or mutable annotations. or
 * configurations. All passed annotations are recreated as AnnotationBuilders,
 * "forgetting" previous parents and listeners if any.
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class MetaBuilder extends MuttableMetaNode<MetaBuilder> {

    private ValueProvider valueContext;

    public MetaBuilder(String name) {
        super(name);
    }

    public MetaBuilder(String name, ValueProvider context) {
        super(name);
        this.valueContext = context;
    }

    /**
     * A deep copy constructor
     *
     * @param annotation
     */
    public MetaBuilder(Meta annotation) {
        super(annotation.getName());
        Collection<String> valueNames = annotation.getValueNames();
        for (String valueName : valueNames) {
            setValueItem(valueName, annotation.getValue(valueName));
        }

        Collection<String> elementNames = annotation.getNodeNames();
        for (String elementName : elementNames) {
            List<MetaBuilder> item = annotation.getNodes(elementName).stream()
                    .<MetaBuilder>map((an) -> new MetaBuilder(an))
                    .collect(Collectors.toList());
            setNodeItem(elementName, new ArrayList<>(item));
        }
        if (annotation instanceof MetaBuilder) {
            valueContext = ((MetaBuilder) annotation).valueContext;
        }

    }

    protected MetaBuilder(String name, MetaBuilder parent) {
        super(name, parent);
    }

    /**
     * return an immutable annotation base on this builder
     *
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    public Meta build() {
        return MetaNode.from(this);
    }

    @Override
    public MetaBuilder getBuilder() {
        return this;
    }

    /**
     * <p>
     * setName.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.meta.MetaBuilder} object.
     */
    public MetaBuilder setName(String name) {
        rename(name);
        return currentState();
    }

    /**
     * <p>
     * putAnnotation.</p>
     *
     * @param element a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link hep.dataforge.meta.MetaBuilder} object.
     */
    @Override
    public MetaBuilder putNode(Meta element) {
        super.putNode(new MetaBuilder(element));
        return currentState();
    }

    public MetaBuilder rename(String newName) {
        super.renameNode(newName);
        return currentState();
    }

    /**
     * Replace an item with the given name with new item. If provided list is
     * null or empty than corresponding item is removed if it exists.
     *
     * @param name a {@link java.lang.String} object.
     * @param elements a {@link hep.dataforge.meta.Meta} object.
     * @return a {@link hep.dataforge.meta.MetaBuilder} object.
     */
    @Override
    public MetaBuilder setNode(String name, Meta... elements) {
        if (elements == null || elements.length == 0) {
            super.removeNode(name);
        } else {
            super.setNode(name);
            for (Meta element : elements) {
                MetaBuilder newElement = new MetaBuilder(element);
                if (!name.equals(newElement.getName())) {
                    newElement.rename(name);
                }
                super.putNode(newElement);
            }
        }
        return currentState();
    }

    public MetaBuilder setNode(String name, Collection<? extends Meta> elements) {
        if (elements == null || elements.isEmpty()) {
            super.removeNode(name);
        } else {
            super.setNode(name);
            for (Meta element : elements) {
                MetaBuilder newElement = new MetaBuilder(element);
                if (!name.equals(newElement.getName())) {
                    newElement.rename(name);
                }
                super.putNode(newElement);
            }
        }
        return currentState();
    }

    public MetaBuilder updateNode(String name, Meta element) {
        return updateNode(name, MergeRule.getDefault(), element);
    }

    /**
     * Update an element or element item using given merge rule
     *
     * @param name
     * @param rule
     * @param elements
     * @return
     */
    public MetaBuilder updateNode(String name, MergeRule rule, Meta... elements) {
        if (!hasNode(name)) {
            MetaBuilder.this.setNode(name, elements);
        }
        List<MetaBuilder> list = super.getChildNodeItem(name);
        if (list.size() != elements.length) {
            throw new AnnotationBuilderException("Can't update element item with an item of different size");
        } else {
            MetaBuilder[] newList = new MetaBuilder[list.size()];
            for (int i = 0; i < list.size(); i++) {
                newList[i] = rule.merge(elements[i], list.get(i)).rename(name);
            }
            super.setNode(name, newList);
        }
        return this;
    }

    /**
     * Update this annotation with new Annotation
     *
     * @param annotation a {@link hep.dataforge.meta.MetaBuilder}
     * object.
     * @param valueMerger a {@link hep.dataforge.meta.ListMergeRule}
     * object.
     * @param elementMerger a {@link hep.dataforge.meta.ListMergeRule}
     * object.
     * @return a {@link hep.dataforge.meta.MetaBuilder} object.
     */
    public MetaBuilder update(Meta annotation,
            ListMergeRule<Value> valueMerger,
            ListMergeRule<Meta> elementMerger) {
        return new CustomMergeRule(valueMerger, elementMerger).merge(annotation, this);
    }

    /**
     * <p>
     * update.</p>
     *
     * @param annotation a {@link hep.dataforge.meta.MetaBuilder}
     * object.
     * @return a {@link hep.dataforge.meta.MetaBuilder} object.
     */
    public MetaBuilder update(MetaBuilder annotation) {
        return MergeRule.replace(annotation, this);
    }

    @Override
    protected MetaBuilder currentState() {
        return this;
    }

    /**
     * The transformation which should be performed on each value before it is
     * returned to user. Basically is used to ensure automatic substitutions. If
     * the reference not found in the current annotation scope than the value is
     * returned as-is.
     *
     * @param val
     * @return
     */
    protected Value transformValue(Value val) {
        if (val.valueType().equals(ValueType.STRING) && val.stringValue().contains("$")) {
            String valStr = val.stringValue();
            Matcher matcher = Pattern.compile("\\$\\{(?<sub>.*)\\}").matcher(valStr);
            while (matcher.find()) {
                String group = matcher.group();
                String sub = matcher.group("sub");
                ValueProvider context = getValueContext();
                if (context != null && context.hasValue(sub)) {
                    valStr = valStr.replace(group, context.getString(sub));
                } else if (this.hasValue(sub)) {
                    valStr = valStr.replace(group, this.getString(sub));
                }
            }
            return Value.of(valStr);
        } else {
            return val;
        }
    }

    /**
     * The value substitution context is inherited
     *
     * @return
     */
    private ValueProvider getValueContext() {
        if (this.valueContext != null) {
            return this.valueContext;
        } else if (parent != null) {
            return parent.getValueContext();
        } else {
            return null;
        }
    }
    
    public MetaBuilder setContext(ValueProvider context){
        this.valueContext = context;
        return this;
    }
    
    /**
     * Return transformed value
     * @param name
     * @return 
     */
    @Override
    public Value getValue(String name) {
        return Value.of(super.getValue(name).listValue().stream().<Value>map((val) -> transformValue(val)).collect(Collectors.toList()));
    }

    /**
     * Create an empty child node 
     * @param name
     * @return 
     */
    @Override
    protected MetaBuilder createChildNode(String name) {
        return new MetaBuilder(name, this);
    }

    @Override
    protected MetaBuilder cloneNode(Meta node) {
        return new MetaBuilder(node);
    }

    /**
     * Attach nod to this one changing attached node's parent
     * @param node 
     */
    @Override
    public void attachNode(MetaBuilder node) {
        super.attachNode(node);
    }

    /**
     * Attach a list of nodes, changing each node's parent to this node
     * @param name
     * @param nodes 
     */
    @Override
    public void attachNodeItem(String name, List<MetaBuilder> nodes) {
        super.attachNodeItem(name, nodes);
    }
    
    /**
     * Recursively apply in-place node transformation to node
     * @param transformation
     * @return 
     */
    public MetaBuilder transform(final UnaryOperator<MetaBuilder> transformation){
        MetaBuilder res = transformation.apply(this);
        for(List<MetaBuilder> item: res.nodes.values()){
            item.replaceAll((MetaBuilder t) -> t.transform(transformation));
        }
        return res;
    }
    
}
