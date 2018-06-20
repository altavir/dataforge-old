package hep.dataforge.storage.filestorage;

import hep.dataforge.meta.Meta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Performance tests for
 * Created by darksnake on 22.06.2017.
 */
public class FileEnvelopeTest {

    private FileEnvelope envelope;

    @Before
    public void setUp() throws Exception {
        Path path = Files.createTempFile("df_envelope_test", ".df");
        envelope = FileEnvelope.Companion.createEmpty(path, Meta.empty());
    }

    @After
    public void tearDown() throws Exception {
        envelope.close();
        Files.delete(envelope.getFile());
    }

    @Test(timeout = 100)
    public void writePerformance() throws Exception {
        for (int i = 0; i < 1000; i++) {
            envelope.appendLine("This is the line number\t" + i);
        }
    }
}