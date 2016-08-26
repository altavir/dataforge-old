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

package hep.dataforge.computation;

import java.util.function.Consumer;

/**
 * A process manager callback
 */
public class ProgressCallback {

    private final WorkManager manager;
    private final String workName;

    public ProgressCallback(WorkManager manager, String processName) {
        this.manager = manager;
        this.workName = processName;
    }

    public WorkManager getManager() {
        return manager;
    }

    public String workName() {
        return workName;
    }

    public Work work() {
        return getManager().find(workName());
    }

    public void update(Consumer<Work> consumer) {
        getManager().update(workName(), consumer);
    }

    public void setProgress(double progress) {
        update(p -> p.setProgress(progress));
    }

    public void setProgressToMax() {
        update(p -> p.setProgressToMax());
    }

    public void setMaxProgress(double progress) {
        update(p -> p.setMaxProgress(progress));
    }

    public void increaseProgress(double incProgress) {
        update(p -> p.increaseProgress(incProgress));
    }

    public void updateTitle(String title) {
        update(p -> p.setTitle(title));
    }

    public void updateMessage(String message) {
        update(p -> p.setMessage(message));
    }

}
