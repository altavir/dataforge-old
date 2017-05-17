package hep.dataforge.plots.fx;

import hep.dataforge.names.Named;
import hep.dataforge.utils.MetaFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by darksnake on 29-Mar-17.
 */
public interface FXPlotFrameFactory extends MetaFactory<FXPlotFrame>, Named, Comparable<FXPlotFrameFactory> {
    default int getPriority() {
        return 0;
    }

    @Override
    default int compareTo(@NotNull FXPlotFrameFactory o) {
        return -Integer.compare(this.getPriority(), o.getPriority());
    }
}