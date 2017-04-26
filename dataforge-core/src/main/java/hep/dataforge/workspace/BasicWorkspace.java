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
import org.jetbrains.annotations.NotNull;

/**
 * A basic non-caching workspace
 *
 * @author Alexander Nozik
 */
public class BasicWorkspace extends AbstractWorkspace {

    DataTree.Builder data = DataTree.builder();

    @NotNull
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
        public Builder loadData(String as, Data data) {
            if (w.getData().optNode(as).isPresent()) {
                getContext().getLogger().warn("Overriding non-empty data during workspace data fill");
            }
            getData().putData(as, data);
            return self();
        }

        @Override
        public Builder loadData(String as, DataNode datanode) {
            if (as == null || as.isEmpty()) {
                if (!w.data.isEmpty()) {
                    getContext().getLogger().warn("Overriding non-empty root data node during workspace construction");
                }
                w.data = new DataTree.Builder<>(datanode);
            } else {
                getData().putNode(as, datanode);
            }
            return self();
        }

        @Override
        public Builder loadTask(Task task) {
            w.tasks.put(task.getName(), task);
            return self();
        }

        @Override
        public Builder target(String name, Meta meta) {
            w.targets.put(name, meta);
            return self();
        }

        @Override
        public Workspace build() {
            return w;
        }

    }

}
