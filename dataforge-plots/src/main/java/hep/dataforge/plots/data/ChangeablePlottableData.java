/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.plots.data;

import hep.dataforge.data.DataPoint;
import hep.dataforge.meta.Meta;


public class ChangeablePlottableData extends PlottableData {

    public ChangeablePlottableData(String name, Meta meta) {
        super(name, meta);
    }
    
    @Override
    public void fillData(Iterable<DataPoint> it) {
        super.fillData(it); 
        notifyDataChanged();
    }
    
    public void clear(){
        data.clear();
        notifyDataChanged();
    }
}
