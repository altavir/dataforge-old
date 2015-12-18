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
package hep.dataforge.storage.jcr;

import hep.dataforge.content.Content;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.exceptions.JCRStorageException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 * @author Darksnake
 * @param <T>
 */
public abstract class ContentLoader<T extends Content>{

    public static String ANNOTATION_NODE = "annotation";
    public static String ANNOTATION_NODE_TYPE = "df:annotation";
    public static String NAME_PROPERTY = "df:name";
    public static String TYPE_PROPERTY = "df:type";

    private final Session session;

    public ContentLoader(Session session) {
        this.session = session;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    // возвращает корневую ноду для всех объектов такого типа
    abstract protected String getParentPath();

    abstract protected String getNodeType();

    public T pull(String relPath) throws StorageException {
        try {
            Node workNode = getSession().getRootNode().getNode(getParentPath()).getNode(relPath);
            return (T) restoreFromNode(workNode).annotate(AnnotationLoader.load(workNode));
        } catch (RepositoryException ex) {
            throw new JCRStorageException(ex);
        }
    }

    public T pull(Node reference) throws StorageException, RepositoryException {
        return restoreFromNode(reference);
    }

    public PushNodeResult push(T content) throws StorageException {
        try {
            Node root = getSession().getRootNode();
            String path = getParentPath();
            Node parentNode;
            if (root.hasNode(path)) {
                parentNode = root.getNode(path);
            } else {
                parentNode = root.addNode(path);
            }

            if (parentNode.hasNode(content.getName())) {
                // ContentLoader не может изменять один раз записанную ноду, поэтому возвращает ту, что уже есть
                return new PushNodeResult(parentNode.getNode(content.getName()),false);
            } else {
                Node worknode = parentNode.addNode(content.getName(), getNodeType());
                worknode.setProperty(NAME_PROPERTY, content.getName());
                worknode.setProperty(TYPE_PROPERTY, content.getClass().getName());
                AnnotationLoader.save(content.getAnnotation(),worknode);
                updateNode(worknode, content);
                getSession().save();
                return new PushNodeResult(worknode);
            }
        } catch (RepositoryException ex) {
            throw new JCRStorageException(ex);
        }
    }

    protected String getName(Node node) throws RepositoryException {
        return node.getProperty(NAME_PROPERTY).getString();
    }

    protected String getContentType(Node node) throws RepositoryException {
        return node.getProperty(TYPE_PROPERTY).getString();
    }

    protected abstract T restoreFromNode(Node node) throws StorageException, RepositoryException;

    protected abstract void updateNode(Node node, T content) throws StorageException, RepositoryException;

}
