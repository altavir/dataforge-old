package hep.dataforge.goals;

import org.jetbrains.annotations.Nullable;

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

    default void onGoalComplete(T result){

    }

    default void onGoalFailed(@Nullable Throwable ex){

    }

}
