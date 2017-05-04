package hep.dataforge.plots.jzy3d;

import hep.dataforge.meta.Meta;
import hep.dataforge.plots.XYPlotFrame;
import hep.dataforge.plots.fx.FXPlotFrame;
import javafx.scene.Node;

import java.io.OutputStream;

/**
 * Created by darksnake on 30-Mar-17.
 */
public class JZY3DFrame extends XYPlotFrame implements FXPlotFrame {



    @Override
    protected void updatePlotData(String name) {

    }

    @Override
    protected void updatePlotConfig(String name) {

    }

    @Override
    protected void updateFrame(Meta annotation) {

    }

    @Override
    protected void updateAxis(String axisName, Meta axisMeta, Meta plotMeta) {

    }

    @Override
    protected void updateLegend(Meta legendMeta) {

    }

    @Override
    public void snapshot(OutputStream stream, Meta config) {

    }

    @Override
    public Node getRoot() {
        return null;
    }
}
