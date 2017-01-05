package hep.dataforge.io;

import hep.dataforge.meta.Meta;
import hep.dataforge.tables.Table;

/**
 * An interface to show table data
 * Created by darksnake on 29-Dec-16.
 */
public interface TableDisplay {

    /**
     * Display the table. The meta is displayed separately from the table itself. Values from meta could be used to customize display
     *
     * @param table
     * @param tableMeta
     */
    void displayTable(Table table, Meta tableMeta);

    /**
     * Displat a table without meta
     *
     * @param table
     */
    default void displayTable(Table table) {
        displayTable(table, Meta.empty());
    }
}
