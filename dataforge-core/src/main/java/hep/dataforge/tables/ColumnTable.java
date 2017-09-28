package hep.dataforge.tables;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import hep.dataforge.values.Values;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A column based table. Column access is fast, but row access is slow. Best memory efficiency.
 * Column table is immutable all operations create new tables.
 * Created by darksnake on 12.07.2017.
 */
public class ColumnTable implements Table {

    public static ColumnTable copy(Table table) {
        if (table instanceof ColumnTable) {
            return (ColumnTable) table;
        } else {
            return new ColumnTable(table.getColumns().collect(Collectors.toList()));
        }
    }

    /**
     * Create instance of column table using given columns with appropriate names
     *
     * @param columns
     * @return
     */
    public static ColumnTable of(Map<String, Column> columns) {
        return new ColumnTable(
                columns.entrySet()
                        .stream()
                        .map(entry -> ListColumn.copy(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
    }

    private final Map<String, Column> columns = new LinkedHashMap<>();
    private final int size;

    /**
     * Build a table from pre-constructed columns
     *
     * @param columns
     */
    public ColumnTable(Collection<Column> columns) {
        columns.forEach(it -> this.columns.put(it.getName(), ListColumn.copy(it)));
        if (this.columns.values().stream().mapToInt(Column::size).distinct().count() != 1) {
            throw new IllegalArgumentException("Column dimension mismatch");
        }
        size = this.columns.values().stream().findFirst().map(Column::size).orElse(0);
    }

    /**
     * Create empty column table
     */
    public ColumnTable() {
        size = 0;
    }

    @Override
    public Values getRow(int i) {
        return new ValueMap(getColumns().map(Column::getName).collect(Collectors.toMap(it -> it, it -> get(it, i))));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Column getColumn(String name) {
        return columns.get(name);
    }

    @Override
    public Stream<Column> getColumns() {
        return columns.values().stream();
    }

    @Override
    public Value get(String columnName, int rowNumber) {
        return getColumn(columnName).get(rowNumber);
    }

    @Override
    public TableFormat getFormat() {
        return () -> getColumns().map(Column::getFormat);
    }

    @NotNull
    @Override
    public Iterator<Values> iterator() {
        return getRows().iterator();
    }

    @Override
    public Stream<Values> getRows() {
        return IntStream.range(0, size).mapToObj(this::getRow);
    }


    /**
     * Add or replace column
     *
     * @param column
     * @return
     */
    public ColumnTable addColumn(Column column) {
        Map<String, Column> map = new LinkedHashMap<>(columns);
        map.put(column.getName(), column);
        return new ColumnTable(map.values());
    }

    /**
     * Add a new column built from object stream
     *
     * @param name
     * @param type
     * @param data
     * @param tags
     * @return
     */
    public ColumnTable addColumn(String name, ValueType type, Stream<?> data, String... tags) {
        ColumnFormat format = ColumnFormat.build(name, type, tags);
        Column column = new ListColumn(format, data.map(Value::of));
        return addColumn(column);
    }

    /**
     * Create a new table with values derived from appropriate rows. The operation does not consume a lot of memory
     * and time since existing columns are immutable and are reused.
     * <p>
     * If column with given name exists, it is replaced.
     *
     * @param format
     * @param transform
     * @return
     */
    public ColumnTable buildColumn(ColumnFormat format, Function<Values, Object> transform) {
        List<Column> list = new ArrayList<>(columns.values());
        Column newColumn = ListColumn.build(format, getRows().map(transform));
        list.add(newColumn);
        return new ColumnTable(list);
    }

    public ColumnTable buildColumn(String name, ValueType type, Function<Values, Object> transform) {
        ColumnFormat format = ColumnFormat.build(name, type);
        return buildColumn(format, transform);
    }

    /**
     * Replace existing column with new values (without changing format)
     *
     * @param columnName
     * @param transform
     * @return
     */
    public ColumnTable replaceColumn(String columnName, Function<Values, Object> transform) {
        if (!columns.containsKey(columnName)) {
            throw new NameNotFoundException(columnName);
        }
        Map<String, Column> map = new LinkedHashMap<>(columns);
        Column newColumn = ListColumn.build(columns.get(columnName).getFormat(), getRows().map(transform));
        map.put(columnName, newColumn);
        return new ColumnTable(map.values());
    }

    /**
     * Return a new Table with given columns being removed
     *
     * @param columnName
     * @return
     */
    public ColumnTable removeColumn(String... columnName) {
        Map<String, Column> map = new LinkedHashMap<>(columns);
        for (String c : columnName) {
            map.remove(c);
        }
        return new ColumnTable(map.values());
    }
}
