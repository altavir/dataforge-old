/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.exceptions;

/**
 *
 * @author <a href="mailto:altavir@gmail.com">Alexander Nozik</a>
 */
public class WrongTargetException extends StorageException {
    

    /**
     * Creates a new instance of <code>WrongTargetException</code> without
     * detail message.
     */
    public WrongTargetException() {
    }

    /**
     * Constructs an instance of <code>WrongTargetException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public WrongTargetException(String msg) {
        super(msg);
    }
}
