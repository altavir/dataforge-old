package hep.dataforge.plots;

import hep.dataforge.description.Described;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Name;

public interface Plottable extends Metoid, Configurable, Described {
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
        return meta().getString("title", getName().toUnescaped());
    }
}
