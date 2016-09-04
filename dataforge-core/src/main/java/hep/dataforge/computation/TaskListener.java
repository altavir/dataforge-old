/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import java.util.concurrent.CompletableFuture;

/**
 * A handler to update progress info. The processName could be relative to some
 * root process name
 *
 * @author Alexander Nozik
 */
public interface TaskListener {

    /**
     * Set current progress
     *
     * @param processName
     * @param progress
     */
    void setProgress(String processName, double progress);

    /**
     * Set max Progress
     *
     * @param processName
     * @param progress
     */
    void setMaxProgress(String processName, double progress);

    /**
     * notify process finished (set current progress to max)
     *
     * @param processName
     */
    void finish(String processName);

    /**
     * Set process title
     *
     * @param processName
     * @param title
     */
    void updateTitle(String processName, String title);

    /**
     * Set Process message
     *
     * @param processName
     * @param message
     */
    void updateMessage(String processName, String message);

    /**
     * Set task content to automatically notify when task is completed and allow
     * to cancel it
     *
     * @param processName
     * @param task
     */
    Task submit(String processName, CompletableFuture<?> task);
}
