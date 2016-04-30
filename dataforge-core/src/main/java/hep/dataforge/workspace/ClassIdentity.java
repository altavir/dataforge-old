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
package hep.dataforge.workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * The identity for classes. Uses reflection to ensure the same version of generated
 * bytecode is used.
 *
 * @author Alexander Nozik
 */
public class ClassIdentity implements Identity {

    String id;

    public ClassIdentity(Class cl) {
        File classFile = new File(cl.getResource(cl.getCanonicalName()).getFile());
        try {
            this.id = new String(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(classFile.toPath())));
        } catch (NoSuchAlgorithmException ex) {
            throw new Error();
        } catch (IOException ex) {
            throw new RuntimeException("Class not found", ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClassIdentity other = (ClassIdentity) obj;
        return Objects.equals(this.id, other.id);
    }
    

}
