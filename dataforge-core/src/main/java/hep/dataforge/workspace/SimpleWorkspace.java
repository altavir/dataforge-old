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
package hep.dataforge.workspace;

import hep.dataforge.context.Context;
import hep.dataforge.meta.Meta;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import hep.dataforge.data.Data;
import hep.dataforge.data.DataNode;

/**
 * A simple workspace without caching and stages
 *
 * @author Alexander Nozik
 */
public class SimpleWorkspace implements Workspace {

    Map<String, Task> tasks = new HashMap<>();
    Map<String, Data> data = new HashMap<>();
    Meta meta;
    String name;
    Context context;

    @Override
    public DataNode runTask(String taskName, Meta config, String... targets) throws InterruptedException, ExecutionException {
        if (!tasks.containsKey(taskName)) {
            throw new TaskNotFoundException(taskName);
        }

        return tasks.get(taskName).run(config, targets);
    }

    @Override
    public boolean hasData(String stage, String name) {
        return data.containsKey(name);
    }

    @Override
    public Data getData(String stage, String name) {
        Data d = data.get(name);
        return d;
    }

    @Override
    public DependencyResolver getDependencyResolver() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ThreadGroup getWorkspaceThreadGroup() {
        return null;
    }

    @Override
    public void notifyTaskComplete(TaskResult result) {
        //do nothing here
    }

    @Override
    public Identity getIdentity() {
        return new CombinedIdentity(
                new ClassIdentity(this.getClass()),
                new ClassIdentity(context.getClass()),//TODO add identity to context
                new MetaIdentity(meta)
        );
    }

    @Override
    public Meta meta() {
        return meta;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Context getContext() {
        return context;
    }

}
