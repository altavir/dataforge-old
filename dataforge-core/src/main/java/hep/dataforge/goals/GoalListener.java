package hep.dataforge.goals;

import hep.dataforge.context.Global;
import org.jetbrains.annotations.Nullable;

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
     *
     * @param goalExecutor the executor of completed goal
     * @param result
     */
    default void onGoalComplete(Executor goalExecutor, T result) {

    }

    default void onGoalComplete(T result) {
        onGoalComplete(getDefaultExecutor(), result);
    }

    default void onGoalFailed(Executor goalExecutor, @Nullable Exception ex) {

    }

    default void onGoalFailed(@Nullable Exception ex) {
        onGoalFailed(getDefaultExecutor(), ex);
    }

    default Executor getDefaultExecutor() {
        return Global.instance().singleThreadExecutor();
    }
}
