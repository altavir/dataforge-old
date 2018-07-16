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

import hep.dataforge.Type
import hep.dataforge.exceptions.NamingException
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaMorph
import hep.dataforge.tables.Filtering.getTagCondition
import hep.dataforge.tables.Filtering.getValueCondition
import hep.dataforge.tables.Table.Companion.TABLE_TYPE
import hep.dataforge.toList
import hep.dataforge.values.Value
import hep.dataforge.values.ValueFactory
import hep.dataforge.values.Values
import java.util.*
import java.util.function.Predicate


/**
 * An immutable table of values
 *
 * @author Alexander Nozik
 */
@Type(TABLE_TYPE)
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

object Tables{

    @JvmStatic
    fun sort(table: Table, comparator: Comparator<Values>): Table {
        return ListTable(table.format, table.rows.sorted(comparator).toList())
    }

    @JvmStatic
    fun sort(table: Table, name: String, ascending: Boolean): Table {
        return sort(
                table,
                Comparator { o1: Values, o2: Values ->
                    val signum = if (ascending) +1 else -1
                    o1.getValue(name).compareTo(o2.getValue(name)) * signum
                }
        )
    }

    /**
     * Фильтрует набор данных и оставляет только те точки, что удовлетовряют
     * условиям
     *
     * @param condition a [java.util.function.Predicate] object.
     * @return a [hep.dataforge.tables.Table] object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    @Throws(NamingException::class)
    @JvmStatic
    fun filter(table: Table, condition: Predicate<Values>): Table {
        return ListTable(table.format, table.rows.filter(condition).toList())
    }

    /**
     * Быстрый фильтр для значений одного поля
     *
     * @param valueName
     * @param a
     * @param b
     * @return
     * @throws hep.dataforge.exceptions.NamingException
     */
    @Throws(NamingException::class)
    @JvmStatic
    fun filter(table: Table, valueName: String, a: Value, b: Value): Table {
        return filter(table, getValueCondition(valueName, a, b))
    }

    @Throws(NamingException::class)
    @JvmStatic
    fun filter(table: Table, valueName: String, a: Number, b: Number): Table {
        return filter(table, getValueCondition(valueName, ValueFactory.of(a), ValueFactory.of(b)))
    }

    /**
     * Быстрый фильтр по меткам
     *
     * @param tags
     * @return a [hep.dataforge.tables.Column] object.
     * @throws hep.dataforge.exceptions.NamingException
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    @Throws(NamingException::class)
    @JvmStatic
    fun filter(table: Table, vararg tags: String): Table {
        return filter(table, getTagCondition(*tags))
    }
}
