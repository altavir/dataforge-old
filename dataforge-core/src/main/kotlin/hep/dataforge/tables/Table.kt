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
package hep.dataforge.tables

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaMorph
import hep.dataforge.values.Value


/**
 * An immutable table of values
 *
 * @author Alexander Nozik
 */
interface Table : NavigableValuesSource, MetaMorph {

    /**
     * Get columns as a stream
     *
     * @return
     */
    val columns: Collection<Column>

    /**
     * A minimal set of fields to be displayed in this table. Could return empty format if source is unformatted
     *
     * @return
     */
    val format: TableFormat

    /**
     * Get an immutable column from this table
     *
     * @param name
     * @return
     */
    fun getColumn(name: String): Column

    /**
     * Get a specific value
     *
     * @param columnName the name of the column
     * @param rowNumber  the number of the row
     * @return
     */
    @JvmDefault
    override fun get(columnName: String, rowNumber: Int): Value

//    @JvmDefault
//    override fun markup(configuration: Meta): Markup {
//        val builder = TableMarkup()
//
//        //render header
//        val header = RowMarkup(builder)
//        format.columns.map { col -> TextMarkup.create(col.title) }.forEach{ header.add(it) }
//        builder.header = header
//
//
//        //render table itself
//        forEach { dp ->
//            val row = RowMarkup(builder)
//            format.columns.map { col -> TextMarkup.create(dp.getString(col.name)) }.forEach{ row.add(it) }
//            builder.addRow(row)
//        }
//        return builder
//    }

    @JvmDefault
    override fun toMeta(): Meta {
        val res = MetaBuilder("table")
        res.putNode("format", format.toMeta())
        val dataNode = MetaBuilder("data")
        forEach { dp -> dataNode.putNode("point", dp.toMeta()) }
        res.putNode(dataNode)
        return res
    }

    //    default Meta toMeta() {
    //        MetaBuilder res = new MetaBuilder("table");
    //        res.putNode("format", getFormat().toMeta());
    //        MetaBuilder dataNode = new MetaBuilder("data");
    //        forEach(dp -> dataNode.putNode("point", dp.toMeta()));
    //        res.putNode(dataNode);
    //        return res;
    //    }

    companion object {
        const val TABLE_TYPE = "hep.dataforge.table"
    }
}
