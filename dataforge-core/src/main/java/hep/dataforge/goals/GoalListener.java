package hep.dataforge.goals;

import hep.dataforge.context.Global;

import java.util.concurrent.Executor;

/**
 * A universal goal state listener
 * Created by darksnake on 19-Mar-17.
 */
public interface GoalListener<T> {

    /**
     * Do something when the goal actually starts
     */
    default void onGoalStart() {

    }

    /**
     * Execute when goal is completed successfully
     * @param goalExecutor the executor of completed goal
     * @param result
     */
    void onGoalComplete(Executor goalExecutor, T result);

    default void onGoalComplete(T result) {
        onGoalComplete(getDefaultExecutor(), result);
    }

   void onGoalFailed(Executor goalExecutor);

    default void onGoalFailed() {
        onGoalFailed(getDefaultExecutor());
    }

    default Executor getDefaultExecutor() {
        return Global.instance().singleThreadExecutor();
    }
}
