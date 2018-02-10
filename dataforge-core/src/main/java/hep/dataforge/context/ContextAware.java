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
package hep.dataforge.context;

import hep.dataforge.names.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interface for something that encapsulated in context
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public interface ContextAware {
    /**
     * Get context for this object
     *
     * @return
     */
    Context getContext();

    default Logger getLogger() {
        if (this instanceof Named) {
            return LoggerFactory.getLogger(getContext().getName() + "." + ((Named) this).getName());
        } else {
            return getContext().getLogger();
        }
    }
}
