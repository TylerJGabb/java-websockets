package com.gabb.sb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * A collection of objects that is thread safe, facilitating access to and modification of said objects
 * in a way that prevents those objects from being simultaneously mutated or accessed concurrently
 */
public abstract class GuardedThreadSafeCollection<G extends Guarded> {

    private final ReentrantLock oReentrantLock;
    private final List<G> oGuardedObjects;

    protected GuardedThreadSafeCollection() {
        oGuardedObjects = new ArrayList<>();
        oReentrantLock = new ReentrantLock(true);
    }

    protected void protectedAdd(G aGuarded) {
        try {
            oReentrantLock.lock();
            oGuardedObjects.add(aGuarded);
        } finally {
            oReentrantLock.unlock();
        }
    }

    protected void protectedRemove(G aGuarded) {
        try {
            oReentrantLock.lock();
            oGuardedObjects.remove(aGuarded);
        } finally {
            oReentrantLock.unlock();
        }
    }

    public List<G> getListCopy(){
        return new ArrayList<>(oGuardedObjects);
    }

    /**
     * Filters items in the collection according to aFilter, then applies the function aMutateThenReturnWhetherToStopIterating
     * to each item returned from the filer. If this method returns true, then iteration stops, otherwise iteration
     * will continue until exhausting all items in the filtered list.
     *
     * @param aFilter a filter that is applied to the current collection before beginning iteration
     * @param aMutateThenReturnWhetherToStopIterating a function that presumably mutates an object of type G and
     *                                                then returns true if iteration should stop
     */
    public void applyForEachAfterFiltering(
            Predicate<G> aFilter,
            Function<G, Boolean> aMutateThenReturnWhetherToStopIterating) {

        // FIRST: filter the collection according to the passed filter
        List<G> filtered;
        try {
            oReentrantLock.lock();
            filtered = oGuardedObjects.stream().filter(aFilter).collect(toList());
        } finally {
            oReentrantLock.unlock();
        }
        // SECOND: for each item in the filtered list, apply the provided function, stopping immediately
        // if said function returns true
        for (var guarded : filtered) {
            try {
                guarded.guard();
                Boolean stopIterating = aMutateThenReturnWhetherToStopIterating.apply(guarded);
                if(null != stopIterating && stopIterating) return;
            } finally {
                guarded.relinquish();
            }
        }
    }

    public void findFirstAndConsume(Predicate<G> aFilter, Consumer<G> aConsumer) {

        // first get a filtered list according to the filter
        G found;
        try {
            oReentrantLock.lock();
            if (null == (found = oGuardedObjects.stream().filter(aFilter).findFirst().orElse(null))) return;
        } finally {
            oReentrantLock.unlock();
        }

        // next accept consumer with found item, enforcing locks
        try {
            found.guard();
            aConsumer.accept(found);
        } finally {
            found.relinquish();
        }
    }
}
