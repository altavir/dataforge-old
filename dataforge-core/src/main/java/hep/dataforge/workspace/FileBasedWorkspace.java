package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.utils.GenericBuilder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * Dynamic workspace that is parsed from file using external algorithm. Workspace is reloaded only if file is changed
 */
public class FileBasedWorkspace extends DynamicWorkspace {

    /**
     * Find appropriate parser and builder a workspace
     *
     * @param context        a parent context for workspace. Workspace usually creates its own context.
     * @param path           path of the file to create workspace from
     * @param transformation a finalization transformation applied to workspace after loading
     * @return
     */
    public static Workspace build(Context context, Path path, Function<Workspace.Builder, Workspace> transformation) {
        String fileName = path.getFileName().toString();
        return StreamSupport.stream(ServiceLoader.load(WorkspaceParser.class).spliterator(), false)
                .filter(parser -> parser.listExtensions().stream().anyMatch(fileName::endsWith))
                .findFirst()
                .map(parser -> parser.parse(context, path))
                .map(transformation)
                .orElseThrow(() -> new RuntimeException("Workspace parser for " + path + " not found"));
    }

    public static Workspace build(Context context, Path path) {
        return build(context, path, GenericBuilder::build);
    }

    public static Workspace build(Path path) {
        return build(Global.instance(), path, GenericBuilder::build);
    }


    private final Path path;
    private final Function<Path, Workspace> parser;
    private byte[] checkSum;

    public FileBasedWorkspace(Path path, Function<Path, Workspace> parser) {
        this.path = path;
        this.parser = parser;
    }

    @Override
    protected Workspace buildWorkspace() {
        return parser.apply(path);
    }

    @Override
    protected synchronized Workspace getWorkspace() {
        byte[] oldCheckSum = checkSum;
        checkSum = getCheckSum();
        if (!Arrays.equals(oldCheckSum, checkSum)) {
            invalidate();
        }
        return super.getWorkspace();
    }


    private byte[] getCheckSum() {
        try (InputStream is = Files.newInputStream(path)) {
            MessageDigest md = MessageDigest.getInstance("MD5");

            DigestInputStream dis = new DigestInputStream(is, md);
            return md.digest();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate file checksum", ex);
        }
    }
}
