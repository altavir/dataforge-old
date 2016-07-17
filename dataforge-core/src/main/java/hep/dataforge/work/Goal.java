/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.work;

import java.util.Map;
import java.util.concurrent.Future;

/**
 *
 * @author Alexander Nozik
 */
public interface Goal {

    /**
     * Bind the result of dependency goal with name {@code source} to this goal input slot
     * {@code tatget}
     *
     * @param goal
     * @param source
     * @param target
     */
    void bind(Goal dependency, String source, String target);

//    /**
//     * Notify this goal that its dependency with name {@code target} is computed
//     * with result
//     *
//     * @param target
//     * @param result
//     */
//    void notifyResult(String target, Object result);
//
//    /**
//     * Notify this goal that its dependency with name {@code target} failed with
//     * exception
//     *
//     * @param target
//     * @param exception
//     */
//    void notifyError(String target, Exception exception);

    /**
     * Compute this goal using its dependencies and notify all bound goals. If
     * computation is started return it.
     */
    Future<Map<String, Object>> run();
}
