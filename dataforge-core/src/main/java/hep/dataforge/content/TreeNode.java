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

/**
 *
 * @author Alexander Nozik
 * @param <T>
 */
interface TreeNode<T>{

//    public static TreeNode<T> getNode(Tree contentTree){
//        return new TreeBranch(contentTree);
//    }
//    
//    public static TreeNode<T> getNode(T content){
//        if(!(content instanceof Tree)){
//            return new TreeLeaf(content);
//        } else {
//            return new TreeBranch((Tree) content);
//        }
//    }
    
    /**
     * <p>isBranch.</p>
     *
     * @return a boolean.
     */
    boolean isBranch();
    
    /**
     * <p>getValue.</p>
     *
     * @return a T object.
     */
    T getValue();    
    
    /**
     * <p>getBranch.</p>
     *
     * @return a {@link hep.dataforge.content.Tree} object.
     */
    Tree<T> getBranch();
    
//    String getName();
}
