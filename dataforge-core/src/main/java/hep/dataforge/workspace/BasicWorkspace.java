/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;
import hep.dataforge.data.DataTree;
import hep.dataforge.meta.Meta;

/**
 * A basic non-caching workspace
 *
 * @author Alexander Nozik
 */
public class BasicWorkspace extends AbstractWorkspace {

    DataTree.Builder data = DataTree.builder();

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public DataNode<Object> getData() {
        return data.build();
    }

    //    @Override
//    public <T> DataNode<T> updateStage(String stage, DataNode<T> data) {
//        if (!this.stages.containsKey(stage)) {
//            this.stages.put(stage, DataTree.builder().setName(stage));
//        }
//        DataTree.Builder stageBuilder = this.stages.get(stage);
//        stageBuilder.putNode(data);
//        return stageBuilder.build();
//    }

    public static class Builder implements Workspace.Builder<Builder> {

        BasicWorkspace w = new BasicWorkspace();

        @Override
        public Builder self() {
            return this;
        }

        @Override
        public Context getContext() {
            return w.getContext();
        }

        @Override
        public Builder setContext(Context ctx) {
            w.setContext(ctx);
            return self();
        }

        private DataTree.Builder getData() {
            return w.data;
        }

        @Override
        public Builder loadData(String name, Data data) {
            if (w.getData().provides(name)) {
                getContext().getLogger().warn("Overriding non-empty data during workspace data fill");
            }
            getData().putData(name, data);
            return self();
        }

        @Override
        public Builder loadData(String name, DataNode datanode) {
            if (name == null || name.isEmpty()) {
                //FIXME add warning?
                w.data = new DataTree.Builder<>(datanode);
            } else {
                getData().putNode(name, datanode);
            }
            return self();
        }

        @Override
        public Builder loadTask(Task task) {
            w.tasks.put(task.getName(), task);
            return self();
        }

        @Override
        public Builder loadMeta(String name, Meta meta) {
            w.metas.put(name, meta);
            return self();
        }

        @Override
        public Workspace build() {
            return w;
        }

    }

}
