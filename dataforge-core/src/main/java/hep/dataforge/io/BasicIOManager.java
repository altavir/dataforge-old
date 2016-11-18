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
package hep.dataforge.io;

import hep.dataforge.context.BasicPlugin;
import hep.dataforge.context.Context;
import hep.dataforge.context.PluginDef;
import hep.dataforge.names.Name;

import java.io.*;

/**
 * <p>
 * BasicIOManager class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@PluginDef(name = "io", group = "hep.dataforge", description = "Basic input and output plugin")
public class BasicIOManager extends BasicPlugin implements IOManager {

    private Context context;

    private OutputStream out;
    private InputStream in;

    public BasicIOManager() {
    }

    public BasicIOManager(OutputStream out) {
        this.out = out;
    }

    public BasicIOManager(OutputStream out, InputStream in) {
        this.out = out;
        this.in = in;
    }



    @Override
    public InputStream in() {
        if (in == null) {
            return System.in;
        } else {
            return in;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream out(Name stage, Name name) {
        return out();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream out() {
        if (out == null) {
            return System.out;
        } else {
            return out;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile(String path) {
        File file = new File(path);
        if (file.isAbsolute()) {
            return file;
        } else {
            return new File(getRootDirectory(), path);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getRootDirectory() {
        String rootDir = getContext().getString(ROOT_DIRECTORY_CONTEXT_KEY, System.getProperty("user.home"));
        File root = new File(rootDir);
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;
    }



    @Override
    public InputStream in(String path) {
        try {
            return new FileInputStream(getFile(path));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

}
