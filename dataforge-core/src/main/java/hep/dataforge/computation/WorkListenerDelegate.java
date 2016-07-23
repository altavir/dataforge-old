/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.computation;

import hep.dataforge.names.Name;
import java.util.concurrent.CompletableFuture;


public class WorkListenerDelegate implements WorkListener {
    
    private final WorkManager manager;
    private final String rootWork;

    public WorkListenerDelegate(WorkManager manager, String rootWork) {
        this.manager = manager;
        this.rootWork = rootWork;
    }

    private String makeName(String processName){
        return Name.joinString(rootWork,processName);
    }
    
    @Override
    public void setProgress(String processName, double progress) {
        manager.setProgress(makeName(processName), progress);
    }

    @Override
    public void setMaxProgress(String processName, double progress) {
        manager.setMaxProgress(makeName(processName), progress);
    }

    @Override
    public void finish(String processName) {
        manager.finish(makeName(processName));
    }

    @Override
    public void updateTitle(String processName, String title) {
        manager.updateTitle(makeName(processName), title);
    }

    @Override
    public void updateMessage(String processName, String message) {
        manager.updateMessage(makeName(processName), message);   
    }

    @Override
    public <U> Work<U> submit(String processName, CompletableFuture<U> task) {
        return manager.submit(makeName(processName), task);
    }
    
}
