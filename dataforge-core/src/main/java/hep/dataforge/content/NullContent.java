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
package hep.dataforge.content;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;

/**
 * Null-контент не может быть аннотирован или наследован. Служит исключительно в
 * качестве заглушки
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public final class NullContent implements Content {

    /** {@inheritDoc} */
    @Override
    public Content configure(Meta a) {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Meta meta() {
        return MetaBuilder.buildEmpty("");
    }


    /** {@inheritDoc} */
    @Override
    public String getName() {
        return null;
    }

}
