package hep.dataforge.plots;

import hep.dataforge.description.Described;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Named;

import java.util.Collections;
import java.util.Map;

public interface Plottable extends Named, Metoid, Configurable, Described {
    /**
     * Add plottable state listener
     *
     * @param listener
     */
    void addListener(PlotStateListener listener);

    /**
     * Remove plottable state listener
     *
     * @param listener
     */
    void removeListener(PlotStateListener listener);

    default Map<String, Plottable> getChildren(){
        return Collections.emptyMap();
    }
}
