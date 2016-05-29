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
import hep.dataforge.storage.api.PointLoader;
import hep.dataforge.storage.commons.LoaderFactory;
import hep.dataforge.storage.commons.StorageManager;
import hep.dataforge.storage.commons.ValueIndex;
import hep.dataforge.tables.DataPoint;
import hep.dataforge.tables.MapPoint;
import hep.dataforge.tables.TableFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Darksnake
 */
public class FileDataPointLoaderTest {

    File dir;

    public FileDataPointLoaderTest() {
    }

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

    @Test
    public void test() throws FileNotFoundException, StorageException {
        String[] names = {"key", "2key", "sqrt"};

        FileStorage storage = FileStorage.in(dir, null);

        PointLoader loader = LoaderFactory.buildPointLoder(storage, "test_points", null, "key", TableFormat.forNames(names));

        System.out.println("push");
        for (int i = 0; i < 100; i++) {

            loader.push(new MapPoint(names, i, i * 2, Math.sqrt(i)));
            System.out.printf("Point with number %d loaded%n", i);
        }

        System.out.println("pull");
        ValueIndex<DataPoint> index = ((FilePointLoader) loader).getMapIndex("key");
        DataPoint dp = index.pull(24, 26).get(0);
        assertEquals(Math.sqrt(24), dp.getValue("sqrt").doubleValue(), 0.001);
    }

}
