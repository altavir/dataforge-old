/*
 * Copyright  2017 Alexander Nozik.
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

package hep.dataforge.kodex

import hep.dataforge.tables.Column
import hep.dataforge.tables.ColumnFormat
import hep.dataforge.tables.ColumnTable
import hep.dataforge.tables.Table
import hep.dataforge.values.ValueType
import hep.dataforge.values.Values
import java.util.stream.Stream

/**
 *  Extension methods for tables
 */

///**
// * Create a new table applying given transformation to each row
// */
//fun Table.transform(transform: (Values)-> Values): Table{
//    return ListTable.copy(this)
//}

/**
 * Return a new table with additional column.
 * Warning: if initial table is not a column table, then the whole amount of data will be copied, which could be ineffective for large tables
 */
operator fun Table.plus(column: Column): Table {
    return ColumnTable.copy(this).addColumn(column)
}

/**
 *  Warning: if initial table is not a column table, then the whole amount of data will be copied, which could be ineffective for large tables
 */
fun Table.addColumn(name: String, type: ValueType, data: Stream<*>, vararg tags: String): Table {
    return ColumnTable.copy(this).addColumn(name, type, data, *tags)
}

/**
 *  Warning: if initial table is not a column table, then the whole amount of data will be copied, which could be ineffective for large tables
 */
fun Table.addColumn(format: ColumnFormat, transform: Values.() -> Any): Table {
    return ColumnTable.copy(this).buildColumn(format, transform)
}

fun Table.replaceColumn(name: String, transform: Values.() -> Any): Table {
    return ColumnTable.copy(this).replaceColumn(name,transform)
}
