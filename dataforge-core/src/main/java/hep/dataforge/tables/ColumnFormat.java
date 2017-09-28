package hep.dataforge.tables;

import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.NonEmptyMetaMorphException;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.Named;
import hep.dataforge.utils.SimpleMetaMorph;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hep.dataforge.tables.ColumnFormat.TAG_KEY;
import static hep.dataforge.values.ValueType.NUMBER;

/**
 * Created by darksnake on 29-Dec-16.
 */
@ValueDef(name = "name", required = true, info = "The name of the column.")
@ValueDef(name = "title", info = "Column title.")
@ValueDef(name = "type", multiple = true, info = "A type of this column or a list of allowed types. First entry designates primary type.")
@ValueDef(name = "precision", type = {NUMBER}, info = "Expected precision for number values or length for string values")
@ValueDef(name = TAG_KEY, multiple = true, info = "The role of data in this column for plotting or other purposes")
public class ColumnFormat extends SimpleMetaMorph implements Named {

    public static final String TAG_KEY = "tag";

//    /**
//     * mark column as optional so its value is replaced by {@code null} in table builder if it is not present
//     */
//    public static final String OPTIONAL_TAG = "optional";

    /**
     * Construct simple column format
     * @param name
     * @param type
     * @return
     */
    @NotNull
    public static ColumnFormat build(String name, ValueType type, String... tags){
        return new ColumnFormat(new MetaBuilder("column")
                .putValue("name",name)
                .putValue("type",type)
                .putValue(TAG_KEY, Stream.of(tags).collect(Collectors.toList()))
        );
    }

    /**
     * Create a new format instance with changed name. Returns argument if name is not changed
     * @param name
     * @param columnFormat
     * @return
     */
    public static ColumnFormat rename(String name, ColumnFormat columnFormat){
        if(name.equals(columnFormat.getName())){
            return columnFormat;
        } else {
            return new ColumnFormat(columnFormat.toMeta().getBuilder().setValue("name",name).build());
        }
    }

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
     * Check if value is allowed by the format. It 'type' field of meta is empty then any type is allowed.
     *
     * @param value
     * @return
     */
    public boolean isAllowed(Value value) {
        //TODO add complex analysis here including enum-values
        return !hasValue("type") || Arrays.asList(getStringArray("type")).contains(value.getType().name());
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
    public List<String> getTags(){
        return Arrays.asList(getStringArray(TAG_KEY));
    }
}
