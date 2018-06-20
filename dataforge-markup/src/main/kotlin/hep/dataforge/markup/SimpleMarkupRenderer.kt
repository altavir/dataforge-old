/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package hep.dataforge.markup

import hep.dataforge.markup.markup.StreamMarkupRenderer
import java.io.OutputStream
import java.io.PrintStream

/**
 * A simple renderer using basic PrintStream without color support
 * Created by darksnake on 03-Jan-17.
 */
class SimpleMarkupRenderer(private val stream: PrintStream = System.out) : StreamMarkupRenderer() {

    constructor(stream: OutputStream) : this(PrintStream(stream))

    override fun printText(string: String) {
        stream.print(string)
    }

    override fun ln() {
        stream.println()
        stream.flush()
    }
}
