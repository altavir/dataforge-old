/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io;

import hep.dataforge.context.Context;
import hep.dataforge.context.GlobalContext;
import hep.dataforge.meta.MetaBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ServiceLoader;

//TODO add examples for transformations
/**
 * A reader for meta file in any supported format. Additional file formats could
 * be statically registered by plug-ins.
 *
 * <p>
 * Basically reader performs two types of "on read" transformations:
 * <ul>
 * <li>Includes: include a meta from given file instead of given node</li>
 * <li>Substitutions: replaces all occurrences of {@code ${<key>}} in child meta
 * nodes by given value. Substitutions are made as strings.</li>
 * </ul>
 *
 * </p>
 *
 * @author Alexander Nozik <altavir@gmail.com>
 */
public class MetaFileReader {

    public static String SUBST_ELEMENT = "df:subst";
    public static String INCLUDE_ELEMENT = "df:include";

    private static final MetaFileReader instance = new MetaFileReader();
    private static final ServiceLoader<MetaStreamReader> loader = ServiceLoader.load(MetaStreamReader.class);

    public static MetaFileReader instance() {
        return instance;
    }

    public static MetaBuilder read(String name, File file) throws IOException, ParseException {
        return instance().read(GlobalContext.instance(), file, null).rename(name);
    }

    public static MetaBuilder read(File file) throws IOException, ParseException {
        return instance().read(GlobalContext.instance(), file, null);
    }

    public MetaBuilder read(Context context, String path, Charset encoding) throws IOException, ParseException {
        return read(context, context.io().getFile(path), encoding);
    }

    public MetaBuilder read(Context context, String path) throws IOException, ParseException {
        return read(context, context.io().getFile(path), null);
    }

    public MetaBuilder read(Context context, File file, Charset encoding) throws IOException, ParseException {
        for (MetaStreamReader reader : loader) {
            if (reader.acceptsFile(file)) {
                return transform(context, reader.read(new FileInputStream(file), file.length(), encoding));
            }
        }
        
        throw new RuntimeException("Could not find appropriate reader for meta file: " + file.toString());
    }

    /**
     * Evaluate parameter substitution and include substitution
     *
     * @param builder
     * @return
     */
    protected MetaBuilder transform(Context context, MetaBuilder builder) {
        //TODO implement substs
        return builder.setContext(context);
    }

    protected String evaluateSubst(Context context, String subst) {
        return subst;
    }

}
