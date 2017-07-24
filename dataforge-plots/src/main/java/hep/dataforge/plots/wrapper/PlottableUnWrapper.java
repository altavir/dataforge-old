/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.wrapper;

import hep.dataforge.io.envelopes.Envelope;
import hep.dataforge.io.envelopes.UnWrapper;
import hep.dataforge.meta.Meta;
import hep.dataforge.plots.Plottable;
import hep.dataforge.plots.data.PlottableData;
import hep.dataforge.values.Values;

import java.io.ObjectInputStream;
import java.util.List;

/**
 *
 * @author Alexander Nozik
 */
public class PlottableUnWrapper implements UnWrapper<Plottable>{

    @Override
    public String type() {
        return "df.plots.Plottable";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Plottable unWrap(Envelope envelope) {
        try {
            Meta plottableMeta = envelope.meta().getMeta("meta");
            String name = envelope.meta().getString("name");
            List<Values> data = (List<Values>) new ObjectInputStream(envelope.getData().getStream()).readObject();

            //Restore always as plottableData
            PlottableData pl = new PlottableData(name);
            pl.configure(plottableMeta);
            pl.fillData(data);
            return pl;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read Plottable", ex);
        }
    }
    
}
