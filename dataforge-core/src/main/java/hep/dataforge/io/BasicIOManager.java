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

import hep.dataforge.context.Context;
import hep.dataforge.names.Name;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * <p>
 * BasicIOManager class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class BasicIOManager implements IOManager {

    private Context context;

    private OutputStream out;
    private InputStream in;

    public BasicIOManager() {
    }

    public BasicIOManager(OutputStream out) {
        this.out = out;
    }

    public BasicIOManager(PrintStream out, InputStream in) {
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
        String rootDir = getContext().getString(ROOT_DIRECTORY_CONTEXT_KEY, System.getProperty("user.dir"));
        return new File(rootDir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext() {
        if (context == null) {
            throw new RuntimeException("IOManager not attached to a context.");
        }
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
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
