package hep.dataforge.tables;

import hep.dataforge.Named;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.SimpleMetaMorph;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by darksnake on 29-Dec-16.
 */
public class ColumnFormat extends SimpleMetaMorph implements Named {

    public static final String TAG_KEY = "tag";

//    /**
//     * mark column as optional so its value is replaced by {@code null} in table builder if it is not present
//     */
//    public static final String OPTIONAL_TAG = "optional";

    /**
     * Construct simple column format
     *
     * @param name
     * @param type
     * @return
     */
    @NotNull
    public static ColumnFormat build(String name, ValueType type, String... tags) {
        return new ColumnFormat(new MetaBuilder("column")
                .putValue("name", name)
                .putValue("type", type)
                .putValue(TAG_KEY, Stream.of(tags).collect(Collectors.toList()))
        );
    }

    /**
     * Create a new format instance with changed name. Returns argument if name is not changed
     *
     * @param name
     * @param columnFormat
     * @return
     */
    public static ColumnFormat rename(String name, ColumnFormat columnFormat) {
        if (name.equals(columnFormat.getName())) {
            return columnFormat;
        } else {
            return new ColumnFormat(columnFormat.toMeta().getBuilder().setValue("name", name).build());
        }
    }

    public ColumnFormat(Meta meta) {
        super(meta);
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
     * @return
     */
    public List<String> getTags() {
        return Arrays.asList(getStringArray(TAG_KEY));
    }

}
