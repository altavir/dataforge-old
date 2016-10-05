/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.envelopes;

import hep.dataforge.io.MetaStreamReader;
import hep.dataforge.io.MetaStreamWriter;

/**
 *
 * @author Alexander Nozik
 */
public interface MetaType {

    short getCode();

    String getName();

    MetaStreamReader getReader();

    MetaStreamWriter getWriter();
}
