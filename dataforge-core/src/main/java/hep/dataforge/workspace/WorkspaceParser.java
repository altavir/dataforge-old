package hep.dataforge.workspace;

import hep.dataforge.context.Context;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A parser for a workspace
 */
public interface WorkspaceParser {
    /**
     * List all extensions managed by this parser
     *
     * @return
     */
    List<String> listExtensions();

    /**
     * Parse a file as a workspace
     *
     * @param path
     * @return
     */
    default Workspace.Builder parse(Context parentContext, Path path) {
        try {
            return parse(parentContext, Files.newBufferedReader(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Workspace.Builder parse(Context parentContext, Reader reader);
}
