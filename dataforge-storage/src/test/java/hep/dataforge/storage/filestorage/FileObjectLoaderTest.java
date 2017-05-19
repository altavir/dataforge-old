/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.filestorage;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.api.ObjectLoader;
import hep.dataforge.storage.commons.LoaderFactory;
import hep.dataforge.storage.commons.StorageManager;
import org.junit.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Alexander Nozik
 */
@SuppressWarnings("unchecked")
public class FileObjectLoaderTest {

    File dir;

    public FileObjectLoaderTest() {
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

        FileStorage storage = FileStorageFactory.buildLocal(dir);

        ObjectLoader loader = LoaderFactory.buildObjectLoder(storage, "test", "");

        loader.push("myFragment", "MyString");
        assertEquals("MyString", loader.pull("myFragment"));
    }
}
