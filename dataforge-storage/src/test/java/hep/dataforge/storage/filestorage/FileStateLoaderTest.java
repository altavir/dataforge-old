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
package hep.dataforge.storage.filestorage;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.StateLoader;
import hep.dataforge.storage.commons.LoaderFactory;
import hep.dataforge.storage.commons.StorageManager;
import hep.dataforge.values.Value;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Alexander Nozik
 */
public class FileStateLoaderTest {

    File dir;

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
        new StorageManager().startGlobal();
        dir = Files.createTempDirectory("df_storage").toFile();
    }

    @After
    public void tearDown() {
        dir.delete();
    }

    public FileStateLoaderTest() {
    }

    @Test
    public void testIO() throws StorageException, Exception {
        FileStorage storage = FileStorage.in(dir, null);
        StateLoader loader = LoaderFactory.buildStateLoder(storage, "test_states", null);

        System.out.println("***starting write test***");
        loader.setValue("my.favorite.key", Value.of("my.favorite.value"));
        loader.setValue("pi", Value.of(Math.PI));
        loader.setValue("giberish", Value.of("Воркальось, хрипкие шарьки пырялись по мове и хрюкатали зелюки, как мюмзики в мове"));
        loader.close();
        System.out.println("passed!");
        System.out.println("***starting read test***");
        loader = (StateLoader) storage.getLoader("test_states");
        System.out.println(loader.getValue("giberish"));
        assertEquals(Math.PI, loader.getValue("pi").doubleValue(),0.01);

    }

}
