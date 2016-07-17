/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.cache;

/**
 *
 * @author Alexander Nozik
 */
public class DataCacheException extends Exception {

    /**
     * Creates a new instance of <code>DataCacheException</code> without detail
     * message.
     */
    public DataCacheException() {
    }

    /**
     * Constructs an instance of <code>DataCacheException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DataCacheException(String msg) {
        super(msg);
    }

    public DataCacheException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
