/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;


public class CompositeTask<T> extends GenericTask<T> {

    @Override
    protected DataNode gather(TaskExecutor executor, Workspace workspace, Meta config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void transform(TaskExecutor executor, Context context, TaskState state, Meta config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void report(TaskExecutor executor, Context context, TaskState state, Meta config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected DataNode<T> result(TaskExecutor executor, Workspace workspace, TaskState state, Meta config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
