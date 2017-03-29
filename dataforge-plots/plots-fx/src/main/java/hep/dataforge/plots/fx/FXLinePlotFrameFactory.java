package hep.dataforge.plots.fx;

import hep.dataforge.meta.Meta;

/**
 * Created by darksnake on 29-Mar-17.
 */
public class FXLinePlotFrameFactory implements FXPlotFrameFactory {
    public static final String FX_LINE_FRAME_TYPE = "fx.line";

    @Override
    public String getName() {
        return FX_LINE_FRAME_TYPE;
    }

    @Override
    public FXPlotFrame build(Meta meta) {
        FXPlotFrame res =  new FXLineChartFrame();
        res.configure(meta);
        return res;
    }
}
