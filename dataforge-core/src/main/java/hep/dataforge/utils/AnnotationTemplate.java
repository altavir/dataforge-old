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
package hep.dataforge.utils;

import hep.dataforge.meta.Meta;
import hep.dataforge.context.GlobalContext;

/**
 * <p>Abstract AnnotationTemplate class.</p>
 *
 * @deprecated replaced by descriptors
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
@Deprecated
public abstract class AnnotationTemplate<T> implements MetaFactory<T> {
    /**
     * <p>getDefaultPath.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getDefaultPath();
    
    /**
     * Возвращает непосредственно элемент, содержащий описание объекта
     *
     * @param object a T object.
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    public abstract Meta getAnnotation(T object);
    
    /**
     * <p>buildFromParent.</p>
     *
     * @param parent a {@link hep.dataforge.meta.Meta} object.
     * @param path a {@link java.lang.String} object.
     * @return a T object.
     */
    public T buildFromParent(Meta parent, String path){
        if(parent.hasNode(path)){
            return build(GlobalContext.instance(), parent.getNode(path));
        } else {
            return build();
        }
    }
    
    /**
     * <p>buildFromParent.</p>
     *
     * @param parent a {@link hep.dataforge.meta.Meta} object.
     * @return a T object.
     */
    public T buildFromParent(Meta parent){
        return buildFromParent(parent, getDefaultPath());
    }
    
    /**
     * Добавляет в {@code source} элемент, описывающий объект
     *
     * @param source a {@link hep.dataforge.meta.Meta} object.
     * @param object a T object.
     * @return a {@link hep.dataforge.meta.Meta} object.
     */
    public Meta updateAnnotation(Meta source, T object){
        return source.getBuilder().setNode(getDefaultPath(), getAnnotation(object)).build();
    }
}
