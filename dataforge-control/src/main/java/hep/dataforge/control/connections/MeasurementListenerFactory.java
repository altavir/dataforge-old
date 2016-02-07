/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.control.connections;

import hep.dataforge.control.measurements.MeasurementListener;

/**
 *
 * @author Alexander Nozik
 */
public interface MeasurementListenerFactory {
    public abstract <T> MeasurementListener<T> getListener(String name, Class<T> resultType);
}
