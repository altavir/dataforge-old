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
import static hep.dataforge.workspace.Workspace.DATA_STAGE_NAME;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic non-caching workspace
 *
 * @author Alexander Nozik
 */
public class BasicWorkspace extends AbstractWorkspace {

    public static Builder builder() {
        return new Builder();
    }

    private final Map<String, DataTree.Builder> stages = new HashMap<>();

    @Override
    public <T> DataNode<T> getStage(String stageName) {
        if (stages.containsKey(stageName)) {
            return stages.get(stageName).build();
        } else {
            return null;
        }
    }

    @Override
    public <T> DataNode<T> updateStage(String stage, DataNode<T> data) {
        if (!this.stages.containsKey(stage)) {
            this.stages.put(stage, DataTree.builder().setName(stage));
        }
        DataTree.Builder stageBuilder = this.stages.get(stage);
        stageBuilder.putNode(data);
        return stageBuilder.build();
    }

    public static class Builder implements Workspace.Builder<Builder> {

        BasicWorkspace w = new BasicWorkspace();

        @Override
        public Builder self() {
            return this;
        }

        @Override
        public Builder setContext(Context ctx) {
            w.setContext(ctx);
            return self();
        }

        @Override
        public Context getContext() {
            return w.getContext();
        }

        private DataTree.Builder getDataStage() {
            if (!w.stages.containsKey(DATA_STAGE_NAME)) {
                w.stages.put(DATA_STAGE_NAME, DataTree.builder());
            }
            return w.stages.get(DATA_STAGE_NAME);
        }

        @Override
        public Builder loadData(String name, Data data) {
            if (w.getDataStage().provides(name)) {
                getContext().getLogger().warn("Overriding non-empty data during workspace data fill");
            }
            getDataStage().putData(name, data);
            return self();
        }

        @Override
        public Builder loadData(String name, DataNode datanode) {
            if (name == null || name.isEmpty()) {
                if (w.stages.containsKey(DATA_STAGE_NAME)) {
                    getContext().getLogger().warn("Overriding non-empty data node during workspace data fill");
                }
                w.stages.put(DATA_STAGE_NAME, new DataTree.Builder<>(DataTree.cloneNode(datanode)));
            } else {
                getDataStage().putNode(name, datanode);
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
