package hep.dataforge.plots.jfreechart;

import hep.dataforge.meta.Meta;
import hep.dataforge.plots.fx.FXPlotFrame;
import hep.dataforge.plots.fx.FXPlotFrameFactory;

/**
 * Created by darksnake on 29-Mar-17.
 */
public class JFCFrameFactory implements FXPlotFrameFactory {
    public static final String JFREECHART_FRAME_TYPE = "jfc";

    @Override
    public FXPlotFrame build(Meta meta) {
        return new JFreeChartFrame(meta);
    }

    @Override
    public String getName() {
        return JFREECHART_FRAME_TYPE;
    }
}
