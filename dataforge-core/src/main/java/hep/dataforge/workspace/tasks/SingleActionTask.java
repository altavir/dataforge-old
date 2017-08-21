/*
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hep.dataforge.workspace.tasks;

import hep.dataforge.actions.Action;
import hep.dataforge.data.DataNode;
import hep.dataforge.meta.Meta;

/**
 * A task wrapper for single action
 * Created by darksnake on 21-Aug-16.
 */
public abstract class SingleActionTask<T, R> extends AbstractTask<R> {

    protected DataNode<T> gatherNode(DataNode<?> data) {
        return (DataNode<T>) data;
    }

    protected abstract Action<T, R> getAction(TaskModel model);

    protected Meta transformMeta(TaskModel model) {
        return model.meta();
    }

    @Override
    protected DataNode<R> run(TaskModel model, DataNode<?> data) {
        Meta actionMeta = transformMeta(model);
        DataNode<T> checkedData = gatherNode(data);
        return getAction(model).run(model.getContext(), checkedData, actionMeta);
    }

}
