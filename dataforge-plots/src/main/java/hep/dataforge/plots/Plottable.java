package hep.dataforge.plots;

import hep.dataforge.description.Described;
import hep.dataforge.meta.Configurable;
import hep.dataforge.meta.Metoid;
import hep.dataforge.names.Named;

import java.util.Collection;
import java.util.Collections;

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

    default Collection<Plottable> getChildren(){
        return Collections.emptyList();
    }
}
