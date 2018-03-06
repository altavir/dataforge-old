/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.io.envelopes.EnvelopeReader;
import hep.dataforge.io.envelopes.MetaType;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Optional;
import java.util.ServiceLoader;

//TODO add examples for transformations

/**
 * A reader for meta file in any supported format. Additional file formats could
 * be statically registered by plug-ins.
 * <p>
 * Basically reader performs two types of "on read" transformations:
 * <ul>
 * <li>
 * Includes: include a meta from given file instead of given node
 * </li>
 * <li>
 * Substitutions: replaces all occurrences of {@code ${<key>}} in child meta nodes by given value. Substitutions are made as strings.
 * </li>
 * </ul>
 *
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public class MetaFileReader {

    public static String SUBST_ELEMENT = "df:subst";
    public static String INCLUDE_ELEMENT = "df:include";

    private static final MetaFileReader instance = new MetaFileReader();
    private static final ServiceLoader<MetaType> loader = ServiceLoader.load(MetaType.class);

    public static MetaFileReader instance() {
        return instance;
    }

    public static Meta read(Path file) {
        try {
            return instance().read(Global.INSTANCE, file, null);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to read meta file " + file.toString(), e);
        }
    }

    /**
     * Resolve the file with given name (without extension) in the directory and read it as meta. If multiple files with the same name exist in the directory, the ran
     *
     * @param directory
     * @param name
     * @return
     */
    public static Optional<Meta> resolve(Path directory, String name) {
        try {
            return Files.list(directory).filter(it -> it.startsWith(name)).findFirst().map(MetaFileReader::read);
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files in the directory " + directory.toString(), e);
        }
    }

    public Meta read(Context context, String path, Charset encoding) throws IOException, ParseException {
        return read(context, context.getIo().getRootDir().resolve(path), encoding);
    }

    public Meta read(Context context, String path) throws IOException, ParseException {
        return read(context, context.getIo().getRootDir().resolve(path), null);
    }

    public Meta read(Context context, Path file, Charset encoding) throws IOException, ParseException {
        String fileName = file.getFileName().toString();
        for (MetaType type : loader) {
            if (type.getFileNameFilter().invoke(fileName)) {
                return transform(context, type.getReader().withCharset(encoding).readFile(file));
            }
        }
        //Fall back and try to resolve meta as an envelope ignoring extension
        return EnvelopeReader.Companion.readFile(file).getMeta();
    }

    /**
     * Evaluate parameter substitution and include substitution
     *
     * @param builder
     * @return
     */
    protected MetaBuilder transform(Context context, MetaBuilder builder) {
        return builder.substituteValues(context);
    }

    protected String evaluateSubst(Context context, String subst) {
        return subst;
    }

}
