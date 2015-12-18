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
package hep.dataforge.content;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.description.DescriptorUtils;
import hep.dataforge.description.NodeDescriptor;
import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.values.Value;

/**
 * <p>
 * Abstract AbstractContent class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public abstract class AbstractContent implements Content {

    private Meta annotation;
    private NodeDescriptor descriptor;
    private String name;

    /**
     * <p>
     * Constructor for AbstractContent.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public AbstractContent(String name) {
        if ((name == null || name.isEmpty()) && getClass().isAnnotationPresent(AnonimousNotAlowed.class)) {
            throw new AnonymousNotAlowedException();
        }
        this.name = name;
    }

    /**
     * <p>
     * Constructor for AbstractContent.</p>
     */
    public AbstractContent() {
        if (getClass().isAnnotationPresent(AnonimousNotAlowed.class)) {
            throw new AnonymousNotAlowedException();
        }
    }

    /**
     * <p>
     * Constructor for AbstractContent.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     */
    public AbstractContent(String name, Meta annotation) {
        this(name);
        this.annotation = annotation;
    }

    /**
     * <p>
     * Constructor for AbstractContent.</p>
     *
     * @param annotation a {@link hep.dataforge.meta.Meta} object.
     */
    public AbstractContent(Meta annotation) {
        if (getClass().isAnnotationPresent(AnonimousNotAlowed.class)) {
            throw new AnonymousNotAlowedException();
        }

        this.annotation = annotation;
        if (annotation != null) {
            this.name = annotation.getName();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDescriptor getDescriptor() {
        if (this.descriptor == null) {
            this.descriptor = DescriptorUtils.buildDescriptor(getClass());
        }
        return descriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * Вставляет новую аннотацию
     */
    @Override
    public synchronized Content configure(Meta a) {
        if (getClass().isAnnotationPresent(ImmutableContent.class)) {
            throw new ContentException("Content annotation is marked immutable and can't be replaced.");
        }
        this.annotation = a;
        setDescriptor(null);
        return this;
    }

    /**
     * <p>
 configure.</p>
     *
     * @param a a {@link hep.dataforge.meta.MetaBuilder} object.
     * @return a {@link hep.dataforge.content.Content} object.
     */
    public Content annotate(MetaBuilder a) {
        return configure(a.build());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Meta meta() {
        if (annotation == null) {
            annotation = MetaBuilder.buildEmpty(getName());
        }
        return annotation;
    }

    /**
     * <p>
     * Setter for the field <code>descriptor</code>.</p>
     *
     * @param descriptor the descriptor to set
     */
    protected void setDescriptor(NodeDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    @Override
    public Value getValue(String name) {
        if(meta().hasValue(name)){
            return meta().getValue(name);
        } else {
            return DescriptorUtils.buildDefaultNode(getDescriptor()).getValue(name);
        }
    }

    @Override
    public boolean hasValue(String name) {
        return meta().hasValue(name) || DescriptorUtils.buildDefaultNode(getDescriptor()).hasValue(name);
    }

}
