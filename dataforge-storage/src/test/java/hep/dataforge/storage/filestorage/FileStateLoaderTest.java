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

import hep.dataforge.context.Global;
import hep.dataforge.storage.api.StateLoader;
import hep.dataforge.storage.commons.LoaderFactory;
import hep.dataforge.storage.commons.StorageManager;
import hep.dataforge.values.ValueFactory;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

/**
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
        Global.INSTANCE.load(StorageManager.class);
        dir = Files.createTempDirectory("df_storage").toFile();
    }

    @After
    public void tearDown() {
        dir.delete();
    }

    public FileStateLoaderTest() {
    }

    @Test
    public void testIO() throws Exception {
        FileStorage storage = FileStorageFactory.Companion.buildLocal(Global.INSTANCE, dir, false, true);
        StateLoader loader = LoaderFactory.buildStateLoder(storage, "test_states", "");

        System.out.println("***starting write test***");
        loader.push("my.favorite.key", ValueFactory.of("my.favorite.value"));
        loader.push("pi", ValueFactory.of(Math.PI));
        loader.push("giberish", ValueFactory.of("Воркальось, хрипкие шарьки пырялись по мове и хрюкатали зелюки, как мюмзики в мове"));
        loader.close();
        System.out.println("passed!");
        System.out.println("***starting read test***");
        loader = (StateLoader) storage.optLoader("test_states").get();
        System.out.println(loader.getValue("giberish"));
        assertEquals(Math.PI, loader.getValue("pi").getDouble(), 0.01);

    }

}
