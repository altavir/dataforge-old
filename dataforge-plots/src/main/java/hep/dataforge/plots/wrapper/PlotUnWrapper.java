/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.wrapper;

import hep.dataforge.io.envelopes.DefaultEnvelopeReader;
import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.UnWrapper;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.PlotFrame;
import hep.dataforge.plots.Plottable;

import java.io.BufferedInputStream;

/**
 *
 * @author Alexander Nozik
 */
public class PlotUnWrapper implements UnWrapper<PlotFrame> {

    public static final String PLOT_WRAPPER_TYPE = "df.plotFrame";

    @Override
    public String type() {
        return PLOT_WRAPPER_TYPE;
    }

    @Override
    public PlotFrame unWrap(Envelope envelope) {
        String plotFrameClassName = envelope.meta().getString("plotFrameClass", "hep.dataforge.plots.JFreeChartFrame");
        Meta plotMeta = envelope.meta().getMeta("plotMeta");
        try {
            PlotFrame frame = (PlotFrame) Class.forName(plotFrameClassName).newInstance();
            frame.configure(plotMeta);
            //Buffering stream to avoid rebufferization
            BufferedInputStream dataStream = new BufferedInputStream(envelope.getData().getStream());
            PlottableUnWrapper unwrapper = new PlottableUnWrapper();
            
            while (dataStream.available() > 0) {
                Plottable pl = unwrapper.unWrap(DefaultEnvelopeReader.instance.readWithData(dataStream));
                frame.add(pl);
            }

            return frame;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
