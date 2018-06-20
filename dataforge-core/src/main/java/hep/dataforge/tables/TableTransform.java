package hep.dataforge.tables;

import hep.dataforge.exceptions.NamingException;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueFactory;
import hep.dataforge.values.Values;

import java.util.Comparator;
import java.util.function.Predicate;

import static hep.dataforge.tables.Filtering.getTagCondition;
import static hep.dataforge.tables.Filtering.getValueCondition;

/**
 * Created by darksnake on 14-Nov-16.
 */
public class TableTransform {

    public static Table sort(Table table, Comparator<Values> comparator) {
        return new ListTable(table.getFormat(), table.getRows().sorted(comparator));
    }

    public static Table sort(Table table, String name, boolean ascending) {
        return sort(table, (Values o1, Values o2) -> {
            int signum = ascending ? +1 : -1;
            return o1.getValue(name).compareTo(o2.getValue(name)) * signum;
        });
    }

    /**
     * Фильтрует набор данных и оставляет только те точки, что удовлетовряют
     * условиям
     *
     * @param condition a {@link java.util.function.Predicate} object.
     * @return a {@link hep.dataforge.tables.Table} object.
     * @throws hep.dataforge.exceptions.NamingException if any.
     */
    public static Table filter(Table table, Predicate<Values> condition) throws NamingException {
        return new ListTable(table.getFormat(), table.getRows().filter(condition));
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
    public static Table filter(Table table, String valueName, Value a, Value b) throws NamingException {
        return filter(table, getValueCondition(valueName, a, b));
    }

    public static Table filter(Table table, String valueName, Number a, Number b) throws NamingException {
        return filter(table, getValueCondition(valueName, ValueFactory.of(a), ValueFactory.of(b)));
    }

    /**
     * Быстрый фильтр по меткам
     *
     * @param tags
     * @return a {@link hep.dataforge.tables.Column} object.
     * @throws hep.dataforge.exceptions.NamingException
     * @throws hep.dataforge.exceptions.NameNotFoundException if any.
     */
    public static Table filter(Table table, String... tags) throws NamingException {
        return filter(table, getTagCondition(tags));
    }


}
