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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This object holds task thread and controls execution of different parts of
 * the task.
 *
 * @author Alexander Nozik
 */
public class TaskExecutor {

    private final ExecutorService executor;
    private ThreadGroup threadGroup;

    /**
     * completion of the task in percents. -1 means indeterminate.
     */
    private double progress = -1;
    private boolean isFinished = false;

    private TaskProgressListener listener;

    public TaskExecutor(ThreadGroup parent, String threadName, TaskProgressListener listener) {
        if (parent == null) {
            threadGroup = new ThreadGroup(threadName);
        } else {
            threadGroup = new ThreadGroup(parent, threadName);
        }
        executor = Executors.newCachedThreadPool((Runnable r) -> new Thread(threadGroup, r));
        this.listener = listener;
    }

    /**
     * Post a process to be executed in task thread. Depending on executor
     * configuration it could be executed on main task thread or delegated to
     * the new thread.
     *
     * @param r the Runnable of the process
     */
    public Future<?> submit(Runnable r) {
        return executor.submit(r);
    }

    /**
     * Post a process with result to be executed on task thread.
     *
     * @param <T> the type of the result
     * @param c
     * @return
     */
    public <T> Future<T> submit(Callable<T> c) {
        return executor.submit(c);
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
        if (listener != null) {
            listener.notifyProgress(progress);
        }
    }

    /**
     * Should be called by task logic when task execution is finished. This
     * method does not guarantee that all processes inside task are finished,
     * only that not new processes could be posted.
     */
    public void finish() {
        executor.shutdown();
        isFinished = true;
        progress = 100d;
        if (listener != null) {
            listener.notifyFinished();
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    /**
     * Force shut down of task execution ignoring incomplete tasks
     */
    public void cancel() {
        executor.shutdownNow();
        threadGroup.interrupt();
        threadGroup.destroy();
        isFinished = true;
        progress = 100d;
        if (listener != null) {
            listener.notifyCanceled();
        }
    }

}
