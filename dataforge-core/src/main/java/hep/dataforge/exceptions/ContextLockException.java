package hep.dataforge.exceptions;

import hep.dataforge.names.Named;

public class ContextLockException extends RuntimeException {
    private final Object locker;

    public ContextLockException(Object locker) {
        this.locker = locker;
    }

    private String getObjectName(){
        if(locker instanceof Named){
            return locker.getClass().getSimpleName() + ":" + ((Named) locker).getName();
        } else {
            return locker.getClass().getSimpleName();
        }
    }

    @Override
    public String getMessage() {
        return "Context is locked by " + getObjectName();
    }
}
