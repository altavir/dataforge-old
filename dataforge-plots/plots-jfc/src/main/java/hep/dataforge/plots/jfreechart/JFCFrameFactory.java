package hep.dataforge.plots.jfreechart;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.fx.FXPlotFrame;
import hep.dataforge.plots.fx.FXPlotFrameFactory;
import hep.dataforge.plots.fx.FXPlotManager;

/**
 * Created by darksnake on 29-Mar-17.
 */
public class JFCFrameFactory implements FXPlotFrameFactory {
    public static final String JFREECHART_FRAME_TYPE = "jfc";

    public static void setDefault(Context context){
        context.pluginManager().getOrLoad(FXPlotManager.class).configureValue(FXPlotManager.FX_FRAME_TYPE_KEY, JFCFrameFactory.JFREECHART_FRAME_TYPE);
    }

    @Override
    public FXPlotFrame build(Meta meta) {
        return new JFreeChartFrame(meta);
    }

    @Override
    public String getName() {
        return JFREECHART_FRAME_TYPE;
    }

    @Override
    public int getPriority() {
        return 1000;
    }
}
