package com.gabb.sb;

import java.util.concurrent.locks.ReentrantLock;

public abstract class Guarded {

    private ReentrantLock oReentrantLock;

    public Guarded() {
         oReentrantLock = new ReentrantLock(true);
    }

    /**
     * Guards this preventing others from mutating it.
     */
    final void guard(){
        oReentrantLock.lock();
    }

    /**
     * Releases the guard on this object, allowing others to mutate it.
     */
    final void relinquish(){
        oReentrantLock.unlock();
    }
}
