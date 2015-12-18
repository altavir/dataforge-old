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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NodeType;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

/**
 *
 * Реализация дата-сервера на основе стандартного репозитория JackRabbit 2.x
 *
 * @author Darksnake
 */
public class JackrabbitServer extends JCRStorage {

    public JackrabbitServer(Annotation annotation) {
        super(annotation);
    }

    /**
     * Возвращает подключение к существующему внутреннему репозиторию. Если его
     * нет, создает его.
     *
     * @param repositoryPath
     * @return
     */
    @Override
    protected Repository getRepository(String repositoryPath) {
        try {
            return JcrUtils.getRepository("file:///"+repositoryPath);
        } catch (RepositoryException ex) {
            //не смогли загрузить репозиторий, создаем новый
            InputStream config = getClass().getResourceAsStream("repository.xml");
            try {
                return RepositoryImpl.create(RepositoryConfig.create(config, "file:///" + repositoryPath));
            } catch (RepositoryException ex1) {
                throw new Error(ex1);
            }
        }
    }

    @Override
    public void open() {
        super.open(); //To change body of generated methods, choose Tools | Templates.
        try {
            loadNodeTypes(getSession());
        } catch (Exception ex) {
            Logger.getLogger(JackrabbitServer.class.getName()).log(Level.SEVERE, "Can not load node types", ex);
        }
    }

    @Override
    protected Credentials getDefaultCredentials() {
        return new SimpleCredentials("admin", "admin".toCharArray());
    }

    public void loadNodeTypes(Session session) throws Exception {
        InputStream is = getClass().getResourceAsStream("NodesDefinition.cnd");

        NodeType[] nodeTypes;
        nodeTypes = CndImporter.registerNodeTypes(
                new InputStreamReader(is),
                session, true);
        for (NodeType nt : nodeTypes) {
            System.out.println("Registered: " + nt.getName());
        }

        session.save();
    }

}
