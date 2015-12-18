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
package hep.dataforge.data;

import hep.dataforge.context.Context;
import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.io.MetaFileReader;
import hep.dataforge.meta.Meta;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;

/**
 * <p>SourceSetLoader class.</p>
 *
 * @author Alexander Nozik TODO перенести в контекст?
 * @version $Id: $Id
 */
public class SourceSetLoader {

    /** Constant <code>SOURCE_SET_TAG="sourceSetTag"</code> */
    public static final String SOURCE_SET_TAG = "sourceSetTag";
    /** Constant <code>SOURCE_SET="sourceSet"</code> */
    public static final String SOURCE_SET = "sourceSet";

    private static final Map<String, SourceSetLoader> loaders = new ConcurrentHashMap<>();

    /**
     * <p>loadFromFile.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param sourceSetConfig a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.data.SourceSetLoader} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.InterruptedException if any.
     */
    public static SourceSetLoader loadFromFile(Context context, String sourceSetConfig) throws IOException, InterruptedException, ParseException {
        if (loaders.containsKey(sourceSetConfig)) {
            return loaders.get(sourceSetConfig);
        } else {
            Meta config = MetaFileReader.instance().read(context, sourceSetConfig, null);

            List<? extends Meta> list = config.getNodes(SOURCE_SET);

            if (list.isEmpty()) {
                LoggerFactory.getLogger(SourceSetLoader.class).warn("The source set is empty");
            }

            Map<String, Object> sources = new HashMap<>();

            for (Meta an : list) {
                String tag = an.getString(SOURCE_SET_TAG);
                sources.put(tag, new DataManager(context).read(an));
            }
            SourceSetLoader res = new SourceSetLoader(sources);
            
            loaders.put(sourceSetConfig, res);
            return res;
        }
    }

    private final Map<String, Object> sources;

    private SourceSetLoader(Map<String, Object> sources) {
        this.sources = sources;
    }

    /**
     * <p>getSourceSet.</p>
     *
     * @param tag a {@link java.lang.String} object.
     * @return a {@link hep.dataforge.content.Content} object.
     */
    public Object getSourceSet(String tag) {
        if (sources.containsKey(tag)) {
            return sources.get(tag);
        } else {
            throw new NameNotFoundException(tag);
        }
    }
}
