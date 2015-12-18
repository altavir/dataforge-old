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
package hep.dataforge.dependencies;

import hep.dataforge.meta.Meta;
import hep.dataforge.content.Content;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.names.Names;

/**
 * A dependency on content
 * @author darksnake
 * @param <T>
 */
public class ContentDependency<T extends Content> implements Dependency<T> {
    
    private final T content;

    public ContentDependency(T content) {
        this.content = content;
    }

    @Override
    public T get() {
        return content;
    }

    @Override
    public <R> R get(String key) {
        switch (key) {
            case META_KEY:
                return (R) content.meta();
            case DEFAULT_KEY:
                return (R) content;
            default:
                throw new NameNotFoundException(key);
        }
    }

    @Override
    public Meta meta() {
        return content.meta();
    }

    @Override
    public String getName() {
        return content.getName();
    }

    @Override
    public Names keys() {
        return Names.of(META_KEY);
    }

    @Override
    public Class type(String key) {
        switch (key) {
            case META_KEY:
                return Meta.class;
            case DEFAULT_KEY:
                return content.getClass();
            default:
                throw new NameNotFoundException(key);
        }
    }
    
}
