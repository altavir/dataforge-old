package hep.dataforge.plots.jzy3d;

import hep.dataforge.meta.Meta;
import hep.dataforge.plots.fx.FXPlotFrame;
import hep.dataforge.plots.fx.FXPlotFrameFactory;

/**
 * Created by darksnake on 30-Mar-17.
 */
public class JZY3DFrameFactory implements FXPlotFrameFactory {
    public static final String JZY3D_FRAME_TYPE = "jzy3d";

    @Override
    public FXPlotFrame build(Meta meta) {
        return new JZY3DFrame();
    }

    @Override
    public String getName() {
        return JZY3D_FRAME_TYPE;
    }
}
