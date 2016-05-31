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
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Name;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * IOManager interface.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface IOManager {

    public static final String ROOT_DIRECTORY_CONTEXT_KEY = "rootDir";
    public static final String WORK_DIRECTORY_CONTEXT_KEY = "workDir";
    public static final String TEMP_DIRECTORY_CONTEXT_KEY = "tempDir";

    /**
     * Output stream for specific stage and specific name. All parameters could
     * be null. In this case default values are used.
     *
     * @param stage
     * @param name
     * @param type
     * @return
     */
    OutputStream out(Name stage, Name name);

    /**
     * Custom output builder using given configuration
     *
     * @param outConfig
     * @return
     */
    default OutputStream out(Meta outConfig) {
        return out(Name.of(outConfig.getString("stage", "")),
                Name.of(outConfig.getString("name", ""))
        );
    }

    default OutputStream out(String stage, String name) {
        return out(Name.of(stage), Name.of(name));
    }

    /**
     * The default outputStream for this IOManager. Should not be used for any
     * sensitive data or results
     *
     * @return
     */
    OutputStream out();

    /**
     * User input Stream
     *
     * @return a {@link java.io.InputStream} object.
     */
    InputStream in();

    /**
     * Inputstream built by custom path
     *
     * @param path
     * @return
     */
    InputStream in(String path);

    /**
     * Get a file where {@code path} is relative to root directory.
     *
     * @param path a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    File getFile(String path);

    /**
     * Return the root directory for this IOManager. By convention, Context
     * should not have access outside root directory to prevent System damage.
     *
     * @return a {@link java.io.File} object.
     */
    File getRootDirectory();
    
    /**
     * The working directory for output and temporary files. Is always inside root directory
     * @return 
     */
    default File getWorkDirectory(){
        String tmpDir = getContext().getString(WORK_DIRECTORY_CONTEXT_KEY, ".dataforge");
        File work = new File(getRootDirectory(), tmpDir);
        if(!work.exists()){
            work.mkdirs();
        }
        return work;
    }

    /**
     * The directory for temporary files. This directory could be cleaned up any
     * moment. Is always inside root directory.
     *
     * @return
     */
    default File getTmpDirectory() {
        String tmpDir = getContext().getString(TEMP_DIRECTORY_CONTEXT_KEY, ".dataforge/.temp");
        File tmp = new File(getRootDirectory(), tmpDir);
        if(!tmp.exists()){
            tmp.mkdirs();
        }
        return tmp;
    }

    /**
     * Context for this IOManager
     *
     * @return a {@link hep.dataforge.context.Context} object.
     */
    Context getContext();

    void setContext(Context context);

}
