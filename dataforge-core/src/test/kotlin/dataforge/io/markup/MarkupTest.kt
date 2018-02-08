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

package dataforge.io.markup

import hep.dataforge.io.markup.SimpleMarkupRenderer
import hep.dataforge.io.markup.markup
import org.junit.Test

class MarkupTest {
    @Test
    fun testSimpleRenderer() {
        val markup = markup {
            +"this is my text "
            list{
                item{
                    +"test"
                }
            }

            list {
                +"first line"
                item {
                    +"second "
                    text("line", "blue")
                }
                text("red line", "red")
                item {
                    +"sub list"
                    list {
                        +"first line"
                        +"second line"
                        text("red line", "green")
                    }
                }
                text("blue line", "blue")
            }
            text("text end")
        }

        SimpleMarkupRenderer(System.out).render(markup)
    }
}