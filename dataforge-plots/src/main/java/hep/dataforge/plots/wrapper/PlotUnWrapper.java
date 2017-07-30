/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.wrapper;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.EnvelopeType;
import hep.dataforge.io.envelopes.UnWrapper;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.PlotFrame;
import hep.dataforge.plots.Plottable;
import org.slf4j.LoggerFactory;

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

        EnvelopeType internalEnvelopeType = EnvelopeType.resolve(envelope.meta().getString("envelopeType","default"));
        try {
            PlotFrame frame = (PlotFrame) Class.forName(plotFrameClassName).newInstance();
            frame.configure(plotMeta);
            //Buffering stream to avoid rebufferization
            BufferedInputStream dataStream = new BufferedInputStream(envelope.getData().getStream());
            PlottableUnWrapper unwrapper = new PlottableUnWrapper();
            
            while (dataStream.available() > 0) {
                try {
                    Plottable pl = unwrapper.unWrap(internalEnvelopeType.getReader().read(dataStream));
                    frame.add(pl);
                } catch (Exception ex){
                    LoggerFactory.getLogger(getClass()).error("Failed to unwrap plottable");
                }

            }

            return frame;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
