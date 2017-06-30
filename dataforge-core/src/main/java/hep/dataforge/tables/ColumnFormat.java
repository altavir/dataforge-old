package hep.dataforge.tables;

import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NonEmptyMetaMorphException;
import hep.dataforge.meta.Meta;
import hep.dataforge.names.Named;
import hep.dataforge.utils.SimpleMetaMorph;
import hep.dataforge.values.ValueType;

import java.util.Arrays;
import java.util.List;

import static hep.dataforge.values.ValueType.NUMBER;

/**
 * Created by darksnake on 29-Dec-16.
 */
@ValueDef(name = "name", required = true, info = "The name of the column.")
@ValueDef(name = "title", info = "Column title.")
@ValueDef(name = "type", multiple = true, info = "A type of this column or a list of allowed types. First entry designates primary type.")
@ValueDef(name = "precision", type = {NUMBER}, info = "Expected precision for number values or length for string values")
@ValueDef(name = "purpose", multiple = true, info = "The role of data in this column for plotting or other purposes")
public class ColumnFormat extends SimpleMetaMorph implements Named {

    public ColumnFormat() {
    }

    public ColumnFormat(Meta meta) {
        super(meta);
    }

    @Override
    public Meta toMeta() {
        return meta();
    }

    @Override
    public void fromMeta(Meta meta) {
        if (!meta().isEmpty()) {
            throw new NonEmptyMetaMorphException(getClass());
        }
        setMeta(meta);
    }

    @Override
    public String getName() {
        return getString("name");
    }

    /**
     * Check if value of this type is allowed by the format. It 'type' field of meta is empty then any type is allowed.
     *
     * @param type
     * @return
     */
    public boolean isAllowed(ValueType type) {
        return !hasValue("type") || Arrays.asList(getStringArray("type")).contains(type.name());
    }

    /**
     * Return primary type. By default primary type is {@code STRING}
     *
     * @return
     */
    public ValueType getPrimaryType() {
        return ValueType.valueOf(getString("type", ValueType.STRING.name()));
    }

    /**
     * Get displayed title for this column. By default returns column name
     *
     * @return
     */
    public String getTitle() {
        return getString("title", this::getName);
    }

    /**
     *
     * @return
     */
    public List<String> getPurpose(){
        return Arrays.asList(getStringArray("purpose"));
    }
}
