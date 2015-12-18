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

import hep.dataforge.exceptions.AnonymousNotAlowedException;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.names.Name;
import java.util.Map;

/**
 * <p>
 * Tree class.</p>
 *
 * @author Alexander Nozik
 * @param <T>
 * @version $Id: $Id
 */
public class Tree<T> {

    private Map<String, TreeNode<T>> map;

//    public Tree(String name) {
//        super(name);
//    }
    /**
     * <p>
     * get.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @return a T object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     */
    public T get(String path) throws ContentException {
        return Tree.this.get(Name.of(path));
    }

    /**
     * <p>
     * get.</p>
     *
     * @param path a {@link hep.dataforge.names.Name} object.
     * @return a T object.
     * @throws hep.dataforge.exceptions.ContentException if any.
     */
    public T get(Name path) throws ContentException {
        Name prefix = path.getFirst();
        if (map.containsKey(prefix.toString())) {
            TreeNode<T> target = map.get(prefix.toString());
            if (path.length() == 1) {
                return target.getValue();
            } else if (target.isBranch()) {
                return target.getBranch().get(path.cutFirst());
            } else {
                throw new ContentException(String.format("The node '%s' is not a branch", prefix));
            }
        } else {
            throw new ContentException(String.format("Content not fount on path '%s'", path.toString()));
        }
    }

    /**
     * <p>
     * contains.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean contains(String path) {
        return contains(Name.of(path));
    }

    /**
     * <p>
     * contains.</p>
     *
     * @param path a {@link hep.dataforge.names.Name} object.
     * @return a boolean.
     */
    public boolean contains(Name path) {
        Name prefix = path.getFirst();
        if (map.containsKey(prefix.toString())) {
            TreeNode target = map.get(prefix.toString());
            if (path.length() == 1) {
                return true;
            } else if (target.isBranch()) {
                return target.getBranch().contains(path);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * <p>
     * has.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean has(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
//    public Iterator<Content> iterator() {
//        final Iterator<ContentTreeNode> it = map.values().iterator();
//        return new Iterator<Content>() {
//
//            @Override
//            public boolean hasNext() {
//                return it.hasNext();
//            }
//
//            @Override
//            public Content next() {
//                return it.next().getValue();
//            }
//        };
//    }
    /**
     * добавляет новый элемент к дереву или заментяет существующий. Имя узла
     * соответствует имени контента. Можно использовать для добавления ветвей.
     *
     * @param name a {@link java.lang.String} object.
     * @param content a T object.
     * @return a {@link hep.dataforge.content.Tree} object.
     */
    public Tree put(String name, T content) {
        if (name == null || name.isEmpty()) {
            throw new AnonymousNotAlowedException();
        }
        this.map.put(name, new TreeLeaf<>(content));
        return this;
    }

//    /**
//     * Добавляет или заменят узел с именем {@code name}. Имя контента при этом
//     * игнорируется. Контент может быть анонимным.
//     *
//     * @param name
//     * @param content
//     * @return
//     */
//    public Tree putBranch(String name, T content) {
//        this.map.putBranch(name, TreeNode.getNode(content));
//        return this;
//    }
    /**
     * Добавляет или заменяет ветвь. Анонимные ветви недопустимы
     *
     * @param name a {@link java.lang.String} object.
     * @param branch a {@link hep.dataforge.content.Tree} object.
     * @return a {@link hep.dataforge.content.Tree} object.
     */
    public Tree putBranch(String name, Tree branch) {
        if (name == null || name.isEmpty()) {
            throw new AnonymousNotAlowedException();
        }
        this.map.put(name, new TreeBranch<>(branch));
        return this;
    }

}
