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
import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.TableLoader;
import hep.dataforge.storage.api.ValueIndex;
import hep.dataforge.storage.commons.LoaderFactory;
import hep.dataforge.storage.commons.MapIndex;
import hep.dataforge.storage.commons.StorageManager;
import hep.dataforge.tables.MetaTableFormat;
import hep.dataforge.tables.ValueMap;
import hep.dataforge.utils.DateTimeUtils;
import hep.dataforge.values.Value;
import hep.dataforge.values.Values;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Darksnake
 */
public class FileDataTableLoaderTest {

    File dir;

    public FileDataTableLoaderTest() {
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
    public void testReadWrite() throws StorageException {
        String[] names = {"key", "2key", "sqrt"};

        FileStorage storage = FileStorageFactory.Companion.buildLocal(Global.INSTANCE, dir, false, true);

        TableLoader loader = LoaderFactory.buildPointLoader(storage, "test_points", "", "key", MetaTableFormat.forNames(names));

        System.out.println("push");
        Instant start = DateTimeUtils.now();
        for (int i = 0; i < 1000; i++) {
            loader.push(ValueMap.of(names, i, i * 2, Math.sqrt(i)));
//            System.out.printf("Point with number %d loaded%n", i);
        }
        System.out.printf("Push operation for 1000 element completed in %s%n", Duration.between(start, DateTimeUtils.now()));


        System.out.println("direct pull");

        start = DateTimeUtils.now();
        ValueIndex<Values> index = loader.getIndex("key");

//        IntStream.range(0, 100).mapToObj(i -> {
//            try {
//                return index.pull(i * 10);
//            } catch (StorageException e) {
//                throw new RuntimeException(e);
//            }
//        }).flatMap(it -> it.stream()).map(it -> it.get()).count();

        for (int i = 0; i < 100; i++) {
            index.pull(i * 10).collect(Collectors.toList());
        }
        System.out.printf("Selective pull operation on 100 element completed in %s%n", Duration.between(start, DateTimeUtils.now()));


        System.out.println("smart pull");

        start = DateTimeUtils.now();
        int smartPullSize = index.pull(Value.NULL, Value.NULL, 100).collect(Collectors.toList()).size();
        assertTrue(smartPullSize <= 100);

        System.out.printf("Smart pull operation on %d element completed in %s%n", smartPullSize, Duration.between(start, DateTimeUtils.now()));

        System.out.println("pull consistency check");
        Values dp = index.pull(24, 26).findFirst().get();
        assertEquals(Math.sqrt(24), dp.getValue("sqrt").doubleValue(), 0.001);

        ((MapIndex) index).invalidate();
    }

}
