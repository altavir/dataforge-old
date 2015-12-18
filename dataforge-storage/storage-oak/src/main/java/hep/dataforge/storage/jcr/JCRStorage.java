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

import hep.dataforge.annotations.Annotation;
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.Loader;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.common.AbstractStorage;
import javax.jcr.Node;
import javax.jcr.Session;

/**
 *
 * @author Darksnake
 */
public class JCRStorage extends AbstractStorage {

    private Session session;
    private Node node;


    public JCRStorage(Storage parent, String name, Annotation annotation) {
        super(parent, name, annotation);
    }

    @Override
    public Loader buildLoader(Annotation loaderConfiguration) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Storage buildShelf(String shelfName, Annotation shelfConfiguration) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
//    public JCRStorage(Annotation annotation) {
//        super(annotation);
//    }
//
//    /**
//     * Возвращает текущую сессию. Если ее нет, то лениво инициализирует ее.
//     *
//     * @return
//     */
//    public Session getSession() {
//        if (session == null) {
//            open();
//        }
//        if (session == null) {
//            throw new Error();
//        }
//        return session;
//    }
//
//    protected Credentials getDefaultCredentials() {
//        return null;
//    }
//
//    /**
//     * Возвращает подключение к существующему внутреннему репозиторию. Если его
//     * нет, создает его.
//     *
//     * @param path
//     * @return
//     */
//    protected abstract Repository getRepository(String path);
//
//    @Override
//    public Loader getLoader(String shelf, String name) throws StorageException {
//        Annotation an = getLoaderConfig(name);
//        if (an != null) {
//            return super.buildLoader(an);
//        } else {
//            try {
//                return new JCRLoader(this, DEFAULT_NODE_PATH, name);
//            } catch (JCRStorageException ex) {
//                throw new RuntimeException(ex);
//            }
//        }
//    }
//
//    @Override
//    public JCRLoader buildLoader(Annotation loaderConfiguration) {
//        return JCRLoader.getDataSetLoader(this, loaderConfiguration);
//    }
//
//    //TODO runXPath
//    /**
//     * Закрываем репозиторий и обнуляем сессию
//     */
//    @Override
//    public void close() {
//        if (session != null) {
//            session.logout();
//            session = null;
//        }
//    }
//
//    @Override
//    public void open() {
//        try {
//            String workspaceName = getAnnotation().getString("workspace", "default");
//            String repositoryPath = getAnnotation().getString("repository", "./repository");            
//            //Пока принципиально работаем без пароля, так как репозиторий должен быть локальным
//
//            this.session = getRepository(repositoryPath).login(getDefaultCredentials(), workspaceName);
//        } catch (RepositoryException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    protected Annotation getLoaderConfig(String name) {
//        return null;
//    }