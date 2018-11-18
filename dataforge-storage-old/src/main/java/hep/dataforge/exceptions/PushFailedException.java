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
package hep.dataforge.exceptions;

import hep.dataforge.storage.api.Loader;

/**
 *
 * @author Alexander Nozik
 */
public class PushFailedException extends StorageException {
    private Loader loader;

    public PushFailedException(Loader loader, String string) {
        super(string);
        this.loader = loader;
    }

    public PushFailedException(Loader loader, String string, Throwable thrwbl) {
        super(string, thrwbl);
        this.loader = loader;
    }

    public PushFailedException(Loader loader, Throwable thrwbl) {
        super(thrwbl);
        this.loader = loader;
    }

    public Loader getLoader() {
        return loader;
    }


}
