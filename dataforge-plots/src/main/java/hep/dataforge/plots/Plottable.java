package hep.dataforge.plots;

import hep.dataforge.description.Described;
import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Configurable;
import hep.dataforge.names.Name;
import hep.dataforge.values.ValueType;

@ValueDef(name = "title", info = "The title of series. Could be not unique. By default equals series name.")
@ValueDef(name = "visible", def = "true", type = ValueType.BOOLEAN, info = "The current visibility of this plottable")
public interface Plottable extends Configurable, Described {
    /**
     * Add plottable state listener
     *
     * @param listener
     */
    void addListener(PlotListener listener);

    /**
     * Remove plottable state listener
     *
     * @param listener
     */
    void removeListener(PlotListener listener);

    Name getName();

    default String getTitle() {
        return getConfig().getString("title", getName().toUnescaped());
    }
}
