package hep.dataforge.tables;

import hep.dataforge.exceptions.NameNotFoundException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.MetaMorph;
import hep.dataforge.names.NameSetContainer;
import hep.dataforge.names.Names;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A description of table columns
 * Created by darksnake on 12.07.2017.
 */
public interface TableFormat extends NameSetContainer, Iterable<ColumnFormat>, MetaMorph {

    @NotNull
    static TableFormat subFormat(TableFormat format, String... names){
        Names theNames = Names.of(names);
        return () -> format.getColumns().filter(it -> theNames.contains(it.getName()));
    }

    /**
     * Convert this table format to its meta representation
     * @return
     */
    @NotNull
    @Override
    default Meta toMeta(){
        MetaBuilder builder = new MetaBuilder("format");
        getColumns().forEach(column -> builder.putNode(column.toMeta()));
        return builder;
    }

    /**
     * Names of the columns
     * @return
     */
    @Override
    default Names getNames(){
        return Names.of(getColumns().map(ColumnFormat::getName));
    }

    /**
     * Column format for given name
     * @param column
     * @return
     */
    default ColumnFormat getColumn(String column) {
        return getColumns()
                .filter(it -> it.getName().equals(column))
                .findFirst()
                .orElseThrow(() -> new NameNotFoundException(column));
    }

    /**
     * Stream of column formats
     * @return
     */
    Stream<ColumnFormat> getColumns();

    @NotNull
    @Override
    default Iterator<ColumnFormat> iterator() {
        return getColumns().iterator();
    }
}
